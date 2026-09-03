// ================================================================
// [ 전체 흐름 (Flow) 개요 ]
// ================================================================
//
// 이 스크립트는 크게 3가지 모드(currentMode)로 동작한다.
//
//   1) IDLE            : 아무것도 안 하는 기본 상태
//                         - 배치된 노드 클릭 → 선택(selected)
//                         - 선택된 노드 드래그 → 이동 (연결된 선도 같이 따라옴)
//                         - 빈 캔버스 클릭 → 선택 해제
//                         - ESC → 선택된 노드 + 그 노드에 연결된 선까지 전부 삭제
//
//   2) PLACING_MODE     : 사이드바에서 노드 아이템을 클릭한 직후의 상태
//                         - 마우스를 따라다니는 "고스트 노드" 미리보기 표시
//                         - 캔버스 클릭 시 그 위치에 실제 노드(div) 생성
//                         - 노드 생성과 동시에 상하좌우 4개의 connection handle도 같이 붙임
//                         - 노드 하나 배치하면 자동으로 다시 IDLE로 복귀 (1개씩만 배치됨)
//
//   3) CONNECTING_MODE  : 사이드바에서 커넥션 타입(true/false/null)을 클릭한 직후의 상태
//                         - 노드의 handle을 mousedown → 임시 점선(tempLine) 그리기 시작
//                         - 마우스를 움직이면 임시선의 끝점이 마우스를 따라다님
//                         - 다른 노드 위에서 mouseup → 연결 완료 (점선 → 실선 전환)
//                         - 빈 공간에서 mouseup → 연결 취소
//
// [ 모드 전환 트리거 정리 ]
//   - 사이드바 노드 아이템 클릭      → IDLE / CONNECTING_MODE → PLACING_MODE
//   - 사이드바 커넥션 타입 클릭      → IDLE → CONNECTING_MODE
//   - 배치 완료 / 연결 완료         → 자동으로 IDLE 복귀
//   - 배너의 취소 버튼 또는 ESC 키   → 진행 중이던 배치/연결을 취소하고 IDLE 복귀
//
// [ 데이터 흐름 (화면 요소 ↔ 저장용 데이터) ]
//   - placedNodes       : 실제 DOM 노드(.placed-node)와 1:1 매칭되는 저장용 배열
//                         (백엔드 저장 시 이 배열을 그대로 전송)
//   - placedConnections : 실제 DOM 선(<line>)과 1:1 매칭되는 저장용 배열
//   - nodeId는 신규 생성 시 음수(nextNodeId)로 임시 발급됨
//     → 서버에 저장될 때 실제 DB의 양수 ID로 치환되는 구조를 염두에 둔 설계
//
//!! [ 흐름표에 없던 / 놓치기 쉬운 동작들 ]
//!!   - '행동(action) → 조건(condition)' 순서로 선을 그으면 내부적으로
//!!     시작/도착 노드를 강제로 뒤집어서 저장함 (실제 로직 흐름상 조건이 먼저 와야 하므로)
//!!   - 완성된 선(completed-line)은 더블클릭하면 바로 삭제됨
//!!   - ESC 키 하나로 "그리는 중인 선 취소" + "배치/연결 모드 취소" + "선택 노드 삭제"
//!!     세 가지 역할을 모두 처리하고 있음 (상황별로 조건 분기)
//!!   - 노드 삭제 시 연결된 선도 함께 삭제되지 않으면 "끊어진 선"이 남으므로
//!!     반드시 노드 삭제 로직 안에서 관련 line들도 같이 지워줘야 함
//!!   - 동일 노드 간 중복 커넥션 생성 방지 로직 적용
//!!   - 노드 0개 일 때 캔버스 안내 힌트 문구 자동 복원
//================================================================


//< ---------------- 사이드바 토글 버튼 ----------------
//< 사이드바 내 카테고리(조건/행동 등) 아코디언을 열고 닫는 단순 UI 토글
const toggleBtns = document.querySelectorAll('.toggle-btn');
toggleBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        btn.classList.toggle('open');
    });
});

fd
//< ---------------- 전역 상태(State) 변수 ----------------
//< 현재 에디터 모드: IDLE(대기) / PLACING_MODE(노드 배치중) / CONNECTING_MODE(선 연결중)
let currentMode = 'IDLE';

//< 배치할 노드에 대한 정보 (사이드바에서 클릭한 노드 종류를 임시로 기억)
let selectedNodeCategory = null;
let selectedNodeType = null;
let selectedNodeLabel = '';

const nodeItems = document.querySelectorAll('.node-item:not(.static)');
const ghostNode = document.getElementById('ghost-node');
const placementCancelBtn = document.getElementById('placement-cancel');
const canvasArea = document.getElementById('canvas-area');
const placementBanner = document.getElementById('placement-banner');
const placementBannerText = document.getElementById('placement-banner-text');
const canvasHint = document.getElementById('canvas-hint');
const saveBtn = document.querySelector('.save-btn');
const nodeConfigModal = document.getElementById('node-config-modal');
const nodeConfigForm = document.getElementById('node-config-form');
const nodeConfigFields = document.getElementById('node-config-fields');
const nodeConfigTitle = document.getElementById('node-config-title');
const nodeConfigType = document.getElementById('node-config-type');
const nodeConfigStatus = document.getElementById('node-config-status');
const nodeNameInput = document.getElementById('node-name-input');
const nodeConfigCloseBtn = document.getElementById('node-config-close');
const nodeConfigCancelBtn = document.getElementById('node-config-cancel');
const nodeConfigValidateBtn = document.getElementById('node-config-validate');

