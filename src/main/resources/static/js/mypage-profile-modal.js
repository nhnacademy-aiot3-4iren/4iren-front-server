// mypage-profile-modal.js
// "Setting" 버튼 클릭 -> 수정 팝업 오픈 / 저장 처리
// 비밀번호 눈 아이콘 토글은 기존 /js/eye.js 가 처리한다고 가정 (동일한 .eye-icon 마크업 사용)
// PROFILE_UPDATE_URL 은 실제 API 경로에 맞게 수정하세요.

document.addEventListener('DOMContentLoaded', () => {
    const PROFILE_UPDATE_URL = '/api/mypage/profile'; // TODO: 실제 API 경로로 교체

    const openBtn = document.getElementById('openEditModalBtn');
    const dialog = document.getElementById('editProfileModal');
    const closeBtn = document.getElementById('editModalCloseBtn');
    const cancelBtn = document.getElementById('editModalCancelBtn');
    const form = document.getElementById('editProfileForm');
    const errorEl = document.getElementById('editFormError');
    const saveBtn = document.getElementById('editModalSaveBtn');

    const usernameInput = document.getElementById('editUsername');
    const emailInput = document.getElementById('editEmail');
    const passwordInput = document.getElementById('editPassword');
    const passwordCheckInput = document.getElementById('editPasswordCheck');

    // 저장 성공 시 프로필 카드(읽기 전용)도 같이 갱신
    const displayUsername = document.getElementById('username');
    const displayEmail = document.getElementById('email');

    function openModal() {
        errorEl.textContent = '';
        passwordInput.value = '';
        passwordCheckInput.value = '';
        dialog.showModal(); // 포커스 트랩 / 배경 inert 처리는 브라우저가 자동으로 함
        usernameInput.focus();
    }

    function closeModal() {
        dialog.close(); // 닫을 때 포커스는 브라우저가 열기 전 요소로 자동 복원
    }

    openBtn.addEventListener('click', openModal);
    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);

    // dialog 바깥(backdrop) 클릭 시 닫기 — backdrop을 클릭하면 이벤트 target이 dialog 자신이 됨
    dialog.addEventListener('click', (e) => {
        if (e.target === dialog) closeModal();
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        errorEl.textContent = '';

        const name = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const passwordCheck = passwordCheckInput.value;

        if (!name || !email) {
            errorEl.textContent = '사용자 이름과 e-mail을 입력해 주세요.';
            return;
        }

        // 비밀번호는 입력했을 때만 변경 (비워두면 기존 비밀번호 유지)
        if (password || passwordCheck) {
            if (password.length < 8) {
                errorEl.textContent = '비밀번호는 8자 이상이어야 합니다.';
                return;
            }
            if (password !== passwordCheck) {
                errorEl.textContent = '비밀번호가 일치하지 않습니다.';
                return;
            }
        }

        const payload = { name, email };
        if (password) payload.password = password;

        saveBtn.disabled = true;
        saveBtn.textContent = '저장 중...';

        try {
            const res = await fetch(PROFILE_UPDATE_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                throw new Error(data.message || '수정 중 오류가 발생했습니다.');
            }

            // 읽기 전용 카드 값 갱신
            if (displayUsername) displayUsername.value = name;
            if (displayEmail) displayEmail.value = email;

            closeModal();
        } catch (err) {
            errorEl.textContent = err.message || '수정 중 오류가 발생했습니다.';
        } finally {
            saveBtn.disabled = false;
            saveBtn.textContent = '저장';
        }
    });

    // 회원 탈퇴 로직
    const withdrawBtn = document.getElementById('withdrawBtn');
    if (withdrawBtn) {
        withdrawBtn.addEventListener('click', async () => {
            if (confirm('정말로 회원 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                try {
                    const res = await fetch(PROFILE_UPDATE_URL, {
                        method: 'DELETE'
                    });
                    if (res.ok) {
                        alert('회원 탈퇴가 완료되었습니다.');
                        window.location.href = '/login';
                    } else {
                        const data = await res.json().catch(() => ({}));
                        alert(data.message || '탈퇴 처리에 실패했습니다.');
                    }
                } catch (err) {
                    alert('오류가 발생했습니다.');
                }
            }
        });
    }
});