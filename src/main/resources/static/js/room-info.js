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
