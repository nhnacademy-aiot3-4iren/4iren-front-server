(() => {
    const rows = document.getElementById('room-rows');
    if (!rows) return;

    const state = document.getElementById('room-list-state');
    const roomCount = document.getElementById('room-count');
    const metricUnits = {
        temperature: '°C',
        humidity: '%',
        co2: ' ppm'
    };

    document.addEventListener('DOMContentLoaded', initialize);

    async function initialize() {
        try {
            const rooms = await fetchJson('/api/front/dashboard/rooms');
            roomCount.textContent = String(rooms.length);
            if (!rooms.length) {
                state.textContent = '구독한 공간이 없습니다.';
                return;
            }

            rows.replaceChildren(...rooms.map(createRoomRow));
            rows.hidden = false;
            state.hidden = true;
            rooms.forEach(loadSummary);
        } catch (error) {
            roomCount.textContent = '-';
            state.textContent = '공간 목록을 불러오지 못했습니다.';
        }
    }

    function createRoomRow(room) {
        const row = document.createElement('div');
        row.className = 'room-row';
        row.dataset.roomId = room.roomId;
        row.innerHTML = `
            <div class="room-identity">
                <span class="room-thumb" aria-hidden="true">⌂</span>
                <div><strong></strong><p></p></div>
            </div>
            ${metricMarkup('temperature', '온도')}
            ${metricMarkup('humidity', '습도')}
            ${metricMarkup('co2', 'CO2')}
            <span class="room-arrow" aria-hidden="true">›</span>`;
        row.querySelector('.room-identity strong').textContent = room.roomName || `Room ${room.roomId}`;
        row.querySelector('.room-identity p').textContent = room.buildingName || '위치 정보 없음';
        return row;
    }

    function metricMarkup(code, label) {
        return `<div class="room-metric" data-metric="${code}">
            <span class="room-metric-label">${label}</span>
            <div><strong>-</strong><span class="room-metric-unit"></span></div>
            <p>불러오는 중</p>
        </div>`;
    }

    async function loadSummary(room) {
        const row = rows.querySelector(`[data-room-id="${CSS.escape(String(room.roomId))}"]`);
        try {
            const summary = await fetchJson(`/api/front/dashboard/teams/${room.teamId}/rooms/${room.roomId}/summary`);
            ['temperature', 'humidity', 'co2'].forEach(code => renderMetric(row, code, summary[code]));
        } catch (error) {
            ['temperature', 'humidity', 'co2'].forEach(code => {
                const metric = row.querySelector(`[data-metric="${code}"]`);
                metric.querySelector('p').textContent = '데이터 없음';
            });
        }
    }

    function renderMetric(row, code, metricValue) {
        const metric = row.querySelector(`[data-metric="${code}"]`);
        if (metricValue && metricValue.averageValue != null) {
            metric.querySelector('strong').textContent = formatNumber(metricValue.averageValue);
            metric.querySelector('.room-metric-unit').textContent = metricUnits[code] || '';
            metric.querySelector('p').textContent = '최근 15분 평균';
        } else {
            metric.querySelector('strong').textContent = '-';
            metric.querySelector('.room-metric-unit').textContent = '';
            metric.querySelector('p').textContent = '데이터 없음';
        }
    }

    async function fetchJson(url) {
        const response = await fetch(url, {headers: {Accept: 'application/json'}});
        if (!response.ok) throw new Error(`요청 실패 (${response.status})`);
        return response.json();
    }

    function formatNumber(value) {
        return new Intl.NumberFormat('ko-KR', {maximumFractionDigits: 1}).format(value);
    }
})();
