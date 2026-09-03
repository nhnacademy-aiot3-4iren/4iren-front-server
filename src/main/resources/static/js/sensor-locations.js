const containerElement = document.getElementById('roomInfoContainer') || document.getElementById('buildingInfoContainer');
const teamId = containerElement ? containerElement.dataset.teamId : null;
const buildingId = containerElement ? containerElement.dataset.buildingId : null;
const roomId = containerElement ? containerElement.dataset.roomId : null;

document.addEventListener('DOMContentLoaded', () => {
    const openSensorModalBtn = document.getElementById('openSensorModalBtn');
    const closeSensorModalBtn = document.getElementById('closeSensorModalBtn');
    const sensorModalOverlay = document.getElementById('sensorModalOverlay');
    const availableSensorsContainer = document.getElementById('availableSensorsContainer');
    const createSensorBtn = document.getElementById('createSensorBtn');

    // 1. 모달 열기 & 미등록 센서 목록 조회 (등록된 센서 필터링)
    if (openSensorModalBtn) {
        openSensorModalBtn.onclick = async () => {
            availableSensorsContainer.innerHTML = '<p class="sensor-select-status">센서 목록을 불러오는 중...</p>';
            sensorModalOverlay.classList.add('active');

            try {
                // 건물 전체 센서와 현재 강의실에 이미 등록된 센서를 병렬로 조회
                const [buildingSensorsRes, registeredSensorsRes] = await Promise.all([
                    fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/sensors`),
                    fetch(`/api/front/teams/${teamId}/rooms/${roomId}/sensor-locations?size=1000`)
                ]);

                if (!buildingSensorsRes.ok || !registeredSensorsRes.ok) {
                    throw new Error('센서 목록을 불러오지 못했습니다.');
                }

                const allSensors = await buildingSensorsRes.json();
                const registeredData = await registeredSensorsRes.json();

                // 이미 등록된 센서들의 DevEUI Set 생성
                const registeredDevEuis = new Set(
                    (registeredData.content || []).map(item => item.devEui)
                );

                // 이미 등록된 센서 제외 (미등록 센서만 필터링)
                const unassignedSensors = (allSensors || []).filter(
                    sensor => !registeredDevEuis.has(sensor.devEui)
                );

                if (unassignedSensors.length === 0) {
                    availableSensorsContainer.innerHTML = '<p class="sensor-select-status">모든 센서가 이미 등록되어 추가 가능한 센서가 없습니다.</p>';
                    return;
                }

                // 미등록 센서 목록만 체크박스로 렌더링
                availableSensorsContainer.innerHTML = unassignedSensors.map(s => {
                    const loc = (s.location || '').trim();
                    const pt = (s.point || '').trim();
                    const displayName = (loc || pt) ? `${loc} ${pt}`.trim() : s.devEui;

                    return `
                        <label class="sensor-select-item">
                            <input type="checkbox" name="selectedSensor" class="sensor-checkbox" value="${escapeHtml(s.devEui)}">
                            <div class="sensor-item-info">
                                <span class="sensor-location-title">${escapeHtml(displayName)}</span>
                                <span class="sensor-deveui-sub">(${escapeHtml(s.devEui)})</span>
                            </div>
                        </label>
                    `;
                }).join('');

            } catch (error) {
                availableSensorsContainer.innerHTML = `<p class="sensor-select-status" style="color:#e5484d;">${escapeHtml(error.message)}</p>`;
            }
        };
    }

    // 2. 모달 닫기
    if (closeSensorModalBtn) {
        closeSensorModalBtn.onclick = () => {
            sensorModalOverlay.classList.remove('active');
        };
    }

    // 3. 센서 일괄 등록 (Submit)
    if (createSensorBtn) {
        createSensorBtn.onclick = async () => {
            const checkedBoxes = availableSensorsContainer.querySelectorAll('input[name="selectedSensor"]:checked');
            const selectedDevEuis = Array.from(checkedBoxes).map(cb => cb.value);

            if (selectedDevEuis.length === 0) {
                alert('등록할 센서를 최소 1개 이상 선택해주세요.');
                return;
            }

            createSensorBtn.disabled = true;
            const originalText = createSensorBtn.textContent;
            createSensorBtn.textContent = '등록 중...';

            try {
                const res = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}/rooms/${roomId}/sensors/bulk`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sensorIds: selectedDevEuis })
                });

                if (!res.ok) {
                    throw new Error(await res.text() || '센서 등록에 실패했습니다.');
                }

                alert('센서가 정상적으로 등록되었습니다.');
                sensorModalOverlay.classList.remove('active');
                window.location.reload();
            } catch (error) {
                alert(error.message || '센서 등록 중 오류가 발생했습니다.');
                createSensorBtn.disabled = false;
                createSensorBtn.textContent = originalText;
            }
        };
    }

    // 4. 센서 삭제
    document.querySelectorAll('.sensor-delete-btn').forEach(btn => {
        btn.onclick = async () => {
            const sensorLocationId = btn.dataset.sensorLocationId;
            const devEui = btn.dataset.devEui;

            if (!confirm(`[${devEui}] 센서를 삭제하시겠습니까?`)) return;

            try {
                const res = await fetch(`/api/front/teams/${teamId}/rooms/${roomId}/sensor-locations/${sensorLocationId}`, {
                    method: 'DELETE'
                });

                if (!res.ok) throw new Error(await res.text() || '센서 삭제에 실패했습니다.');

                alert('센서가 삭제되었습니다.');
                window.location.reload();
            } catch (error) {
                alert(error.message || '삭제 중 오류가 발생했습니다.');
            }
        };
    });

    function escapeHtml(value) {
        return String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }
});