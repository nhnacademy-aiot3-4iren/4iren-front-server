const subscribeBtn = document.getElementById('subscribeBtn');
const unsubscribeBtn = document.getElementById('unsubscribeBtn');
const notificationToggle = document.getElementById('notificationToggle');
const subscriptionStatusText = document.getElementById('subscriptionStatusText');
const notificationToggleLabel = document.querySelector('.notification-toggle');

if (subscribeBtn) {
    subscribeBtn.addEventListener('click', async () => {
        try {
            subscribeBtn.disabled = true;
            const subscription = await requestJson(subscriptionUrl(), { method: 'PUT' });
            renderSubscription(true, subscription.notificationEnabled);
        } catch (error) {
            alert(error.message || '강의실 구독에 실패했습니다.');
        } finally {
            subscribeBtn.disabled = false;
        }
    });
}

if (unsubscribeBtn) {
    unsubscribeBtn.addEventListener('click', async () => {
        try {
            unsubscribeBtn.disabled = true;
            await requestJson(subscriptionUrl(), { method: 'DELETE' });
            renderSubscription(false, false);
        } catch (error) {
            alert(error.message || '구독 취소에 실패했습니다.');
        } finally {
            unsubscribeBtn.disabled = false;
        }
    });
}

if (notificationToggle) {
    notificationToggle.addEventListener('change', async () => {
        const nextEnabled = notificationToggle.checked;

        try {
            notificationToggle.disabled = true;
            const subscription = await requestJson(subscriptionUrl(), {
                method: 'PATCH',
                body: JSON.stringify({ notificationEnabled: nextEnabled })
            });
            renderSubscription(true, subscription.notificationEnabled);
        } catch (error) {
            notificationToggle.checked = !nextEnabled;
            alert(error.message || '알림 설정 변경에 실패했습니다.');
        } finally {
            notificationToggle.disabled = false;
        }
    });
}

function subscriptionUrl() {
    return `/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}/subscription`;
}

function renderSubscription(subscribed, notificationEnabled) {
    if (subscriptionStatusText) {
        subscriptionStatusText.textContent = subscribed ? '구독 중인 강의실입니다.' : '구독하지 않은 강의실입니다.';
    }

    if (subscribeBtn) {
        subscribeBtn.style.display = subscribed ? 'none' : '';
    }
    if (unsubscribeBtn) {
        unsubscribeBtn.style.display = subscribed ? '' : 'none';
    }
    if (notificationToggle) {
        notificationToggle.checked = Boolean(notificationEnabled);
        notificationToggle.disabled = !subscribed;
    }
    if (notificationToggleLabel) {
        notificationToggleLabel.classList.toggle('disabled', !subscribed);
    }
}

// =======================================================
// 강의실 정보 수정 모달 제어
// =======================================================
const openEditRoomModalBtn = document.getElementById('openEditRoomModalBtn');
const editRoomModal = document.getElementById('editRoomModal');
const closeEditRoomModalBtn = document.getElementById('closeEditRoomModalBtn');
const cancelEditRoomModalBtn = document.getElementById('cancelEditRoomModalBtn');
const editRoomForm = document.getElementById('editRoomForm');
const editRoomError = document.getElementById('editRoomError');
const saveEditRoomBtn = document.getElementById('saveEditRoomBtn');

if (openEditRoomModalBtn && editRoomModal) {
    openEditRoomModalBtn.addEventListener('click', () => {
        if (editRoomError) editRoomError.textContent = '';
        editRoomModal.showModal();
    });
}

const closeEditRoomModal = () => editRoomModal && editRoomModal.close();
if (closeEditRoomModalBtn) closeEditRoomModalBtn.addEventListener('click', closeEditRoomModal);
if (cancelEditRoomModalBtn) cancelEditRoomModalBtn.addEventListener('click', closeEditRoomModal);

if (editRoomForm) {
    editRoomForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (editRoomError) editRoomError.textContent = '';

        const roomName = document.getElementById('editRoomName').value.trim();
        const description = document.getElementById('editRoomDesc').value.trim();

        if (!roomName) {
            editRoomError.textContent = '강의실 이름을 입력해주세요.';
            return;
        }

        if (saveEditRoomBtn) {
            saveEditRoomBtn.disabled = true;
            saveEditRoomBtn.textContent = '저장 중...';
        }

        try {
            await requestJson(`/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}`, {
                method: 'PATCH',
                body: JSON.stringify({
                    roomName,
                    description: description || null
                })
            });

            alert('강의실 정보가 성공적으로 수정되었습니다.');
            closeEditRoomModal();
            window.location.reload();
        } catch (err) {
            if (editRoomError) editRoomError.textContent = err.message || '저장 중 오류가 발생했습니다.';
            if (saveEditRoomBtn) {
                saveEditRoomBtn.disabled = false;
                saveEditRoomBtn.textContent = '수정 저장';
            }
        }
    });
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `요청 실패 (${response.status})`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}
