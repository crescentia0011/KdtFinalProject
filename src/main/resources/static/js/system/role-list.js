function initRoleList() {
    let deleteForm = null;

    document.querySelectorAll(".delete-role-btn").forEach(function (btn) {
        btn.addEventListener("click", function () {
            deleteForm = this.closest("td").querySelector(".delete-form");
            document.getElementById("confirmModal").style.display = "flex";
        });
    });

    window.closeModal = function () {
        document.getElementById("confirmModal").style.display = "none";
    };
    window.submitDelete = function () {
        if (deleteForm) {
            document.getElementById("confirmModal").style.display = "none";
            deleteForm.submit();
        }
    };

    const message = (typeof serverToastMessage !== 'undefined') ? serverToastMessage : '';
    if (message && message.trim() !== '') {
        var isError = (typeof serverToastType !== 'undefined') && serverToastType === 'error';
        showToast(message, isError);
    }
}

/* ══ 토스트 (task-detail.js와 동일한 방식으로 통일) ══ */
function showToast(message, isError) {
    var toastWrap = document.getElementById('dynamicToast');
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

if (document.readyState === "complete") { initRoleList(); }
else { window.addEventListener("load", initRoleList); }

function toggleDropdown(el) {
    document.querySelectorAll('.dropdown-menu-box').forEach(function (menu) {
        if (menu !== el.nextElementSibling) menu.classList.remove('show');
    });
    el.nextElementSibling.classList.toggle('show');
}

document.addEventListener('click', function () {
    document.querySelectorAll('.dropdown-menu-box').forEach(function (menu) {
        menu.classList.remove('show');
    });
});