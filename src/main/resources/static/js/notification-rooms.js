document.querySelectorAll('.notif-toggle').forEach(toggle => {
    toggle.addEventListener('change', async () => {
        const row = toggle.closest('.notif-room-row');
        const teamId = row.dataset.teamId;
        const buildingId = row.dataset.buildingId;
        const roomId = row.dataset.roomId;
        const subscribed = row.dataset.subscribed === 'true';
        const enabled = toggle.checked;
        const url = `/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}/subscription`;

        toggle.disabled = true;
        try {
            if (!subscribed) {
                if (!enabled) {
                    return;
                }
                await fetch(url, { method: 'PUT' }).then(assertOk);
                row.dataset.subscribed = 'true';
            } else {
                await fetch(url, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ notificationEnabled: enabled })
                }).then(assertOk);
            }
        } catch (error) {
            console.error(error);
            alert('알림 설정을 변경하지 못했습니다.');
            toggle.checked = !enabled;
        } finally {
            toggle.disabled = false;
        }
    });
});

async function assertOk(response) {
    if (!response.ok) {
        throw new Error(`요청 실패 (${response.status})`);
    }
    return response;
}