//< 커넥션(선) 관련 상태
let selectedConnectionType = null;         //< 'true' / 'false' / 'null' 중 하나
const connectionItems = document.querySelectorAll('.connection-item');
const connectionSvg = document.getElementById('connection-svg');

let isDrawing = false;      //< 현재 선을 긋고 있는 중인지 여부
let startNode = null;       //< 선의 시작 노드
let tempLine = null;        //< 그리는 중인 임시 SVG line 엘리먼트

//< 백엔드로 저장할 노드/커넥션 데이터 (실제 화면 요소와는 별개의 "장부" 역할)
let placedNodes = [];
let placedConnections = [];
let nextNodeId = -1;        //< 신규 노드 임시 ID (음수로 발급 → 저장 시 서버가 실제 ID로 치환)
let selectedPlacedNode = null;

//< ---------------- 노드 드래그 관련 변수 ----------------
let isDraggingNode = false;
let draggedNode = null;
let dragOffsetX = 0;
let dragOffsetY = 0;
let didDragNode = false;
let suppressNextNodeClick = false;

//< 노드 설정 폼 관련 상태
let flowBuildForm = null;
let editingNodeId = null;

const fallbackSensorMetaInfoList = [
    { measurementType: 'CO2', displayName: '이산화탄소', description: 'CO2', symbol: 'ppm' },
    { measurementType: 'TEMPERATURE', displayName: '온도', description: 'Temperature', symbol: '°C' },
    { measurementType: 'HUMIDITY', displayName: '상대습도', description: 'Humidity', symbol: '%' },
    { measurementType: 'ILLUMINATION', displayName: '주변 조도', description: 'Illumination', symbol: 'lux' },
    { measurementType: 'PRESSURE', displayName: '대기압', description: 'Pressure', symbol: 'hPa' },
    { measurementType: 'TVOC', displayName: '총유기화합물', description: 'TVOC', symbol: 'ppb' },
    { measurementType: 'INFRARED', displayName: '적외선', description: 'Infrared', symbol: '' }
];

const operatorOptions = [
    { value: 'GT', label: '>' },
    { value: 'GTE', label: '>=' },
    { value: 'LT', label: '<' },
    { value: 'LTE', label: '<=' },
    { value: 'EQ', label: '=' },
    { value: 'NEQ', label: '!=' }
];

const alertTypeOptions = [
    { value: 'COMFORT_LIMIT_EXCEEDED', label: '긴급' },
    { value: 'VENTILATION_RECOMMEND', label: '비긴급' }
];

const nodeTypeMeta = {
    START: { title: '시작 노드 설정', category: '시작 노드' },
    THRESHOLD: { title: '임계치 판단 설정', category: '조건 노드' },
    AVERAGE: { title: '평균값 판단 설정', category: '조건 노드' },
    DURATION: { title: '지속시간 판단 설정', category: '조건 노드' },
    GRADIENT: { title: '기울기 판단 설정', category: '조건 노드' },
    OR: { title: 'OR 조건 설정', category: '논리 노드' },
    ALERT: { title: '알람 설정', category: '행동 노드' }
};

function getRoomId() {
    const params = new URLSearchParams(window.location.search);
    const candidates = [
        document.body.dataset.roomId,
        canvasArea?.dataset.roomId,
        params.get('roomId'),
        params.get('room-id'),
        window.location.pathname.match(/rooms\/(\d+)/)?.[1],
        window.location.pathname.match(/flowdiy\/(\d+)/)?.[1]
    ];

    return candidates.find(value => value && /^\d+$/.test(value));
}

function showConfigStatus(message, type = '') {
    if (!nodeConfigStatus) return;

    nodeConfigStatus.textContent = message || '';
    nodeConfigStatus.className = `node-config-status${message ? ' visible' : ''}${type ? ' ' + type : ''}`;
}

function loadFlowBuildForm() {
    const roomId = getRoomId();
    if (!roomId) {
        showConfigStatus('roomId가 없어 센서 목록 API를 호출하지 않았습니다. URL에 ?roomId=값 을 붙이면 /api/rule/rooms/{room-id}/flows/form 을 호출합니다.');
        return;
    }

    fetch(`/api/rule/rooms/${roomId}/flows/form`, { method: 'GET' })
        .then(res => {
            if (!res.ok) throw new Error(`폼 정보 조회 실패 (status: ${res.status})`);
            return res.json();
        })
        .then(data => {
            flowBuildForm = data;
            showConfigStatus('');
        })
        .catch(err => {
            console.error('플로우 폼 정보 조회 실패:', err);
            showConfigStatus('센서 목록을 불러오지 못해 기본 목록으로 표시합니다.', 'error');
        });
}

function getSensorMetaInfoList() {
    return flowBuildForm?.sensorMetaInfoList?.length
        ? flowBuildForm.sensorMetaInfoList
        : fallbackSensorMetaInfoList;
}

