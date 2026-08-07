package notification.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="4iren-gateway", path="/api/notification", contextId="notificationClient")
public interface NotificationClient {


}

