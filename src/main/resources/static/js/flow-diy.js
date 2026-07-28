//      ====== flow js 흐름 ======
//       *---- 사이드바 ----*
//   사이드바 메뉴 열고 닫기 (Node, Connection)

//       *---- Node ----*
// 사이드바에서 조건/행동 노드를 클릭하면 노드를 배치하는 모드로 바뀜
//  >  모드가 바뀜과 동시에 커서에 클릭한 노드가 불투명하게 따라다님 (고스트 노드) <미리보기>
//  >  배치하는 캔버스를 다시. 클릭하면 클릭된 해당 좌표에 실제 노드를 생성시킴
//  +  노드를 클릭 후, 다른 노드를 사이드바에서 클릭하거나 esc 키를 입력할 경우, 고스트 노드가 없어지면서 배치모드 종룟

//       *---- Connection ----*
// 사이드바에서 참,거짓, null 노드를 클릭하면 커넥션을 배치하는 모드로 바뀜 (마우스 커서를 + 십자가 모양으로 바뀌게)
// 캔버스에 이미 배치가 된 "노드"를 클릭하면 그 노드가 선의 시작점이 됨
//





// ---------- DOM 요소 참조 ----------
// 사이드바 토글 버튼들은 클래스(.toggle-btn)로 한 번에 잡기
const toggleButtons = document.querySelectorAll('.toggle-btn');

const canvasArea = document.getElementById('canvas-area');
const canvasHint = document.getElementById('canvas-hint');
const ghostNode = document.getElementById('ghost-node');
const placementBanner = document.getElementById('placement-banner');
const placementBannerText = document.getElementById('placement-banner-text');
const placementCancelBtn = document.getElementById('placement-cancel');

const roomId = document.body.dataset.roomId;

// 배치된 노드 개수 카운터 (임시)
let placedNodeCount = 0;

// 캔버스에 배치된 노드/연결선 데이터를 담는 배열
// 저장 버튼을 누르면 이 배열 그대로 서버로 전송.
let placedNodes = [];
let placedConnections = []; // TODO: 연결선(Connection) UI 구현 전까지는 비어있음


// ---------- 사이드바 Node / Connection 토글 (접기·펼치기) ----------
// 클릭하면 .open 클래스만 토글하고, 실제 배경색/화살표 회전/목록 표시-숨김은
// flow-diy.css 쪽에서 아래 규칙으로 자동 처리
toggleButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
        btn.classList.toggle('open');
    });
});


// ---------- 배치 모드 상태 관리 ----------
// selectedType: 사이드바에서 어떤 노드를 클릭해서 "배치 대기 중"인지 저장.
// null이면 배치 모드가 아님
let selectedType = null;

// 사이드바에서 노드 항목(조건/행동 노드)을 클릭했을 때 배치 모드
function enterPlacementMode(item) {
    // 전에 선택된 항목 해제
    document.querySelectorAll('.node-item.selecting').forEach(el => el.classList.remove('selecting'));

    // 지금 클릭한 항목
    item.classList.add('selecting');

    // data-category / data-type / data-label 값을 그대로 선택 상태로 저장
    selectedType = {
        category: item.dataset.category,
        type: item.dataset.type,
        label: item.dataset.label
    };

    // 캔버스 커서를 십자선으로 바꾸고, 상단 안내 배너에 안내 문구바꾸기
    canvasArea.classList.add('placing');
    placementBannerText.textContent = `"${selectedType.label}" 노드를 배치할 위치를 캔버스에서 클릭하세요`;
    placementCancelBtn.style.display = 'inline-block';

    // 마우스를 따라다니는 미리보기노드
    ghostNode.textContent = selectedType.label;
    ghostNode.className = 'ghost-node ' + selectedType.category;
    ghostNode.style.display = 'block';
}

// 배치 모드를 취소하고 원래 상태로 복귀 (ESC, 취소 버튼, 같은 항목 다시 클릭하기)
function exitPlacementMode() {
    document.querySelectorAll('.node-item.selecting').forEach(el => el.classList.remove('selecting'));
    selectedType = null;

    canvasArea.classList.remove('placing');
    placementBannerText.textContent = '배치할 노드 종류를 선택하세요';
    placementCancelBtn.style.display = 'none';

    ghostNode.style.display = 'none';
}

// 사이드바의 노드 항목들에 클릭 이벤트 추가하기
document.querySelectorAll('.node-item:not(.static)').forEach(item => {
    item.addEventListener('click', () => {
        // 이미 선택한 항목 다시 클릭하면 배치 모드 취소됨
        if (item.classList.contains('selecting')) {
            exitPlacementMode();
            return;
        }
        enterPlacementMode(item);
    });
});

// 상단 배너의 "취소(ESC)" 버튼
placementCancelBtn.addEventListener('click', exitPlacementMode);

