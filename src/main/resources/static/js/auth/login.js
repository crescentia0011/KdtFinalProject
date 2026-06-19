document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.querySelector(".login-card form");
    const loginIdInput = document.getElementById("loginId");
    const saveIdCheckbox = document.getElementById("saveId");

    if (!loginForm || !loginIdInput || !saveIdCheckbox) {
        return;
    }

    // 저장된 로그인 아이디 조회
    const savedLoginId = localStorage.getItem("wepleSavedLoginId");

    if (savedLoginId) {
        // 저장된 아이디를 입력창에 세팅
        loginIdInput.value = savedLoginId;

        // 아이디 저장 체크박스 선택 상태로 변경
        saveIdCheckbox.checked = true;
    }

    loginForm.addEventListener("submit", function () {
        const loginId = loginIdInput.value.trim();

        if (saveIdCheckbox.checked && loginId) {
            // 아이디 저장 체크 시 로그인 아이디 저장
            localStorage.setItem("wepleSavedLoginId", loginId);
            return;
        }

        // 체크 해제 또는 빈 값이면 저장된 아이디 삭제
        localStorage.removeItem("wepleSavedLoginId");
    });
});