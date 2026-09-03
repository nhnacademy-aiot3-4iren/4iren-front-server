const openDeviceModalBtn = document.getElementById('openDeviceModalBtn');
const closeDeviceModalBtn = document.getElementById('closeDeviceModalBtn');
const createDeviceBtn = document.getElementById('createDeviceBtn');
const deviceModalOverlay = document.getElementById('deviceModalOverlay');
const deviceNameInput = document.getElementById('deviceNameInput');

if (openDeviceModalBtn) {
    openDeviceModalBtn.addEventListener('click', () => {
        deviceModalOverlay.classList.add('active');
        deviceNameInput.focus();
    });
}

if (closeDeviceModalBtn) {
    closeDeviceModalBtn.addEventListener('click', closeDeviceModal);
}

if (deviceModalOverlay) {
    deviceModalOverlay.addEventListener('click', event => {
        if (event.target === deviceModalOverlay) {
            closeDeviceModal();
        }
    });
}

if (createDeviceBtn) {
    createDeviceBtn.addEventListener('click', async () => {
        const deviceName = deviceNameInput.value.trim();

        if (!deviceName) {
            alert('장치 이름을 입력해주세요.');
            deviceNameInput.focus();
            return;
        }

        try {
            createDeviceBtn.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/rooms/${roomId}/devices`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ deviceName })
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '장치 생성에 실패했습니다.');
        } finally {
            createDeviceBtn.disabled = false;
        }
    });
}

document.querySelectorAll('.device-delete-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const deviceId = button.dataset.deviceId;
        const deviceName = button.dataset.deviceName || '장치';

        if (!confirm(`${deviceName}을(를) 삭제할까요?`)) {
            return;
        }

        try {
            button.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/rooms/${roomId}/devices/${deviceId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '장치 삭제에 실패했습니다.');
        } finally {
            button.disabled = false;
        }
    });
});

document.querySelectorAll('.device-power-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const deviceId = button.dataset.deviceId;
        const powerState = button.dataset.nextPowerState;

        try {
            button.disabled = true;
            const response = await fetch(
                `/api/front/teams/${teamId}/rooms/${roomId}/devices/${deviceId}/power-state`,
                {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ powerState })
                }
            );

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '장치 전원 상태를 변경하지 못했습니다.');
            button.disabled = false;
        }
    });
});

function closeDeviceModal() {
    deviceModalOverlay.classList.remove('active');
    deviceNameInput.value = '';
}
