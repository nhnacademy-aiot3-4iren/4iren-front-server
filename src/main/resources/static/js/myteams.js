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

document.addEventListener('DOMContentLoaded', loadTeams);

openBtn.addEventListener('click', () => modalOverlay.classList.add('active'));

openJoinTeamBtn.addEventListener('click', () => {
    joinTeamModal.classList.add('active');
    joinCodeInput.focus();
});

joinTeamCloseBtn.addEventListener('click', closeJoinTeamModal);

joinCodeInput.addEventListener('input', () => {
    joinCodeInput.value = joinCodeInput.value.replace(/\s/g, '').slice(0, 8);
});

joinTeamSubmitBtn.addEventListener('click', joinTeam);

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
        alert(error.message || '팀 생성에 실패했습니다.');
    }
});

async function loadTeams() {
    try {
        const page = await requestJson('/api/front/teams?size=50');
        renderTeams(page.content || []);
    } catch (error) {
        console.error(error);
        renderTeams([]);
    }
}

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

// 역할값 → 뱃지에 쓸 라벨/클래스 매핑
// team.myRole은 "OWNER" / "ADMIN" / "NORMAL" 중 하나로 내려옴 (TeamRole enum)
// 화면 표기는 일반 유저를 "MEMBER"로 부르기로 했으므로 NORMAL만 라벨을 바꿔줌
function getRoleBadge(myRole) {
    const label = myRole === 'NORMAL' ? 'MEMBER' : myRole;
    const cssClass = 'role-badge-' + (myRole || 'normal').toLowerCase();
    return `<span class="role-badge ${cssClass}">${label}</span>`;
}

function createTeamCard(team) {
    const card = document.createElement('div');
    card.className = 'team-card';
    const canManageTeam = team.myRole === 'OWNER' || team.myRole === 'ADMIN';

    card.innerHTML = `
      <div class="team-info">
        <div class="team-header-row">
          <span class="team-card-name">${escapeHtml(team.teamName || '')}</span>
          ${getRoleBadge(team.myRole)}
          <span class="team-card-desc">${escapeHtml(team.description || '')}</span>
        </div>
        <div class="team-meta-row">
          <span><img src="/photo/icon/buidling-icon.png" alt="건물" class="meta-icon"> 건물 : 총 ${team.buildingCount ?? 0}개</span>
          <span><img src="/photo/icon/door-icon.png" alt="강의실" class="meta-icon classroom-icon-img"> 강의실 : 총 ${team.roomCount ?? 0}개</span>
        </div>
      </div>
      <div class="team-actions">
        ${canManageTeam ? '<button class="btn-outline-orange invite-gen-btn" type="button">초대 코드 생성</button>' : ''}
        <button class="btn-solid-blue detail-btn" type="button">팀 상세 정보</button>
      </div>
    `;

    const inviteGenBtn = card.querySelector('.invite-gen-btn');
    if (inviteGenBtn) {
        inviteGenBtn.addEventListener('click', () => {
            openInviteModal(team.teamId, team.teamName);
        });
    }
    card.querySelector('.detail-btn').addEventListener('click', () => {
        window.location.href = `/team-info/${team.teamId}`;
    });

    return card;
}

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
        throw new Error(text || `요청 실패 (${response.status})`);
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

