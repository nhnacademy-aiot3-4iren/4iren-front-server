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

function closeDeviceModal() {
    deviceModalOverlay.classList.remove('active');
    deviceNameInput.value = '';
}
