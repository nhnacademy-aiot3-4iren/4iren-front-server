document.addEventListener('DOMContentLoaded', () => {
    // 1. Thymeleaf 데이터 속성에서 teamId 및 권한 정보 읽기
    const listsPanel = document.querySelector('.info-lists-panel');
    const teamId = window.teamId ?? (listsPanel ? Number(listsPanel.dataset.teamId) : null);
    const canManage = window.canManage !== undefined
        ? window.canManage
        : (listsPanel ? listsPanel.dataset.canManage === 'true' : false);

    // DOM 요소 캐싱
    const editTeamName = document.getElementById('editTeamName');
    const clearNameBtn = document.getElementById('clearNameBtn');
    const saveTeamInfoBtn = document.getElementById('saveTeamInfoBtn');
    const editTeamDesc = document.getElementById('editTeamDesc');

    // 2. 팀 이름 입력창 클리어(X) 버튼 토글
    if (clearNameBtn && editTeamName) {
        function toggleClearButton() {
            clearNameBtn.style.display = editTeamName.value.length > 0 ? 'flex' : 'none';
        }
        toggleClearButton();
        editTeamName.addEventListener('input', toggleClearButton);
        clearNameBtn.addEventListener('click', () => {
            editTeamName.value = '';
            toggleClearButton();
            editTeamName.focus();
        });
    }

    // 3. 팀 정보(이름, 설명) 수정 저장
    if (saveTeamInfoBtn) {
        saveTeamInfoBtn.addEventListener('click', async () => {
            const updatedName = editTeamName.value.trim();
            const updatedDesc = editTeamDesc.value.trim();

            if (!updatedName) {
                alert("팀 이름을 입력해주세요.");
                return;
            }

            try {
                const response = await fetch(`/api/front/teams/${teamId}`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ teamName: updatedName, description: updatedDesc })
                });

                if (!response.ok) throw new Error(await response.text());

                alert("팀 정보가 성공적으로 수정되었습니다!");
                window.location.reload();
            } catch (error) {
                alert(error.message || "팀 정보 수정에 실패했습니다.");
            }
        });
    }

    // 우측 리스트 DOM 요소
    const buildingListEl = document.getElementById('buildingList');
    const classroomListEl = document.getElementById('classroomList');
    const classroomListTitle = document.getElementById('classroomListTitle');
    const classroomListSubtitle = document.getElementById('classroomListSubtitle');
    const classroomActions = document.getElementById('classroomActions');
    const addBuildingBtn = document.getElementById('addBuildingBtn');
    const deleteBuildingBtn = document.getElementById('deleteBuildingBtn');
    const addRoomBtn = document.getElementById('addRoomBtn');
    const deleteRoomBtn = document.getElementById('deleteRoomBtn');

    let selectedBuildingId = null;
    let selectedRoomId = null;

    // 공통 Fetch JSON 헬퍼 함수
    async function requestJson(url, options = {}) {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
            ...options
        });
        if (!response.ok) throw new Error((await response.text()) || `요청 실패 (${response.status})`);
        if (response.status === 204) return null;
        return response.json();
    }

    // 4. 건물 목록 조회 및 렌더링
    async function loadBuildings() {
        if (!buildingListEl) return;
        buildingListEl.innerHTML = '<li class="entity-list-empty">불러오는 중입니다...</li>';
        try {
            const result = await requestJson(`/api/front/teams/${teamId}/buildings?size=100&sort=id,ASC`);
            renderBuildings(result.content || []);
        } catch (error) {
            buildingListEl.innerHTML = `<li class="entity-list-empty">${error.message || '건물 목록을 불러오지 못했습니다.'}</li>`;
        }
    }

    function renderBuildings(buildings) {
        if (buildings.length === 0) {
            buildingListEl.innerHTML = '<li class="entity-list-empty">등록된 건물이 없습니다.</li>';
            return;
        }

        buildingListEl.innerHTML = buildings.map(b => `
            <li class="entity-list-item" data-building-id="${b.buildingId}">
                <span class="entity-name">${escapeHtml(b.buildingName)}</span>
                <a href="/teams/${teamId}/buildings/${b.buildingId}" class="btn-detail-blue" onclick="event.stopPropagation()">상세보기</a>
            </li>`).join('');

        buildingListEl.querySelectorAll('.entity-list-item').forEach(li => {
            li.addEventListener('click', () => selectBuilding(Number(li.dataset.buildingId), li));
        });
    }

    function selectBuilding(buildingId, liEl) {
        selectedBuildingId = buildingId;
        selectedRoomId = null;

        buildingListEl.querySelectorAll('.entity-list-item').forEach(li => li.classList.remove('selected'));
        liEl.classList.add('selected');

        if (deleteBuildingBtn) deleteBuildingBtn.disabled = false;
        if (deleteRoomBtn) deleteRoomBtn.disabled = true;

        loadRooms(buildingId);
    }

    // 5. 강의실 목록 조회 및 렌더링
    async function loadRooms(buildingId) {
        const selectedName = buildingListEl.querySelector('.entity-list-item.selected .entity-name')?.textContent || '';
        classroomListTitle.textContent = 'Classroom List';
        classroomListSubtitle.textContent = `${selectedName}에 소속된 강의실 리스트`;
        classroomListEl.innerHTML = '<li class="entity-list-empty">불러오는 중입니다...</li>';

        if (canManage && classroomActions) classroomActions.style.display = 'flex';

        try {
            const result = await requestJson(`/api/front/teams/${teamId}/buildings/${buildingId}/rooms?size=100&sort=id,ASC`);
            renderRooms(result.content || []);
        } catch (error) {
            classroomListEl.innerHTML = `<li class="entity-list-empty">${error.message || '강의실 목록을 불러오지 못했습니다.'}</li>`;
        }
    }

    function renderRooms(rooms) {
        if (rooms.length === 0) {
            classroomListEl.innerHTML = '<li class="entity-list-empty">등록된 강의실이 없습니다.</li>';
            return;
        }

        classroomListEl.innerHTML = rooms.map(r => `
            <li class="entity-list-item" data-room-id="${r.roomId}">
                <span class="entity-name">${escapeHtml(r.roomName)}</span>
                <a href="/teams/${teamId}/buildings/${selectedBuildingId}/rooms/${r.roomId}" class="btn-detail-blue" onclick="event.stopPropagation()">상세보기</a>
            </li>`).join('');

        classroomListEl.querySelectorAll('.entity-list-item').forEach(li => {
            li.addEventListener('click', () => {
                classroomListEl.querySelectorAll('.entity-list-item').forEach(el => el.classList.remove('selected'));
                li.classList.add('selected');
                selectedRoomId = Number(li.dataset.roomId);
                if (deleteRoomBtn) deleteRoomBtn.disabled = false;
            });
        });
    }

    // 6. 건물 추가 모달 컨트롤
    const addBuildingModal = document.getElementById('addBuildingModal');

    function resetAddBuildingForm() {
        document.getElementById('newBuildingName').value = '';
        document.getElementById('newBuildingDesc').value = '';
        document.getElementById('newBuildingRegion').value = '';
        document.getElementById('newBuildingRoadAddress').value = '';
        document.getElementById('newBuildingDetailAddress').value = '';
        document.getElementById('newMqttServerName').value = '';
        document.getElementById('newMqttBrokerUrl').value = '';
        document.getElementById('newMqttTopic').value = '';
        document.getElementById('newMqttUsername').value = '';
        document.getElementById('newMqttPassword').value = '';
        document.getElementById('addBuildingError').textContent = '';
    }

    if (addBuildingBtn) {
        addBuildingBtn.addEventListener('click', () => {
            resetAddBuildingForm();
            addBuildingModal.showModal();
        });
    }
    document.getElementById('addBuildingModalCloseBtn')?.addEventListener('click', () => addBuildingModal.close());
    document.getElementById('addBuildingCancelBtn')?.addEventListener('click', () => addBuildingModal.close());

    // 6-1. 카카오/다음 우편번호 검색 연동
    document.getElementById('newBuildingSearchAddressBtn')?.addEventListener('click', () => {
        if (!globalThis.kakao || !globalThis.kakao.Postcode) {
            alert('주소 검색 서비스를 불러오지 못했습니다.');
            return;
        }

        new kakao.Postcode({
            oncomplete: data => {
                const jibunAddress = data.jibunAddress || data.autoJibunAddress || data.address || '';
                document.getElementById('newBuildingRoadAddress').value = data.roadAddress || '';
                document.getElementById('newBuildingRegion').value = extractRegionName(jibunAddress, data);
                document.getElementById('newBuildingDetailAddress').focus();
            }
        }).open();
    });

    function extractRegionName(jibunAddress, data) {
        const fallback = [data.sido, data.sigungu].filter(Boolean).join(' ');
        const tokens = jibunAddress.trim().split(/\s+/);
        const dongIndex = tokens.findIndex(token => token.endsWith('동'));

        if (dongIndex < 0) {
            return fallback;
        }

        return tokens.slice(0, dongIndex + 1).join(' ');
    }

    document.getElementById('addBuildingSubmitBtn')?.addEventListener('click', async () => {
        const errorEl = document.getElementById('addBuildingError');
        const buildingName = document.getElementById('newBuildingName').value.trim();
        if (!buildingName) {
            errorEl.textContent = '건물 이름을 입력해주세요.';
            return;
        }

        try {
            const buildingResult = await requestJson(`/api/front/teams/${teamId}/buildings`, {
                method: 'POST',
                body: JSON.stringify({
                    buildingName,
                    description: document.getElementById('newBuildingDesc').value.trim(),
                    regionName: document.getElementById('newBuildingRegion').value.trim(),
                    roadAddress: document.getElementById('newBuildingRoadAddress').value.trim(),
                    detailAddress: document.getElementById('newBuildingDetailAddress').value.trim()
                })
            });

            const buildingId = buildingResult.buildingId;
            const serverName = document.getElementById('newMqttServerName').value.trim();
            const brokerUrl = document.getElementById('newMqttBrokerUrl').value.trim();
            const topic = document.getElementById('newMqttTopic').value.trim();
            const username = document.getElementById('newMqttUsername').value.trim();
            const password = document.getElementById('newMqttPassword').value.trim();

            if (serverName && brokerUrl && topic) {
                try {
                    await requestJson(`/api/front/teams/${teamId}/buildings/${buildingId}/mqtt`, {
                        method: 'POST',
                        body: JSON.stringify({
                            serverName,
                            brokerUrl,
                            topic,
                            username: username || null,
                            password: password || null
                        })
                    });
                } catch (mqttErr) {
                    console.error('MQTT 브로커 등록 실패:', mqttErr);
                    alert(`건물은 추가되었으나, MQTT 브로커 등록에 실패했습니다:\n${mqttErr.message || ''}`);
                }
            }

            resetAddBuildingForm();
            addBuildingModal.close();
            await loadBuildings();
        } catch (error) {
            errorEl.textContent = error.message || '건물 추가에 실패했습니다.';
        }
    });

    // 7. 건물 삭제
    if (deleteBuildingBtn) {
        deleteBuildingBtn.addEventListener('click', async () => {
            if (!selectedBuildingId) return;
            if (!confirm('선택한 건물을 삭제하시겠습니까? 하위 강의실이 있는 건물은 삭제할 수 없습니다.')) return;

            try {
                await requestJson(`/api/front/teams/${teamId}/buildings/${selectedBuildingId}`, { method: 'DELETE' });
                selectedBuildingId = null;
                deleteBuildingBtn.disabled = true;
                classroomListEl.innerHTML = '<li class="entity-list-empty">왼쪽에서 건물을 선택하세요.</li>';
                classroomListSubtitle.textContent = '건물을 선택하면 강의실 목록이 표시됩니다';
                classroomActions.style.display = 'none';
                await loadBuildings();
            } catch (error) {
                alert(error.message || '건물 삭제에 실패했습니다. (강의실이 남아있지 않은지 확인해주세요)');
            }
        });
    }

    // 8. 강의실 추가 모달 컨트롤
    const addRoomModal = document.getElementById('addRoomModal');
    if (addRoomBtn) {
        addRoomBtn.addEventListener('click', () => {
            if (!selectedBuildingId) {
                alert('먼저 왼쪽에서 건물을 선택해주세요.');
                return;
            }
            document.getElementById('newRoomName').value = '';
            document.getElementById('newRoomDesc').value = '';
            document.getElementById('addRoomError').textContent = '';
            addRoomModal.showModal();
        });
    }
    document.getElementById('addRoomModalCloseBtn')?.addEventListener('click', () => addRoomModal.close());
    document.getElementById('addRoomCancelBtn')?.addEventListener('click', () => addRoomModal.close());

    document.getElementById('addRoomSubmitBtn')?.addEventListener('click', async () => {
        const errorEl = document.getElementById('addRoomError');
        const roomName = document.getElementById('newRoomName').value.trim();

        if (!roomName) {
            errorEl.textContent = '강의실 이름을 입력해주세요.';
            return;
        }

        try {
            await requestJson(`/api/front/teams/${teamId}/buildings/${selectedBuildingId}/rooms`, {
                method: 'POST',
                body: JSON.stringify({
                    roomName,
                    description: document.getElementById('newRoomDesc').value.trim()
                })
            });
            addRoomModal.close();
            errorEl.textContent = '';
            await loadRooms(selectedBuildingId);
        } catch (error) {
            errorEl.textContent = error.message || '강의실 추가에 실패했습니다.';
        }
    });

    // 9. 강의실 삭제
    if (deleteRoomBtn) {
        deleteRoomBtn.addEventListener('click', async () => {
            if (!selectedRoomId) return;
            if (!confirm('선택한 강의실을 삭제하시겠습니까?')) return;

            try {
                await requestJson(`/api/front/teams/${teamId}/buildings/${selectedBuildingId}/rooms/${selectedRoomId}`, { method: 'DELETE' });
                selectedRoomId = null;
                deleteRoomBtn.disabled = true;
                await loadRooms(selectedBuildingId);
            } catch (error) {
                alert(error.message || '강의실 삭제에 실패했습니다.');
            }
        });
    }

    // XSS 방지 HTML Escape 헬퍼 함수
    function escapeHtml(value) {
        return String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    // 건물 목록 최초 로드
    loadBuildings();
});