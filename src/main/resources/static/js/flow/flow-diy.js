// ================================================================
// flow-diy.js — 플로우 편집기
//
// 모드
//   IDLE        노드 선택 / 드래그 / 삭제
//   PLACING     사이드바에서 고른 노드를 캔버스에 배치
//   CONNECTING  노드 handle을 끌어 다른 노드에 연결
//
// 데이터
//   nodes        저장용 노드 배열. 신규 노드는 nodeId가 음수
//   connections  저장용 커넥션 배열. branchType은 TRUE / FALSE
// ================================================================
(function () {
    'use strict';

    const layout = document.querySelector('.layout');
    if (!layout) return;

    const ROOM_ID = layout.dataset.roomId;
    const FLOW_ID = layout.dataset.flowId || null;
    const TEMPLATE_ID = layout.dataset.templateId || null;
    const API = layout.dataset.apiBase || '/flows/api';

    const canvas = document.getElementById('canvas');
    const canvasHint = document.getElementById('canvasHint');
    const svg = document.getElementById('connection-svg');
    const ghost = document.getElementById('ghostNode');
    const banner = document.getElementById('banner');
    const bannerText = document.getElementById('bannerText');

    /* ================================================================
       노드 타입 정의
       ================================================================ */

    const NODE_META = {
        START:     { category: 'start',     label: '시작',        maxIn: 0,        maxOut: Infinity },
        THRESHOLD: { category: 'condition', label: '임계치 판단',  maxIn: 1,        maxOut: Infinity },
        AVERAGE:   { category: 'condition', label: '평균값 판단',  maxIn: 1,        maxOut: Infinity },
        DURATION:  { category: 'condition', label: '지속시간 판단', maxIn: 1,       maxOut: Infinity },
        GRADIENT:  { category: 'condition', label: '기울기 판단',  maxIn: 1,        maxOut: Infinity },
        OR:        { category: 'logic',     label: 'OR 조건',     maxIn: Infinity, maxOut: Infinity },
        ALERT:     { category: 'action',    label: '알람(텔레그램)', maxIn: 1,      maxOut: 0 }
    };

    // com.nhnacademy.front.rule.enums.Operator 와 값이 일치해야 한다
    const OPERATORS = [
        { value: 'GT',  label: '초과 (>)' },
        { value: 'GTE', label: '이상 (>=)' },
        { value: 'LT',  label: '미만 (<)' },
        { value: 'LTE', label: '이하 (<=)' },
        { value: 'EQ',  label: '같음 (=)' },
        { value: 'NEQ', label: '같지 않음 (!=)' }
    ];

    const ALERT_TYPES = [
        { value: 'COMFORT_LIMIT_EXCEEDED', label: '긴급 — 즉각적인 조치가 필요한 상태' },
        { value: 'VENTILATION_RECOMMEND',  label: '비긴급 — 환기를 권장하는 상태' }
    ];

    /* ================================================================
       상태
       ================================================================ */

    let mode = 'IDLE';
    let pendingNode = null;      // 배치 대기 중인 노드 타입
    let pendingBranch = null;    // 연결 대기 중인 branchType

    let nodes = [];              // { nodeId, nodeName, nodeType, nodeConfig }
    let connections = [];        // { sourceNodeId, targetNodeId, branchType }
    let nextTempId = -1;

    let selectedNodeId = null;
    let sensorMeta = [];         // [{ measurementType, displayName, unit }]

    let isDrawing = false;
    let drawStartId = null;
    let tempLine = null;

    let isDragging = false;
    let dragEl = null;
    let dragOffsetX = 0;
    let dragOffsetY = 0;

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
            let payload = null;
            try { payload = await res.json(); } catch (e) { /* 본문 없음 */ }
            const err = new Error((payload && payload.message) || '요청을 처리하지 못했습니다.');
            err.status = res.status;
            err.payload = payload;
            throw err;
        }
        if (res.status === 204) return null;
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }

    function toast(message, isError) {
        const el = document.getElementById('toast');
        el.textContent = message;
        el.classList.toggle('is-error', Boolean(isError));
        el.hidden = false;
        clearTimeout(toast._t);
        toast._t = setTimeout(() => { el.hidden = true; }, 3400);
    }

    const findNode = id => nodes.find(n => n.nodeId === id);
    const nodeEl = id => canvas.querySelector(`.placed-node[data-node-id="${id}"]`);

    /* ================================================================
       초기 로딩
       ================================================================ */

    (async function init() {
        try {
            if (FLOW_ID) {
                const data = await request(`${API}/rooms/${ROOM_ID}/flows/${FLOW_ID}`);
                sensorMeta = data.sensorMetaInfos || data.sensorMetaInfo || [];
                document.getElementById('flowName').value = data.flowName || '';
                document.getElementById('flowDescription').value = data.description || '';
                setActive(Boolean(data.isActive));
                loadGraph(data.nodes || [], data.connections || [], false);
            } else if (TEMPLATE_ID) {
                const data = await request(`${API}/rooms/${ROOM_ID}/flow-templates/${TEMPLATE_ID}`);
                sensorMeta = data.sensorMetaInfos || data.sensorMetaInfo || [];
                document.getElementById('flowName').value = data.templateName || '';
                document.getElementById('flowDescription').value = data.description || '';
                // 템플릿의 nodeId는 템플릿 DB의 id이므로 전부 음수 임시 id로 바꾼다
                loadGraph(data.nodes || [], data.connections || [], true);
                toast('템플릿을 불러왔습니다. 값을 확인한 뒤 저장하세요.');
            } else {
                const data = await request(`${API}/rooms/${ROOM_ID}/flows/form`);
                sensorMeta = data.sensorMetaInfos || data.sensorMetaInfo || [];
            }
        } catch (err) {
            toast(err.message, true);
        }
    })();

    function loadGraph(rawNodes, rawConnections, remap) {
        const idMap = new Map();

        rawNodes.forEach(n => {
            const id = remap ? nextTempId-- : n.nodeId;
            idMap.set(n.nodeId, id);
            const cfg = n.nodeConfig || {};
            nodes.push({
                nodeId: id,
                nodeName: n.nodeName,
                nodeType: n.nodeType,
                nodeConfig: { ...cfg, nodeType: n.nodeType }
            });
            drawNode(id, cfg.x || 120, cfg.y || 120);
        });

        rawConnections.forEach(c => {
            const s = idMap.get(c.sourceNodeId);
            const t = idMap.get(c.targetNodeId);
            if (s == null || t == null) return;
            connections.push({ sourceNodeId: s, targetNodeId: t, branchType: c.branchType });
            drawLine(s, t, c.branchType);
        });

        refreshHint();
        refreshStartAvailability();
        refreshConfigMarks();
    }

    /* ================================================================
       사이드바
       ================================================================ */

    document.querySelectorAll('.node-item[data-type]').forEach(item => {
        item.addEventListener('click', () => {
            if (item.disabled) return;
            mode = 'PLACING';
            pendingNode = {
                type: item.dataset.type,
                category: item.dataset.category,
                label: item.dataset.label
            };
            ghost.textContent = pendingNode.label;
            ghost.className = `ghost-node ${pendingNode.category}`;
            ghost.style.display = 'block';
            showBanner('배치할 위치를 클릭하세요.');
            markSelecting(item);
        });
    });

    document.querySelectorAll('.connection-item').forEach(item => {
        item.addEventListener('click', () => {
            mode = 'CONNECTING';
            pendingBranch = item.dataset.branch;
            canvas.classList.add('is-placing');
            showBanner(`${pendingBranch} 연결: 시작 노드의 점을 끌어 대상 노드에 놓으세요.`);
            markSelecting(item);
        });
    });

    function markSelecting(active) {
        document.querySelectorAll('.node-item').forEach(el => el.classList.remove('is-selecting'));
        if (active) active.classList.add('is-selecting');
    }

    function showBanner(text) {
        bannerText.textContent = text;
        banner.hidden = false;
    }

    function resetMode() {
        mode = 'IDLE';
        pendingNode = null;
        pendingBranch = null;
        ghost.style.display = 'none';
        banner.hidden = true;
        canvas.classList.remove('is-placing');
        markSelecting(null);
        if (isDrawing && tempLine) {
            tempLine.remove();
            isDrawing = false;
            drawStartId = null;
            tempLine = null;
        }
    }

    document.getElementById('bannerCancel').addEventListener('click', resetMode);

    // START 노드는 하나만 배치할 수 있다
    function refreshStartAvailability() {
        const item = document.getElementById('startNodeItem');
        const has = nodes.some(n => n.nodeType === 'START');
        item.disabled = has;
        item.title = has ? '시작 노드는 하나만 배치할 수 있습니다' : '';
    }

    document.addEventListener('mousemove', e => {
        if (mode === 'PLACING') {
            ghost.style.left = e.clientX + 'px';
            ghost.style.top = e.clientY + 'px';
        }
    });

    /* ================================================================
       노드 배치
       ================================================================ */

    canvas.addEventListener('click', e => {
        if (mode === 'IDLE') {
            if (!e.target.closest('.placed-node')) selectNode(null);
            return;
        }
        if (mode !== 'PLACING') return;

        const rect = canvas.getBoundingClientRect();
        const x = Math.round(e.clientX - rect.left);
        const y = Math.round(e.clientY - rect.top);

        const id = nextTempId--;
        nodes.push({
            nodeId: id,
            nodeName: pendingNode.label,
            nodeType: pendingNode.type,
            nodeConfig: { nodeType: pendingNode.type, x, y }
        });
        drawNode(id, x, y);

        resetMode();
        refreshHint();
        refreshStartAvailability();
        refreshConfigMarks();
        selectNode(id);
    });

    function drawNode(id, x, y) {
        const data = findNode(id);
        const meta = NODE_META[data.nodeType] || { category: 'condition' };

        const el = document.createElement('div');
        el.className = `placed-node ${meta.category}`;
        el.dataset.nodeId = id;
        el.dataset.category = meta.category;
        el.style.left = x + 'px';
        el.style.top = y + 'px';
        el.textContent = data.nodeName;

        el.addEventListener('click', evt => {
            if (mode !== 'IDLE') return;
            evt.stopPropagation();
            selectNode(id);
        });

        el.addEventListener('mousedown', evt => {
            if (evt.target.classList.contains('node-handle')) return;
            if (mode !== 'IDLE') return;
            evt.preventDefault();
            evt.stopPropagation();

            isDragging = true;
            dragEl = el;
            const rect = canvas.getBoundingClientRect();
            dragOffsetX = (evt.clientX - rect.left) - parseFloat(el.style.left);
            dragOffsetY = (evt.clientY - rect.top) - parseFloat(el.style.top);
            el.style.cursor = 'grabbing';
        });

        ['top', 'bottom', 'left', 'right'].forEach(pos => {
            const handle = document.createElement('div');
            handle.className = `node-handle ${pos}`;
            handle.addEventListener('mousedown', evt => startDrawing(evt, id, handle));
            el.appendChild(handle);
        });

        canvas.appendChild(el);
    }

    function selectNode(id) {
        selectedNodeId = id;
        canvas.querySelectorAll('.placed-node')
            .forEach(el => el.classList.toggle('is-selected', el.dataset.nodeId === String(id)));
        renderConfigPanel(id);
    }

    /* ================================================================
       커넥션
       ================================================================ */

    function startDrawing(evt, id, handle) {
        if (mode !== 'CONNECTING' || isDrawing) return;

        const meta = NODE_META[findNode(id).nodeType];
        if (meta && meta.maxOut === 0) {
            toast('행동 노드에서는 연결을 시작할 수 없습니다.', true);
            return;
        }
        if (findNode(id).nodeType === 'START' && pendingBranch !== 'TRUE') {
            toast('시작 노드에서 나가는 연결은 TRUE만 가능합니다.', true);
            return;
        }

        evt.stopPropagation();
        isDrawing = true;
        drawStartId = id;

        tempLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        applyLineStyle(tempLine, pendingBranch);
        tempLine.setAttribute('stroke-dasharray', '5,5');

        const hRect = handle.getBoundingClientRect();
        const cRect = canvas.getBoundingClientRect();
        const sx = hRect.left + hRect.width / 2 - cRect.left;
        const sy = hRect.top + hRect.height / 2 - cRect.top;
        tempLine.setAttribute('x1', sx);
        tempLine.setAttribute('y1', sy);
        tempLine.setAttribute('x2', sx);
        tempLine.setAttribute('y2', sy);
        svg.appendChild(tempLine);
    }

    function applyLineStyle(line, branch) {
        const color = branch === 'FALSE' ? '#d9694a' : '#2fa37f';
        const marker = branch === 'FALSE' ? 'url(#arrow-false)' : 'url(#arrow-true)';
        line.setAttribute('stroke', color);
        line.setAttribute('stroke-width', '3');
        line.setAttribute('marker-end', marker);
    }

    canvas.addEventListener('mouseup', e => {
        if (!isDrawing) return;

        const targetEl = e.target.closest('.placed-node');
        if (!targetEl) { resetMode(); return; }

        const targetId = parseInt(targetEl.dataset.nodeId, 10);
        const error = validateConnection(drawStartId, targetId, pendingBranch);
        if (error) {
            toast(error, true);
            resetMode();
            return;
        }

        connections.push({
            sourceNodeId: drawStartId,
            targetNodeId: targetId,
            branchType: pendingBranch
        });
        tempLine.setAttribute('stroke-dasharray', '0');
        tempLine.dataset.sourceId = drawStartId;
        tempLine.dataset.targetId = targetId;
        attachLineDelete(tempLine, drawStartId, targetId);
        updateLines(drawStartId);

        tempLine = null;
        isDrawing = false;
        drawStartId = null;
        resetMode();
        refreshConfigMarks();
    });

    function validateConnection(sourceId, targetId, branch) {
        if (sourceId === targetId) return '같은 노드끼리는 연결할 수 없습니다.';

        const source = findNode(sourceId);
        const target = findNode(targetId);
        if (!source || !target) return '노드를 찾을 수 없습니다.';

        const sMeta = NODE_META[source.nodeType];
        const tMeta = NODE_META[target.nodeType];

        if (tMeta.maxIn === 0) return '시작 노드로는 연결할 수 없습니다.';

        const dup = connections.some(c =>
            c.sourceNodeId === sourceId && c.targetNodeId === targetId);
        if (dup) return '이미 연결된 노드입니다.';

        const inCount = connections.filter(c => c.targetNodeId === targetId).length;
        if (inCount >= tMeta.maxIn) {
            return `${NODE_META[target.nodeType].label} 노드는 입력을 ${tMeta.maxIn}개만 받을 수 있습니다.`;
        }

        if (wouldCycle(sourceId, targetId)) return '순환 연결은 만들 수 없습니다.';
        return null;
    }

    function wouldCycle(sourceId, targetId) {
        // targetId에서 출발해 sourceId에 도달하면 순환
        const stack = [targetId];
        const seen = new Set();
        while (stack.length) {
            const cur = stack.pop();
            if (cur === sourceId) return true;
            if (seen.has(cur)) continue;
            seen.add(cur);
            connections.filter(c => c.sourceNodeId === cur)
                .forEach(c => stack.push(c.targetNodeId));
        }
        return false;
    }

    function drawLine(sourceId, targetId, branch) {
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        applyLineStyle(line, branch);
        line.dataset.sourceId = sourceId;
        line.dataset.targetId = targetId;
        svg.appendChild(line);
        attachLineDelete(line, sourceId, targetId);
        updateLines(sourceId);
    }

    function attachLineDelete(line, sourceId, targetId) {
        line.addEventListener('dblclick', evt => {
            evt.stopPropagation();
            line.remove();
            connections = connections.filter(c =>
                !(c.sourceNodeId === sourceId && c.targetNodeId === targetId));
            toast('연결을 삭제했습니다.');
        });
        line.setAttribute('title', '더블클릭하면 연결이 삭제됩니다');
    }

    // 두 노드 사이에서 가장 가까운 handle끼리 선을 다시 잇는다
    function updateLines(nodeId) {
        const lines = svg.querySelectorAll(
            `line[data-source-id="${nodeId}"], line[data-target-id="${nodeId}"]`);
        const cRect = canvas.getBoundingClientRect();

        lines.forEach(line => {
            const s = nodeEl(line.dataset.sourceId);
            const t = nodeEl(line.dataset.targetId);
            if (!s || !t) return;

            const sRect = s.getBoundingClientRect();
            const tRect = t.getBoundingClientRect();
            const sCx = sRect.left + sRect.width / 2;
            const sCy = sRect.top + sRect.height / 2;
            const tCx = tRect.left + tRect.width / 2;
            const tCy = tRect.top + tRect.height / 2;

            const best = (el, towardX, towardY) => {
                let bx = 0, by = 0, min = Infinity;
                el.querySelectorAll('.node-handle').forEach(h => {
                    const r = h.getBoundingClientRect();
                    const hx = r.left + r.width / 2;
                    const hy = r.top + r.height / 2;
                    const d = Math.hypot(hx - towardX, hy - towardY);
                    if (d < min) { min = d; bx = hx - cRect.left; by = hy - cRect.top; }
                });
                return [bx, by];
            };

            const [x1, y1] = best(s, tCx, tCy);
            const [x2, y2] = best(t, sCx, sCy);
            line.setAttribute('x1', x1);
            line.setAttribute('y1', y1);
            line.setAttribute('x2', x2);
            line.setAttribute('y2', y2);
        });
    }

    /* ================================================================
       드래그 / 삭제
       ================================================================ */

    document.addEventListener('mousemove', e => {
        const rect = canvas.getBoundingClientRect();

        if (isDrawing && tempLine) {
            tempLine.setAttribute('x2', e.clientX - rect.left);
            tempLine.setAttribute('y2', e.clientY - rect.top);
        }

        if (isDragging && dragEl) {
            const x = e.clientX - rect.left - dragOffsetX;
            const y = e.clientY - rect.top - dragOffsetY;
            dragEl.style.left = x + 'px';
            dragEl.style.top = y + 'px';
            updateLines(dragEl.dataset.nodeId);
        }
    });

    document.addEventListener('mouseup', () => {
        if (!isDragging || !dragEl) return;
        dragEl.style.cursor = 'grab';

        const data = findNode(parseInt(dragEl.dataset.nodeId, 10));
        if (data) {
            data.nodeConfig.x = Math.round(parseFloat(dragEl.style.left));
            data.nodeConfig.y = Math.round(parseFloat(dragEl.style.top));
        }
        isDragging = false;
        dragEl = null;
    });

    document.addEventListener('keydown', e => {
        if (e.key !== 'Escape') return;
        if (mode !== 'IDLE' || isDrawing) { resetMode(); return; }
        if (selectedNodeId != null) deleteNode(selectedNodeId);
    });

    function deleteNode(id) {
        const el = nodeEl(id);
        if (el) el.remove();

        svg.querySelectorAll(`line[data-source-id="${id}"], line[data-target-id="${id}"]`)
            .forEach(l => l.remove());

        nodes = nodes.filter(n => n.nodeId !== id);
        connections = connections.filter(c => c.sourceNodeId !== id && c.targetNodeId !== id);

        selectedNodeId = null;
        renderConfigPanel(null);
        refreshHint();
        refreshStartAvailability();
        refreshConfigMarks();
    }

    function refreshHint() {
        canvasHint.hidden = nodes.length > 0;
    }

    /* ================================================================
       노드 설정 패널
       ================================================================ */

    const panel = document.getElementById('configPanel');
    const cfgBody = document.getElementById('cfgBody');
    const cfgMsg = document.getElementById('cfgMsg');

    document.getElementById('cfgClose').addEventListener('click', () => selectNode(null));
    document.getElementById('cfgDelete').addEventListener('click', () => {
        if (selectedNodeId != null) deleteNode(selectedNodeId);
    });

    function cfgSay(text, isError) {
        cfgMsg.textContent = text;
        cfgMsg.className = 'cfg-msg ' + (isError ? 'is-error' : 'is-ok');
        cfgMsg.hidden = false;
    }
    function cfgClear() { cfgMsg.hidden = true; cfgMsg.textContent = ''; }

    function field(label, html, hint) {
        return `<div class="cfg-field"><label>${label}</label>${html}` +
            (hint ? `<p class="hint">${hint}</p>` : '') + `</div>`;
    }
    function options(list, selected) {
        return list.map(o =>
            `<option value="${o.value}"${o.value === selected ? ' selected' : ''}>${o.label}</option>`
        ).join('');
    }
    function measurementOptions(selected) {
        if (!sensorMeta.length) {
            return '<option value="">사용 가능한 센서가 없습니다</option>';
        }
        return sensorMeta.map(m => {
            const v = m.measurementType || m.MeasurementType;
            const name = m.displayName || v;
            const unit = m.symbol || m.unit || '';
            return `<option value="${v}"${v === selected ? ' selected' : ''}>${name}${unit ? ' (' + unit + ')' : ''}</option>`;
        }).join('');
    }

    function renderConfigPanel(id) {
        cfgClear();
        if (id == null) { panel.hidden = true; return; }

        const node = findNode(id);
        if (!node) { panel.hidden = true; return; }

        const meta = NODE_META[node.nodeType];
        panel.hidden = false;
        document.getElementById('cfgTitle').textContent = meta.label;
        document.getElementById('cfgSubtitle').textContent = node.nodeType;

        const c = node.nodeConfig || {};
        let html = field('노드 이름',
            `<input type="text" id="cfgNodeName" maxlength="50" value="${node.nodeName || ''}">`);

        if (node.nodeType === 'START' || node.nodeType === 'OR') {
            html += `<p class="cfg-empty">이 노드는 추가 설정이 필요하지 않습니다.</p>`;
        } else if (node.nodeType === 'ALERT') {
            html += field('알림 채널',
                `<select id="cfgChannel"><option value="TELEGRAM">텔레그램</option></select>`);
            html += field('알림 제목',
                `<input type="text" id="cfgAlertTitle" maxlength="50" value="${c.alertTitle || ''}">`);
            html += field('알림 등급',
                `<select id="cfgAlertType">${options(ALERT_TYPES, c.alertType)}</select>`);
            html += field('중복 방지 시간 (초)',
                `<input type="number" id="cfgDedup" min="1" value="${c.dedupWindowSec != null ? c.dedupWindowSec : 300}">`,
                '같은 알림을 이 시간 안에는 다시 보내지 않습니다.');
        } else {
            html += field('측정 항목',
                `<select id="cfgMeasurement">${measurementOptions(c.measurementType)}</select>`);
            html += field('단위',
                `<input type="text" id="cfgUnit" maxlength="20" value="${c.unit || ''}">`);
            html += `<div class="cfg-row">`;
            html += field('비교 조건',
                `<select id="cfgOperator">${options(OPERATORS, c.operator)}</select>`);

            if (node.nodeType === 'THRESHOLD') {
                html += field('기준값', `<input type="number" step="any" id="cfgValue" value="${c.threshold != null ? c.threshold : ''}">`);
            } else if (node.nodeType === 'AVERAGE') {
                html += field('평균값', `<input type="number" step="any" id="cfgValue" value="${c.average != null ? c.average : ''}">`);
            } else if (node.nodeType === 'GRADIENT') {
                html += field('기울기', `<input type="number" step="any" id="cfgValue" value="${c.gradiant != null ? c.gradiant : ''}">`);
            } else {
                html += field('기준값', `<input type="number" step="any" id="cfgValue" value="${c.threshold != null ? c.threshold : ''}">`);
            }
            html += `</div>`;

            if (node.nodeType === 'AVERAGE' || node.nodeType === 'GRADIENT') {
                html += field('계산 구간 (초)',
                    `<input type="number" id="cfgWindow" min="1" value="${c.windowSec != null ? c.windowSec : 300}">`,
                    '이 시간 동안의 값을 모아 판단합니다.');
            }
            if (node.nodeType === 'DURATION') {
                html += field('지속 시간 (초)',
                    `<input type="number" id="cfgDuration" min="1" value="${c.durationSec != null ? c.durationSec : 300}">`,
                    '조건이 이 시간 이상 이어지면 참으로 판단합니다.');
            }
        }

        cfgBody.innerHTML = html;

        // 측정 항목을 고르면 단위를 자동으로 채운다
        const measure = document.getElementById('cfgMeasurement');
        const unit = document.getElementById('cfgUnit');
        if (measure && unit) {
            const fill = () => {
                const found = sensorMeta.find(m =>
                    (m.measurementType || m.MeasurementType) === measure.value);
                if (found && !unit.value) unit.value = found.symbol || found.unit || '';
            };
            fill();
            measure.addEventListener('change', () => { unit.value = ''; fill(); });
        }
    }

    document.getElementById('cfgApply').addEventListener('click', async () => {
        if (selectedNodeId == null) return;
        const node = findNode(selectedNodeId);
        if (!node) return;

        const name = document.getElementById('cfgNodeName').value.trim();
        if (!name) return cfgSay('노드 이름을 입력하세요.', true);

        const cfg = { nodeType: node.nodeType, x: node.nodeConfig.x, y: node.nodeConfig.y };
        const num = id => {
            const el = document.getElementById(id);
            return el && el.value !== '' ? Number(el.value) : null;
        };
        const str = id => {
            const el = document.getElementById(id);
            return el ? el.value.trim() : '';
        };

        if (node.nodeType === 'ALERT') {
            cfg.channel = str('cfgChannel') || 'TELEGRAM';
            cfg.alertTitle = str('cfgAlertTitle');
            cfg.alertType = str('cfgAlertType');
            cfg.dedupWindowSec = num('cfgDedup');
            if (!cfg.alertTitle) return cfgSay('알림 제목을 입력하세요.', true);
            if (cfg.dedupWindowSec != null && cfg.dedupWindowSec <= 0) {
                return cfgSay('중복 방지 시간은 1초 이상이어야 합니다.', true);
            }
        } else if (node.nodeType !== 'START' && node.nodeType !== 'OR') {
            cfg.measurementType = str('cfgMeasurement');
            cfg.unit = str('cfgUnit');
            cfg.operator = str('cfgOperator');

            if (!cfg.measurementType) return cfgSay('측정 항목을 선택하세요.', true);
            if (!cfg.unit) return cfgSay('단위를 입력하세요.', true);

            const value = num('cfgValue');
            if (value == null) return cfgSay('기준이 될 값을 입력하세요.', true);

            if (node.nodeType === 'AVERAGE') cfg.average = value;
            else if (node.nodeType === 'GRADIENT') cfg.gradiant = value;
            else cfg.threshold = value;

            if (node.nodeType === 'AVERAGE' || node.nodeType === 'GRADIENT') {
                cfg.windowSec = num('cfgWindow');
                if (!cfg.windowSec || cfg.windowSec <= 0) {
                    return cfgSay('계산 구간은 1초 이상이어야 합니다.', true);
                }
            }
            if (node.nodeType === 'DURATION') {
                cfg.durationSec = num('cfgDuration');
                if (!cfg.durationSec || cfg.durationSec <= 0) {
                    return cfgSay('지속 시간은 1초 이상이어야 합니다.', true);
                }
            }
        }

        // 서버에도 한 번 확인한다. 실패해도 화면 입력은 유지한다.
        try {
            const result = await request(
                `${API}/rooms/${ROOM_ID}/node-config/${node.nodeId}`,
                { method: 'POST', body: JSON.stringify({ nodeConfig: cfg }) });

            if (result && result.valid === false) {
                const first = (result.errors && result.errors[0]) || {};
                return cfgSay(first.message || result.message || '설정을 확인해 주세요.', true);
            }
        } catch (err) {
            // 검증 API를 쓸 수 없어도 저장 단계에서 다시 걸러진다
            console.warn('노드 설정 검증 실패:', err.message);
        }

        node.nodeName = name;
        node.nodeConfig = cfg;

        const el = nodeEl(node.nodeId);
        if (el) {
            el.childNodes[0].nodeValue = name;
        }
        refreshConfigMarks();
        cfgSay('적용했습니다.', false);
    });

    // 설정이 아직 안 채워진 노드에 표시를 남긴다
    function refreshConfigMarks() {
        nodes.forEach(n => {
            const el = nodeEl(n.nodeId);
            if (el) el.classList.toggle('needs-config', !isConfigured(n));
        });
    }

    function isConfigured(node) {
        const c = node.nodeConfig || {};
        if (node.nodeType === 'START' || node.nodeType === 'OR') return true;
        if (node.nodeType === 'ALERT') {
            return Boolean(c.channel && c.alertTitle && c.alertType);
        }
        if (!c.measurementType || !c.unit || !c.operator) return false;
        if (node.nodeType === 'AVERAGE') return c.average != null && c.windowSec > 0;
        if (node.nodeType === 'GRADIENT') return c.gradiant != null && c.windowSec > 0;
        if (node.nodeType === 'DURATION') return c.threshold != null && c.durationSec > 0;
        return c.threshold != null;
    }

    /* ================================================================
       플로우 메타
       ================================================================ */

    const metaDialog = document.getElementById('metaDialog');
    document.getElementById('flowMetaBtn').addEventListener('click', () => metaDialog.showModal());

    const activeBtn = document.getElementById('flowActive');
    activeBtn.addEventListener('click', () =>
        setActive(activeBtn.getAttribute('aria-checked') !== 'true'));
    function setActive(on) { activeBtn.setAttribute('aria-checked', String(on)); }

    /* ================================================================
       저장
       ================================================================ */

    const errorBox = document.getElementById('errorBox');
    const errorList = document.getElementById('errorList');

    function showErrors(list) {
        errorList.innerHTML = '';
        list.forEach(text => {
            const li = document.createElement('li');
            li.textContent = text;
            errorList.appendChild(li);
        });
        errorBox.hidden = list.length === 0;
    }

    function validateFlow() {
        const errors = [];
        const name = document.getElementById('flowName').value.trim();
        const nameInput = document.getElementById('flowName');

        if (!name) errors.push('플로우 이름을 입력하세요.');
        else if (name.length > 50) errors.push('플로우 이름은 50자를 넘을 수 없습니다.');
        nameInput.classList.toggle('is-invalid', !name);

        const desc = document.getElementById('flowDescription').value.trim();
        if (desc.length > 255) errors.push('설명은 255자를 넘을 수 없습니다.');

        const starts = nodes.filter(n => n.nodeType === 'START');
        const conditions = nodes.filter(n => NODE_META[n.nodeType].category === 'condition');
        const actions = nodes.filter(n => n.nodeType === 'ALERT');

        if (starts.length === 0) errors.push('시작 노드가 필요합니다.');
        if (starts.length > 1) errors.push('시작 노드는 하나만 둘 수 있습니다.');
        if (conditions.length === 0) errors.push('조건 노드가 최소 하나 필요합니다.');
        if (actions.length === 0) errors.push('행동 노드가 최소 하나 필요합니다.');
        if (nodes.length < 3) errors.push('노드는 최소 3개가 필요합니다.');

        nodes.forEach(n => {
            if (!isConfigured(n)) errors.push(`"${n.nodeName}" 노드의 설정이 비어 있습니다.`);
        });

        nodes.forEach(n => {
            const meta = NODE_META[n.nodeType];
            const outCount = connections.filter(c => c.sourceNodeId === n.nodeId).length;
            const inCount = connections.filter(c => c.targetNodeId === n.nodeId).length;

            if (meta.maxOut === 0 && outCount > 0) {
                errors.push(`"${n.nodeName}" 행동 노드에서 나가는 연결은 둘 수 없습니다.`);
            }
            if (meta.maxIn === 0 && inCount > 0) {
                errors.push(`"${n.nodeName}" 시작 노드로 들어오는 연결은 둘 수 없습니다.`);
            }
            if (n.nodeType !== 'START' && inCount === 0) {
                errors.push(`"${n.nodeName}" 노드가 아무데도 연결되어 있지 않습니다.`);
            }
            if (meta.maxOut !== 0 && outCount === 0) {
                errors.push(`"${n.nodeName}" 노드에서 나가는 연결이 없습니다.`);
            }
        });

        return errors;
    }

    document.getElementById('saveBtn').addEventListener('click', async () => {
        const errors = validateFlow();
        showErrors(errors);
        if (errors.length) {
            toast('저장할 수 없습니다. 위 내용을 확인해 주세요.', true);
            return;
        }

        const payload = {
            flowName: document.getElementById('flowName').value.trim(),
            description: document.getElementById('flowDescription').value.trim() || null,
            isActive: activeBtn.getAttribute('aria-checked') === 'true',
            nodes: nodes.map(n => ({
                nodeId: n.nodeId,
                nodeName: n.nodeName,
                nodeType: n.nodeType,
                nodeConfig: n.nodeConfig
            })),
            connections: connections.map(c => ({
                sourceNodeId: c.sourceNodeId,
                targetNodeId: c.targetNodeId,
                branchType: c.branchType
            }))
        };

        const saveBtn = document.getElementById('saveBtn');
        saveBtn.disabled = true;

        try {
            if (FLOW_ID) {
                await request(`${API}/rooms/${ROOM_ID}/flows/${FLOW_ID}`, {
                    method: 'PUT', body: JSON.stringify(payload)
                });
            } else {
                await request(`${API}/rooms/${ROOM_ID}/flows`, {
                    method: 'POST', body: JSON.stringify(payload)
                });
            }
            toast('저장했습니다.');
            setTimeout(() => { window.location.href = `/rooms/${ROOM_ID}/flows`; }, 700);
        } catch (err) {
            // 서버 검증 결과가 errors 배열로 오면 그대로 보여준다
            const list = err.payload && err.payload.errors;
            if (Array.isArray(list) && list.length) {
                showErrors(list.map(e => e.message || String(e)));
            }
            toast(err.message, true);
            saveBtn.disabled = false;
        }
    });
})();