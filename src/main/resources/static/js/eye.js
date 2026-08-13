// 비밀번호 입력의 eye icon 토글 효과 (여러 개 지원)
const eyeIcons = document.querySelectorAll('.eye-icon');

eyeIcons.forEach((eyeIcon) => {
    // 아이콘 바로 앞(형제)에 있는 input을 찾음
    const passwordInput = eyeIcon.previousElementSibling;

    eyeIcon.addEventListener('click', () => {
        const isPassword = passwordInput.type === 'password';

        passwordInput.type = isPassword ? 'text' : 'password';

        eyeIcon.src = isPassword
            ? '/photo/icon/pw-eye-close.png'
            : '/photo/icon/pw-eye.png';
    });
});