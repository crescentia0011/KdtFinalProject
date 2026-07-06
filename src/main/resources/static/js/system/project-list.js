function initProjectList() {
    let deleteForm = null;

    document.querySelectorAll(".delete-project-btn").forEach(function (btn) {
        btn.addEventListener("click", function () {

            deleteForm = this.closest("td").querySelector(".delete-form");
            const title = this.dataset.projectTitle || "";
            const identifier = this.dataset.projectIdentifier || "";

            document.getElementById("modalProjectTitle").textContent = "[" + title + "]";

            const promptEl = document.getElementById("modalDeletePrompt");
            const confirmInput = document.getElementById("deleteConfirmationInput");
            const confirmBtn = document.getElementById("confirmDeleteBtn");

            promptEl.textContent = "삭제하려면 식별자 " + identifier + "을(를) 입력하세요.";
            confirmInput.value = "";
            confirmBtn.disabled = true;
            confirmBtn.style.background = "#fca5a5";
            confirmBtn.style.cursor = "not-allowed";

            // 실시간 입력값 비교 (식별자가 정확히 일치할 때만 삭제 버튼 활성화)
            confirmInput.oninput = function () {
                if (this.value.trim() === identifier.trim()) {
                    confirmBtn.disabled = false;
                    confirmBtn.style.background = "#ef4444";
                    confirmBtn.style.cursor = "pointer";
                } else {
                    confirmBtn.disabled = true;
                    confirmBtn.style.background = "#fca5a5";
                    confirmBtn.style.cursor = "not-allowed";
                }
            };

            document.getElementById("confirmModal").style.display = "flex";
        });
    });
    window.closeModal = function () {
        document.getElementById("confirmModal").style.display = "none";
    };

    window.submitDelete = function () {
        const confirmBtn = document.getElementById("confirmDeleteBtn");
        if (deleteForm && !confirmBtn.disabled) {
            document.getElementById("confirmModal").style.display = "none";
            deleteForm.submit();
        }
    };

    // 토스트
    const message = (typeof serverToastMessage !== 'undefined') ? serverToastMessage : '';
    if (message && message.trim() !== '') {
        const isError = (typeof serverToastType !== 'undefined') && serverToastType === 'error';
        showToast(message, isError);
    }
}

/* ══ 토스트 (task-detail.js와 동일한 방식으로 통일) ══ */
function showToast(message, isError) {
    let toastWrap = document.getElementById('dynamicToast');
    if (toastWrap) toastWrap.remove();

    toastWrap = document.createElement('div');
    toastWrap.id = 'dynamicToast';
    toastWrap.className = 'toast-wrap';

    toastWrap.innerHTML = '<div class="toast-msg ' + (isError ? 'toast-error' : 'toast-success') + '">' + message + '</div>';
    document.body.appendChild(toastWrap);

    setTimeout(function () {
        if (document.body.contains(toastWrap)) toastWrap.remove();
    }, 3500);
}

// scriptFragment 로드 완료 후 실행 보장
if (document.readyState === "complete") {
    initProjectList();
} else {
    window.addEventListener("load", initProjectList);
}

function toggleDropdown(el) {
    document.querySelectorAll('.dropdown-menu-box').forEach(menu => {
        if (menu !== el.nextElementSibling) {
            menu.classList.remove('show');
        }
    });

    el.nextElementSibling.classList.toggle('show');
}

document.addEventListener('click', function () {
    document.querySelectorAll('.dropdown-menu-box').forEach(menu => {
        menu.classList.remove('show');
    });
});