function defaultMeasurementType() {
    return getSensorMetaInfoList()[0]?.measurementType || 'CO2';
}

function sensorSymbol(measurementType) {
    return getSensorMetaInfoList().find(sensor => sensor.measurementType === measurementType)?.symbol || '';
}

function buildDefaultNodeConfig(nodeType, x, y) {
    const roundedX = Math.round(x);
    const roundedY = Math.round(y);
    const base = { nodeType, x: roundedX, y: roundedY };
    const measurementType = defaultMeasurementType();
    const unit = sensorSymbol(measurementType) || 'ppm';

    if (nodeType === 'THRESHOLD') {
        return { ...base, measurementType, unit, operator: 'GTE', threshold: 1000 };
    }
    if (nodeType === 'AVERAGE') {
        return { ...base, measurementType, unit, operator: 'GTE', average: 900, windowSec: 300 };
    }
    if (nodeType === 'DURATION') {
        return { ...base, measurementType, unit, operator: 'GTE', threshold: 1000, durationSec: 180 };
    }
    if (nodeType === 'GRADIENT') {
        return { ...base, measurementType, unit, operator: 'GTE', gradient: 100, windowSec: 300 };
    }
    if (nodeType === 'ALERT') {
        return { ...base, alertTitle: '환경 알림', alertType: 'COMFORT_LIMIT_EXCEEDED', dedupWindowSec: 300 };
    }

    return base;
}

function syncNodeUnit(config) {
    if (!config.measurementType) return config;
    if (config.unit) return config;
    return { ...config, unit: sensorSymbol(config.measurementType) };
}

function addField({ name, label, type = 'text', value = '', required = true, step, min, help, full = false }) {
    const row = document.createElement('div');
    row.className = `form-row${full ? ' full' : ''}`;

    const labelEl = document.createElement('label');
    labelEl.setAttribute('for', `node-config-${name}`);
    labelEl.textContent = label;
    row.appendChild(labelEl);

    const input = document.createElement('input');
    input.id = `node-config-${name}`;
    input.name = name;
    input.type = type;
    input.value = value ?? '';
    input.required = required;
    if (step) input.step = step;
    if (min !== undefined) input.min = min;
    row.appendChild(input);

    if (help) {
        const helpEl = document.createElement('p');
        helpEl.className = 'field-help';
        helpEl.textContent = help;
        row.appendChild(helpEl);
    }

    nodeConfigFields.appendChild(row);
    return input;
}

function addSelect({ name, label, options, value = '', required = true, help, full = false }) {
    const row = document.createElement('div');
    row.className = `form-row${full ? ' full' : ''}`;

    const labelEl = document.createElement('label');
    labelEl.setAttribute('for', `node-config-${name}`);
    labelEl.textContent = label;
    row.appendChild(labelEl);

    const select = document.createElement('select');
    select.id = `node-config-${name}`;
    select.name = name;
    select.required = required;

    options.forEach(option => {
        const optionEl = document.createElement('option');
        optionEl.value = option.value;
        optionEl.textContent = option.label;
        if (option.value === value) optionEl.selected = true;
        select.appendChild(optionEl);
    });

    row.appendChild(select);

    if (help) {
        const helpEl = document.createElement('p');
        helpEl.className = 'field-help';
        helpEl.textContent = help;
        row.appendChild(helpEl);
    }

    nodeConfigFields.appendChild(row);
    return select;
}

function addMeasurementFields(config) {
    const sensorOptions = getSensorMetaInfoList().map(sensor => ({
        value: sensor.measurementType,
        label: `${sensor.displayName || sensor.measurementType}${sensor.symbol ? ` (${sensor.symbol})` : ''}`
    }));

    const measurementSelect = addSelect({
        name: 'measurementType',
        label: '측정 항목',
        options: sensorOptions,
        value: config.measurementType || defaultMeasurementType()
    });

    const unitInput = addField({
        name: 'unit',
        label: '단위',
        value: config.unit || sensorSymbol(config.measurementType || defaultMeasurementType()),
        help: '측정 항목을 바꾸면 API에서 받은 symbol 값으로 자동 변경됩니다.'
    });

    measurementSelect.addEventListener('change', () => {
        unitInput.value = sensorSymbol(measurementSelect.value);
    });
}

