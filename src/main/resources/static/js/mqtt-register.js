const mqttRegisterForm = document.getElementById('mqttRegisterForm');
const mqttRegisterBtn = document.getElementById('mqttRegisterBtn');
const mqttResult = document.getElementById('mqttResult');
const serverNameInput = document.getElementById('serverNameInput');
const brokerUrlInput = document.getElementById('brokerUrlInput');
const usernameInput = document.getElementById('usernameInput');
const passwordInput = document.getElementById('passwordInput');
const topicInput = document.getElementById('topicInput');

if (mqttRegisterForm) {
    mqttRegisterForm.addEventListener('submit', async event => {
        event.preventDefault();

        const serverName = serverNameInput.value.trim();
        const brokerUrl = brokerUrlInput.value.trim();
        const topic = topicInput.value.trim();

        if (!serverName) {
            alert('서버 이름을 입력해주세요.');
            serverNameInput.focus();
            return;
        }
        if (!brokerUrl) {
            alert('브로커 URL을 입력해주세요.');
            brokerUrlInput.focus();
            return;
        }
        if (!topic) {
            alert('구독 토픽을 입력해주세요.');
            topicInput.focus();
            return;
        }

        try {
            mqttRegisterBtn.disabled = true;
            const broker = await requestJson(`/api/front/teams/${teamId}/buildings/${buildingId}/mqtt`, {
                method: 'POST',
                body: JSON.stringify({
                    serverName,
                    brokerUrl,
                    username: usernameInput.value.trim(),
                    password: passwordInput.value,
                    topic
                })
            });

            renderResult(broker);
        } catch (error) {
            alert(error.message || 'MQTT 등록에 실패했습니다.');
        } finally {
            mqttRegisterBtn.disabled = false;
        }
    });
}

function renderResult(broker) {
    if (!mqttResult) {
        return;
    }

    mqttResult.hidden = false;
    mqttResult.innerHTML = `
        <strong>등록 완료</strong>
        <span>ID ${escapeHtml(String(broker.id ?? '-'))}</span>
        <span>${escapeHtml(broker.serverName ?? '')}</span>
        <span>${escapeHtml(broker.topic ?? '')}</span>
    `;
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

    return response.json();
}

function escapeHtml(value) {
    return value
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
