const openBtn = document.getElementById('openBtn');
const closeBtn = document.getElementById('closeBtn');
const modalOverlay = document.getElementById('modalOverlay');

const emptyState = document.getElementById('emptyState');
const teamListContainer = document.getElementById('teamListContainer');
const teamNameInput = document.getElementById('teamNameInput');
const teamDescInput = document.getElementById('teamDescInput');

const inviteCodeModal = document.getElementById('inviteCodeModal');
const inviteTeamName = document.getElementById('inviteTeamName');
const copyCodeBtn = document.getElementById('copyCodeBtn');
const inviteConfirmBtn = document.getElementById('inviteConfirmBtn');
const inviteCloseBtn = document.getElementById('inviteCloseBtn');
const codeSquares = document.querySelectorAll('.code-square');

const openJoinTeamBtn = document.getElementById('openJoinTeamBtn');
const joinTeamModal = document.getElementById('joinTeamModal');
const joinCodeInput = document.getElementById('joinCodeInput');
const joinTeamSubmitBtn = document.getElementById('joinTeamSubmitBtn');
const joinTeamCloseBtn = document.getElementById('joinTeamCloseBtn');

let currentInviteCode = '';

// DOM 로드 완료 시 팀 목록 조회
document.addEventListener('DOMContentLoaded', loadTeams);

// 1. 팀 생성 모달 열기
openBtn.addEventListener('click', () => {
    if (openBtn.dataset.role === 'NORMAL') {
        window.location.href = '/payment/plans';
        return;
    }
    modalOverlay.classList.add('active');
});

// 2. 초대 코드로 가입 모달 제어
openJoinTeamBtn.addEventListener('click', () => {
    joinTeamModal.classList.add('active');
    joinCodeInput.focus();
});

joinTeamCloseBtn.addEventListener('click', closeJoinTeamModal);

joinCodeInput.addEventListener('input', () => {
    joinCodeInput.value = joinCodeInput.value.replace(/\s/g, '').slice(0, 8);
});

joinTeamSubmitBtn.addEventListener('click', joinTeam);

// 3. 팀 생성 요청
closeBtn.addEventListener('click', async () => {
    const nameValue = teamNameInput.value.trim();
    const descValue = teamDescInput.value.trim();

    if (!nameValue) {
        alert('팀 이름을 입력해주세요!');
        return;
    }

    try {
        await requestJson('/api/front/teams', {
            method: 'POST',
            body: JSON.stringify({
                teamName: nameValue,
                description: descValue
            })
        });

        modalOverlay.classList.remove('active');
        teamNameInput.value = '';
        teamDescInput.value = '';
        await loadTeams();
    } catch (error) {
        if (error.status === 403) {
            window.location.href = '/payment/plans';
            return;
        }
        alert(error.message || '팀 생성에 실패했습니다.');
    }
});

// 4. 팀 목록 조회
async function loadTeams() {
    try {
        const page = await requestJson('/api/front/teams?size=50');
        renderTeams(page.content || []);
    } catch (error) {
        console.error(error);
        renderTeams([]);
    }
}

// 5. 팀 목록 렌더링
function renderTeams(teams) {
    teamListContainer.innerHTML = '';

    if (teams.length === 0) {
        emptyState.style.display = 'block';
        teamListContainer.style.display = 'none';
        return;
    }

    emptyState.style.display = 'none';
    teamListContainer.style.display = 'flex';
    teams.forEach(team => teamListContainer.appendChild(createTeamCard(team)));
}

// 역할 뱃지 생성 함수
function getRoleBadge(myRole) {
    const label = myRole === 'NORMAL' ? 'MEMBER' : myRole;
    const cssClass = 'role-badge-' + (myRole || 'normal').toLowerCase();
    return `<span class="role-badge ${cssClass}">${label}</span>`;
}

