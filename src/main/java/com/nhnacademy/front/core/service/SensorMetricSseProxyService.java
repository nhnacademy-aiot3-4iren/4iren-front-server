package com.nhnacademy.front.core.service;

import com.nhnacademy.front.auth.service.AuthService;
import com.nhnacademy.front.core.client.CoreSensorMetricStreamClient;
import feign.FeignException;
import feign.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorMetricSseProxyService {

    private static final int STREAM_BUFFER_SIZE = 8 * 1024;

    private final CoreSensorMetricStreamClient streamClient;
    private final AuthService authService;

    /**
     * Gateway와 Core의 SSE 응답을 먼저 연 뒤, 본문을 가공하지 않고 브라우저로 전달한다.
     * Feign 호출을 컨트롤러 요청 스레드에서 시작하므로 기존 쿠키 기반 Bearer 인증을
     * 사용할 수 있고, 응답이 커밋되기 전에 401 갱신과 한 번의 재연결을 수행할 수 있다.
     */
    public StreamingResponseBody openRoomSensorMetricStream(
            Long teamId,
            Long roomId,
            List<String> devEuis,
            List<String> metricCodes,
            Instant since,
            String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return openAuthenticatedStream(
                () -> streamClient.openRoomSensorMetricStream(
                        teamId,
                        roomId,
                        devEuis,
                        metricCodes,
                        since,
                        lastEventId
                ),
                "센서",
                teamId,
                roomId,
                request,
                response
        );
    }

    public StreamingResponseBody openDashboardMetricStream(
            Long teamId,
            List<Long> roomIds,
            List<String> metricCodes,
            String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return openAuthenticatedStream(
                () -> streamClient.openDashboardMetricStream(
                        teamId,
                        roomIds,
                        metricCodes,
                        lastEventId
                ),
                "대시보드",
                teamId,
                null,
                request,
                response
        );
    }

    private StreamingResponseBody openAuthenticatedStream(
            Supplier<Response> upstreamCall,
            String streamName,
            Long teamId,
            Long roomId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Response upstreamResponse = openUpstreamStream(upstreamCall, streamName);
        if (upstreamResponse.status() == HttpStatus.UNAUTHORIZED.value()) {
            upstreamResponse.close();
            String refreshToken = findCookieValue(request, "refreshToken");
            if (refreshToken != null && authService.refresh(refreshToken, response)) {
                upstreamResponse = openUpstreamStream(upstreamCall, streamName);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        streamName + " SSE 인증을 갱신할 수 없습니다."
                );
            }
        }

        requireSuccessfulResponse(upstreamResponse, streamName);

        if (upstreamResponse.body() == null) {
            upstreamResponse.close();
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Core " + streamName + " SSE 응답 본문이 없습니다."
            );
        }

        Response acceptedResponse = upstreamResponse;
        return outputStream -> relay(
                acceptedResponse,
                outputStream,
                streamName,
                teamId,
                roomId
        );
    }

    private Response openUpstreamStream(
            Supplier<Response> upstreamCall,
            String streamName
    ) {
        try {
            return upstreamCall.get();
        } catch (FeignException e) {
            throw translateUpstreamException(e, streamName);
        }
    }

    private void requireSuccessfulResponse(
            Response upstreamResponse,
            String streamName
    ) {
        int status = upstreamResponse.status();
        if (status >= 200 && status < 300) {
            return;
        }

        upstreamResponse.close();
        HttpStatusCode responseStatus = status >= 400 && status <= 599
                ? HttpStatusCode.valueOf(status)
                : HttpStatus.BAD_GATEWAY;
        throw new ResponseStatusException(
                responseStatus,
                "Core " + streamName + " SSE 연결이 거부되었습니다."
        );
    }

    private String findCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void relay(
            Response upstreamResponse,
            OutputStream browserOutput,
            String streamName,
            Long teamId,
            Long roomId
    ) {
        try (upstreamResponse;
             InputStream upstreamInput = upstreamResponse.body().asInputStream()) {
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = upstreamInput.read(buffer)) != -1) {
                browserOutput.write(buffer, 0, bytesRead);
                // heartbeat와 각 이벤트가 Servlet 응답 버퍼에 머물지 않게 즉시 전달한다.
                browserOutput.flush();
            }
        } catch (IOException e) {
            // 브라우저 탭 종료나 네트워크 단절도 IOException으로 전달되므로 정상적인 연결 종료로 처리한다.
            log.debug(
                    "{} SSE 프록시 연결이 종료되었습니다. teamId={}, roomId={}, reason={}",
                    streamName,
                    teamId,
                    roomId,
                    e.getMessage()
            );
        }
    }

    private ResponseStatusException translateUpstreamException(
            FeignException exception,
            String streamName
    ) {
        int status = exception.status();
        HttpStatusCode responseStatus = status >= 400 && status <= 599
                ? HttpStatusCode.valueOf(status)
                : HttpStatus.SERVICE_UNAVAILABLE;

        return new ResponseStatusException(
                responseStatus,
                "Core " + streamName + " SSE 연결에 실패했습니다.",
                exception
        );
    }
}
