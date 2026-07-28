

const nodeToggle = document.getElementById('node-toggle');
const nodeGroup = document.getElementById('node-group');
const canvasArea = document.getElementById('canvas-area');
const canvasHint = document.getElementById('canvas-hint');
const ghostNode = document.getElementById('ghost-node');
const placementBanner = document.getElementById('placement-banner');
const placementBannerText = document.getElementById('placement-banner-text');
const placementCancelBtn = document.getElementById('placement-cancel');


const roomId = document.body.dataset.roomId;

let placedNodeCount = 0;


let placedNodes = [];
let placedConnections = [];

// ---------- 사이드바 Node 토글 (접기/펼치기) - 기존과 동일 ----------
nodeToggle.addEventListener('click', () => {
    nodeToggle.classList.toggle('open');
    nodeGroup.style.display = nodeGroup.style.display === 'none' ? 'block' : 'none';
});

// ---------- 배치 모드 상태 - 기존과 동일 ----------
let selectedType = null; // { category, type, label }

function enterPlacementMode(item) {
    document.querySelectorAll('.node-item.selecting').forEach(el => el.classList.remove('selecting'));

    item.classList.add('selecting');

    selectedType = {
        category: item.dataset.category,
        type: item.dataset.type,
        label: item.dataset.label
    };

    canvasArea.classList.add('placing');
    placementBannerText.textContent = `"${selectedType.label}" 노드를 배치할 위치를 캔버스에서 클릭하세요`;
    placementCancelBtn.style.display = 'inline-block';

    ghostNode.textContent = selectedType.label;
    ghostNode.className = 'ghost-node ' + selectedType.category;
    ghostNode.style.display = 'block';
}

function exitPlacementMode() {
    document.querySelectorAll('.node-item.selecting').forEach(el => el.classList.remove('selecting'));
    selectedType = null;

    canvasArea.classList.remove('placing');
    placementBannerText.textContent = '배치할 노드 종류를 선택하세요';
    placementCancelBtn.style.display = 'none';

    ghostNode.style.display = 'none';
}

document.querySelectorAll('.node-item:not(.static)').forEach(item => {
    item.addEventListener('click', () => {
        if (item.classList.contains('selecting')) {
            exitPlacementMode();
            return;
        }
        enterPlacementMode(item);
    });
});

placementCancelBtn.addEventListener('click', exitPlacementMode);

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && selectedType) {
        exitPlacementMode();
    }
});

document.addEventListener('mousemove', (e) => {
    if (!selectedType) return;
    ghostNode.style.left = e.clientX + 'px';
    ghostNode.style.top = e.clientY + 'px';
});

// ---------- 캔버스 클릭 시 노드 배치 - 기존과 동일 ----------
canvasArea.addEventListener('click', (e) => {
    if (!selectedType) return;
    if (e.target.closest('.placed-node')) return;

    const rect = canvasArea.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    createPlacedNode(selectedType, x, y);
    exitPlacementMode();
});


function createPlacedNode(nodeInfo, x, y) {
    placedNodeCount++;

    if (canvasHint) {
        canvasHint.remove();
    }

    const node = document.createElement('div');
    node.className = 'placed-node ' + nodeInfo.category;
    node.style.left = x + 'px';
    node.style.top = y + 'px';


    const tempId = 'temp-' + placedNodeCount;
    node.dataset.id = tempId;
    node.dataset.category = nodeInfo.category;
    node.dataset.type = nodeInfo.type;

    node.innerHTML = `
        ${nodeInfo.label}
        <div class="tooltip">${nodeInfo.category === 'condition' ? '조건 노드' : '행동 노드'} · ${nodeInfo.type}</div>
    `;

    canvasArea.appendChild(node);
    makeNodeDraggable(node);


    placedNodes.push({
        tempId: tempId,       // 서버로 보낼 때는 안 보낼 수도 있음(연결선 매핑용 내부값)
        category: nodeInfo.category,
        type: nodeInfo.type,
        x: x,
        y: y
    });
}

// ---------- 배치된 노드 드래그 이동 + 더블클릭 삭제 ----------
function makeNodeDraggable(node) {
    node.addEventListener('mousedown', (e) => {
        e.stopPropagation();

        const startX = e.clientX;
        const startY = e.clientY;
        const startLeft = parseFloat(node.style.left);
        const startTop = parseFloat(node.style.top);

        function onMouseMove(e) {
            node.style.left = (startLeft + (e.clientX - startX)) + 'px';
            node.style.top = (startTop + (e.clientY - startY)) + 'px';
        }

        function onMouseUp() {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);


            const target = placedNodes.find(n => n.tempId === node.dataset.id);
            if (target) {
                target.x = parseFloat(node.style.left);
                target.y = parseFloat(node.style.top);
            }
        }

        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    });

    // 더블클릭으로 삭제
    node.addEventListener('dblclick', (e) => {
        e.stopPropagation();


        placedNodes = placedNodes.filter(n => n.tempId !== node.dataset.id);

        // TODO: 이 노드와 연결된 connection들도 같이 정리해줘야 함
        // (연결선 UI가 아직 없어서 지금은 생략, 나중에 connection 구현 시 추가 필요)

        node.remove();
    });
}

async function saveFlow() {
    const flowName = document.getElementById('flow-name-input').value;
    const description = document.getElementById('flow-desc-input').value;

    // 필수값 검증 (백엔드 @NotBlank랑 맞춰서 프론트에서도 최소한의 체크)
    if (!flowName || flowName.trim() === '') {
        alert('플로우 이름을 입력해주세요.');
        return;
    }
    if (placedNodes.length === 0) {
        alert('노드를 하나 이상 배치해주세요.');
        return;
    }

    // FlowCreateRequest 스펙에 맞춰 body 구성
    const requestBody = {
        flowName: flowName,
        description: description,
        isActive: true,          // TODO: 활성화 여부 토글 UI 생기면 그 값으로 교체
        schedules: [],           // TODO: 스케줄 UI 아직 없어서 일단 빈 배열
        nodes: placedNodes.map(n => ({
            category: n.category,
            type: n.type,
            x: n.x,
            y: n.y
            // tempId는 서버 스펙에 없는 필드라 여기서는 빼고 보냄
        })),
        connections: placedConnections
    };

    try {
        const res = await fetch(`/api/rooms/${roomId}/flows`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!res.ok) {
            // 서버가 400/500 등 에러를 줬을 때
            console.error('플로우 저장 실패, status:', res.status);
            alert('플로우 저장에 실패했어요. 잠시 후 다시 시도해주세요.');
            return;
        }

        const data = await res.json(); // 201 CREATED { flow_id }
        console.log('생성된 flowId:', data.flowId);

        // 저장 성공 후 상세 페이지로 이동 (URL은 실제 라우팅에 맞게 수정 필요)
        window.location.href = `/rooms/${roomId}/flows/${data.flowId}`;

    } catch (err) {
        // 네트워크 에러 등
        console.error('플로우 저장 중 오류:', err);
        alert('네트워크 오류가 발생했어요.');
    }
}

// 저장 버튼에 이벤트 연결 (HTML에 해당 버튼이 있어야 동작함)
const saveFlowBtn = document.getElementById('save-flow-btn');
if (saveFlowBtn) {
    saveFlowBtn.addEventListener('click', saveFlow);
}