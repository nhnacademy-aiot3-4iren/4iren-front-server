(() => {
    'use strict';

    const root = document.querySelector('.dashboard-content');
    const teamId = Number(root?.dataset.teamId);
    if (!root || !Number.isFinite(teamId) || teamId <= 0) {
        return;
    }

    const widgetStorageKey = `4iren-dashboard-widgets:${teamId}`;
    const roomMetricStorageKey = `4iren-dashboard-room-metrics:${teamId}`;
    const maxWidgetCount = 4;
    const maxRoomMetricCount = 4;
    const widgetPeriods = {
        '24H': { label: '최근 24시간' },
        '7D': { label: '최근 7일' },
        '30D': { label: '최근 30일' }
    };
    const widgetTones = ['blue', 'green', 'amber', 'red'];
    const widgetToneStyles = {
        blue: { line: '#356da8' },
        green: { line: '#2f8a5b' },
        amber: { line: '#bb7a18' },
        red: { line: '#c95050' }
    };
    const legacyWidgetState = readStoredWidgets();

    const elements = {
        teamSelector: document.getElementById('team-selector'),
        refreshButton: document.getElementById('refresh-dashboard'),
        refreshState: document.getElementById('refresh-state'),
        searchInput: document.getElementById('dashboard-search-input'),
        loading: document.getElementById('dashboard-loading'),
        roomList: document.getElementById('room-list'),
        empty: document.getElementById('dashboard-empty'),
        noResults: document.getElementById('dashboard-no-results'),
        error: document.getElementById('dashboard-error'),
        errorMessage: document.getElementById('dashboard-error-message'),
        retryButton: document.getElementById('retry-dashboard'),
        pagination: document.getElementById('dashboard-pagination'),
        paginationCount: document.getElementById('pagination-count'),
        paginationPage: document.getElementById('pagination-page'),
        previousPage: document.getElementById('previous-page'),
        nextPage: document.getElementById('next-page'),
        openModalButtons: [
            document.getElementById('open-subscribe-modal'),
            document.getElementById('open-subscribe-modal-empty')
        ].filter(Boolean),
        modal: document.getElementById('subscribe-modal'),
        closeModalButton: document.getElementById('close-subscribe-modal'),
        roomSearchForm: document.getElementById('room-search-form'),
        roomSearchInput: document.getElementById('room-search-input'),
        roomSearchButton: document.getElementById('room-search-button'),
        roomSearchGuide: document.getElementById('room-search-guide'),
        roomSearchResults: document.getElementById('room-search-results'),
        roomSearchPagination: document.getElementById('room-search-pagination'),
        roomSearchPrevious: document.getElementById('room-search-previous'),
        roomSearchPage: document.getElementById('room-search-page'),
        roomSearchNext: document.getElementById('room-search-next'),
        widgetGrid: document.getElementById('widget-grid'),
        widgetEmpty: document.getElementById('widget-empty'),
        openWidgetModalButton: document.getElementById('open-widget-modal'),
        widgetModal: document.getElementById('widget-modal'),
        closeWidgetModalButton: document.getElementById('close-widget-modal'),
        cancelWidgetModalButton: document.getElementById('cancel-widget-modal'),
        widgetForm: document.getElementById('widget-form'),
        widgetRoomSelect: document.getElementById('widget-room-select'),
        widgetMetricSelect: document.getElementById('widget-metric-select'),
        widgetPeriodSelect: document.getElementById('widget-period-select'),
        widgetSubmitButton: document.getElementById('add-widget'),
        widgetFormMessage: document.getElementById('widget-form-message'),
        roomMetricModal: document.getElementById('room-metric-modal'),
        openRoomMetricModalButton: document.getElementById('open-room-metric-modal'),
        closeRoomMetricModalButton: document.getElementById('close-room-metric-modal'),
        cancelRoomMetricModalButton: document.getElementById('cancel-room-metric-modal'),
        roomMetricForm: document.getElementById('room-metric-form'),
        roomMetricOptions: document.getElementById('room-metric-options'),
        roomMetricFormMessage: document.getElementById('room-metric-form-message'),
        roomMetricSelectedCount: document.getElementById('room-metric-selected-count'),
        saveRoomMetricsButton: document.getElementById('save-room-metrics'),
        toast: document.getElementById('dashboard-toast')
    };

    const state = {
        page: 0,
        size: 10,
        query: '',
        loaded: false,
        dashboardAbortController: null,
        dashboardMetricsAbortController: null,
        dashboardEventSource: null,
        dashboardStreamKey: '',
        dashboardStreamConnected: false,
        dashboardMetricRefreshTimer: null,
        pendingDashboardMetricRoomIds: new Set(),
        widgetSeriesAbortController: null,
        widgetSortable: null,
        widgetOrderSaving: false,
        chartInstances: new Map(),
        chartResizeFrame: null,
        refreshTimer: null,
        searchTimer: null,
        roomSearchAbortController: null,
        roomSearchQuery: '',
        roomSearchCurrentPage: 0,
        roomSearchLastPage: true,
        toastTimer: null,
        lastFocusedElement: null,
        lastWidgetFocusedElement: null,
        lastRoomMetricFocusedElement: null,
        widgetRooms: [],
        widgetOptionsLoaded: false,
        widgets: [],
        widgetConfigurationLoaded: false,
        availableRoomMetrics: [],
        selectedRoomMetricCodes: readStoredRoomMetricCodes(),
        currentRooms: []
    };

    function bindEvents() {
        elements.teamSelector?.addEventListener('change', (event) => {
            const selectedTeamId = Number(event.target.value);
            if (Number.isFinite(selectedTeamId) && selectedTeamId > 0) {
                window.location.assign(`/teams/${selectedTeamId}/dashboard`);
            }
        });

        elements.refreshButton?.addEventListener('click', () => {
            loadDashboard({ showLoading: true });
            refreshWidgets();
        });
        elements.retryButton?.addEventListener('click', () => loadDashboard({ showLoading: true }));

        elements.searchInput?.addEventListener('input', () => {
            window.clearTimeout(state.searchTimer);
            state.searchTimer = window.setTimeout(() => {
                state.query = elements.searchInput.value.trim();
                state.page = 0;
                loadDashboard({ showLoading: true });
            }, 350);
        });

        elements.previousPage?.addEventListener('click', () => {
            if (state.page > 0) {
                state.page -= 1;
                loadDashboard({ showLoading: true });
            }
        });

        elements.nextPage?.addEventListener('click', () => {
            state.page += 1;
            loadDashboard({ showLoading: true });
        });

        elements.openModalButtons.forEach(button => button.addEventListener('click', openSubscribeModal));
        elements.closeModalButton?.addEventListener('click', closeSubscribeModal);
        elements.modal?.addEventListener('click', (event) => {
            if (event.target === elements.modal) {
                closeSubscribeModal();
            }
        });
        document.addEventListener('keydown', (event) => {
            if (event.key !== 'Escape') {
                return;
            }
            if (elements.roomMetricModal && !elements.roomMetricModal.hidden) {
                closeRoomMetricModal();
            } else if (elements.widgetModal && !elements.widgetModal.hidden) {
                closeWidgetModal();
            } else if (elements.modal && !elements.modal.hidden) {
                closeSubscribeModal();
            }
        });

        elements.roomSearchForm?.addEventListener('submit', searchRooms);
        elements.roomSearchResults?.addEventListener('click', subscribeRoomFromResult);
        elements.roomSearchPrevious?.addEventListener('click', () => {
            if (state.roomSearchCurrentPage > 0) {
                loadSubscriptionCandidates(state.roomSearchCurrentPage - 1);
            }
        });
        elements.roomSearchNext?.addEventListener('click', () => {
            if (!state.roomSearchLastPage) {
                loadSubscriptionCandidates(state.roomSearchCurrentPage + 1);
            }
        });

        elements.openWidgetModalButton?.addEventListener('click', openWidgetModal);
        elements.closeWidgetModalButton?.addEventListener('click', closeWidgetModal);
        elements.cancelWidgetModalButton?.addEventListener('click', closeWidgetModal);
        elements.widgetModal?.addEventListener('click', (event) => {
            if (event.target === elements.widgetModal) {
                closeWidgetModal();
            }
        });
        elements.widgetRoomSelect?.addEventListener('change', loadWidgetMetricOptions);
        elements.widgetMetricSelect?.addEventListener('change', () => {
            elements.widgetSubmitButton.disabled = !elements.widgetMetricSelect.value;
            setWidgetFormMessage('');
        });
        elements.widgetForm?.addEventListener('submit', addWidget);
        elements.widgetGrid?.addEventListener('change', changeWidgetPeriod);
        elements.widgetGrid?.addEventListener('click', handleWidgetAction);
        elements.widgetGrid?.addEventListener('keydown', handleWidgetSortKeydown);

        elements.openRoomMetricModalButton?.addEventListener('click', openRoomMetricModal);
        elements.closeRoomMetricModalButton?.addEventListener('click', closeRoomMetricModal);
        elements.cancelRoomMetricModalButton?.addEventListener('click', closeRoomMetricModal);
        elements.roomMetricModal?.addEventListener('click', (event) => {
            if (event.target === elements.roomMetricModal) {
                closeRoomMetricModal();
            }
        });
        elements.roomMetricOptions?.addEventListener('change', changeRoomMetricSelection);
        elements.roomMetricForm?.addEventListener('submit', saveRoomMetricSelection);

        document.addEventListener('visibilitychange', () => {
            if (!document.hidden && state.loaded) {
                loadDashboard({ showLoading: false });
                refreshWidgets();
            }
        });
        window.addEventListener('resize', resizeWidgetCharts);
        window.addEventListener('beforeunload', cleanup);
    }

    async function loadDashboard({ showLoading = false } = {}) {
        closeDashboardStream();
        state.dashboardMetricsAbortController?.abort();
        state.dashboardMetricsAbortController = null;
        if (state.dashboardAbortController) {
            state.dashboardAbortController.abort();
        }
        const controller = new AbortController();
        state.dashboardAbortController = controller;

        setLoading(true, showLoading || !state.loaded);
        hidePanelStates();

        const params = new URLSearchParams({
            page: String(state.page),
            size: String(state.size),
            query: state.query
        });
        state.selectedRoomMetricCodes.forEach(metricCode => params.append('metricCode', metricCode));

        try {
            const response = await fetch(`/api/front/teams/${teamId}/dashboard?${params}`, {
                headers: { Accept: 'application/json' },
                signal: controller.signal
            });
            const dashboard = await readJsonResponse(response);
            if (controller !== state.dashboardAbortController) {
                return;
            }

            state.page = dashboard.page;
            state.loaded = true;
            renderDashboard(dashboard);
            scheduleRefresh(dashboard.refreshIntervalSeconds);
        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
            console.error('대시보드 조회 실패:', error);
            showDashboardError(error.message);
        } finally {
            if (controller === state.dashboardAbortController) {
                state.dashboardAbortController = null;
                setLoading(false, false);
            }
        }
    }

    function renderDashboard(dashboard) {
        updateAvailableRoomMetrics(dashboard.availableMetrics || []);
        state.currentRooms = dashboard.rooms || [];
        renderRooms(dashboard.rooms);
        renderPagination(dashboard);
        connectDashboardStream();

        const generatedAt = new Date(dashboard.generatedAt);
        elements.refreshState.textContent = Number.isNaN(generatedAt.getTime())
            ? '방금 갱신'
            : `${formatDateTime(generatedAt)} 갱신`;

        if (dashboard.totalSubscribedRooms === 0) {
            elements.empty.hidden = false;
            elements.pagination.hidden = true;
        } else if (dashboard.totalElements === 0) {
            elements.noResults.hidden = false;
            elements.pagination.hidden = true;
        }
    }

    function renderRooms(rooms) {
        const selectedMetrics = getSelectedRoomMetrics();
        elements.roomList.innerHTML = rooms.map(room => renderRoom(room, selectedMetrics)).join('');
    }

    function renderRoom(room, selectedMetrics) {
        const canOpenDetail = room.buildingId != null;
        const tagName = canOpenDetail ? 'a' : 'div';
        const href = canOpenDetail
            ? ` href="/teams/${teamId}/buildings/${room.buildingId}/rooms/${room.roomId}"`
            : '';

        return `
            <${tagName} class="room-row room-metric-count-${selectedMetrics.length}"${href}>
                <div class="room-heading">
                    <span class="room-symbol" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
                            <rect x="4" y="8" width="16" height="12" rx="2"/><path d="M8 8V5h8v3M8 12h3M13 12h3M8 16h3M13 16h3"/>
                        </svg>
                    </span>
                    <div>
                        <div class="room-name">${escapeHtml(room.roomName || `공간 ${room.roomId}`)}</div>
                        <div class="room-location">${escapeHtml(room.buildingName || '건물 정보 없음')} · 센서 ${room.sensorCount ?? 0}개</div>
                    </div>
                </div>
                ${selectedMetrics.map(metricDefinition => renderMetric(
                    metricDefinition.displayName,
                    findMetric(room.metrics, metricDefinition.metricCode)
                )).join('')}
                <svg class="room-chevron" aria-hidden="true" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 6l6 6-6 6"/></svg>
            </${tagName}>`;
    }

    function renderMetric(fallbackLabel, metric) {
        if (!metric) {
            return `
                <div class="metric-block metric-empty">
                    <div class="metric-label">${escapeHtml(fallbackLabel)}</div>
                    <div class="metric-value">-<small>미지원</small></div>
                </div>`;
        }
        if (metric.value == null) {
            return `
                <div class="metric-block metric-empty">
                    <div class="metric-label">${escapeHtml(metric.displayName || fallbackLabel)}</div>
                    <div class="metric-value">-<small>데이터 없음</small></div>
                </div>`;
        }

        const numericValue = Number(metric.value);
        const fractionDigits = Math.abs(numericValue) >= 100 ? 0 : 1;
        const value = numericValue.toLocaleString('ko-KR', {
            minimumFractionDigits: 0,
            maximumFractionDigits: fractionDigits
        });
        return `
            <div class="metric-block">
                <div class="metric-label">${escapeHtml(metric.displayName || fallbackLabel)}</div>
                <div class="metric-value">${value}<small>${escapeHtml(metric.symbol || '')}</small></div>
            </div>`;
    }

    function findMetric(metrics, metricCode) {
        return (metrics || []).find(metric => metric.metricCode === metricCode);
    }

    function readStoredRoomMetricCodes() {
        try {
            const storedValue = window.localStorage.getItem(roomMetricStorageKey);
            if (storedValue == null) {
                return [];
            }
            const parsed = JSON.parse(storedValue);
            if (!Array.isArray(parsed)) {
                return [];
            }
            return parsed
                .filter(metricCode => typeof metricCode === 'string' && metricCode.trim())
                .slice(0, maxRoomMetricCount);
        } catch (error) {
            console.warn('공간 현황 표시 지표를 불러오지 못했습니다:', error);
            return [];
        }
    }

    function persistRoomMetricCodes() {
        try {
            window.localStorage.setItem(
                roomMetricStorageKey,
                JSON.stringify(state.selectedRoomMetricCodes)
            );
        } catch (error) {
            console.warn('공간 현황 표시 지표를 저장하지 못했습니다:', error);
        }
    }

    function updateAvailableRoomMetrics(metrics) {
        const metricsByCode = new Map();
        for (const metric of metrics) {
            if (!metric?.metricCode || metricsByCode.has(metric.metricCode)) {
                continue;
            }
            metricsByCode.set(metric.metricCode, {
                metricCode: String(metric.metricCode),
                displayName: String(metric.displayName || metric.metricCode),
                symbol: String(metric.symbol || '')
            });
        }
        state.availableRoomMetrics = Array.from(metricsByCode.values());

        const availableCodes = new Set(state.availableRoomMetrics.map(metric => metric.metricCode));
        const retainedCodes = state.selectedRoomMetricCodes.filter(metricCode => availableCodes.has(metricCode));
        state.selectedRoomMetricCodes = retainedCodes.length > 0
            ? retainedCodes
            : state.availableRoomMetrics
                .slice(0, maxRoomMetricCount)
                .map(metric => metric.metricCode);
        persistRoomMetricCodes();

        elements.openRoomMetricModalButton.disabled = state.availableRoomMetrics.length === 0;
        elements.openRoomMetricModalButton.title = state.availableRoomMetrics.length === 0
            ? '표시할 수 있는 센서 지표가 없습니다.'
            : '';
    }

    function getSelectedRoomMetrics() {
        const metricsByCode = new Map(
            state.availableRoomMetrics.map(metric => [metric.metricCode, metric])
        );
        return state.selectedRoomMetricCodes
            .map(metricCode => metricsByCode.get(metricCode))
            .filter(Boolean)
            .slice(0, maxRoomMetricCount);
    }

    function openRoomMetricModal() {
        if (state.availableRoomMetrics.length === 0) {
            showToast('표시할 수 있는 센서 지표가 없습니다.', true);
            return;
        }

        state.lastRoomMetricFocusedElement = document.activeElement;
        renderRoomMetricOptions();
        elements.roomMetricModal.hidden = false;
        document.body.style.overflow = 'hidden';
        window.setTimeout(() => {
            elements.roomMetricOptions.querySelector('input')?.focus();
        }, 0);
    }

    function closeRoomMetricModal() {
        elements.roomMetricModal.hidden = true;
        document.body.style.overflow = '';
        state.lastRoomMetricFocusedElement?.focus?.();
    }

    function renderRoomMetricOptions() {
        const selectedCodes = new Set(state.selectedRoomMetricCodes);
        elements.roomMetricOptions.innerHTML = state.availableRoomMetrics.map(metric => `
            <label class="metric-option">
                <input type="checkbox" name="roomMetricCode" value="${escapeHtml(metric.metricCode)}"${selectedCodes.has(metric.metricCode) ? ' checked' : ''}>
                <span>
                    <strong>${escapeHtml(metric.displayName)}</strong>
                    <small>${escapeHtml(metric.metricCode)}${metric.symbol ? ` · ${escapeHtml(metric.symbol)}` : ''}</small>
                </span>
            </label>`).join('');
        setRoomMetricFormMessage('');
        updateRoomMetricSelectionState();
    }

    function changeRoomMetricSelection(event) {
        if (!event.target.matches('input[name="roomMetricCode"]')) {
            return;
        }

        const checkedInputs = getCheckedRoomMetricInputs();
        if (checkedInputs.length > maxRoomMetricCount) {
            event.target.checked = false;
            setRoomMetricFormMessage(`표시 지표는 최대 ${maxRoomMetricCount}개까지 선택할 수 있습니다.`);
        } else {
            setRoomMetricFormMessage('');
        }
        updateRoomMetricSelectionState();
    }

    function updateRoomMetricSelectionState() {
        const selectedCount = getCheckedRoomMetricInputs().length;
        elements.roomMetricSelectedCount.textContent = selectedCount;
        elements.saveRoomMetricsButton.disabled = selectedCount === 0;
    }

    function getCheckedRoomMetricInputs() {
        return Array.from(
            elements.roomMetricOptions.querySelectorAll('input[name="roomMetricCode"]:checked')
        );
    }

    function saveRoomMetricSelection(event) {
        event.preventDefault();
        const selectedCodes = getCheckedRoomMetricInputs().map(input => input.value);
        if (selectedCodes.length === 0) {
            setRoomMetricFormMessage('하나 이상의 표시 지표를 선택해 주세요.');
            return;
        }

        state.selectedRoomMetricCodes = selectedCodes.slice(0, maxRoomMetricCount);
        persistRoomMetricCodes();
        closeRoomMetricModal();
        loadDashboard({ showLoading: true });
        showToast('공간 현황의 표시 지표를 변경했습니다.');
    }

    function setRoomMetricFormMessage(message) {
        elements.roomMetricFormMessage.textContent = message;
        elements.roomMetricFormMessage.hidden = !message;
    }

    function renderPagination(dashboard) {
        if (dashboard.totalElements === 0) {
            elements.pagination.hidden = true;
            return;
        }

        elements.pagination.hidden = false;
        const start = dashboard.page * dashboard.size + 1;
        const end = Math.min(start + dashboard.rooms.length - 1, dashboard.totalElements);
        elements.paginationCount.textContent = `${start}-${end} / 총 ${dashboard.totalElements}개`;
        elements.paginationPage.textContent = `${dashboard.page + 1} / ${dashboard.totalPages}`;
        elements.previousPage.disabled = dashboard.first;
        elements.nextPage.disabled = dashboard.last;
    }

    function setLoading(isLoading, showSkeleton) {
        elements.refreshButton?.classList.toggle('is-loading', isLoading);
        if (elements.refreshButton) elements.refreshButton.disabled = isLoading;
        if (showSkeleton) {
            elements.loading.hidden = false;
            elements.roomList.innerHTML = '';
            elements.pagination.hidden = true;
        } else {
            elements.loading.hidden = true;
        }
    }

    function hidePanelStates() {
        elements.empty.hidden = true;
        elements.noResults.hidden = true;
        elements.error.hidden = true;
    }

    function showDashboardError(message) {
        elements.loading.hidden = true;
        elements.roomList.innerHTML = '';
        elements.pagination.hidden = true;
        elements.errorMessage.textContent = message || '잠시 후 다시 시도해 주세요.';
        elements.error.hidden = false;
        elements.refreshState.textContent = '갱신 실패';
    }

    function scheduleRefresh(seconds) {
        window.clearInterval(state.refreshTimer);
        const intervalSeconds = Math.max(Number(seconds) || 30, 10);
        state.refreshTimer = window.setInterval(() => {
            if (document.hidden) {
                return;
            }
            if (!state.dashboardAbortController
                && !state.dashboardMetricsAbortController
                && !state.dashboardStreamConnected) {
                refreshDashboardMetrics();
            }
            if (!state.widgetSeriesAbortController) {
                refreshWidgets();
            }
        }, intervalSeconds * 1000);
    }

    function connectDashboardStream() {
        const roomIds = state.currentRooms
            .map(room => Number(room.roomId))
            .filter(roomId => Number.isFinite(roomId) && roomId > 0);
        const metricCodes = state.selectedRoomMetricCodes.slice(0, maxRoomMetricCount);
        if (roomIds.length === 0 || metricCodes.length === 0 || typeof EventSource === 'undefined') {
            closeDashboardStream();
            return;
        }

        const streamKey = `${roomIds.join(',')}|${metricCodes.join(',')}`;
        if (state.dashboardEventSource && state.dashboardStreamKey === streamKey) {
            return;
        }

        closeDashboardStream();
        const params = new URLSearchParams();
        roomIds.forEach(roomId => params.append('roomId', String(roomId)));
        metricCodes.forEach(metricCode => params.append('metricCode', metricCode));

        const eventSource = new EventSource(
            `/api/front/teams/${teamId}/dashboard/stream?${params}`
        );
        state.dashboardEventSource = eventSource;
        state.dashboardStreamKey = streamKey;

        eventSource.addEventListener('dashboard-connected', () => {
            if (state.dashboardEventSource !== eventSource) {
                return;
            }
            state.dashboardStreamConnected = true;
            scheduleDashboardMetricRefresh(roomIds, 0);
        });

        eventSource.addEventListener('room-metric-changed', event => {
            if (state.dashboardEventSource !== eventSource) {
                return;
            }
            try {
                const change = JSON.parse(event.data);
                const roomId = Number(change.roomId);
                if (roomIds.includes(roomId) && metricCodes.includes(change.metricCode)) {
                    scheduleDashboardMetricRefresh([roomId], 1000);
                }
            } catch (error) {
                console.warn('대시보드 SSE 이벤트를 해석하지 못했습니다:', error);
            }
        });

        eventSource.onerror = () => {
            if (state.dashboardEventSource === eventSource) {
                state.dashboardStreamConnected = false;
            }
        };
    }

    function closeDashboardStream() {
        state.dashboardEventSource?.close();
        state.dashboardEventSource = null;
        state.dashboardStreamKey = '';
        state.dashboardStreamConnected = false;
        window.clearTimeout(state.dashboardMetricRefreshTimer);
        state.dashboardMetricRefreshTimer = null;
        state.pendingDashboardMetricRoomIds.clear();
    }

    function scheduleDashboardMetricRefresh(roomIds, delayMillis) {
        roomIds.forEach(roomId => state.pendingDashboardMetricRoomIds.add(Number(roomId)));
        if (state.dashboardMetricRefreshTimer != null) {
            return;
        }

        state.dashboardMetricRefreshTimer = window.setTimeout(() => {
            state.dashboardMetricRefreshTimer = null;
            const pendingRoomIds = Array.from(state.pendingDashboardMetricRoomIds);
            state.pendingDashboardMetricRoomIds.clear();
            refreshDashboardMetrics(pendingRoomIds);
        }, delayMillis);
    }

    async function refreshDashboardMetrics(requestedRoomIds = null) {
        const visibleRoomIds = state.currentRooms
            .map(room => Number(room.roomId))
            .filter(roomId => Number.isFinite(roomId) && roomId > 0);
        const visibleRoomIdSet = new Set(visibleRoomIds);
        const roomIds = (requestedRoomIds || visibleRoomIds)
            .map(Number)
            .filter(roomId => visibleRoomIdSet.has(roomId));
        if (roomIds.length === 0 || state.selectedRoomMetricCodes.length === 0) {
            return;
        }
        if (state.dashboardAbortController || state.dashboardMetricsAbortController) {
            scheduleDashboardMetricRefresh(roomIds, 500);
            return;
        }

        const controller = new AbortController();
        state.dashboardMetricsAbortController = controller;

        try {
            const response = await fetch(`/api/front/teams/${teamId}/dashboard/room-metrics`, {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    roomIds,
                    metricCodes: state.selectedRoomMetricCodes
                }),
                signal: controller.signal
            });
            const refreshed = await readJsonResponse(response);
            if (state.dashboardMetricsAbortController !== controller) {
                return;
            }

            const metricsByRoomId = new Map(
                (refreshed.rooms || []).map(room => [Number(room.roomId), room.metrics || []])
            );
            state.currentRooms = state.currentRooms.map(room => {
                const metrics = metricsByRoomId.get(Number(room.roomId));
                return metrics == null ? room : { ...room, metrics };
            });
            renderRooms(state.currentRooms);

            const generatedAt = new Date(refreshed.generatedAt);
            elements.refreshState.textContent = Number.isNaN(generatedAt.getTime())
                ? '방금 갱신'
                : `${formatDateTime(generatedAt)} 갱신`;
        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
            console.error('대시보드 공간 지표 갱신 실패:', error);
            elements.refreshState.textContent = '자동 갱신 실패';
        } finally {
            if (state.dashboardMetricsAbortController === controller) {
                state.dashboardMetricsAbortController = null;
            }
        }
    }

    function openSubscribeModal() {
        state.roomSearchAbortController?.abort();
        state.roomSearchAbortController = null;
        state.roomSearchQuery = '';
        state.roomSearchCurrentPage = 0;
        state.roomSearchLastPage = true;
        state.lastFocusedElement = document.activeElement;
        elements.modal.hidden = false;
        document.body.style.overflow = 'hidden';
        elements.roomSearchButton.disabled = false;
        elements.roomSearchPrevious.disabled = true;
        elements.roomSearchNext.disabled = true;
        elements.roomSearchInput.value = '';
        elements.roomSearchResults.innerHTML = '';
        elements.roomSearchPagination.hidden = true;
        elements.roomSearchGuide.hidden = false;
        elements.roomSearchGuide.textContent = '공간이나 건물 이름을 입력해 검색해 주세요.';
        window.setTimeout(() => elements.roomSearchInput.focus(), 0);
    }

    function closeSubscribeModal() {
        state.roomSearchAbortController?.abort();
        state.roomSearchAbortController = null;
        elements.modal.hidden = true;
        document.body.style.overflow = '';
        state.lastFocusedElement?.focus?.();
    }

    async function searchRooms(event) {
        event.preventDefault();
        const query = elements.roomSearchInput.value.trim();
        if (!query) {
            elements.roomSearchInput.focus();
            return;
        }

        state.roomSearchQuery = query;
        await loadSubscriptionCandidates(0);
    }

    async function loadSubscriptionCandidates(page) {
        state.roomSearchAbortController?.abort();
        const controller = new AbortController();
        state.roomSearchAbortController = controller;

        elements.roomSearchButton.disabled = true;
        elements.roomSearchPrevious.disabled = true;
        elements.roomSearchNext.disabled = true;
        elements.roomSearchPagination.hidden = true;
        elements.roomSearchGuide.hidden = false;
        elements.roomSearchGuide.textContent = '공간을 검색하고 있습니다.';
        elements.roomSearchResults.innerHTML = '';

        try {
            const params = new URLSearchParams({
                query: state.roomSearchQuery,
                page: String(page),
                size: '20'
            });
            const candidates = await requestJson(
                `/api/front/teams/${teamId}/dashboard/subscription-candidates?${params}`,
                { signal: controller.signal }
            );
            if (state.roomSearchAbortController !== controller) {
                return;
            }

            state.roomSearchCurrentPage = candidates.page;
            state.roomSearchLastPage = candidates.last;
            renderRoomSearchResults(candidates.rooms || []);
            renderRoomSearchPagination(candidates);
        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
            console.error('공간 검색 실패:', error);
            elements.roomSearchGuide.textContent = error.message;
        } finally {
            if (state.roomSearchAbortController === controller) {
                state.roomSearchAbortController = null;
                elements.roomSearchButton.disabled = false;
            }
        }
    }

    function renderRoomSearchResults(rooms) {
        if (rooms.length === 0) {
            elements.roomSearchGuide.hidden = false;
            elements.roomSearchGuide.textContent = '일치하는 미구독 공간이 없습니다.';
            return;
        }

        elements.roomSearchGuide.hidden = true;
        elements.roomSearchResults.innerHTML = rooms.map(room => `
            <article class="search-result-item">
                <div>
                    <strong>${escapeHtml(room.roomName || '')}</strong>
                    <span>${escapeHtml(room.buildingName || '건물 정보 없음')}</span>
                </div>
                <button class="room-subscribe-button" type="button" data-room-id="${room.roomId}">추가</button>
            </article>`).join('');
    }

    function renderRoomSearchPagination(candidates) {
        const totalPages = Number(candidates.totalPages) || 0;
        if (totalPages <= 1) {
            elements.roomSearchPagination.hidden = true;
            return;
        }

        elements.roomSearchPagination.hidden = false;
        elements.roomSearchPrevious.disabled = Boolean(candidates.first);
        elements.roomSearchNext.disabled = Boolean(candidates.last);
        elements.roomSearchPage.textContent = `${candidates.page + 1} / ${totalPages} · 총 ${candidates.totalElements}개`;
    }

    async function subscribeRoomFromResult(event) {
        const button = event.target.closest('.room-subscribe-button');
        if (!button) return;

        const roomId = Number(button.dataset.roomId);
        if (!Number.isFinite(roomId) || roomId <= 0) return;

        button.disabled = true;
        button.textContent = '추가 중';
        try {
            await requestJson(`/api/front/teams/${teamId}/rooms/${roomId}/subscription`, { method: 'PUT' });
            button.textContent = '추가됨';
            showToast('모니터링 공간에 추가했습니다.');
            state.widgetRooms = [];
            state.widgetOptionsLoaded = false;
            state.page = 0;
            await loadDashboard({ showLoading: false });
            await loadSubscriptionCandidates(state.roomSearchCurrentPage);
        } catch (error) {
            console.error('공간 구독 실패:', error);
            button.disabled = false;
            button.textContent = '다시 시도';
            showToast(error.message, true);
        }
    }

    function readStoredWidgets() {
        try {
            const storedValue = window.localStorage.getItem(widgetStorageKey);
            if (storedValue == null) {
                return { widgets: [], initialized: false };
            }

            const parsed = JSON.parse(storedValue);
            if (!Array.isArray(parsed)) {
                return { widgets: [], initialized: true };
            }

            const widgets = parsed
                .map(sanitizeStoredWidget)
                .filter(Boolean)
                .slice(0, maxWidgetCount);
            return { widgets, initialized: true };
        } catch (error) {
            console.warn('저장된 차트를 불러오지 못했습니다:', error);
            return { widgets: [], initialized: false };
        }
    }

    function sanitizeStoredWidget(widget) {
        const roomId = Number(widget?.roomId);
        if (!Number.isFinite(roomId) || roomId <= 0 || !widget?.metricCode) {
            return null;
        }

        return {
            id: String(widget.id || createWidgetId()),
            roomId,
            roomName: String(widget.roomName || `공간 ${roomId}`),
            buildingName: String(widget.buildingName || '건물 정보 없음'),
            metricCode: String(widget.metricCode),
            displayName: String(widget.displayName || widget.metricCode),
            symbol: String(widget.symbol || ''),
            period: widgetPeriods[widget.period] ? widget.period : '24H'
        };
    }

    async function persistWidgets() {
        const widgets = await requestJson(`/api/front/teams/${teamId}/dashboard/widgets`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                widgets: state.widgets.map(widget => ({
                    id: widget.id,
                    roomId: widget.roomId,
                    metricCode: widget.metricCode,
                    displayName: widget.displayName,
                    symbol: widget.symbol,
                    period: widget.period
                }))
            })
        });
        state.widgets = (widgets || [])
            .map(sanitizeStoredWidget)
            .filter(Boolean)
            .slice(0, maxWidgetCount);
        clearLegacyWidgetStorage();
    }

    async function initializeWidgets() {
        try {
            const widgets = await requestJson(`/api/front/teams/${teamId}/dashboard/widgets`);
            state.widgets = (widgets || [])
                .map(sanitizeStoredWidget)
                .filter(Boolean)
                .slice(0, maxWidgetCount);

            if (state.widgets.length === 0 && legacyWidgetState.widgets.length > 0) {
                state.widgets = legacyWidgetState.widgets;
                await persistWidgets();
                showToast('브라우저에 저장된 차트 구성을 서버로 이전했습니다.');
            } else {
                clearLegacyWidgetStorage();
            }
        } catch (error) {
            console.error('서버의 차트 구성을 불러오지 못했습니다:', error);
            state.widgets = legacyWidgetState.widgets;
            showToast('차트 구성을 서버에서 불러오지 못했습니다.', true);
        } finally {
            state.widgetConfigurationLoaded = true;
            renderWidgets();
        }
    }

    function clearLegacyWidgetStorage() {
        try {
            window.localStorage.removeItem(widgetStorageKey);
        } catch (error) {
            console.warn('이전 브라우저 차트 구성을 삭제하지 못했습니다:', error);
        }
    }

    function createWidgetId() {
        if (window.crypto?.randomUUID) {
            return window.crypto.randomUUID();
        }
        return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    }

    function renderWidgets() {
        state.widgetSeriesAbortController?.abort();
        state.widgetSeriesAbortController = null;
        destroyWidgetSorting();
        disposeAllWidgetCharts();

        const hasWidgets = state.widgets.length > 0;
        elements.widgetEmpty.hidden = hasWidgets;
        elements.widgetGrid.hidden = !hasWidgets;
        elements.openWidgetModalButton.disabled = !state.widgetConfigurationLoaded
            || state.widgets.length >= maxWidgetCount;
        elements.openWidgetModalButton.title = !state.widgetConfigurationLoaded
            ? '차트 구성을 불러오는 중입니다.'
            : state.widgets.length >= maxWidgetCount
                ? `차트는 최대 ${maxWidgetCount}개까지 추가할 수 있습니다.`
                : '';

        if (!hasWidgets) {
            elements.widgetGrid.innerHTML = '';
            return;
        }

        elements.widgetGrid.innerHTML = state.widgets.map(renderWidgetCard).join('');
        initializeWidgetSorting();
        loadWidgetSeriesBatch();
    }

    function renderWidgetCard(widget, index) {
        const tone = widgetTones[index % widgetTones.length];
        const periodOptions = Object.entries(widgetPeriods).map(([value, period]) => `
            <option value="${value}"${widget.period === value ? ' selected' : ''}>${period.label}</option>`).join('');

        return `
            <article class="chart-widget chart-widget-tone-${tone}" data-widget-id="${escapeHtml(widget.id)}">
                <header class="chart-widget-header">
                    <div class="chart-widget-title-wrap">
                        <button class="widget-drag-handle" type="button"
                                aria-label="${escapeHtml(widget.roomName)} ${escapeHtml(widget.displayName)} 차트 순서 이동"
                                title="드래그하거나 방향키로 순서 이동">
                            <svg aria-hidden="true" width="16" height="20" viewBox="0 0 16 20" fill="currentColor">
                                <circle cx="5" cy="5" r="1.4"/><circle cx="11" cy="5" r="1.4"/>
                                <circle cx="5" cy="10" r="1.4"/><circle cx="11" cy="10" r="1.4"/>
                                <circle cx="5" cy="15" r="1.4"/><circle cx="11" cy="15" r="1.4"/>
                            </svg>
                        </button>
                        <span class="chart-widget-icon" aria-hidden="true">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                                <path d="M4 19V9M10 19V5M16 19v-7M22 19V3"/><path d="M2 19h22"/>
                            </svg>
                        </span>
                        <div>
                            <h4 class="chart-widget-title">${escapeHtml(widget.roomName)} · ${escapeHtml(widget.displayName)}</h4>
                            <p class="chart-widget-subtitle">${escapeHtml(widget.buildingName)}</p>
                        </div>
                    </div>
                    <div class="chart-widget-controls">
                        <label class="sr-only" for="widget-period-${escapeHtml(widget.id)}">조회 기간</label>
                        <select class="chart-widget-period" id="widget-period-${escapeHtml(widget.id)}" data-widget-id="${escapeHtml(widget.id)}">${periodOptions}</select>
                        <button class="widget-remove-button" type="button" data-widget-action="remove" data-widget-id="${escapeHtml(widget.id)}" aria-label="${escapeHtml(widget.roomName)} ${escapeHtml(widget.displayName)} 차트 삭제">
                            <svg aria-hidden="true" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 7h16M9 7V4h6v3M8 10v8M12 10v8M16 10v8M6 7l1 14h10l1-14"/></svg>
                        </button>
                    </div>
                </header>
                <div class="chart-widget-summary">
                    <div class="chart-widget-value">-<small>${escapeHtml(widget.symbol)}</small></div>
                    <div class="chart-widget-change">시계열 데이터 조회 중</div>
                </div>
                <div class="chart-widget-canvas"><div class="widget-loading">그래프를 불러오고 있습니다.</div></div>
            </article>`;
    }

    function findWidgetCard(widgetId) {
        return Array.from(elements.widgetGrid.querySelectorAll('.chart-widget'))
            .find(card => card.dataset.widgetId === String(widgetId));
    }

    function initializeWidgetSorting() {
        const sortableAvailable = typeof window.Sortable === 'function';
        const canSort = sortableAvailable && state.widgets.length > 1;
        elements.widgetGrid.classList.toggle('is-sortable', canSort);
        if (!canSort) {
            return;
        }

        state.widgetSortable = window.Sortable.create(elements.widgetGrid, {
            animation: 180,
            easing: 'cubic-bezier(0.2, 0, 0, 1)',
            draggable: '.chart-widget',
            handle: '.widget-drag-handle',
            ghostClass: 'chart-widget-sort-ghost',
            chosenClass: 'chart-widget-sort-chosen',
            dragClass: 'chart-widget-sort-drag',
            delay: 120,
            delayOnTouchOnly: true,
            touchStartThreshold: 4,
            fallbackTolerance: 5,
            onStart() {
                elements.widgetGrid.classList.add('is-dragging');
            },
            onEnd(event) {
                elements.widgetGrid.classList.remove('is-dragging');
                if (event.oldIndex == null || event.newIndex == null || event.oldIndex === event.newIndex) {
                    resizeWidgetCharts();
                    return;
                }
                reorderAndPersistWidget(event.oldIndex, event.newIndex);
            }
        });
    }

    function destroyWidgetSorting() {
        state.widgetSortable?.destroy();
        state.widgetSortable = null;
        elements.widgetGrid.classList.remove('is-sortable', 'is-dragging', 'is-order-saving');
    }

    function handleWidgetSortKeydown(event) {
        const handle = event.target.closest('.widget-drag-handle');
        if (!handle || state.widgetOrderSaving) {
            return;
        }

        const card = handle.closest('.chart-widget');
        const oldIndex = state.widgets.findIndex(widget => String(widget.id) === card?.dataset.widgetId);
        if (oldIndex < 0) {
            return;
        }

        const columnCount = getWidgetGridColumnCount();
        const indexDelta = {
            ArrowLeft: -1,
            ArrowRight: 1,
            ArrowUp: -columnCount,
            ArrowDown: columnCount
        }[event.key];
        if (indexDelta == null) {
            return;
        }

        event.preventDefault();
        const newIndex = Math.max(0, Math.min(oldIndex + indexDelta, state.widgets.length - 1));
        if (newIndex !== oldIndex) {
            reorderAndPersistWidget(oldIndex, newIndex, true);
        }
    }

    function getWidgetGridColumnCount() {
        const columns = window.getComputedStyle(elements.widgetGrid).gridTemplateColumns;
        return Math.max(columns.split(' ').filter(Boolean).length, 1);
    }

    async function reorderAndPersistWidget(oldIndex, newIndex, focusHandle = false) {
        if (state.widgetOrderSaving
            || oldIndex < 0
            || newIndex < 0
            || oldIndex >= state.widgets.length
            || newIndex >= state.widgets.length) {
            return;
        }

        const previousWidgets = state.widgets.map(widget => ({ ...widget }));
        const [movedWidget] = state.widgets.splice(oldIndex, 1);
        state.widgets.splice(newIndex, 0, movedWidget);
        syncWidgetOrderPresentation();

        state.widgetOrderSaving = true;
        elements.widgetGrid.classList.add('is-order-saving');
        state.widgetSortable?.option('disabled', true);
        try {
            await persistWidgets();
            syncWidgetOrderPresentation();
            showToast('차트 순서를 저장했습니다.');
            if (focusHandle) {
                findWidgetCard(movedWidget.id)?.querySelector('.widget-drag-handle')?.focus();
            }
        } catch (error) {
            console.error('차트 순서 저장 실패:', error);
            state.widgets = previousWidgets;
            syncWidgetOrderPresentation();
            showToast(error.message || '차트 순서를 저장하지 못했습니다.', true);
        } finally {
            state.widgetOrderSaving = false;
            elements.widgetGrid.classList.remove('is-order-saving');
            state.widgetSortable?.option('disabled', false);
        }
    }

    function syncWidgetOrderPresentation() {
        const toneClasses = widgetTones.map(tone => `chart-widget-tone-${tone}`);
        state.widgets.forEach((widget, index) => {
            const card = findWidgetCard(widget.id);
            if (!card) {
                return;
            }

            elements.widgetGrid.appendChild(card);
            const toneName = widgetTones[index % widgetTones.length];
            const tone = widgetToneStyles[toneName];
            card.classList.remove(...toneClasses);
            card.classList.add(`chart-widget-tone-${toneName}`);

            const chart = state.chartInstances.get(String(widget.id));
            if (chart && !chart.isDisposed()) {
                chart.setOption({
                    series: [{
                        id: 'metric-series',
                        lineStyle: { color: tone.line, width: 2.5 },
                        itemStyle: { color: tone.line },
                        markPoint: {
                            itemStyle: {
                                color: '#ffffff',
                                borderColor: tone.line,
                                borderWidth: 2.5
                            }
                        }
                    }]
                }, { lazyUpdate: true });
            }
        });
        resizeWidgetCharts();
    }

    function refreshWidgets() {
        state.widgets.forEach(widget => {
            const card = findWidgetCard(widget.id);
            if (card) {
                card.querySelector('.chart-widget-change').textContent = '시계열 데이터 갱신 중';
            }
        });
        loadWidgetSeriesBatch();
    }

    async function loadWidgetSeriesBatch(widgetId = null) {
        const targetWidgets = widgetId == null
            ? state.widgets
            : state.widgets.filter(widget => String(widget.id) === String(widgetId));
        if (targetWidgets.length === 0) {
            return;
        }

        state.widgetSeriesAbortController?.abort();
        const controller = new AbortController();
        state.widgetSeriesAbortController = controller;

        try {
            const params = new URLSearchParams();
            if (widgetId != null) {
                params.set('widgetId', String(widgetId));
            }
            const queryString = params.size > 0 ? `?${params}` : '';
            const response = await fetch(
                `/api/front/teams/${teamId}/dashboard/widgets/series${queryString}`,
                { headers: { Accept: 'application/json' }, signal: controller.signal }
            );
            const batch = await readJsonResponse(response);
            if (state.widgetSeriesAbortController !== controller) {
                return;
            }

            const seriesByWidgetId = new Map(
                (batch?.widgets || []).map(series => [String(series.id), series])
            );
            targetWidgets.forEach(widget => {
                const series = seriesByWidgetId.get(String(widget.id));
                if (!series) {
                    renderWidgetError(widget, '차트 데이터를 찾을 수 없습니다.');
                } else if (series.errorCode) {
                    renderWidgetError(widget, widgetSeriesErrorMessage(series.errorCode));
                } else {
                    renderWidgetSeries(widget, series);
                }
            });
        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
            console.error('차트 배치 조회 실패:', error);
            targetWidgets.forEach(widget => renderWidgetError(widget, error.message));
        } finally {
            if (state.widgetSeriesAbortController === controller) {
                state.widgetSeriesAbortController = null;
            }
        }
    }

    function widgetSeriesErrorMessage(errorCode) {
        if (errorCode === 'ROOM_SUBSCRIPTION.NOT_FOUND') {
            return '구독이 해제된 공간입니다. 차트를 삭제해 주세요.';
        }
        if (errorCode?.startsWith('INFRASTRUCTURE.')) {
            return '센서 데이터를 일시적으로 불러올 수 없습니다.';
        }
        return '이 차트의 지표를 조회할 수 없습니다.';
    }

    function renderWidgetSeries(widget, series) {
        const card = findWidgetCard(widget.id);
        if (!card) {
            return;
        }

        const points = (series?.points || [])
            .filter(point => point?.averageValue != null)
            .map(point => ({ at: new Date(point.bucketEndAt), value: Number(point.averageValue) }))
            .filter(point => !Number.isNaN(point.at.getTime()) && Number.isFinite(point.value))
            .sort((left, right) => left.at - right.at);
        const displayName = series?.displayName || widget.displayName;
        const symbol = series?.symbol || widget.symbol || '';

        card.querySelector('.chart-widget-title').textContent = `${widget.roomName} · ${displayName}`;
        if (points.length === 0) {
            disposeWidgetChart(widget.id);
            card.querySelector('.chart-widget-value').innerHTML = `-<small>${escapeHtml(symbol)}</small>`;
            card.querySelector('.chart-widget-change').textContent = '조회 기간 내 데이터 없음';
            card.querySelector('.chart-widget-canvas').innerHTML = '<div class="widget-no-data">조회 기간에 수집된 데이터가 없습니다.</div>';
            return;
        }

        const latest = points.at(-1).value;
        const first = points[0].value;
        const average = points.reduce((sum, point) => sum + point.value, 0) / points.length;
        const delta = latest - first;
        card.querySelector('.chart-widget-value').innerHTML = `${formatMetricValue(latest)}<small>${escapeHtml(symbol)}</small>`;
        card.querySelector('.chart-widget-change').textContent = points.length > 1
            ? `시작 대비 ${formatSignedMetricValue(delta)}${symbol} · 평균 ${formatMetricValue(average)}${symbol}`
            : '수집 데이터 1건';
        renderEChart(widget, points, displayName, symbol);
    }

    function renderWidgetError(widget, message) {
        const card = findWidgetCard(widget.id);
        if (!card) {
            return;
        }

        disposeWidgetChart(widget.id);
        card.querySelector('.chart-widget-change').textContent = '갱신 실패';
        card.querySelector('.chart-widget-canvas').innerHTML = `
            <div class="widget-error">
                <span>${escapeHtml(message || '그래프를 불러오지 못했습니다.')}</span>
                <button class="widget-retry-button" type="button" data-widget-action="retry" data-widget-id="${escapeHtml(widget.id)}">다시 시도</button>
            </div>`;
    }

    function renderEChart(widget, points, displayName, symbol) {
        const card = findWidgetCard(widget.id);
        const container = card?.querySelector('.chart-widget-canvas');
        if (!container) {
            return;
        }
        if (!window.echarts) {
            renderWidgetError(widget, '차트 라이브러리를 불러오지 못했습니다.');
            return;
        }

        const key = String(widget.id);
        let chart = state.chartInstances.get(key);
        const isNewChart = !chart || chart.isDisposed() || chart.getDom() !== container;
        if (isNewChart) {
            disposeWidgetChart(widget.id);
            container.innerHTML = '';
            chart = window.echarts.init(container, null, { renderer: 'canvas' });
            state.chartInstances.set(key, chart);
        }
        container.setAttribute(
            'aria-label',
            `${widgetPeriods[widget.period]?.label || '조회 기간'} ${displayName} 추이 그래프, 단위 ${symbol || '없음'}`
        );

        chart.setOption(
            buildChartOption(widget, points, displayName, symbol, isNewChart),
            { notMerge: isNewChart, lazyUpdate: !isNewChart }
        );
    }

    function buildChartOption(widget, points, displayName, symbol, isNewChart) {
        const toneIndex = Math.max(state.widgets.findIndex(candidate => candidate.id === widget.id), 0);
        const tone = widgetToneStyles[widgetTones[toneIndex % widgetTones.length]];
        const latestPoint = points.at(-1);
        const lineData = points.map(point => [point.at.getTime(), point.value]);

        return {
            animationDuration: isNewChart ? 450 : 0,
            animationEasing: 'cubicOut',
            animationDurationUpdate: 220,
            animationEasingUpdate: 'cubicOut',
            aria: {
                enabled: true,
                label: {
                    description: `${widgetPeriods[widget.period]?.label || '조회 기간'} ${displayName} 추이 그래프`
                }
            },
            grid: {
                left: 10,
                right: 14,
                top: 12,
                bottom: 8,
                containLabel: true
            },
            tooltip: {
                trigger: 'axis',
                renderMode: 'richText',
                confine: true,
                axisPointer: {
                    type: 'line',
                    lineStyle: { color: '#9ba8b7', type: 'dashed' }
                },
                backgroundColor: '#ffffff',
                borderColor: '#dfe5ec',
                borderWidth: 1,
                padding: [9, 11],
                textStyle: { color: '#435064', fontSize: 12 },
                formatter(parameters) {
                    const parameter = (Array.isArray(parameters) ? parameters : [parameters])
                        .find(candidate => candidate.seriesType === 'line');
                    if (!parameter || !Array.isArray(parameter.value)) {
                        return '';
                    }
                    const [timestamp, value] = parameter.value;
                    return `${formatWidgetTooltipTime(new Date(timestamp))}\n${displayName}  ${formatMetricValue(value)}${symbol}`;
                }
            },
            xAxis: {
                type: 'time',
                boundaryGap: false,
                axisLine: { lineStyle: { color: '#dfe5ec' } },
                axisTick: { show: false },
                axisLabel: {
                    color: '#8a95a4',
                    fontSize: 10,
                    hideOverlap: true,
                    formatter: value => formatWidgetTime(new Date(value), widget.period)
                },
                splitLine: { show: false }
            },
            yAxis: {
                type: 'value',
                scale: true,
                splitNumber: 3,
                axisLine: { show: false },
                axisTick: { show: false },
                axisLabel: {
                    color: '#8a95a4',
                    fontSize: 10,
                    formatter: formatAxisValue
                },
                splitLine: {
                    lineStyle: { color: '#e9edf2', type: 'dashed' }
                }
            },
            series: [{
                id: 'metric-series',
                name: displayName,
                type: 'line',
                data: lineData,
                smooth: points.length > 2 ? 0.18 : false,
                showSymbol: points.length === 1,
                symbol: 'circle',
                symbolSize: 7,
                lineStyle: { color: tone.line, width: 2.5 },
                itemStyle: { color: tone.line },
                markPoint: {
                    silent: true,
                    symbol: 'circle',
                    symbolSize: 9,
                    label: { show: false },
                    itemStyle: {
                        color: '#ffffff',
                        borderColor: tone.line,
                        borderWidth: 2.5
                    },
                    data: [{ coord: [latestPoint.at.getTime(), latestPoint.value] }]
                },
                emphasis: { focus: 'series' }
            }]
        };
    }

    function disposeWidgetChart(widgetId) {
        const key = String(widgetId);
        const chart = state.chartInstances.get(key);
        if (chart && !chart.isDisposed()) {
            chart.dispose();
        }
        state.chartInstances.delete(key);
    }

    function disposeAllWidgetCharts() {
        state.chartInstances.forEach(chart => {
            if (!chart.isDisposed()) {
                chart.dispose();
            }
        });
        state.chartInstances.clear();
    }

    function resizeWidgetCharts() {
        window.cancelAnimationFrame(state.chartResizeFrame);
        state.chartResizeFrame = window.requestAnimationFrame(() => {
            state.chartResizeFrame = null;
            state.chartInstances.forEach(chart => {
                if (!chart.isDisposed()) {
                    chart.resize();
                }
            });
        });
    }

    function formatMetricValue(value) {
        const maximumFractionDigits = Math.abs(value) >= 100 ? 0 : 1;
        return Number(value).toLocaleString('ko-KR', { maximumFractionDigits });
    }

    function formatSignedMetricValue(value) {
        const formatted = formatMetricValue(Math.abs(value));
        if (Math.abs(value) < 0.05) {
            return '0';
        }
        return `${value > 0 ? '+' : '-'}${formatted}`;
    }

    function formatAxisValue(value) {
        const maximumFractionDigits = Math.abs(value) >= 100 ? 0 : 1;
        return Number(value).toLocaleString('ko-KR', { maximumFractionDigits });
    }

    function formatWidgetTime(date, periodKey) {
        if (periodKey === '24H') {
            return new Intl.DateTimeFormat('ko-KR', {
                hour: '2-digit',
                minute: '2-digit',
                hourCycle: 'h23'
            }).format(date);
        }
        return new Intl.DateTimeFormat('ko-KR', { month: 'numeric', day: 'numeric' }).format(date);
    }

    function formatWidgetTooltipTime(date) {
        return new Intl.DateTimeFormat('ko-KR', {
            month: 'numeric',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hourCycle: 'h23'
        }).format(date);
    }

    async function openWidgetModal() {
        if (!state.widgetConfigurationLoaded) {
            showToast('차트 구성을 불러오는 중입니다. 잠시 후 다시 시도해 주세요.', true);
            return;
        }
        if (state.widgets.length >= maxWidgetCount) {
            showToast(`차트는 최대 ${maxWidgetCount}개까지 추가할 수 있습니다.`, true);
            return;
        }

        state.lastWidgetFocusedElement = document.activeElement;
        elements.widgetModal.hidden = false;
        document.body.style.overflow = 'hidden';
        elements.widgetForm.reset();
        elements.widgetMetricSelect.disabled = true;
        elements.widgetMetricSelect.innerHTML = '<option value="">공간을 먼저 선택해 주세요</option>';
        elements.widgetSubmitButton.disabled = true;
        setWidgetFormMessage('');

        if (!state.widgetOptionsLoaded) {
            await loadWidgetOptions();
        } else {
            renderWidgetRoomOptions();
        }
        window.setTimeout(() => elements.widgetRoomSelect.focus(), 0);
    }

    function closeWidgetModal() {
        elements.widgetModal.hidden = true;
        document.body.style.overflow = '';
        state.lastWidgetFocusedElement?.focus?.();
    }

    async function loadWidgetOptions() {
        elements.widgetRoomSelect.disabled = true;
        elements.widgetRoomSelect.innerHTML = '<option value="">공간을 불러오는 중입니다</option>';
        try {
            const options = await requestJson(`/api/front/teams/${teamId}/dashboard/widget-options`);
            state.widgetRooms = (options.rooms || []).filter(room => Number(room.roomId) > 0);
            state.widgetOptionsLoaded = true;
            renderWidgetRoomOptions();
        } catch (error) {
            console.error('차트 선택지 조회 실패:', error);
            state.widgetOptionsLoaded = false;
            elements.widgetRoomSelect.innerHTML = '<option value="">공간을 불러오지 못했습니다</option>';
            setWidgetFormMessage(error.message || '공간을 불러오지 못했습니다.');
        }
    }

    function renderWidgetRoomOptions() {
        if (state.widgetRooms.length === 0) {
            elements.widgetRoomSelect.innerHTML = '<option value="">모니터링 중인 공간이 없습니다</option>';
            elements.widgetRoomSelect.disabled = true;
            setWidgetFormMessage('먼저 모니터링 공간을 추가해 주세요.');
            return;
        }

        elements.widgetRoomSelect.disabled = false;
        elements.widgetRoomSelect.innerHTML = `
            <option value="">공간 선택</option>
            ${state.widgetRooms.map(room => `<option value="${room.roomId}">${escapeHtml(room.buildingName || '건물 정보 없음')} · ${escapeHtml(room.roomName || `공간 ${room.roomId}`)}</option>`).join('')}`;
    }

    function loadWidgetMetricOptions() {
        const roomId = Number(elements.widgetRoomSelect.value);
        elements.widgetSubmitButton.disabled = true;
        setWidgetFormMessage('');
        if (!Number.isFinite(roomId) || roomId <= 0) {
            elements.widgetMetricSelect.disabled = true;
            elements.widgetMetricSelect.innerHTML = '<option value="">공간을 먼저 선택해 주세요</option>';
            return;
        }

        const room = state.widgetRooms.find(candidate => Number(candidate.roomId) === roomId);
        const metrics = room?.metrics || [];
        if (metrics.length === 0) {
            elements.widgetMetricSelect.disabled = true;
            elements.widgetMetricSelect.innerHTML = '<option value="">그래프로 표시할 수 있는 지표가 없습니다</option>';
            setWidgetFormMessage('이 공간에는 시계열 조회가 가능한 지표가 없습니다.');
            return;
        }

        elements.widgetMetricSelect.innerHTML = `
            <option value="">환경 지표 선택</option>
            ${metrics.map(metric => `
                <option value="${escapeHtml(metric.metricCode)}"
                        data-display-name="${escapeHtml(metric.displayName || metric.metricCode)}"
                        data-symbol="${escapeHtml(metric.symbol || '')}">${escapeHtml(metric.displayName || metric.metricCode)}${metric.symbol ? ` (${escapeHtml(metric.symbol)})` : ''}</option>`).join('')}`;
        elements.widgetMetricSelect.disabled = false;
    }

    async function addWidget(event) {
        event.preventDefault();
        if (state.widgets.length >= maxWidgetCount) {
            setWidgetFormMessage(`차트는 최대 ${maxWidgetCount}개까지 추가할 수 있습니다.`);
            return;
        }

        const roomId = Number(elements.widgetRoomSelect.value);
        const metricCode = elements.widgetMetricSelect.value;
        const room = state.widgetRooms.find(candidate => Number(candidate.roomId) === roomId);
        const metricOption = elements.widgetMetricSelect.selectedOptions[0];
        if (!room || !metricCode || !metricOption) {
            setWidgetFormMessage('공간과 환경 지표를 선택해 주세요.');
            return;
        }

        const duplicated = state.widgets.some(widget =>
            widget.roomId === roomId && widget.metricCode === metricCode
        );
        if (duplicated) {
            setWidgetFormMessage('같은 공간과 지표의 차트가 이미 있습니다. 기존 차트의 기간을 변경해 주세요.');
            return;
        }

        const previousWidgets = state.widgets.map(widget => ({ ...widget }));
        state.widgets.push({
            id: createWidgetId(),
            roomId,
            roomName: room.roomName || `공간 ${roomId}`,
            buildingName: room.buildingName || '건물 정보 없음',
            metricCode,
            displayName: metricOption.dataset.displayName || metricCode,
            symbol: metricOption.dataset.symbol || '',
            period: widgetPeriods[elements.widgetPeriodSelect.value] ? elements.widgetPeriodSelect.value : '24H'
        });
        elements.widgetSubmitButton.disabled = true;
        try {
            await persistWidgets();
            renderWidgets();
            closeWidgetModal();
            showToast('차트를 추가했습니다.');
        } catch (error) {
            console.error('차트 저장 실패:', error);
            state.widgets = previousWidgets;
            renderWidgets();
            setWidgetFormMessage(error.message || '차트를 저장하지 못했습니다.');
            elements.widgetSubmitButton.disabled = false;
        }
    }

    async function changeWidgetPeriod(event) {
        if (!event.target.matches('.chart-widget-period')) {
            return;
        }

        const widget = state.widgets.find(candidate => candidate.id === event.target.dataset.widgetId);
        if (!widget || !widgetPeriods[event.target.value]) {
            return;
        }
        const previousPeriod = widget.period;
        widget.period = event.target.value;
        const card = findWidgetCard(widget.id);
        disposeWidgetChart(widget.id);
        card.querySelector('.chart-widget-change').textContent = '조회 기간 변경 중';
        card.querySelector('.chart-widget-canvas').innerHTML = '<div class="widget-loading">그래프를 불러오고 있습니다.</div>';
        try {
            await persistWidgets();
            loadWidgetSeriesBatch(widget.id);
        } catch (error) {
            console.error('차트 조회 기간 저장 실패:', error);
            widget.period = previousPeriod;
            renderWidgets();
            showToast(error.message || '조회 기간을 저장하지 못했습니다.', true);
        }
    }

    async function handleWidgetAction(event) {
        const button = event.target.closest('[data-widget-action]');
        if (!button) {
            return;
        }

        const widget = state.widgets.find(candidate => candidate.id === button.dataset.widgetId);
        if (!widget) {
            return;
        }

        if (button.dataset.widgetAction === 'retry') {
            const card = findWidgetCard(widget.id);
            disposeWidgetChart(widget.id);
            card.querySelector('.chart-widget-canvas').innerHTML = '<div class="widget-loading">그래프를 불러오고 있습니다.</div>';
            loadWidgetSeriesBatch(widget.id);
            return;
        }

        if (button.dataset.widgetAction === 'remove') {
            const previousWidgets = state.widgets.map(candidate => ({ ...candidate }));
            state.widgets = state.widgets.filter(candidate => candidate.id !== widget.id);
            renderWidgets();
            try {
                await persistWidgets();
                showToast('차트를 삭제했습니다.');
            } catch (error) {
                console.error('차트 삭제 저장 실패:', error);
                state.widgets = previousWidgets;
                renderWidgets();
                showToast(error.message || '차트를 삭제하지 못했습니다.', true);
            }
        }
    }

    function setWidgetFormMessage(message) {
        elements.widgetFormMessage.textContent = message;
        elements.widgetFormMessage.hidden = !message;
    }

    async function requestJson(url, options = {}) {
        const response = await fetch(url, {
            headers: { Accept: 'application/json', ...(options.headers || {}) },
            ...options
        });
        return readJsonResponse(response);
    }

    async function readJsonResponse(response) {
        if (response.status === 401) {
            window.location.assign('/login');
            throw new Error('로그인이 필요합니다.');
        }

        if (!response.ok) {
            let message = response.status === 403
                ? '이 팀의 대시보드를 볼 권한이 없습니다.'
                : `요청을 처리하지 못했습니다. (${response.status})`;
            const contentType = response.headers.get('content-type') || '';
            try {
                if (contentType.includes('application/json')) {
                    const errorBody = await response.json();
                    message = errorBody.message || message;
                } else {
                    const text = await response.text();
                    if (text && text.length < 200) message = text;
                }
            } catch (ignored) {
                // 기본 상태 메시지를 사용한다.
            }
            throw new Error(message);
        }

        if (response.status === 204) {
            return null;
        }
        return response.json();
    }

    function showToast(message, isError = false) {
        window.clearTimeout(state.toastTimer);
        elements.toast.textContent = message;
        elements.toast.classList.toggle('is-error', isError);
        elements.toast.hidden = false;
        state.toastTimer = window.setTimeout(() => {
            elements.toast.hidden = true;
        }, 3200);
    }

    function formatDateTime(date) {
        return new Intl.DateTimeFormat('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hourCycle: 'h23'
        }).format(date);
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    function cleanup() {
        window.clearInterval(state.refreshTimer);
        window.clearTimeout(state.searchTimer);
        window.clearTimeout(state.toastTimer);
        closeDashboardStream();
        state.roomSearchAbortController?.abort();
        state.roomSearchAbortController = null;
        state.dashboardAbortController?.abort();
        state.dashboardMetricsAbortController?.abort();
        state.dashboardMetricsAbortController = null;
        state.widgetSeriesAbortController?.abort();
        state.widgetSeriesAbortController = null;
        destroyWidgetSorting();
        window.removeEventListener('resize', resizeWidgetCharts);
        window.cancelAnimationFrame(state.chartResizeFrame);
        disposeAllWidgetCharts();
    }

    bindEvents();
    renderWidgets();
    initializeWidgets();
    loadDashboard({ showLoading: true });
})();
