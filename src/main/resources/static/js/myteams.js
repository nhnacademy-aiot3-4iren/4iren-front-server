// ==========================================
//  <팀 생성 팝업 변수>
// ==========================================
const openBtn = document.getElementById('openBtn');
const closeBtn = document.getElementById('closeBtn');
const modalOverlay = document.getElementById('modalOverlay');

const emptyState = document.getElementById('emptyState');
const teamListContainer = document.getElementById('teamListContainer');
const teamNameInput = document.getElementById('teamNameInput');
const teamDescInput = document.getElementById('teamDescInput');

// ==========================================
// <팀 초대 팝업 변수>
// ==========================================
const inviteCodeModal = document.getElementById('inviteCodeModal');
const inviteTeamName = document.getElementById('inviteTeamName');
const copyCodeBtn = document.getElementById('copyCodeBtn');
const inviteConfirmBtn = document.getElementById('inviteConfirmBtn');
const inviteCloseBtn = document.getElementById('inviteCloseBtn');
const codeSquares = document.querySelectorAll('.code-square');

// 현재 발급된 코드를 저장할 변수
let currentInviteCode = "";

// ==========================================
// <팀 생성 팝업 열고 닫기>
// ==========================================
openBtn.addEventListener('click', () => modalOverlay.classList.add('active'));

closeBtn.addEventListener('click', () => {
    const nameValue = teamNameInput.value.trim();
    const descValue = teamDescInput.value.trim();

    if (!nameValue) {
        alert("팀 이름을 입력해주세요!");
        return;
    }

    emptyState.style.display = 'none';
    teamListContainer.style.display = 'flex';

    const newTeamCard = document.createElement('div');
    newTeamCard.className = 'team-card';
    newTeamCard.innerHTML = `
      <div class="team-info">
        <div class="team-header-row">
          <span class="team-card-name">${nameValue}</span>
          <span class="team-card-desc">${descValue}</span>
        </div>
        <div class="team-meta-row">
          <span><img src="/photo/icon/buidling-icon.png" alt="건물" class="meta-icon"> 건물 : 존재하지 않음</span>
          <span><img src="/photo/icon/door-icon.png" alt="강의실" class="meta-icon classroom-icon-img"> 강의실 : 존재하지 않음</span>
        </div>
      </div>
      <div class="team-actions">
        <button class="btn-outline-orange invite-gen-btn">초대 코드 생성</button>
        <button class="btn-solid-blue" onclick="window.location.href='/team-info?name=' + encodeURIComponent('${nameValue}') + '&desc=' + encodeURIComponent('${descValue}')">팀 상세 정보</button>
      </div>
    `;

    // !! 방금 생성한 카드의 '초대 코드 생성' 버튼에 이벤트 생성
    const inviteGenBtn = newTeamCard.querySelector('.invite-gen-btn');
    inviteGenBtn.addEventListener('click', () => {
        openInviteModal(nameValue); // 팀 이름을 모달에 넘겨주며 열기
    });

    teamListContainer.appendChild(newTeamCard);

    modalOverlay.classList.remove('active');
    teamNameInput.value = '';
    teamDescInput.value = '';
});

// ==========================================
// <팀 초대 팝업 기능>
// ==========================================

// 팝업열기
function openInviteModal(teamName) {
    // 팝업 <>에 팀 이름 넣기
    inviteTeamName.textContent = teamName;

    // 팝업 띄우기 (배경 블러)
    inviteCodeModal.classList.add('active');

    // 백엔드 연동 전까지 빈칸으로 두기 위해 값 비우기
    currentInviteCode = "";
    codeSquares.forEach(square => square.textContent = "");

    // ★ 백엔드 연동
    /*
    fetch('/api/team/generate-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ teamName: teamName })
    })
    .then(response => response.json())
    .then(data => {
        currentInviteCode = data.code;
        for (let i = 0; i < 8; i++) {
            codeSquares[i].textContent = currentInviteCode[i] || "";
        }
    })
    .catch(error => console.error('Error:', error));
    */
}

// 복사 버튼
copyCodeBtn.addEventListener('click', () => {
    // 코드가 비어있으면(아직 백엔드 연동 안됨) 경고창 띄우기
    if (!currentInviteCode) {
        alert("아직 코드가 발급되지 않았습니다. (백엔드 연동 필요)");
        return;
    }

    // 클립보드에 복사
    navigator.clipboard.writeText(currentInviteCode).then(() => {
        alert("초대 코드가 복사되었습니다!");
    }).catch(err => {
        alert("복사에 실패했습니다.");
    });
});

// 확인 & 창 닫기 버튼 누르면 팝업 닫기
inviteConfirmBtn.addEventListener('click', () => inviteCodeModal.classList.remove('active'));
inviteCloseBtn.addEventListener('click', () => inviteCodeModal.classList.remove('active'));