function renderNodeConfigFields(config) {
    nodeConfigFields.replaceChildren();

    if (['THRESHOLD', 'AVERAGE', 'DURATION', 'GRADIENT'].includes(config.nodeType)) {
        addMeasurementFields(config);
        addSelect({ name: 'operator', label: '비교 조건', options: operatorOptions, value: config.operator || 'GTE' });
    }

    if (config.nodeType === 'THRESHOLD') {
        addField({ name: 'threshold', label: '임계값', type: 'number', step: '0.01', value: config.threshold });
    } else if (config.nodeType === 'AVERAGE') {
        addField({ name: 'average', label: '평균 기준값', type: 'number', step: '0.01', value: config.average });
        addField({ name: 'windowSec', label: '평균 계산 구간(초)', type: 'number', min: 1, value: config.windowSec });
    } else if (config.nodeType === 'DURATION') {
        addField({ name: 'threshold', label: '임계값', type: 'number', step: '0.01', value: config.threshold });
        addField({ name: 'durationSec', label: '지속 시간(초)', type: 'number', min: 1, value: config.durationSec });
    } else if (config.nodeType === 'GRADIENT') {
        addField({ name: 'gradient', label: '기울기 기준값', type: 'number', step: '0.01', value: config.gradient });
        addField({ name: 'windowSec', label: '변화 감지 구간(초)', type: 'number', min: 1, value: config.windowSec });
    } else if (config.nodeType === 'ALERT') {
        addField({ name: 'alertTitle', label: '알림 제목', value: config.alertTitle, full: true });
        addSelect({ name: 'alertType', label: '알림 유형', options: alertTypeOptions, value: config.alertType });
        addField({ name: 'dedupWindowSec', label: '중복 방지 시간(초)', type: 'number', min: 1, value: config.dedupWindowSec });
    } else {
        const row = document.createElement('div');
        row.className = 'form-row full';
        const help = document.createElement('p');
        help.className = 'field-help';
        help.textContent = '이 노드는 좌표와 노드 타입만 저장됩니다.';
        row.appendChild(help);
        nodeConfigFields.appendChild(row);
    }
}

function openNodeConfigModal(nodeId) {
    const nodeData = placedNodes.find(node => node.tempNodeId === nodeId);
    if (!nodeData) return;

    editingNodeId = nodeId;
    const config = syncNodeUnit(nodeData.nodeConfig);
    nodeData.nodeConfig = config;

    const meta = nodeTypeMeta[config.nodeType] || { title: '노드 설정', category: 'Node' };
    nodeConfigTitle.textContent = meta.title;
    nodeConfigType.textContent = `${meta.category} · ${config.nodeType}`;
    nodeNameInput.value = nodeData.nodeName;
    renderNodeConfigFields(config);

    nodeConfigModal.classList.add('active');
    nodeConfigModal.setAttribute('aria-hidden', 'false');
    showConfigStatus(flowBuildForm ? '' : nodeConfigStatus.textContent);
}

function closeNodeConfigModal() {
    editingNodeId = null;
    nodeConfigModal.classList.remove('active');
    nodeConfigModal.setAttribute('aria-hidden', 'true');
}

function formValue(formData, key) {
    const value = formData.get(key);
    return typeof value === 'string' ? value.trim() : value;
}

function numericFormValue(formData, key) {
    const value = Number(formValue(formData, key));
    return Number.isFinite(value) ? value : null;
}

function nodeConfigSignature(config) {
    const detailConfig = { ...config };
    delete detailConfig.x;
    delete detailConfig.y;
    return JSON.stringify(detailConfig, Object.keys(detailConfig).sort());
}

function formatValidationErrors(response) {
    if (response?.errors?.length) {
        return response.errors
            .map(error => `${error.field || 'nodeConfig'}: ${error.message || '검증 실패'}`)
            .join('\n');
    }

    return response?.message || '노드 설정 검증에 실패했습니다.';
}

function collectNodeConfig(formData, previousConfig) {
    const config = {
        nodeType: previousConfig.nodeType,
        x: Math.round(previousConfig.x),
        y: Math.round(previousConfig.y)
    };

    if (['THRESHOLD', 'AVERAGE', 'DURATION', 'GRADIENT'].includes(previousConfig.nodeType)) {
        config.measurementType = formValue(formData, 'measurementType');
        config.unit = formValue(formData, 'unit');
        config.operator = formValue(formData, 'operator');
    }

    if (previousConfig.nodeType === 'THRESHOLD') {
        config.threshold = numericFormValue(formData, 'threshold');
    } else if (previousConfig.nodeType === 'AVERAGE') {
        config.average = numericFormValue(formData, 'average');
        config.windowSec = numericFormValue(formData, 'windowSec');
    } else if (previousConfig.nodeType === 'DURATION') {
        config.threshold = numericFormValue(formData, 'threshold');
        config.durationSec = numericFormValue(formData, 'durationSec');
    } else if (previousConfig.nodeType === 'GRADIENT') {
        config.gradient = numericFormValue(formData, 'gradient');
        config.windowSec = numericFormValue(formData, 'windowSec');
    } else if (previousConfig.nodeType === 'ALERT') {
        config.alertTitle = formValue(formData, 'alertTitle');
        config.alertType = formValue(formData, 'alertType');
        config.dedupWindowSec = numericFormValue(formData, 'dedupWindowSec');
    }

    return config;
}

loadFlowBuildForm();


