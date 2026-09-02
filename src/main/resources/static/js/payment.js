document.addEventListener('DOMContentLoaded', () => {
    const tossForm = document.getElementById('tossForm');
    const kakaoForm = document.getElementById('kakaoForm');

    const syncSelectedPlan = (form) => {
        const selected = document.querySelector('input[name="plan"]:checked');
        if (selected) {
            form.querySelector('input[name="plan"]').value = selected.value;
        }
    };

    tossForm.addEventListener('submit', () => syncSelectedPlan(tossForm));
    kakaoForm.addEventListener('submit', () => syncSelectedPlan(kakaoForm));
});
