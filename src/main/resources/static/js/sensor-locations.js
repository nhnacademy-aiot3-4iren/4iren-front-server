const openSensorModalBtn = document.getElementById('openSensorModalBtn');
const closeSensorModalBtn = document.getElementById('closeSensorModalBtn');
const createSensorBtn = document.getElementById('createSensorBtn');
const sensorModalOverlay = document.getElementById('sensorModalOverlay');
const devEuiInput = document.getElementById('devEuiInput');
const locationDetailInput = document.getElementById('locationDetailInput');

if (openSensorModalBtn) {
    openSensorModalBtn.addEventListener('click', () => {
        sensorModalOverlay.classList.add('active');
        devEuiInput.focus();
    });
}

if (closeSensorModalBtn) {
    closeSensorModalBtn.addEventListener('click', closeSensorModal);
}

if (sensorModalOverlay) {
    sensorModalOverlay.addEventListener('click', event => {
        if (event.target === sensorModalOverlay) {
            closeSensorModal();
        }
    });
}

if (devEuiInput) {
    devEuiInput.addEventListener('input', () => {
        devEuiInput.value = devEuiInput.value
            .replace(/[^0-9a-fA-F]/g, '')
            .slice(0, 16)
            .toUpperCase();
    });
}

if (createSensorBtn) {
    createSensorBtn.addEventListener('click', async () => {
        const devEui = devEuiInput.value.trim();

        if (!/^[0-9A-Fa-f]{16}$/.test(devEui)) {
            alert('DevEUI는 16자리 HEX 값으로 입력해주세요.');
            devEuiInput.focus();
            return;
        }

        try {
            createSensorBtn.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/rooms/${roomId}/sensor-locations`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    devEui,
                    locationDetail: locationDetailInput.value.trim()
                })
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '센서 생성에 실패했습니다.');
        } finally {
            createSensorBtn.disabled = false;
        }
    });
}

document.querySelectorAll('.sensor-delete-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const sensorLocationId = button.dataset.sensorLocationId;
        const devEui = button.dataset.devEui || '센서';

        if (!confirm(`${devEui}을(를) 삭제할까요?`)) {
            return;
        }

        try {
            button.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/rooms/${roomId}/sensor-locations/${sensorLocationId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '센서 삭제에 실패했습니다.');
        } finally {
            button.disabled = false;
        }
    });
});

function closeSensorModal() {
    sensorModalOverlay.classList.remove('active');
    devEuiInput.value = '';
    locationDetailInput.value = '';
}