// ESC 키로도 배치 모드 취소됨
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && selectedType) {
        exitPlacementMode();
    }
});

// 배치 모드 중에는 고스트 노드가 실제 마우스 커서를 따라다니게하기
document.addEventListener('mousemove', (e) => {
    if (!selectedType) return;
    ghostNode.style.left = e.clientX + 'px';
    ghostNode.style.top = e.clientY + 'px';
});


// ---------- 캔버스 클릭 시 실제 노드 배치 ----------
canvasArea.addEventListener('click', (e) => {
    if (!selectedType) return;
    // 이미 배치된 노드를 클릭한 경우(드래그/삭제 목적)는 새로 배치하지 않음
    if (e.target.closest('.placed-node')) return;

    // 캔버스 기준 좌표로 바꾸기
    //write 이게 왜 필요하지? DB에 저장 하기 위해?
    const rect = canvasArea.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    createPlacedNode(selectedType, x, y);
    exitPlacementMode();
});

// 캔버스 위에 실제 노드 생성하고 placedNodes에 데이터 추가
function createPlacedNode(nodeInfo, x, y) {
    placedNodeCount++;

    // 첫 노드가 배치되면 위 배너에 노드 없다는 문구 없애기
    if (canvasHint) {
        canvasHint.remove();
    }

    const node = document.createElement('div');
    node.className = 'placed-node ' + nodeInfo.category; // condition / action 색상 구분
    node.style.left = x + 'px';
    node.style.top = y + 'px';

    // 서버에 저장하기 전까지 임시로 쓸 프론트 전용 id
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

    // 저장 시 서버로 보낼 데이터 추가
    placedNodes.push({
        tempId: tempId,
        category: nodeInfo.category,
        type: nodeInfo.type,
        x: x,
        y: y
    });
}


// ---------- 배치된 노드 드래그 이동 + 더블클릭 삭제 ----------
function makeNodeDraggable(node) {
    // 마우스 드래그 위치 이동
    node.addEventListener('mousedown', (e) => {
        e.stopPropagation(); // 캔버스 클릭 이벤트(=새로운노드0) 번지는 것 방지

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

            // 드래그가 끝난 최종 좌표를 placedNodes 데이터
            const target = placedNodes.find(n => n.tempId === node.dataset.id);
            if (target) {
                target.x = parseFloat(node.style.left);
                target.y = parseFloat(node.style.top);
            }
        }

        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    });

    // 더블클릭하면 해당 노드 삭제
    node.addEventListener('dblclick', (e) => {
        e.stopPropagation();

        placedNodes = placedNodes.filter(n => n.tempId !== node.dataset.id);

        // TODO: 이 노드와 연결된 connection들도 같이 정리해줘야 함
        // (연결선 UI가 아직 없어서 지금은 생략, 나중에 connection 구현 시 추가 필요)

        node.remove();
    });
}



// ---------- 플로우 저장 (백엔드 REST API 호출) ----------
async function saveFlow() {
    const flowNameInput = document.getElementById('flow-name-input');
    const flowDescInput = document.getElementById('flow-desc-input');

    // 아직 HTML에 이름/설명 입력 필드가 없다면 null이 반환되므로 방어 처리
    const flowName = flowNameInput ? flowNameInput.value : '';
    const description = flowDescInput ? flowDescInput.value : '';

    // 필수값 검증 (백엔드 @NotBlank와 맞춰서 프론트에서도 최소한 체크)
    if (!flowName || flowName.trim() === '') {
        alert('플로우 이름을 입력해주세요.');
        return;
    }
    if (placedNodes.length === 0) {
        alert('노드를 하나 이상 배치해주세요.');
        return;
    }

    // FlowCreateRequest 스펙에 맞춰 요청 body 구성
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

        const data = await res.json(); // 201 CREATED { flowId }
        console.log('생성된 flowId:', data.flowId);

        // 저장 성공 후 상세 페이지로 이동 (실제 라우팅 경로에 맞게 수정 필요)
        window.location.href = `/rooms/${roomId}/flows/${data.flowId}`;

    } catch (err) {
        // 네트워크 에러 등
        console.error('플로우 저장 중 오류:', err);
        alert('네트워크 오류가 발생했어요.');
    }
}

// 사이드바 "저장" 버튼에 이벤트 연결
// 지금 보내주신 html의 저장 버튼(<button class="save-btn">저장</button>)엔 id가 없어서
// 아래 코드는 버튼을 못 찾고 조용히 넘어감. 실제로 저장 기능을 붙이려면
// <button class="save-btn" id="save-flow-btn">저장</button> 처럼 id를 추가해야 함.
const saveFlowBtn = document.getElementById('save-flow-btn');
if (saveFlowBtn) {
    saveFlowBtn.addEventListener('click', saveFlow);
}