//< ================================================================
//< [ 함수 ] updateConnectedLines
//< 특정 노드(nodeId)와 연결된 모든 선을 찾아서, 두 노드 사이 가장 가까운
//< handle끼리 다시 이어주는 함수. 노드를 드래그할 때마다 호출되어
//< 선이 노드를 실시간으로 "따라다니게" 만드는 핵심 함수.
//< ================================================================
function updateConnectedLines(nodeId) {
    //< 이 노드를 source 또는 target으로 가진 모든 <line> 요소를 찾음
    const connectedLines = document.querySelectorAll(
        `line[data-source-id="${nodeId}"], line[data-target-id="${nodeId}"]`
    );
    const canvasRect = canvasArea.getBoundingClientRect();

    connectedLines.forEach(line => {
        const sId = line.getAttribute('data-source-id');
        const tId = line.getAttribute('data-target-id');
        const sNode = document.querySelector(`.placed-node[data-node-id="${sId}"]`);
        const tNode = document.querySelector(`.placed-node[data-node-id="${tId}"]`);

        if (sNode && tNode) {
            const sRect = sNode.getBoundingClientRect();
            const tRect = tNode.getBoundingClientRect();

            const sCenterX = sRect.left + sRect.width / 2;
            const sCenterY = sRect.top + sRect.height / 2;
            const tCenterX = tRect.left + tRect.width / 2;
            const tCenterY = tRect.top + tRect.height / 2;

            //< 시작 노드의 4개 handle(top/bottom/left/right) 중
            //< 상대 노드 중심과 가장 가까운 handle을 시작점으로 선택
            let bestSX = 0, bestSY = 0, minSDist = Infinity;
            sNode.querySelectorAll('.node-handle').forEach(handle => {
                const hRect = handle.getBoundingClientRect();
                const hX = hRect.left + hRect.width / 2;
                const hY = hRect.top + hRect.height / 2;
                const dist = Math.sqrt(Math.pow(hX - tCenterX, 2) + Math.pow(hY - tCenterY, 2));
                if (dist < minSDist) {
                    minSDist = dist;
                    bestSX = hX - canvasRect.left;
                    bestSY = hY - canvasRect.top;
                }
            });

            //< 도착 노드도 동일한 방식으로 가장 가까운 handle을 끝점으로 선택
            let bestTX = 0, bestTY = 0, minTDist = Infinity;
            tNode.querySelectorAll('.node-handle').forEach(handle => {
                const hRect = handle.getBoundingClientRect();
                const hX = hRect.left + hRect.width / 2;
                const hY = hRect.top + hRect.height / 2;
                const dist = Math.sqrt(Math.pow(hX - sCenterX, 2) + Math.pow(hY - sCenterY, 2));
                if (dist < minTDist) {
                    minTDist = dist;
                    bestTX = hX - canvasRect.left;
                    bestTY = hY - canvasRect.top;
                }
            });

            line.setAttribute('x1', bestSX);
            line.setAttribute('y1', bestSY);
            line.setAttribute('x2', bestTX);
            line.setAttribute('y2', bestTY);
        }
    });
}


//< ================================================================
//< [ 블록 ] 노드 배치 (Placing) 로직
//< 사이드바에서 노드를 고른 뒤 → 캔버스를 클릭하면 실제 노드가 생성되는 흐름
//< ================================================================

//< 사이드바에서 노드 아이템 클릭 → "고스트 노드(미리보기)" 모드 진입
nodeItems.forEach( item => {
    item.addEventListener('click', () => {
        currentMode = 'PLACING_MODE';
        selectedNodeCategory = item.dataset.category;
        selectedNodeType = item.dataset.type;
        selectedNodeLabel = item.dataset.label;

        ghostNode.textContent = selectedNodeLabel;
        ghostNode.className = `ghost-node ${selectedNodeCategory}`;
        ghostNode.style.display = 'block';
        placementBanner.style.display = 'flex';

        //< 다른 사이드바 아이템에 남아있던 선택 표시(selecting) 초기화 후 현재 것만 표시
        nodeItems.forEach(node => node.classList.remove('selecting'));
        connectionItems.forEach(conn => conn.classList.remove('selecting'));
        item.classList.add('selecting');
    });
});

//< 고스트 노드가 마우스 커서를 따라다니게 처리
document.addEventListener('mousemove', (e) => {
    if (currentMode === 'PLACING_MODE') {
        ghostNode.style.left = e.clientX + 'px';
        ghostNode.style.top = e.clientY + 'px';
    }
});

