
//비밀번호 입력의 eye icon 깜빡깜빡 효과
const passwordInput = document.getElementById('password'); //html에서 id가 password인<input> 태그 전체
const eyeIcon = document.getElementById('eye-icon') //html에서ㅓ id가 eye-icon인것

eyeIcon.addEventListener('click', () => { //클릭이라는 이벤트가 생길때 밑에 적히는 코드가 실행됨

    const isPassword = passwordInput.type === 'password' //html에서 type이 password인것을 isPassword라고 부를건데, 그게 'password'라는 타입과 일치한지

    passwordInput.type = isPassword ? 'text' : 'password'; //(조건) ? "참일때값" : "거짓일때값";

    eyeIcon.src = isPassword
        ? '/photo/pw-eye-close.png'
        : '/photo/pw-eye.png';
})