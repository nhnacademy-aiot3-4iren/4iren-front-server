document.addEventListener('DOMContentLoaded', () => {
    console.log('[building-info.js] 로드 완료');

    // HTML 태그에서 ID 데이터 추출 (독립적 실행)
    const container = document.getElementById('buildingInfoContainer');
    if (!container) return; // building-info 페이지가 아니면 종료

    const teamId = container.dataset.teamId ? Number(container.dataset.teamId) : null;
    const buildingId = container.dataset.buildingId ? Number(container.dataset.buildingId) : null;

    if (!teamId || !buildingId) {
        console.error('필수 데이터(teamId, buildingId)가 없습니다.');
        return;
    }

    // =======================================================
    // 1. 건물 정보 수정 모달 제어
    // =======================================================
    const openEditBuildingModalBtn = document.getElementById('openEditBuildingModalBtn');
    const editBuildingModal = document.getElementById('editBuildingModal');
    const closeEditBuildingModalBtn = document.getElementById('closeEditBuildingModalBtn');
    const cancelEditBuildingModalBtn = document.getElementById('cancelEditBuildingModalBtn');
    const editBuildingForm = document.getElementById('editBuildingForm');
    const editBuildingError = document.getElementById('editBuildingError');
    const saveEditBuildingBtn = document.getElementById('saveEditBuildingBtn');
    const editSearchAddressBtn = document.getElementById('editSearchAddressBtn');

    if (openEditBuildingModalBtn && editBuildingModal) {
        openEditBuildingModalBtn.addEventListener('click', () => {
            if (editBuildingError) editBuildingError.textContent = '';
            editBuildingModal.showModal();
        });
    }

    const closeEditModal = () => editBuildingModal && editBuildingModal.close();
    if (closeEditBuildingModalBtn) closeEditBuildingModalBtn.addEventListener('click', closeEditModal);
    if (cancelEditBuildingModalBtn) cancelEditBuildingModalBtn.addEventListener('click', closeEditModal);

    // 카카오 도로명 주소 검색
    if (editSearchAddressBtn) {
        editSearchAddressBtn.addEventListener('click', () => {
            if (typeof daum === 'undefined' || !daum.Postcode) {
                alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
                return;
            }
            new daum.Postcode({
                oncomplete: function (data) {
                    document.getElementById('editRoadAddress').value = data.roadAddress;
                    document.getElementById('editDetailAddress').focus();
                }
            }).open();
        });
    }

    // 건물 정보 수정 저장 (PATCH)
    if (editBuildingForm) {
        editBuildingForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (editBuildingError) editBuildingError.textContent = '';

            const buildingName = document.getElementById('editBuildingName').value.trim();
            const description = document.getElementById('editBuildingDesc').value.trim();
            const roadAddress = document.getElementById('editRoadAddress').value.trim();
            const detailAddress = document.getElementById('editDetailAddress').value.trim();
            const regionName = document.getElementById('editRegionName').value.trim();

            if (!buildingName) {
                editBuildingError.textContent = '건물 이름을 입력해주세요.';
                return;
            }

            if (saveEditBuildingBtn) {
                saveEditBuildingBtn.disabled = true;
                saveEditBuildingBtn.textContent = '저장 중...';
            }

            try {
                const response = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        buildingName,
                        description: description || null,
                        roadAddress: roadAddress || null,
                        detailAddress: detailAddress || null,
                        regionName: regionName || null
                    })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText || '건물 정보 수정에 실패했습니다.');
                }

                alert('건물 정보가 성공적으로 수정되었습니다.');
                closeEditModal();
                window.location.reload();
            } catch (err) {
                if (editBuildingError) editBuildingError.textContent = err.message || '저장 중 오류가 발생했습니다.';
                if (saveEditBuildingBtn) {
                    saveEditBuildingBtn.disabled = false;
                    saveEditBuildingBtn.textContent = '수정 저장';
                }
            }
        });
    }

    // =======================================================
    // 2. MQTT 브로커 설정 모달 제어
    // =======================================================
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
            console.log("MQTT 설정 버튼 클릭됨!");

            if (mqttError) mqttError.textContent = '';
            if (mqttModalTitle) mqttModalTitle.textContent = 'MQTT 브로커 불러오는 중...';
            if (saveMqttBtn) saveMqttBtn.disabled = true;

            mqttModal.showModal();

            try {
                const res = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/mqtt`);
                const broker = res.ok ? await res.json() : null;

                if (broker && broker.serverName) {
                    isUpdate = true;
                    if (mqttModalTitle) mqttModalTitle.textContent = 'MQTT 브로커 수정';
                    if (mqttServerName) mqttServerName.value = broker.serverName || '';
                    if (mqttBrokerUrl) mqttBrokerUrl.value = broker.brokerUrl || '';
                    if (mqttUsername) mqttUsername.value = broker.username || '';
                    if (mqttPassword) mqttPassword.value = broker.password || '';
                    if (mqttTopic) mqttTopic.value = broker.topic || '';
                    if (saveMqttBtn) saveMqttBtn.textContent = '수정 저장';
                } else {
                    isUpdate = false;
                    if (mqttModalTitle) mqttModalTitle.textContent = 'MQTT 브로커 신규 등록';
                    if (mqttServerName) mqttServerName.value = '';
                    if (mqttBrokerUrl) mqttBrokerUrl.value = '';
                    if (mqttUsername) mqttUsername.value = '';
                    if (mqttPassword) mqttPassword.value = '';
                    if (mqttTopic) mqttTopic.value = '';
                    if (saveMqttBtn) saveMqttBtn.textContent = '등록';
                }
            } catch (err) {
                console.error("MQTT 브로커 데이터 조회 실패:", err);
                if (mqttModalTitle) mqttModalTitle.textContent = 'MQTT 브로커 설정';
                isUpdate = false;
            } finally {
                if (saveMqttBtn) saveMqttBtn.disabled = false;
            }
        });
    }

    const closeMqttModal = () => mqttModal && mqttModal.close();
    if (closeMqttModalBtn) closeMqttModalBtn.addEventListener('click', closeMqttModal);
    if (cancelMqttModalBtn) cancelMqttModalBtn.addEventListener('click', closeMqttModal);

    if (mqttForm) {
        mqttForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (mqttError) mqttError.textContent = '';

            const payload = {
                buildingId: buildingId,
                serverName: mqttServerName ? mqttServerName.value.trim() : '',
                brokerUrl: mqttBrokerUrl ? mqttBrokerUrl.value.trim() : '',
                topic: mqttTopic ? mqttTopic.value.trim() : '',
                username: mqttUsername ? (mqttUsername.value.trim() || null) : null,
                password: mqttPassword ? (mqttPassword.value.trim() || null) : null
            };

            if (saveMqttBtn) {
                saveMqttBtn.disabled = true;
                saveMqttBtn.textContent = '저장 중...';
            }

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
                closeMqttModal();
                window.location.reload();
            } catch (err) {
                if (mqttError) mqttError.textContent = err.message || '저장 중 오류가 발생했습니다.';
                if (saveMqttBtn) {
                    saveMqttBtn.disabled = false;
                    saveMqttBtn.textContent = isUpdate ? '수정 저장' : '등록';
                }
            }
        });
    }
});