// 6. 팀 카드 생성 (초대코드 모달 버튼 + 대시보드/상세 이동 a태그 링크)
function createTeamCard(team) {
    const card = document.createElement('div');
    card.className = 'team-card';
    card.style.display = 'flex';
    card.style.justifyContent = 'space-between';
    card.style.alignItems = 'center';
    card.style.padding = '15px 30px';
    card.style.marginBottom = '18px';

    const canManageTeam = team.myRole === 'OWNER' || team.myRole === 'ADMIN';

    card.innerHTML = `
      <!-- 좌측 정보 영역 -->
      <div style="display: flex; flex-direction: column; gap: 12px; flex: 1; min-width: 0; padding-right: 24px;">
        
        <!-- 1줄: 팀명 + 뱃지 + 설명 -->
        <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
          <h3 style="margin: 0; font-size: 22px; font-weight: 700; color: #1e293b;">${escapeHtml(team.teamName)}</h3>
          ${getRoleBadge(team.myRole)}
          <span style="font-size: 14px; color: #64748b;">${escapeHtml(team.description || '')}</span>
        </div>

        <!-- 2줄: 주황색 구분선 -->
        <div style="height: 2px; background-color: #f97316; width: 95%; max-width: 1000px; border-radius: 2px;"></div>

        <!-- 3줄: 건물 및 강의실 개수 -->
        <div style="display: flex; align-items: center; gap: 24px; font-size: 14px; color: #475569; font-weight: 500;">
          <span><img src="/photo/icon/buidling-icon.png" alt="건물" class="meta-icon"> 건물 : 총 ${team.buildingCount ?? 0}개</span>
          <span><img src="/photo/icon/door-icon.png" alt="강의실" class="meta-icon classroom-icon-img"> 강의실 : 총 ${team.roomCount ?? 0}개</span>
        </div>

      </div>

      <!-- 우측 버튼 그룹: 초대 코드 생성 | 대시보드 | 팀 상세 정보 -->
      <div style="display: flex; align-items: center; gap: 10px; flex-shrink: 0;">
        ${canManageTeam ? `
          <button type="button" class="invite-gen-btn" 
                  style="border: 1.5px solid #f97316; background-color: #ffffff; color: #f97316; border-radius: 24px; padding: 0 18px; height: 40px; font-size: 14px; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; box-sizing: border-box;">
            초대 코드 생성
          </button>` : ''}
        <a href="/teams/${team.teamId}/dashboard" class="dashboard-btn" 
                  style="text-decoration: none; border: 1.5px solid #3b82f6; background-color: #ffffff; color: #3b82f6; border-radius: 24px; padding: 0 18px; height: 40px; font-size: 14px; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; box-sizing: border-box;">

          대시보드
        </a>
        <a href="/team-info/${team.teamId}" class="detail-btn" 
           style="text-decoration: none; border: none; background-color: #3b82f6; color: #ffffff; border-radius: 24px; padding: 0 20px; height: 40px; font-size: 14px; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; box-sizing: border-box; transition: background 0.2s;"
           onmouseover="this.style.backgroundColor='#2563eb'" onmouseout="this.style.backgroundColor='#3b82f6'">
          팀 상세 정보
        </a>
      </div>
    `;

    // 초대 코드 생성 모달 이벤트만 단독 바인딩
    const inviteBtn = card.querySelector('.invite-gen-btn');
    if (inviteBtn) {
        inviteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            openInviteModal(team.teamId, team.teamName);
        });
    }

    return card;
}

// 7. 초대 코드 모달 열기
async function openInviteModal(teamId, teamName) {
    inviteTeamName.textContent = teamName;
    inviteCodeModal.classList.add('active');

    currentInviteCode = '';
    codeSquares.forEach(square => square.textContent = '');

    try {
        const data = await requestJson(`/api/front/teams/${teamId}/invitation-codes`, {
            method: 'POST',
            body: JSON.stringify({})
        });

        currentInviteCode = data.code || '';
        for (let i = 0; i < codeSquares.length; i += 1) {
            codeSquares[i].textContent = currentInviteCode[i] || '';
        }
    } catch (error) {
        alert(error.message || '초대 코드 발급에 실패했습니다.');
        inviteCodeModal.classList.remove('active');
    }
}

copyCodeBtn.addEventListener('click', () => {
    if (!currentInviteCode) {
        alert('아직 코드가 발급되지 않았습니다.');
        return;
    }

    navigator.clipboard.writeText(currentInviteCode)
        .then(() => alert('초대 코드가 복사되었습니다!'))
        .catch(() => alert('복사에 실패했습니다.'));
});

inviteConfirmBtn.addEventListener('click', () => inviteCodeModal.classList.remove('active'));
inviteCloseBtn.addEventListener('click', () => inviteCodeModal.classList.remove('active'));

// 8. 초대 코드로 팀 가입
async function joinTeam() {
    const invitationCode = joinCodeInput.value.trim();

    if (!/^[2-9A-HJ-NP-Za-hj-np-z]{8}$/.test(invitationCode)) {
        alert('초대 코드는 8자리 영문자와 숫자로 입력해주세요.');
        joinCodeInput.focus();
        return;
    }

    try {
        joinTeamSubmitBtn.disabled = true;
        await requestJson('/api/front/teams/memberships', {
            method: 'POST',
            body: JSON.stringify({ invitationCode })
        });

        alert('팀에 가입되었습니다.');
        closeJoinTeamModal();
        await loadTeams();
    } catch (error) {
        alert(error.message || '팀 가입에 실패했습니다.');
    } finally {
        joinTeamSubmitBtn.disabled = false;
    }
}

function closeJoinTeamModal() {
    joinTeamModal.classList.remove('active');
    joinCodeInput.value = '';
}

// 9. Fetch 유틸 함수
async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        const text = await response.text();
        const error = new Error(text || `요청 실패 (${response.status})`);
        error.status = response.status;
        throw error;
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}