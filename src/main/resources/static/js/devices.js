const openDeviceModalBtn = document.getElementById('openDeviceModalBtn');
const closeDeviceModalBtn = document.getElementById('closeDeviceModalBtn');
const createDeviceBtn = document.getElementById('createDeviceBtn');
const deviceModalOverlay = document.getElementById('deviceModalOverlay');
const deviceNameInput = document.getElementById('deviceNameInput');
const deviceHistoryDialog = document.getElementById('deviceHistoryDialog');
const closeDeviceHistoryBtn = document.getElementById('closeDeviceHistoryBtn');
const searchDeviceHistoryBtn = document.getElementById('searchDeviceHistoryBtn');
const deviceHistoryDayOfWeek = document.getElementById('deviceHistoryDayOfWeek');
const deviceHistoryStartAt = document.getElementById('deviceHistoryStartAt');
const deviceHistoryEndAt = document.getElementById('deviceHistoryEndAt');
const deviceHistoryMessage = document.getElementById('deviceHistoryMessage');
const deviceHistoryTableBody = document.getElementById('deviceHistoryTableBody');
const deviceHistorySubtitle = document.getElementById('deviceHistorySubtitle');
let selectedHistoryDeviceId = null;
let selectedHistoryDeviceName = '';

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

document.querySelectorAll('.device-action-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const deviceId = button.dataset.deviceId;
        const currentAction = button.dataset.currentAction === 'ON' ? 'ON' : 'OFF';
        const nextAction = currentAction === 'ON' ? 'OFF' : 'ON';

        try {
            button.disabled = true;
            const response = await fetch(
                `/api/front/teams/${teamId}/devices/${deviceId}/action-histories`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action: nextAction })
                }
            );

            if (!response.ok) {
                throw new Error(await response.text());
            }

            renderDeviceAction(button, nextAction);
        } catch (error) {
            alert(error.message || '장치 동작 상태를 변경하지 못했습니다.');
            button.disabled = false;
        }
    });
});

document.querySelectorAll('.device-history-btn').forEach(button => {
    button.addEventListener('click', async () => {
        selectedHistoryDeviceId = button.dataset.deviceId;
        selectedHistoryDeviceName = button.dataset.deviceName || '기기';
        if (deviceHistorySubtitle) {
            deviceHistorySubtitle.textContent = `${selectedHistoryDeviceName}의 ON/OFF 변경 이력을 조회합니다.`;
        }
        clearDeviceHistoryFilters();
        openDeviceHistoryDialog();
        await loadDeviceActionHistories();
    });
});

if (closeDeviceHistoryBtn) {
    closeDeviceHistoryBtn.addEventListener('click', closeDeviceHistoryDialog);
}

if (deviceHistoryDialog) {
    deviceHistoryDialog.addEventListener('click', event => {
        if (event.target === deviceHistoryDialog) {
            closeDeviceHistoryDialog();
        }
    });
}

if (searchDeviceHistoryBtn) {
    searchDeviceHistoryBtn.addEventListener('click', loadDeviceActionHistories);
}

function closeDeviceModal() {
    deviceModalOverlay.classList.remove('active');
    deviceNameInput.value = '';
}

function renderDeviceAction(button, action) {
    button.dataset.currentAction = action;
    button.textContent = action === 'ON' ? '끄기' : '켜기';
    button.disabled = false;

    const row = button.closest('article, li');
    const actionText = row?.querySelector('.device-action-text');
    if (actionText) {
        actionText.textContent = action;
    }

    const actionBadge = row?.querySelector('.device-action-badge');
    if (actionBadge) {
        actionBadge.textContent = action;
        actionBadge.classList.toggle('on', action === 'ON');
        actionBadge.classList.toggle('off', action !== 'ON');
    }
}

function clearDeviceHistoryFilters() {
    if (deviceHistoryDayOfWeek) deviceHistoryDayOfWeek.value = '';
    if (deviceHistoryStartAt) deviceHistoryStartAt.value = '';
    if (deviceHistoryEndAt) deviceHistoryEndAt.value = '';
}

function openDeviceHistoryDialog() {
    if (!deviceHistoryDialog) return;
    if (deviceHistoryDialog.showModal) {
        deviceHistoryDialog.showModal();
    } else {
        deviceHistoryDialog.setAttribute('open', '');
    }
}

function closeDeviceHistoryDialog() {
    if (!deviceHistoryDialog) return;
    if (deviceHistoryDialog.open && deviceHistoryDialog.close) {
        deviceHistoryDialog.close();
    } else {
        deviceHistoryDialog.removeAttribute('open');
    }
}

async function loadDeviceActionHistories() {
    if (!selectedHistoryDeviceId || !deviceHistoryTableBody) return;

    try {
        if (searchDeviceHistoryBtn) searchDeviceHistoryBtn.disabled = true;
        setDeviceHistoryMessage('이력을 불러오는 중입니다.');
        deviceHistoryTableBody.innerHTML = '';

        const response = await fetch(deviceActionHistoryUrl(selectedHistoryDeviceId), {
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        const histories = await response.json();
        renderDeviceActionHistories(histories);
        setDeviceHistoryMessage(histories.length === 0 ? '조회된 이력이 없습니다.' : `총 ${histories.length}건`);
    } catch (error) {
        setDeviceHistoryMessage(error.message || '기기 동작 이력을 불러오지 못했습니다.', true);
    } finally {
        if (searchDeviceHistoryBtn) searchDeviceHistoryBtn.disabled = false;
    }
}

function deviceActionHistoryUrl(deviceId) {
    const params = new URLSearchParams();
    if (deviceHistoryDayOfWeek?.value) params.set('dayOfWeek', deviceHistoryDayOfWeek.value);
    if (deviceHistoryStartAt?.value) params.set('startAt', toLocalDateTime(deviceHistoryStartAt.value));
    if (deviceHistoryEndAt?.value) params.set('endAt', toLocalDateTime(deviceHistoryEndAt.value));

    const query = params.toString();
    return `/api/front/teams/${teamId}/devices/${deviceId}/action-histories${query ? `?${query}` : ''}`;
}

function toLocalDateTime(value) {
    return value.length === 16 ? `${value}:00` : value;
}

function renderDeviceActionHistories(histories) {
    if (!Array.isArray(histories) || histories.length === 0) {
        deviceHistoryTableBody.innerHTML = '<tr><td colspan="3">조회된 이력이 없습니다.</td></tr>';
        return;
    }

    deviceHistoryTableBody.innerHTML = histories.map(history => `
        <tr>
            <td>${escapeHtml(formatRecordedAt(history.recordedAt))}</td>
            <td>${escapeHtml(formatWeekday(history.dayOfWeek))}</td>
            <td><span class="device-action-badge ${history.action === 'ON' ? 'on' : 'off'}">${escapeHtml(history.action)}</span></td>
        </tr>
    `).join('');
}

function formatRecordedAt(value) {
    if (!value) return '-';
    return String(value).replace('T', ' ');
}

function formatWeekday(value) {
    const labels = { MON: '월', TUE: '화', WED: '수', THU: '목', FRI: '금', SAT: '토', SUN: '일' };
    return labels[value] || value || '-';
}

function setDeviceHistoryMessage(text, isError = false) {
    if (!deviceHistoryMessage) return;
    deviceHistoryMessage.textContent = text;
    deviceHistoryMessage.classList.toggle('error', isError);
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