//< 캔버스 클릭 시: 현재 모드에 따라 "노드 선택 해제" 또는 "노드 생성"을 수행
canvasArea.addEventListener('click', (e) => {

    //< IDLE 모드에서 노드가 아닌 빈 공간을 클릭하면 선택된 노드 해제
    if (currentMode === 'IDLE') {
        if (!e.target.closest('.placed-node') && selectedPlacedNode) {
            selectedPlacedNode.classList.remove('selected');
            selectedPlacedNode = null;
        }
    }

    //< PLACING_MODE일 때 클릭한 위치에 실제 노드(div)를 생성
    if (currentMode === 'PLACING_MODE') {
        const newNode = document.createElement('div');
        newNode.className = `placed-node ${selectedNodeCategory}`
        newNode.textContent = selectedNodeLabel;

        const canvasRect = canvasArea.getBoundingClientRect();
        const x = e.clientX - canvasRect.left;
        const y = e.clientY - canvasRect.top;

        newNode.style.left = x + 'px';
        newNode.style.top = y + 'px';
        newNode.dataset.category = selectedNodeCategory;
        newNode.setAttribute('data-node-id', nextNodeId);
        newNode.dataset.nodeId = nextNodeId;

        //< 백엔드 저장용 노드 데이터 등록 (임시 음수 ID 발급)
    const nodeData = {
            tempNodeId: nextNodeId,
            nodeName: selectedNodeLabel,
            nodeType: selectedNodeType.toUpperCase(),
            cooldownSec: 60,
            nodeConfig: buildDefaultNodeConfig(selectedNodeType.toUpperCase(), x, y),
            configValidated: false,
            validatedConfigSignature: null
        };
        placedNodes.push(nodeData);
        nextNodeId--;

        //< 노드 클릭 시 선택 처리 (IDLE 모드에서만 동작 → 배치/연결 중엔 무시)
        newNode.addEventListener('click', (evt) => {
            if (currentMode === 'IDLE') {
                evt.stopPropagation();
                if (suppressNextNodeClick) {
                    suppressNextNodeClick = false;
                    return;
                }
                if (selectedPlacedNode) {
                    selectedPlacedNode.classList.remove('selected');
                }
                selectedPlacedNode = newNode;
                newNode.classList.add('selected');
                openNodeConfigModal(parseInt(newNode.dataset.nodeId, 10));
            }
        });

        //< 노드 드래그 시작 (mousedown 시점에 오프셋 계산)
        newNode.addEventListener('mousedown', (evt) => {
            //!! node-handle 위에서 mousedown 하면 "커넥션 그리기"와 동작이 겹치므로
            //!! 여기서는 handle 클릭인 경우 드래그 로직을 타지 않도록 제외 처리
            if (evt.target.classList.contains('node-handle')) return;

            if (currentMode === 'IDLE') {
                evt.preventDefault(); // 브라우저 텍스트 선택(블록 지정) 방지
                evt.stopPropagation();

                isDraggingNode = true;
                draggedNode = newNode;
                didDragNode = false;

                const cRect = canvasArea.getBoundingClientRect();
                const mouseX = evt.clientX - cRect.left;
                const mouseY = evt.clientY - cRect.top;
                const nodeCenterX = parseFloat(newNode.style.left);
                const nodeCenterY = parseFloat(newNode.style.top);

                //< 마우스 클릭 지점과 노드 좌표 사이의 오프셋을 미리 저장해둬야
                //< 드래그 시 노드가 마우스 위치로 "순간이동"하지 않고 자연스럽게 따라옴
                dragOffsetX = mouseX - nodeCenterX;
                dragOffsetY = mouseY - nodeCenterY;

                newNode.style.cursor = 'grabbing';
            }
        });

        //< 노드 상하좌우 4방향에 커넥션 연결용 handle 생성
        const handlePositions = ['top', 'bottom', 'left', 'right'];

        handlePositions.forEach(pos => {
            const handle = document.createElement('div');
            handle.className = `node-handle ${pos}`;

            //< handle을 mousedown하면 커넥션(선) 그리기 시작 (CONNECTING_MODE 한정)
            handle.addEventListener('mousedown', (evt) => {
                if (currentMode === 'CONNECTING_MODE' && !isDrawing) {
                    evt.stopPropagation();
                    isDrawing = true;
                    startNode = newNode;

                    tempLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                    let lineColor = '#B0B4BA';
                    let arrowId = '';

                    //< 선택된 커넥션 타입(true/false/null)에 따라 선 색상 + 화살표 마커 결정
                    if (selectedConnectionType === 'true') { lineColor = '#33C09C'; arrowId = 'url(#arrow-true)'; }
                    else if (selectedConnectionType === 'false') { lineColor = '#EA5C2A'; arrowId = 'url(#arrow-false)'; }
                    else if (selectedConnectionType === 'null') { lineColor = '#E7C21F'; arrowId = 'url(#arrow-null)'; }

                    tempLine.setAttribute('stroke', lineColor);
                    tempLine.setAttribute('stroke-width', '3');
                    tempLine.setAttribute('stroke-dasharray', '5,5');   //< 그리는 중임을 표시하는 점선
                    tempLine.setAttribute('marker-end', arrowId);

                    //< 선의 시작점 = 클릭한 handle의 중심 좌표
                    const handleRect = handle.getBoundingClientRect();
                    const canvasR = canvasArea.getBoundingClientRect();
                    const startX = handleRect.left + (handleRect.width / 2) - canvasR.left;
                    const startY = handleRect.top + (handleRect.height / 2) - canvasR.top;

                    tempLine.setAttribute('x1', startX);
                    tempLine.setAttribute('y1', startY);
                    tempLine.setAttribute('x2', startX);
                    tempLine.setAttribute('y2', startY);

                    connectionSvg.appendChild(tempLine);
                }
            })
            newNode.appendChild(handle);
        });

        canvasArea.appendChild(newNode);

        //< 노드 하나 배치 완료 → 자동으로 IDLE 모드로 복귀 (연속 배치는 지원 안 함)
        currentMode = 'IDLE';
        ghostNode.style.display = 'none';
        canvasHint.style.display = 'none';
        placementBanner.style.display = 'none';
        nodeItems.forEach(node => node.classList.remove('selecting'));
        connectionItems.forEach(conn => conn.classList.remove('selecting'));
    }
});


//< ================================================================
//< [ 블록 ] 모드 취소 (배치/연결 취소) 로직
//< 배너의 취소 버튼과 ESC 키, 두 가지 경로로 진입 가능
//< ================================================================

