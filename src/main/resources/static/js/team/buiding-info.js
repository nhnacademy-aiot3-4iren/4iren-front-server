document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('buildingInfoContainer');
    const teamId = container ? container.dataset.teamId : null;
    const buildingId = container ? container.dataset.buildingId : null;

    // 하위 rooms.js 호환을 위해 전역 변수로 할당
    window.teamId = teamId;
    window.buildingId = buildingId;

    const openMqttModalBtn = document.getElementById('openMqttModalBtn');
    const closeMqttModalBtn = document.getElementById('closeMqttModalBtn');
    const cancelMqttModalBtn = document.getElementById('cancelMqttModalBtn');
    const mqttModal = document.getElementById('mqttModal');
    const mqttModalTitle = document.getElementById('mqttModalTitle');
    const mqttForm = document.getElementById('mqttForm');
    const saveMqttBtn = document.getElementById('saveMqttBtn');
    const mqttError = document.getElementById('mqttError');

    const mqttServerName = document.getElementById('mqttServerName');
    const mqttBrokerUrl = document.getElementById('mqttBrokerUrl');
    const mqttUsername = document.getElementById('mqttUsername');
    const mqttPassword = document.getElementById('mqttPassword');
    const mqttTopic = document.getElementById('mqttTopic');

    let isUpdate = false;

    if (openMqttModalBtn && mqttModal) {
        openMqttModalBtn.addEventListener('click', async () => {
            mqttError.textContent = '';
            mqttModalTitle.textContent = 'MQTT 브로커 불러오는 중...';
            saveMqttBtn.disabled = true;

            mqttModal.showModal();

            try {
                const res = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/mqtt`);
                const broker = res.ok ? await res.json() : null;

                if (broker && broker.serverName) {
                    isUpdate = true;
                    mqttModalTitle.textContent = 'MQTT 브로커 수정';
                    mqttServerName.value = broker.serverName || '';
                    mqttBrokerUrl.value = broker.brokerUrl || '';
                    mqttUsername.value = broker.username || '';
                    mqttPassword.value = broker.password || '';
                    mqttTopic.value = broker.topic || '';
                    saveMqttBtn.textContent = '수정 저장';
                } else {
                    isUpdate = false;
                    mqttModalTitle.textContent = 'MQTT 브로커 신규 등록';
                    mqttServerName.value = '';
                    mqttBrokerUrl.value = '';
                    mqttUsername.value = '';
                    mqttPassword.value = '';
                    mqttTopic.value = '';
                    saveMqttBtn.textContent = '등록';
                }
            } catch (err) {
                mqttModalTitle.textContent = 'MQTT 브로커 설정';
                isUpdate = false;
            } finally {
                saveMqttBtn.disabled = false;
            }
        });
    }

    const closeModal = () => mqttModal && mqttModal.close();
    if (closeMqttModalBtn) closeMqttModalBtn.addEventListener('click', closeModal);
    if (cancelMqttModalBtn) cancelMqttModalBtn.addEventListener('click', closeModal);

    if (mqttForm) {
        mqttForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            mqttError.textContent = '';

            const payload = {
                buildingId: Number(buildingId),
                serverName: mqttServerName.value.trim(),
                brokerUrl: mqttBrokerUrl.value.trim(),
                topic: mqttTopic.value.trim(),
                username: mqttUsername.value.trim() || null,
                password: mqttPassword.value.trim() || null
            };

            saveMqttBtn.disabled = true;
            saveMqttBtn.textContent = '저장 중...';

            try {
                const method = isUpdate ? 'PUT' : 'POST';
                const res = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/mqtt`, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(errorText || 'MQTT 브로커 설정 저장에 실패했습니다.');
                }

                alert(isUpdate ? 'MQTT 브로커 설정이 성공적으로 수정되었습니다.' : 'MQTT 브로커가 성공적으로 등록되었습니다.');
                closeModal();
                window.location.reload();
            } catch (err) {
                mqttError.textContent = err.message || '저장 중 오류가 발생했습니다.';
                saveMqttBtn.disabled = false;
                saveMqttBtn.textContent = isUpdate ? '수정 저장' : '등록';
            }
        });
    }
});