const openRoomModalBtn = document.getElementById('openRoomModalBtn');
const closeRoomModalBtn = document.getElementById('closeRoomModalBtn');
const createRoomBtn = document.getElementById('createRoomBtn');
const roomModalOverlay = document.getElementById('roomModalOverlay');
const roomNameInput = document.getElementById('roomNameInput');
const roomDescInput = document.getElementById('roomDescInput');

if (openRoomModalBtn) {
    openRoomModalBtn.addEventListener('click', () => {
        roomModalOverlay.classList.add('active');
        roomNameInput.focus();
    });
}

if (closeRoomModalBtn) {
    closeRoomModalBtn.addEventListener('click', closeRoomModal);
}

if (roomModalOverlay) {
    roomModalOverlay.addEventListener('click', event => {
        if (event.target === roomModalOverlay) {
            closeRoomModal();
        }
    });
}

if (createRoomBtn) {
    createRoomBtn.addEventListener('click', async () => {
        const roomName = roomNameInput.value.trim();

        if (!roomName) {
            alert('강의실 이름을 입력해주세요.');
            roomNameInput.focus();
            return;
        }

        try {
            createRoomBtn.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/rooms`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    roomName,
                    description: roomDescInput.value.trim()
                })
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '강의실 생성에 실패했습니다.');
        } finally {
            createRoomBtn.disabled = false;
        }
    });
}

document.querySelectorAll('.room-delete-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const roomId = button.dataset.roomId;
        const roomName = button.dataset.roomName || '강의실';

        if (!confirm(`${roomName}을(를) 삭제할까요?`)) {
            return;
        }

        try {
            button.disabled = true;
            await requestJson(`/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}`, {
                method: 'DELETE'
            });
            window.location.reload();
        } catch (error) {
            alert(error.message || '강의실 삭제에 실패했습니다.');
        } finally {
            button.disabled = false;
        }
    });
});

document.querySelectorAll('.room-subscription-controls').forEach(control => {
    const roomId = control.dataset.roomId;
    const subscribeBtn = control.querySelector('.room-subscribe-btn');
    const unsubscribeBtn = control.querySelector('.room-unsubscribe-btn');
    const notificationToggle = control.querySelector('.room-notification-toggle');

    if (subscribeBtn) {
        subscribeBtn.addEventListener('click', async () => {
            try {
                subscribeBtn.disabled = true;
                const subscription = await requestJson(subscriptionUrl(roomId), { method: 'PUT' });
                renderSubscription(control, true, subscription.notificationEnabled);
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
                await requestJson(subscriptionUrl(roomId), { method: 'DELETE' });
                renderSubscription(control, false, false);
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
                const subscription = await requestJson(subscriptionUrl(roomId), {
                    method: 'PATCH',
                    body: JSON.stringify({ notificationEnabled: nextEnabled })
                });
                renderSubscription(control, true, subscription.notificationEnabled);
            } catch (error) {
                notificationToggle.checked = !nextEnabled;
                alert(error.message || '알림 설정 변경에 실패했습니다.');
            } finally {
                notificationToggle.disabled = false;
            }
        });
    }
});

function closeRoomModal() {
    roomModalOverlay.classList.remove('active');
    roomNameInput.value = '';
    roomDescInput.value = '';
}

function subscriptionUrl(roomId) {
    return `/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}/subscription`;
}

function renderSubscription(control, subscribed, notificationEnabled) {
    const statusText = control.querySelector('.room-subscription-text');
    const subscribeBtn = control.querySelector('.room-subscribe-btn');
    const unsubscribeBtn = control.querySelector('.room-unsubscribe-btn');
    const notificationToggle = control.querySelector('.room-notification-toggle');
    const notificationToggleLabel = control.querySelector('.notification-toggle');

    if (statusText) {
        statusText.textContent = subscribed ? '구독 중' : '미구독';
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
