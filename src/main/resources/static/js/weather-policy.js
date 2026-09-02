(() => {
    const dialog = document.getElementById('weatherPolicyDialog');
    const openButton = document.getElementById('openWeatherPolicyBtn');
    if (!dialog || !openButton) return;

    const closeButton = document.getElementById('closeWeatherPolicyBtn');
    const cancelButton = document.getElementById('cancelWeatherPolicyBtn');
    const form = document.getElementById('weatherPolicyForm');
    const loading = document.getElementById('weatherPolicyLoading');
    const body = document.getElementById('weatherPolicyBody');
    const message = document.getElementById('weatherPolicyMessage');
    const saveButton = document.getElementById('saveWeatherPolicyBtn');
    const reactivateButton = document.getElementById('reactivateWeatherPolicyBtn');
    const enabledInput = document.getElementById('weatherPolicyEnabled');
    const rainPossibleInput = document.getElementById('rainPossibleProbability');
    const rainExpectedInput = document.getElementById('rainExpectedProbability');
    const windSpeedInput = document.getElementById('strongWindSpeed');
    const humidityInput = document.getElementById('highHumidityPercent');
    const policyRoomId = typeof roomId === 'undefined' ? null : roomId;
    const scopeLabel = policyRoomId == null ? '팀' : '강의실';
    let loadedEnabled = true;

    openButton.addEventListener('click', openDialog);
    closeButton.addEventListener('click', closeDialog);
    cancelButton.addEventListener('click', closeDialog);
    form.addEventListener('submit', savePolicy);
    enabledInput.addEventListener('change', updateEnabled);
    if (reactivateButton) reactivateButton.addEventListener('click', reactivatePolicy);
    dialog.addEventListener('click', event => {
        if (event.target === dialog) closeDialog();
    });

    function policyUrl(enabledOnly = false) {
        const query = new URLSearchParams({ teamId: String(teamId) });
        if (policyRoomId != null) query.set('roomId', String(policyRoomId));
        return `/api/front/welcome-briefing/policies${enabledOnly ? '/enabled' : ''}?${query}`;
    }

    async function openDialog() {
        loading.hidden = false;
        body.hidden = true;
        setMessage('');
        dialog.showModal();
        try {
            renderPolicy(await requestPolicy(policyUrl()));
            body.hidden = false;
        } catch (error) {
            setMessage(error.message || '외부날씨 정책을 불러오지 못했습니다.', true);
            body.hidden = false;
        } finally {
            loading.hidden = true;
        }
    }

    function closeDialog() {
        if (dialog.open) dialog.close();
    }

    function renderPolicy(policy) {
        rainPossibleInput.value = policy.rainPossibleProbability;
        rainExpectedInput.value = policy.rainExpectedProbability;
        windSpeedInput.value = policy.strongWindSpeed;
        humidityInput.value = policy.highHumidityPercent;
        enabledInput.checked = Boolean(policy.enabled);
        loadedEnabled = Boolean(policy.enabled);
    }

    function policyPayload() {
        return {
            rainPossibleProbability: Number(rainPossibleInput.value),
            rainExpectedProbability: Number(rainExpectedInput.value),
            strongWindSpeed: Number(windSpeedInput.value),
            highHumidityPercent: Number(humidityInput.value),
            enabled: enabledInput.checked
        };
    }

    async function savePolicy(event) {
        event.preventDefault();
        if (!form.reportValidity()) return;
        const policy = policyPayload();
        if (policy.rainPossibleProbability > policy.rainExpectedProbability) {
            setMessage('비 가능성 기준은 비 예상 기준보다 클 수 없습니다.', true);
            rainPossibleInput.focus();
            return;
        }
        try {
            saveButton.disabled = true;
            setMessage('정책을 저장하는 중입니다.');
            const savedPolicy = await requestPolicy(policyUrl(), {
                method: 'PUT',
                body: JSON.stringify(policy)
            });
            renderPolicy(savedPolicy);
            setMessage(`이 ${scopeLabel}의 외부날씨 정책을 저장했습니다.`);
        } catch (error) {
            setMessage(error.message || '외부날씨 정책 저장에 실패했습니다.', true);
        } finally {
            saveButton.disabled = false;
        }
    }

    async function updateEnabled() {
        const nextEnabled = enabledInput.checked;
        try {
            enabledInput.disabled = true;
            setMessage('활성화 설정을 변경하는 중입니다.');
            await requestPolicy(policyUrl(true), { method: 'PATCH', body: JSON.stringify({ enabled: nextEnabled }) });
            loadedEnabled = nextEnabled;
            setMessage(nextEnabled
                ? `${scopeLabel} 전용 정책을 활성화했습니다.`
                : `${scopeLabel} 전용 정책을 비활성화했습니다. 다음 조회부터 상위 정책이 표시됩니다.`);
        } catch (error) {
            enabledInput.checked = loadedEnabled;
            setMessage(error.status === 404
                ? `저장된 ${scopeLabel} 정책이 없습니다. 먼저 정책 저장 버튼을 눌러주세요.`
                : (error.message || '활성화 설정 변경에 실패했습니다.'), true);
        } finally {
            enabledInput.disabled = false;
        }
    }

    async function reactivatePolicy() {
        if (!reactivateButton) return;
        try {
            reactivateButton.disabled = true;
            setMessage(`${scopeLabel} 전용 정책을 다시 활성화하는 중입니다.`);
            await requestPolicy(policyUrl(true), { method: 'PATCH', body: JSON.stringify({ enabled: true }) });
            renderPolicy(await requestPolicy(policyUrl()));
            setMessage(`${scopeLabel} 전용 정책을 다시 활성화했습니다.`);
        } catch (error) {
            setMessage(error.status === 404
                ? `다시 활성화할 저장된 ${scopeLabel} 전용 정책이 없습니다. 현재 값을 저장하면 새 전용 정책이 만들어집니다.`
                : (error.message || `${scopeLabel} 전용 정책 다시 활성화에 실패했습니다.`), true);
        } finally {
            reactivateButton.disabled = false;
        }
    }

    function setMessage(text, isError = false) {
        message.textContent = text;
        message.classList.toggle('error', isError);
    }

    async function requestPolicy(url, options = {}) {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options
        });
        if (!response.ok) {
            const text = await response.text();
            let errorMessage = text;
            try { errorMessage = JSON.parse(text).message || text; } catch (ignored) { /* 원문 사용 */ }
            const error = new Error(errorMessage || `요청 실패 (${response.status})`);
            error.status = response.status;
            throw error;
        }
        return response.status === 204 ? null : response.json();
    }
})();
