document.addEventListener("DOMContentLoaded", function() {
    // ==========================================
    // 좌측 탭 전환
    // ==========================================
    const tabs = document.querySelectorAll('.settings-tab[data-target]');
    const tabContents = document.querySelectorAll('.tab-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            tabs.forEach(t => t.classList.remove('settings-tab-active'));
            this.classList.add('settings-tab-active');
            tabContents.forEach(content => content.style.display = 'none');
            const targetId = this.getAttribute('data-target');
            const targetContent = document.getElementById(targetId);
            if (targetContent) {
                targetContent.style.display = 'block';
            }
        });
    });

    // ==========================================
    // 버튼 클릭 이벤트 리스너 연결 + 페이지 진입 시 초기 상태 확인
    // ==========================================
    ['member', 'admin'].forEach(botType => {
        const startBtn = document.getElementById(`startLinkBtn-${botType}`);
        const retryBtn = document.getElementById(`retryBtn-${botType}`);
        const relinkBtn = document.getElementById(`relinkBtn-${botType}`);

        // 이 botType 카드 자체가 화면에 없으면(Admin 카드가 th:if로 안 그려진 경우 등) 스킵
        if (!startBtn) return;

        if (startBtn) startBtn.addEventListener('click', () => requestTelegramLink(botType));
        if (retryBtn) retryBtn.addEventListener('click', () => requestTelegramLink(botType));
        if (relinkBtn) relinkBtn.addEventListener('click', () => requestTelegramLink(botType));

        // 페이지 진입 시 최초 1회 연동 상태 확인
        checkInitialStatus(botType);
    });

    // 탭/창으로 복귀했을 때 즉시 한 번 더 확인
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState !== 'visible') return;

        ['member', 'admin'].forEach(botType => {
            const pendingEl = document.getElementById(`state-pending-${botType}`);
            // 지금 '연동중' 화면을 보고 있는 경우에만 즉시 재확인
            if (pendingEl && pendingEl.style.display !== 'none') {
                checkLinkStatusOnce(botType);
            }
        });
    });
});

// ==========================================
// UI 상태 전환
// ==========================================
function showTelegramState(botType, state) {
    ['first', 'pending', 'success', 'fail'].forEach(s => {
        const el = document.getElementById(`state-${s}-${botType}`);
        if (el) el.style.display = 'none';
    });

    const targetEl = document.getElementById(`state-${state}-${botType}`);
    if (targetEl) targetEl.style.display = '';
}

// ==========================================
// API 통신
// ==========================================
const pollingIntervals = {}; // 폴링 타이머 관리
const timerIntervals = {};   // 카운트다운 타이머 관리 (아래 타이머 섹션에서도 사용)

/**
 * 페이지 진입 시 최초 1회 연동 상태 확인
 * linked:true 면 '연동됨' 화면, false면 '연동 전' 화면으로 분기
 */
function checkInitialStatus(botType) {
    fetch(`/api/front/telegram/${botType}/link-status`, { method: 'GET' })
        .then(res => {
            if (!res.ok) throw new Error(`상태 확인 실패 (status: ${res.status})`);
            return res.json();
        })
        .then(data => {
            showTelegramState(botType, data.linked ? 'success' : 'first');
        })
        .catch(err => {
            console.error(`[${botType}] 초기 상태 확인 실패:`, err);
            // 실패 시 기본적으로 '연동 전' 화면 유지 (first가 기본값이라 별도 처리 불필요)
        });
}

/**
 * '연동하기' / '재연동하기' / '다시 연동하기' 버튼 클릭 시 실행
 * POST /api/front/telegram/{botType}/link-token 호출 → 딥링크 발급 → 새 창 오픈 → pending 상태 전환 → 타이머+폴링 시작
 */
function requestTelegramLink(botType) {
    const startBtn = document.getElementById(`startLinkBtn-${botType}`);
    const retryBtn = document.getElementById(`retryBtn-${botType}`);
    const relinkBtn = document.getElementById(`relinkBtn-${botType}`);

    // 중복 클릭 방지
    [startBtn, retryBtn, relinkBtn].forEach(btn => {
        if (btn) btn.style.pointerEvents = 'none';
    });

    // ★ 클릭 이벤트 핸들러 안에서 순차적으로(동기) 빈 창을 먼저 연다.
    //    -> 브라우저가 사용자 제스처로 인정해서 팝업 차단을 피할 수 있음
    const popup = window.open('', '_blank');  //< 빈페이지를 띄움

    fetch(`/api/front/telegram/${botType}/link-token`, { method: 'POST' })
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => {
                    throw new Error(err.message || '링크 발급에 실패했습니다.');
                });
            }
            return res.json();
        })
        .then(data => {
            const deepLinkUrl = data.deepLinkUrl;
            const timeLimit = data.expiresInSeconds;

            // ★ 미리 열어둔 빈 창에 URL만 주입
            if (popup) {
                popup.location.href = deepLinkUrl;
            } else {
                // 브라우저 설정 등으로 애초에 open() 자체가 막힌 경우 (극히 드묾)
                console.warn(`[${botType}] 팝업이 차단되어 새 창을 열지 못했습니다.`);
                alert('팝업이 차단되어 텔레그램 창을 열 수 없습니다. 브라우저의 팝업 차단을 해제해주세요.');
            }

            showTelegramState(botType, 'pending');
            startTelegramTimer(botType, timeLimit);
            startPolling(botType);
        })
        .catch(err => {
            console.error(`[${botType}] 링크 발급 에러:`, err);
            // ★ fetch 실패 시, 열어놨던 빈 창은 닫아준다 (빈 탭만 남는 것 방지)
            if (popup) popup.close();
            alert(err.message || '서버와 통신할 수 없습니다.');
        })
        .finally(() => {
            [startBtn, retryBtn, relinkBtn].forEach(btn => {
                if (btn) btn.style.pointerEvents = '';
            });
        });
}