//< 배너의 "취소" 버튼 클릭 시
placementCancelBtn.addEventListener('click', () => {
    //< 선을 긋는 도중이었다면 임시 선부터 제거
    if (isDrawing && tempLine) {
        tempLine.remove();
        isDrawing = false;
        startNode = null;
        tempLine = null;
    }

    if (currentMode === 'PLACING_MODE' || currentMode === 'CONNECTING_MODE') {
        currentMode = 'IDLE';
        ghostNode.style.display = 'none';
        placementBanner.style.display = 'none';
        nodeItems.forEach(node => node.classList.remove('selecting'));
        connectionItems.forEach(conn => conn.classList.remove('selecting'));
        canvasArea.classList.remove('placing');
    }
});

//< ESC 키: 취소 버튼과 동일한 동작 + "선택된 노드 삭제" 기능까지 한 번에 처리
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        if (nodeConfigModal.classList.contains('active')) {
            closeNodeConfigModal();
            return;
        }

        //< 1) 선을 긋는 중이었다면 취소
        if (isDrawing && tempLine) {
            tempLine.remove();
            isDrawing = false;
            startNode = null;
            tempLine = null;
        }

        //< 2) 배치/연결 모드였다면 IDLE로 복귀
        if (currentMode === 'PLACING_MODE' || currentMode === 'CONNECTING_MODE') {
            currentMode = 'IDLE';
            ghostNode.style.display = 'none';
            placementBanner.style.display = 'none';
            nodeItems.forEach(node => node.classList.remove('selecting'));
            connectionItems.forEach(conn => conn.classList.remove('selecting'));
            canvasArea.classList.remove('placing');
        }

        //< 3) IDLE 상태에서 노드가 선택돼 있었다면 → 노드 + 관련 커넥션까지 함께 삭제
        if (currentMode === 'IDLE' && selectedPlacedNode) {
            const nodeIdToDelete = parseInt(selectedPlacedNode.dataset.nodeId, 10);

            selectedPlacedNode.remove();
            placedNodes = placedNodes.filter(node => node.tempNodeId !== nodeIdToDelete);

            //!! 노드를 지울 때 그 노드에 연결된 선(들)도 반드시 같이 지워야
            //!! "허공에 붕 뜬 선"이 남지 않음 → 화면 요소 + 저장용 데이터 둘 다 정리
            const connectedLines = document.querySelectorAll(
                `line[data-source-id="${nodeIdToDelete}"], line[data-target-id="${nodeIdToDelete}"]`
            );
            connectedLines.forEach(line => line.remove());

            placedConnections = placedConnections.filter(conn =>
                conn.sourceNodeId !== nodeIdToDelete && conn.targetNodeId !== nodeIdToDelete
            );
            selectedPlacedNode = null;

            //!! 노드를 삭제하여 배치된 노드가 0개가 되면 캔버스 힌트 문구 복원
            if (placedNodes.length === 0) {
                canvasHint.style.display = 'block';
            }
        }
    }
});


//< ================================================================
//< [ 블록 ] 커넥션(선 연결) 모드 진입 로직
//< 사이드바에서 true/false/null 커넥션 타입을 클릭하면 CONNECTING_MODE로 전환
//< ================================================================
connectionItems.forEach(item => {
    item.addEventListener('click', () => {
        currentMode = 'CONNECTING_MODE';
        selectedConnectionType = item.dataset.type;
        placementBannerText.textContent = '연결할 시작 노드를 클릭하세요. (취소 : ESC)';
        placementBanner.style.display = 'flex';
        canvasArea.classList.add('placing');
        nodeItems.forEach(node => node.classList.remove('selecting'));
        connectionItems.forEach(conn => conn.classList.remove('selecting'));
        item.classList.add('selecting');
    });
});


//< ================================================================
//< [ 블록 ] 실시간 마우스 이동 처리
//< (1) 선을 긋는 중이면 임시선의 끝점을 마우스 위치로 갱신
//< (2) 노드를 드래그하는 중이면 노드 위치 + 연결된 선을 함께 갱신
//< 하나의 mousemove 리스너에서 두 기능을 같이 처리하고 있음 (동시에 발생 X)
//< ================================================================
document.addEventListener('mousemove', (e) => {

    //< 1. 선 그리기 중 → 임시선의 끝점(x2, y2)만 마우스 좌표로 갱신
    if (isDrawing && tempLine)  {
        const canvasRect = canvasArea.getBoundingClientRect();
        const currentX = e.clientX - canvasRect.left;
        const currentY = e.clientY - canvasRect.top;
        tempLine.setAttribute('x2', currentX);
        tempLine.setAttribute('y2', currentY);
    }

    //< 2. 노드 드래그 중 → 노드 위치를 마우스 좌표 - 오프셋으로 갱신
    if (isDraggingNode && draggedNode) {
        const canvasRect = canvasArea.getBoundingClientRect();
        const mouseX = e.clientX - canvasRect.left;
        const mouseY = e.clientY - canvasRect.top;

        draggedNode.style.left = (mouseX - dragOffsetX) + 'px';
        draggedNode.style.top = (mouseY - dragOffsetY) + 'px';

        //< 노드가 움직일 때마다 연결된 선도 실시간으로 같이 따라오게 처리
        updateConnectedLines(draggedNode.dataset.nodeId);
    }
});


