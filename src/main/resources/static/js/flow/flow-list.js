(function () {
    'use strict';

    const app = document.getElementById('app');
    const sidebar = document.querySelector('.sidebar');
    const content = document.querySelector('.content');
    if (!app) return;

    const API = (content && content.dataset.apiBase)
        || (sidebar && sidebar.dataset.apiBase)
        || '/flows/api';
    const ROOM_ID = content ? content.dataset.roomId : null;
    const ROOM_URL = sidebar ? sidebar.dataset.roomUrlTemplate : '';

    const DAY_LABEL = {
        MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수', THURSDAY: '목',
        FRIDAY: '금', SATURDAY: '토', SUNDAY: '일'
    };
    const DAY_ORDER = Object.keys(DAY_LABEL);

    const scheduleCache = new Map();
    let selectedTemplateId = null;

    /* ================================================================
       공통
       ================================================================ */

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        if (!token || !header) return {};
        return { [header.content]: token.content };
    }

    async function request(url, options = {}) {
        const res = await fetch(url, {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                ...csrfHeaders()
            },
            ...options
        });
        if (!res.ok) {
            let message = '요청을 처리하지 못했습니다.';
            try {
                const body = await res.json();
                if (body && body.message) message = body.message;
            } catch (e) { /* 본문 없음 */ }
            const err = new Error(message);
            err.status = res.status;
            throw err;
        }
        if (res.status === 204) return null;
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }

    function toast(message, isError) {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = message;
        el.classList.toggle('is-error', Boolean(isError));
        el.hidden = false;
        clearTimeout(toast._t);
        toast._t = setTimeout(() => { el.hidden = true; }, 3200);
    }

    /* ================================================================
       사이드바 접기 / 펼치기
       ================================================================ */

    const sideToggle = document.getElementById('sideToggle');
    const STORAGE_KEY = 'flowSidebar';

    function setCollapsed(collapsed) {
        app.classList.toggle('is-collapsed', collapsed);
        if (sideToggle) sideToggle.setAttribute('aria-expanded', String(!collapsed));
        try {
            localStorage.setItem(STORAGE_KEY, collapsed ? 'collapsed' : 'expanded');
        } catch (e) { /* 무시 */ }
    }

    if (sideToggle) {
        sideToggle.addEventListener('click', () => {
            setCollapsed(!app.classList.contains('is-collapsed'));
        });
        try {
            if (localStorage.getItem(STORAGE_KEY) === 'collapsed') {
                app.classList.add('is-collapsed');
                sideToggle.setAttribute('aria-expanded', 'false');
            }
        } catch (e) { /* 무시 */ }
    }

    const searchIcon = document.getElementById('roomSearchIcon');
    if (searchIcon) {
        searchIcon.addEventListener('click', () => {
            setCollapsed(false);
            const input = document.getElementById('roomSearch');
            if (input) setTimeout(() => input.focus(), 240);
        });
    }

    /* ================================================================
       사이드바 트리
       ================================================================ */

    function roomUrl(roomId) {
        return ROOM_URL.replace('__ROOM_ID__', roomId);
    }

    document.querySelectorAll('.fs-building-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const building = btn.closest('.fs-building');
            const list = building.querySelector('.fs-rooms');
            const open = btn.getAttribute('aria-expanded') === 'true';

            btn.setAttribute('aria-expanded', String(!open));
            building.classList.toggle('is-open', !open);
            list.hidden = open;

            if (!open && list.dataset.loaded !== 'true') loadRooms(building, list);
        });
    });

    async function loadRooms(building, list) {
        const buildingId = building.dataset.buildingId;
        list.innerHTML = '<li><span class="skeleton" style="width:80%;height:18px"></span></li>';

        try {
            const data = await request(`${API}/buildings/${buildingId}/rooms`);
            renderRooms(list, (data && data.content) || []);
            list.dataset.loaded = 'true';
            applyRoomSearch();
        } catch (err) {
            list.innerHTML = '';
            const li = document.createElement('li');
            li.className = 'fs-rooms-error';
            li.append(err.message);

            const retry = document.createElement('button');
            retry.type = 'button';
            retry.textContent = '다시 시도';
            retry.addEventListener('click', () => loadRooms(building, list));
            li.appendChild(retry);
            list.appendChild(li);
        }
    }

    function renderRooms(list, rooms) {
        list.innerHTML = '';

        if (!rooms.length) {
            const li = document.createElement('li');
            li.className = 'fs-rooms-empty';
            const span = document.createElement('span');
            span.textContent = '등록된 강의실이 없습니다';
            li.appendChild(span);
            list.appendChild(li);
            return;
        }

        rooms.forEach(room => {
            const li = document.createElement('li');
            li.dataset.roomName = room.roomName;

            const a = document.createElement('a');
            a.href = roomUrl(room.roomId);
            a.textContent = room.roomName;
            a.title = room.roomName;
            if (String(room.roomId) === String(ROOM_ID)) {
                a.classList.add('is-current');
                a.setAttribute('aria-current', 'page');
            }
            li.appendChild(a);
            list.appendChild(li);
        });
    }

    /* ---------- 강의실 검색 ---------- */

    const roomSearch = document.getElementById('roomSearch');
    const roomSearchEmpty = document.getElementById('roomSearchEmpty');
    if (roomSearch) roomSearch.addEventListener('input', applyRoomSearch);

    function applyRoomSearch() {
        if (!roomSearch) return;
        const keyword = roomSearch.value.trim().toLowerCase();
        let shown = 0;

        document.querySelectorAll('.fs-building').forEach(building => {
            const list = building.querySelector('.fs-rooms');
            const btn = building.querySelector('.fs-building-btn');
            const buildingName = (building.dataset.buildingName || '').toLowerCase();

            if (!keyword) {
                building.hidden = false;
                list.querySelectorAll('li').forEach(li => { li.hidden = false; });
                const hasCurrent = list.querySelector('a.is-current') !== null;
                building.classList.toggle('is-open', hasCurrent);
                btn.setAttribute('aria-expanded', String(hasCurrent));
                list.hidden = !hasCurrent;
                return;
            }

            const rooms = [...list.querySelectorAll('li[data-room-name]')];
            let matched = 0;
            rooms.forEach(li => {
                const hit = (li.dataset.roomName || '').toLowerCase().includes(keyword);
                li.hidden = !hit;
                if (hit) matched += 1;
            });
            list.querySelectorAll('.fs-rooms-empty, .fs-rooms-error')
                .forEach(li => { li.hidden = true; });

            const buildingHit = buildingName.includes(keyword);
            const show = matched > 0 || buildingHit;
            building.hidden = !show;

            if (show) {
                building.classList.add('is-open');
                btn.setAttribute('aria-expanded', 'true');
                list.hidden = false;
                if (buildingHit && matched === 0) rooms.forEach(li => { li.hidden = false; });
                if (list.dataset.loaded !== 'true') loadRooms(building, list);
                shown += 1;
            }
        });

        if (roomSearchEmpty) roomSearchEmpty.hidden = !keyword || shown > 0;
    }

    /* ================================================================
       날짜 표기
       ================================================================ */

    function formatAbsolute(value) {
        const d = new Date(value);
        if (isNaN(d)) return value;
        const p = n => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
    }

    function formatRelative(value) {
        const d = new Date(value);
        if (isNaN(d)) return value;
        const diff = Math.floor((Date.now() - d.getTime()) / 1000);
        if (diff < 60) return '방금 전';
        if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
        if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`;
        if (diff < 2592000) return `${Math.floor(diff / 604800)}주 전`;
        return formatAbsolute(value).slice(0, 10);
    }

    document.querySelectorAll('time[data-relative]').forEach(el => {
        el.textContent = formatRelative(el.getAttribute('datetime'));
    });
    document.querySelectorAll('time[data-absolute]').forEach(el => {
        el.textContent = formatAbsolute(el.getAttribute('datetime'));
    });

    /* ================================================================
       플로우 목록
       ================================================================ */

    /* ---------- 스케줄 (행 펼침) ---------- */

    async function loadSchedules(row) {
        const flowId = row.dataset.flowId;
        const slot = row.querySelector('[data-schedule-slot]');
        if (!slot) return;

        if (scheduleCache.has(flowId)) {
            renderSchedules(row, scheduleCache.get(flowId));
            return;
        }

        slot.innerHTML = '<span class="skeleton"></span>';
        try {
            const data = await request(`${API}/rooms/${ROOM_ID}/flows/${flowId}/schedules`);
            const list = (data && data.schedules) || [];
            scheduleCache.set(flowId, list);
            renderSchedules(row, list);
        } catch (err) {
            slot.innerHTML = '';
            const box = document.createElement('div');
            box.className = 'chip-error';
            box.append(err.message);

            const retry = document.createElement('button');
            retry.type = 'button';
            retry.className = 'btn-sm';
            retry.textContent = '다시 시도';
            retry.addEventListener('click', () => {
                scheduleCache.delete(flowId);
                loadSchedules(row);
            });
            box.appendChild(retry);
            slot.appendChild(box);
        }
    }

    function sortSchedules(a, b) {
        const d = DAY_ORDER.indexOf(a.dayOfWeek) - DAY_ORDER.indexOf(b.dayOfWeek);
        return d !== 0 ? d : String(a.startTime).localeCompare(String(b.startTime));
    }

    function trimSeconds(t) {
        return typeof t === 'string' ? t.slice(0, 5) : t;
    }

    function renderSchedules(row, list) {
        const slot = row.querySelector('[data-schedule-slot]');
        slot.innerHTML = '';

        if (!list.length) {
            const p = document.createElement('p');
            p.className = 'chip-none';
            p.textContent = row.dataset.active === 'true'
                ? '등록된 스케줄이 없습니다. 활성 상태이면 상시 실행됩니다.'
                : '등록된 스케줄이 없습니다.';
            slot.appendChild(p);
        } else {
            list.slice().sort(sortSchedules).forEach(s => {
                const chip = document.createElement('span');
                chip.className = 'chip';
                chip.textContent =
                    `${DAY_LABEL[s.dayOfWeek] || s.dayOfWeek} ${trimSeconds(s.startTime)}–${trimSeconds(s.endTime)}`;
                slot.appendChild(chip);
            });
        }
        updateAlwaysBadge(row, list.length === 0);
    }

    function updateAlwaysBadge(row, noSchedule) {
        const badge = row.querySelector('[data-always-badge]');
        if (!badge) return;
        badge.hidden = !(noSchedule && row.dataset.active === 'true');
    }

    document.querySelectorAll('.caret').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('.flow-row');
            const panel = row.querySelector('.row-panel');
            const open = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', String(!open));
            panel.hidden = open;
            if (!open) loadSchedules(row);
        });
    });

    /* ---------- 활성화 토글 ---------- */

    document.querySelectorAll('.switch').forEach(sw => {
        sw.addEventListener('click', async () => {
            const row = sw.closest('.flow-row');
            const next = sw.getAttribute('aria-checked') !== 'true';

            applyActiveState(row, sw, next);
            sw.setAttribute('aria-busy', 'true');

            try {
                await request(`${API}/rooms/${ROOM_ID}/flows/${row.dataset.flowId}/active`, {
                    method: 'PATCH',
                    body: JSON.stringify({ isActive: next })
                });
                updateCount();
            } catch (err) {
                applyActiveState(row, sw, !next);
                updateCount();
                toast(err.message, true);
            } finally {
                sw.removeAttribute('aria-busy');
            }
        });
    });

    function applyActiveState(row, sw, active) {
        sw.setAttribute('aria-checked', String(active));
        row.dataset.active = String(active);
        row.classList.toggle('is-off', !active);

        const badge = row.querySelector('[data-active-badge]');
        if (badge) {
            badge.textContent = active ? '활성' : '비활성';
            badge.classList.toggle('is-on', active);
        }
        const cached = scheduleCache.get(row.dataset.flowId);
        updateAlwaysBadge(row, Array.isArray(cached) && cached.length === 0);
        applyFilters();
    }

    function updateCount() {
        const el = document.getElementById('flowCount');
        if (!el) return;
        const rows = document.querySelectorAll('.flow-row');
        const on = [...rows].filter(r => r.dataset.active === 'true').length;
        el.textContent = `전체 ${rows.length}개 · 활성 ${on}개`;
    }
    updateCount();

    /* ---------- 케밥 메뉴 ---------- */

    document.querySelectorAll('.kebab').forEach(kebab => {
        kebab.addEventListener('click', e => {
            e.stopPropagation();
            const menu = kebab.nextElementSibling;
            const wasOpen = !menu.hidden;
            closeMenus();
            if (!wasOpen) {
                menu.hidden = false;
                kebab.setAttribute('aria-expanded', 'true');
            }
        });
    });

    function closeMenus() {
        document.querySelectorAll('.menu').forEach(m => { m.hidden = true; });
        document.querySelectorAll('.kebab').forEach(k => k.setAttribute('aria-expanded', 'false'));
    }
    document.addEventListener('click', closeMenus);
    document.addEventListener('keydown', e => { if (e.key === 'Escape') closeMenus(); });

    /* ---------- 삭제 ---------- */

    const deleteDialog = document.getElementById('deleteDialog');
    let deleteTargetId = null;

    document.querySelectorAll('[data-action="delete"]').forEach(btn => {
        btn.addEventListener('click', e => {
            e.stopPropagation();
            closeMenus();
            const row = btn.closest('.flow-row');
            deleteTargetId = row.dataset.flowId;
            document.getElementById('deleteTargetName').textContent = row.dataset.flowName;
            deleteDialog.showModal();
        });
    });

    const deleteConfirm = document.getElementById('deleteConfirm');
    if (deleteConfirm) {
        deleteConfirm.addEventListener('click', async () => {
            deleteConfirm.disabled = true;
            try {
                await request(`${API}/rooms/${ROOM_ID}/flows/${deleteTargetId}`, { method: 'DELETE' });
                const row = document.getElementById(`flow-${deleteTargetId}`);
                if (row) row.remove();
                scheduleCache.delete(deleteTargetId);
                deleteDialog.close();
                updateCount();
                applyFilters();
                toast('플로우를 삭제했습니다.');
                if (!document.querySelectorAll('.flow-row').length) window.location.reload();
            } catch (err) {
                toast(err.message, true);
            } finally {
                deleteConfirm.disabled = false;
            }
        });
    }

    /* ---------- 템플릿 ---------- */

    const templateDialog = document.getElementById('templateDialog');
    const templateBody = document.getElementById('templateBody');
    const templateStart = document.getElementById('templateStart');

    ['openTemplateBtn', 'openTemplateBtnEmpty'].forEach(id => {
        const btn = document.getElementById(id);
        if (btn) btn.addEventListener('click', openTemplates);
    });

    async function openTemplates() {
        selectedTemplateId = null;
        templateStart.disabled = true;
        templateBody.innerHTML = '<p class="dlg-loading">템플릿을 불러오는 중입니다.</p>';
        templateDialog.showModal();

        try {
            const data = await request(`${API}/rooms/${ROOM_ID}/flow-templates`);
            renderTemplates((data && data.templateResponseList) || []);
        } catch (err) {
            templateBody.innerHTML = '';
            const p = document.createElement('p');
            p.className = 'dlg-error';
            p.textContent = err.message;
            templateBody.appendChild(p);
        }
    }

    function renderTemplates(list) {
        templateBody.innerHTML = '';

        if (!list.length) {
            const p = document.createElement('p');
            p.className = 'dlg-loading';
            p.textContent = '이 강의실에서 사용할 수 있는 템플릿이 없습니다.';
            templateBody.appendChild(p);
            return;
        }

        list.forEach(tpl => {
            const card = document.createElement('div');
            card.className = 'tpl-card';
            card.tabIndex = 0;
            card.setAttribute('role', 'button');
            card.setAttribute('aria-selected', 'false');

            const title = document.createElement('h3');
            title.textContent = tpl.templateName;
            card.appendChild(title);

            if (tpl.description) {
                const desc = document.createElement('p');
                desc.textContent = tpl.description;
                card.appendChild(desc);
            }

            const types = document.createElement('div');
            types.className = 'tpl-types';
            (tpl.measurementTypes || []).forEach(type => {
                const chip = document.createElement('span');
                chip.className = 'chip';
                chip.textContent = type;
                types.appendChild(chip);
            });
            card.appendChild(types);

            const select = () => {
                templateBody.querySelectorAll('.tpl-card')
                    .forEach(c => c.setAttribute('aria-selected', 'false'));
                card.setAttribute('aria-selected', 'true');
                selectedTemplateId = tpl.templateId;
                templateStart.disabled = false;
            };
            card.addEventListener('click', select);
            card.addEventListener('keydown', e => {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); select(); }
            });

            templateBody.appendChild(card);
        });
    }

    if (templateStart) {
        templateStart.addEventListener('click', () => {
            if (!selectedTemplateId) return;
            window.location.href =
                `/rooms/${ROOM_ID}/flows/new?templateId=${encodeURIComponent(selectedTemplateId)}`;
        });
    }

    document.querySelectorAll('[data-dialog-close]').forEach(btn => {
        btn.addEventListener('click', () => btn.closest('dialog').close());
    });

    /* ---------- 검색 · 필터 · 정렬 ---------- */

    const flowSearch = document.getElementById('flowSearch');
    const flowSort = document.getElementById('flowSort');
    const filterEmpty = document.getElementById('filterEmpty');
    let activeFilter = 'all';

    document.querySelectorAll('.seg button').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.seg button')
                .forEach(b => b.setAttribute('aria-pressed', 'false'));
            btn.setAttribute('aria-pressed', 'true');
            activeFilter = btn.dataset.filter;
            applyFilters();
        });
    });

    if (flowSearch) flowSearch.addEventListener('input', applyFilters);

    function applyFilters() {
        const keyword = flowSearch ? flowSearch.value.trim().toLowerCase() : '';
        const rows = document.querySelectorAll('.flow-row');
        let visible = 0;

        rows.forEach(row => {
            const name = (row.dataset.flowName || '').toLowerCase();
            const state = row.dataset.active === 'true' ? 'on' : 'off';
            const matched = (activeFilter === 'all' || activeFilter === state)
                && (!keyword || name.includes(keyword));
            row.hidden = !matched;
            if (matched) visible += 1;
        });

        if (filterEmpty) filterEmpty.hidden = visible !== 0 || rows.length === 0;
    }

    if (flowSort) {
        flowSort.addEventListener('change', () => {
            const list = document.getElementById('flowList');
            if (!list) return;
            const key = flowSort.value;
            [...list.querySelectorAll('.flow-row')]
                .sort((a, b) => {
                    if (key === 'name') {
                        return (a.dataset.flowName || '').localeCompare(b.dataset.flowName || '', 'ko');
                    }
                    const field = key === 'created' ? 'createdAt' : 'updatedAt';
                    return String(b.dataset[field] || '').localeCompare(String(a.dataset[field] || ''));
                })
                .forEach(row => list.appendChild(row));
        });
    }

    /* ================================================================
       스케줄 관리 모달
       ================================================================ */

    const scheduleDialog = document.getElementById('scheduleDialog');

    // 화면에서만 유지하는 편집 상태. 저장 버튼을 눌러야 서버에 반영된다.
    let scFlowId = null;      // 편집 중인 플로우 id
    let scItems = [];         // 화면에 보이는 시간대 목록
    let scAdded = [];         // 아직 저장하지 않은 추가분
    let scRemoved = [];       // 아직 저장하지 않은 삭제분(scheduleId)
    let scSeq = -1;           // 추가분 임시 id (음수)

    const toMinutes = t => {
        const [h, m] = t.split(':').map(Number);
        return h * 60 + m;
    };
    const toServerTime = v => {
        const p = v.split(':');
        return `${p[0]}:${p[1]}:00`;
    };

    /* ---------- 요일 버튼 ---------- */

    const dayPicker = document.getElementById('dayPicker');
    if (dayPicker) {
        DAY_ORDER.forEach(key => {
            const b = document.createElement('button');
            b.type = 'button';
            b.dataset.day = key;
            b.textContent = DAY_LABEL[key];
            b.setAttribute('aria-pressed', 'false');
            b.addEventListener('click', () => {
                b.setAttribute('aria-pressed', b.getAttribute('aria-pressed') === 'false' ? 'true' : 'false');
                clearScheduleMsg();
            });
            dayPicker.appendChild(b);
        });
    }

    function selectedDays() {
        return [...dayPicker.querySelectorAll('button')]
            .filter(b => b.getAttribute('aria-pressed') === 'true')
            .map(b => b.dataset.day);
    }
    function resetDays() {
        dayPicker.querySelectorAll('button').forEach(b => b.setAttribute('aria-pressed', 'false'));
    }

    /* ---------- 열기 ---------- */

    document.querySelectorAll('[data-action="schedule"]').forEach(btn => {
        btn.addEventListener('click', e => {
            e.stopPropagation();
            closeMenus();
            openScheduleDialog(btn.closest('.flow-row'));
        });
    });

    async function openScheduleDialog(row) {
        scFlowId = row.dataset.flowId;
        scAdded = [];
        scRemoved = [];
        scItems = [];
        resetDays();
        clearScheduleMsg();

        document.getElementById('scheduleSub').textContent = row.dataset.flowName;
        drawSchedule();
        scheduleDialog.showModal();

        if (scheduleCache.has(scFlowId)) {
            scItems = scheduleCache.get(scFlowId).map(x => ({ ...x }));
            drawSchedule();
            return;
        }

        try {
            const data = await request(`${API}/rooms/${ROOM_ID}/flows/${scFlowId}/schedules`);
            const list = (data && data.schedules) || [];
            scheduleCache.set(scFlowId, list);
            scItems = list.map(x => ({ ...x }));
            drawSchedule();
        } catch (err) {
            scheduleMsg(err.message, true);
        }
    }

    document.querySelectorAll('[data-schedule-close]').forEach(btn => {
        btn.addEventListener('click', () => {
            if (scAdded.length || scRemoved.length) {
                if (!window.confirm('저장하지 않은 변경사항이 있습니다. 닫을까요?')) return;
            }
            scheduleDialog.close();
        });
    });

    /* ---------- 그리기 ---------- */

    function drawSchedule() {
        const list = document.getElementById('scheduleList');
        const timeline = document.getElementById('scheduleTimeline');
        if (!list) return;

        list.innerHTML = '';
        timeline.innerHTML = '';

        DAY_ORDER.forEach(key => {
            const rows = scItems
                .filter(x => x.dayOfWeek === key)
                .sort((a, b) => toMinutes(a.startTime) - toMinutes(b.startTime));

            const day = document.createElement('div');
            day.className = 'sc-day' + (rows.length ? '' : ' is-empty');

            const name = document.createElement('div');
            name.className = 'sc-day-name';
            name.textContent = DAY_LABEL[key];
            day.appendChild(name);

            const slots = document.createElement('div');
            slots.className = 'sc-slots';

            if (!rows.length) {
                const none = document.createElement('span');
                none.className = 'sc-none';
                none.textContent = '없음';
                slots.appendChild(none);
            } else {
                rows.forEach(r => {
                    const slot = document.createElement('span');
                    slot.className = 'sc-slot' + (r.scheduleId < 0 ? ' is-new' : '');
                    slot.append(`${trimSeconds(r.startTime)} – ${trimSeconds(r.endTime)}`);

                    const del = document.createElement('button');
                    del.type = 'button';
                    del.className = 'sc-slot-del';
                    del.textContent = '✕';
                    del.setAttribute('aria-label',
                        `${DAY_LABEL[key]} ${trimSeconds(r.startTime)}부터 ${trimSeconds(r.endTime)}까지 삭제`);
                    del.addEventListener('click', () => removeSlot(r.scheduleId));
                    slot.appendChild(del);

                    slots.appendChild(slot);
                });
            }
            day.appendChild(slots);
            list.appendChild(day);

            const tlRow = document.createElement('div');
            tlRow.className = 'sc-tl-row';

            const tlName = document.createElement('div');
            tlName.className = 'sc-tl-name';
            tlName.textContent = DAY_LABEL[key];
            tlRow.appendChild(tlName);

            const track = document.createElement('div');
            track.className = 'sc-tl-track';
            rows.forEach(r => {
                const block = document.createElement('span');
                block.className = 'sc-tl-block' + (r.scheduleId < 0 ? ' is-new' : '');
                const start = toMinutes(r.startTime) / 1440 * 100;
                const width = (toMinutes(r.endTime) - toMinutes(r.startTime)) / 1440 * 100;
                block.style.left = start + '%';
                block.style.width = width + '%';
                track.appendChild(block);
            });
            tlRow.appendChild(track);
            timeline.appendChild(tlRow);
        });

        const ticks = document.createElement('div');
        ticks.className = 'sc-tl-ticks';
        ['00', '06', '12', '18', '24'].forEach(t => {
            const s = document.createElement('span');
            s.textContent = t;
            ticks.appendChild(s);
        });
        timeline.appendChild(ticks);

        const status = document.getElementById('scheduleStatus');
        status.classList.toggle('is-always', scItems.length === 0);
        status.innerHTML = '';
        if (scItems.length) {
            status.append('이 플로우는 등록된 ');
            const strong = document.createElement('strong');
            strong.textContent = `${scItems.length}개 시간대`;
            status.appendChild(strong);
            status.append('에만 실행됩니다.');
        } else {
            status.append('등록된 스케줄이 없습니다. 플로우가 활성 상태이면 ');
            const strong = document.createElement('strong');
            strong.textContent = '상시 실행';
            status.appendChild(strong);
            status.append('됩니다.');
        }

        const dirty = scAdded.length > 0 || scRemoved.length > 0;
        const pending = document.getElementById('schedulePending');
        pending.hidden = !dirty;
        pending.textContent = dirty
            ? `저장 대기: 추가 ${scAdded.length}건 · 삭제 ${scRemoved.length}건`
            : '';

        document.getElementById('scheduleSave').disabled = !dirty;
        document.getElementById('scheduleFootHint').textContent = dirty ? '' : '변경사항 없음';
    }

    function removeSlot(id) {
        const i = scItems.findIndex(x => x.scheduleId === id);
        if (i < 0) return;

        if (scItems[i].scheduleId < 0) {
            scAdded = scAdded.filter(a => a.scheduleId !== id);
        } else {
            scRemoved.push(scItems[i].scheduleId);
        }
        scItems.splice(i, 1);
        drawSchedule();
    }

    /* ---------- 메시지 ---------- */

    function scheduleMsg(text, isError) {
        const el = document.getElementById('scheduleMsg');
        el.textContent = text;
        el.className = 'sc-msg ' + (isError ? 'is-error' : 'is-ok');
        el.hidden = false;
    }
    function clearScheduleMsg() {
        const el = document.getElementById('scheduleMsg');
        if (el) { el.hidden = true; el.textContent = ''; }
    }

    /* ---------- 추가 ---------- */

    const scheduleAdd = document.getElementById('scheduleAdd');
    if (scheduleAdd) {
        scheduleAdd.addEventListener('click', () => {
            const days = selectedDays();
            const startRaw = document.getElementById('scheduleStart').value;
            const endRaw = document.getElementById('scheduleEnd').value;

            if (!days.length) return scheduleMsg('요일을 하나 이상 선택하세요.', true);
            if (!startRaw || !endRaw) return scheduleMsg('시작과 종료 시간을 입력하세요.', true);

            const start = toServerTime(startRaw);
            const end = toServerTime(endRaw);

            if (toMinutes(start) === toMinutes(end)) {
                return scheduleMsg('시작 시간과 종료 시간이 같을 수 없습니다.', true);
            }
            if (toMinutes(start) > toMinutes(end)) {
                return scheduleMsg('자정을 넘는 시간대는 설정할 수 없습니다.', true);
            }

            // 맞닿는 경계는 허용, 실제로 포개지는 경우만 막는다
            const clash = days.filter(d => scItems.some(x =>
                x.dayOfWeek === d
                && toMinutes(start) < toMinutes(x.endTime)
                && toMinutes(x.startTime) < toMinutes(end)));

            if (clash.length) {
                const names = clash.map(c => DAY_LABEL[c]).join(', ');
                return scheduleMsg(`${names}요일에 이미 겹치는 시간대가 있습니다.`, true);
            }

            days.forEach(d => {
                const item = { scheduleId: scSeq--, dayOfWeek: d, startTime: start, endTime: end };
                scItems.push(item);
                scAdded.push(item);
            });

            resetDays();
            scheduleMsg(`${days.length}개 시간대가 추가 대기 중입니다.`, false);
            drawSchedule();
        });
    }

    /* ---------- 저장 ---------- */

    const scheduleSave = document.getElementById('scheduleSave');
    if (scheduleSave) {
        scheduleSave.addEventListener('click', async () => {
            scheduleSave.disabled = true;
            clearScheduleMsg();

            const base = `${API}/rooms/${ROOM_ID}/flows/${scFlowId}/schedules`;
            let done = 0;
            const total = scRemoved.length + scAdded.length;

            try {
                // 삭제를 먼저 처리해야 겹침 검증에 걸리지 않는다
                for (const id of scRemoved) {
                    await request(`${base}/${id}`, { method: 'DELETE' });
                    done += 1;
                }
                for (const item of scAdded) {
                    await request(base, {
                        method: 'POST',
                        body: JSON.stringify({
                            dayOfWeek: item.dayOfWeek,
                            startTime: item.startTime,
                            endTime: item.endTime
                        })
                    });
                    done += 1;
                }
                await refreshSchedules();
                toast('스케줄을 저장했습니다.');
            } catch (err) {
                // 일부만 반영됐을 수 있으므로 서버 상태를 다시 읽어 화면을 맞춘다
                await refreshSchedules();
                scheduleMsg(
                    `${done}/${total}건까지 처리한 뒤 실패했습니다. ${err.message}`, true);
            } finally {
                scheduleSave.disabled = false;
            }
        });
    }

    async function refreshSchedules() {
        scAdded = [];
        scRemoved = [];
        try {
            const data = await request(`${API}/rooms/${ROOM_ID}/flows/${scFlowId}/schedules`);
            const list = (data && data.schedules) || [];
            scheduleCache.set(scFlowId, list);
            scItems = list.map(x => ({ ...x }));
        } catch (e) {
            scheduleCache.delete(scFlowId);
        }
        drawSchedule();
        syncRowAfterSave();
    }

    // 목록 행의 스케줄 칩과 상시 실행 뱃지를 갱신한다
    function syncRowAfterSave() {
        const row = document.getElementById(`flow-${scFlowId}`);
        if (!row) return;
        const cached = scheduleCache.get(scFlowId);
        if (!Array.isArray(cached)) return;

        const panel = row.querySelector('.row-panel');
        if (panel && !panel.hidden) renderSchedules(row, cached);
        updateAlwaysBadge(row, cached.length === 0);
    }
})();