/**
 * 3초 간격 연동됏는지 반복확인하기
 */
function startPolling(botType) {
    if (pollingIntervals[botType]) clearInterval(pollingIntervals[botType]);

    pollingIntervals[botType] = setInterval(() => {
        checkLinkStatusOnce(botType);
    }, 3000);
}

/**??????? 모륵는 부분
 * link-status를 한 번 호출해서 linked:true면 성공 처리
 * (폴링에서도 쓰고, visibilitychange 복귀 시 즉시 확인용으로도 재사용)
 */
function checkLinkStatusOnce(botType) {
    fetch(`/api/front/telegram/${botType}/link-status`, { method: 'GET' })
        .then(res => {
            if (!res.ok) throw new Error(`상태 확인 실패 (status: ${res.status})`);
            return res.json();
        })
        .then(data => {
            if (data.linked === true) {
                clearInterval(pollingIntervals[botType]);
                clearInterval(timerIntervals[botType]);
                showTelegramState(botType, 'success');
                console.log(`[${botType}] 연동 완료!`);
            }
        })
        .catch(err => {
            console.error(`[${botType}] 상태 확인 실패:`, err);
            // 네트워크 순간 오류로 폴링 자체를 끊지않음
        });
}

// ==========================================
// 타이머 로직
// ==========================================
const FULL_DASH_ARRAY = 283; // 타이머 원주 길이

const WARNING_THRESHOLD = 60; // 60초 남았을 때 주황색
const ALERT_THRESHOLD = 5;    // 5초 남았을 때 빨간색

const COLOR_CODES = {
    info: { color: "blue" },
    warning: { color: "orange", threshold: WARNING_THRESHOLD },
    alert: { color: "red", threshold: ALERT_THRESHOLD }
};

function startTelegramTimer(botType, timeLimit) {
    let timePassed = 0;
    let timeLeft = timeLimit;

    const pathId = `timer-path-${botType}`;
    const labelId = `timerText-${botType}`;

    if (timerIntervals[botType]) clearInterval(timerIntervals[botType]);

    const pathEl = document.getElementById(pathId);
    pathEl.classList.remove(COLOR_CODES.warning.color, COLOR_CODES.alert.color);
    pathEl.classList.add(COLOR_CODES.info.color);

    document.getElementById(labelId).innerText = formatTime(timeLeft);
    setCircleDasharray(timeLeft, timeLimit, pathId);

    timerIntervals[botType] = setInterval(() => {
        timePassed += 1;
        timeLeft = timeLimit - timePassed;

        document.getElementById(labelId).innerText = formatTime(timeLeft);
        setCircleDasharray(timeLeft, timeLimit, pathId);
        setRemainingPathColor(timeLeft, pathId);

        if (timeLeft <= 0) {
            clearInterval(timerIntervals[botType]);
            if (pollingIntervals[botType]) {
                clearInterval(pollingIntervals[botType]);
            }
            showTelegramState(botType, 'fail');
            console.log(`[${botType}] 유효시간 만료! 실패 화면으로 전환됨.`);
        }
    }, 1000);
}

function formatTime(time) {
    const minutes = Math.floor(time / 60);
    let seconds = time % 60;
    if (seconds < 10) seconds = `0${seconds}`;
    return `${minutes}:${seconds}`;
}

function setCircleDasharray(timeLeft, timeLimit, pathId) {
    const rawTimeFraction = timeLeft / timeLimit;
    const timeFraction = rawTimeFraction - (1 / timeLimit) * (1 - rawTimeFraction);
    const circleDasharray = `${(timeFraction * FULL_DASH_ARRAY).toFixed(0)} 283`;
    document.getElementById(pathId).setAttribute("stroke-dasharray", circleDasharray);
}

function setRemainingPathColor(timeLeft, pathId) {
    const { alert, warning, info } = COLOR_CODES;
    const pathEl = document.getElementById(pathId);

    if (timeLeft <= alert.threshold) {
        pathEl.classList.remove(warning.color);
        pathEl.classList.add(alert.color);
    } else if (timeLeft <= warning.threshold) {
        pathEl.classList.remove(info.color);
        pathEl.classList.add(warning.color);
    }
}