//< ================================================================
//< [ 블록 ] 마우스 뗄 때 (노드 드래그 종료) 처리
//< 드래그가 끝난 최종 좌표를 저장용 데이터(placedNodes)에 반영
//< ================================================================
document.addEventListener('mouseup', () => {
    if (isDraggingNode && draggedNode) {
        draggedNode.style.cursor = 'grab';

        const nodeId = parseInt(draggedNode.dataset.nodeId, 10);

        //< 드래그가 끝난 최종 위치를 "장부(placedNodes)"에도 반영
        //< → 나중에 백엔드로 저장할 때 이 값을 그대로 사용
        const nodeData = placedNodes.find(n => n.tempNodeId === nodeId);
        if (nodeData) {
            nodeData.nodeConfig.x = parseFloat(draggedNode.style.left);
            nodeData.nodeConfig.y = parseFloat(draggedNode.style.top);
        }

        isDraggingNode = false;
        draggedNode = null;
    }
});


//< ================================================================
//< [ 블록 ] 선 긋기 완료 처리 (캔버스 위에서 mouseup 될 때)
//< 노드의 handle에서 시작해서 다른 노드 위로 드래그 앤 드롭하면 연결이 완성됨
//< ================================================================
canvasArea.addEventListener('mouseup', (e) => {
    if(isDrawing) {
        let targetNode = e.target.closest('.placed-node');

        //< 시작 노드와 다른 노드 위에서 손을 뗐을 때만 "연결 성공"으로 처리
        if (targetNode && startNode !== targetNode) {

            const startCategory = startNode.dataset.category;
            const targetCategory = targetNode.dataset.category;

            //!! 사용자가 "행동(action) 노드 → 조건(condition) 노드" 순서로 선을 그었더라도
            //!! 실제 로직 흐름상은 항상 "조건 → 행동" 순서가 맞아야 하므로
            //!! 여기서 시작/도착 노드를 강제로 뒤집어서 저장함
            if (startCategory === 'action' && targetCategory === 'condition') {
                const tempNode = startNode;
                startNode = targetNode;
                targetNode = tempNode;
            }

            const finalSourceId = parseInt(startNode.dataset.nodeId, 10);
            const finalTargetId = parseInt(targetNode.dataset.nodeId, 10);

            //!! [중복 연결 방지] 이미 두 노드 사이에 연결이 존재하는지 검사
            const isAlreadyConnected = placedConnections.some(conn =>
                conn.sourceNodeId === finalSourceId && conn.targetNodeId === finalTargetId
            );

            if (isAlreadyConnected) {
                tempLine.remove();
                isDrawing = false;
                startNode = null;
                tempLine = null;
                return;
            }

            //< updateConnectedLines 함수를 재사용하기 위해
            //< 먼저 data-source-id / data-target-id 속성을 심어놓음
            tempLine.setAttribute('data-source-id', finalSourceId);
            tempLine.setAttribute('data-target-id', finalTargetId);

            //< 가장 가까운 handle 좌표로 선을 재배치 (노드에 딱 붙게)
            updateConnectedLines(finalSourceId);

            //< 점선 → 실선으로 전환해서 "연결 완료" 상태를 시각적으로 표시
            tempLine.setAttribute('stroke-dasharray', '0');
            tempLine.classList.add('completed-line');

            //< 백엔드 저장용 커넥션 데이터 등록
            const connectionData = {
                sourceNodeId: finalSourceId,
                targetNodeId: finalTargetId,
                conditionResult: selectedConnectionType.toUpperCase()
            };
            placedConnections.push(connectionData);

            //!! 완성된 선은 더블클릭하면 바로 삭제됨 (별도 삭제 버튼 없이 이 방식만 존재)
            const currentLine = tempLine;
            currentLine.addEventListener('dblclick', (evt) => {
                evt.stopPropagation();
                currentLine.remove();
                placedConnections = placedConnections.filter(conn =>
                    !(conn.sourceNodeId === finalSourceId && conn.targetNodeId === finalTargetId)
                );
            });

            //< 연결 완료 → 그리기 관련 상태값 전부 초기화하고 IDLE로 복귀
            isDrawing = false;
            startNode = null;
            tempLine = null;
            currentMode = 'IDLE';
            placementBanner.style.display = 'none';
            canvasArea.classList.remove('placing');
            nodeItems.forEach(node => node.classList.remove('selecting'));
            connectionItems.forEach(conn => conn.classList.remove('selecting'));
        }
        //< 노드가 아닌 빈 공간에서 손을 뗐다면 → 연결 취소, 임시선 제거
        else if (!targetNode) {
            tempLine.remove();
            isDrawing = false;
            startNode = null;
            tempLine = null;
        }
    }
});


//< ================================================================
//< [ 블록 ] 플로우 저장 (Save) 버튼 클릭 처리
//< 백엔드가 지정한 JSON 생성 포맷 그대로 묶어서 전송 또는 출력
//< ================================================================
if (saveBtn) {
    saveBtn.addEventListener('click', () => {
        const flowPayload = {
            flowName: "CO2 임계치 알림 플로우",
            description: "CO2가 기준치 이상이면 텔레그램 알림 전송",
            isActive: true,
            nodes: placedNodes,
            connections: placedConnections
        };

        console.log("=== [백엔드 전송용 JSON Payload] ===");
        console.log(JSON.stringify(flowPayload, null, 2));

        // API 연동 시 fetch 예시:
        // fetch('/api/flows', {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(flowPayload)
        // });
    });
}