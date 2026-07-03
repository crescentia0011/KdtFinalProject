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

    const toast = document.getElementById("toast");
    if (toast && toast.textContent.trim() !== "") {
        toast.style.opacity = "1";
        toast.style.visibility = "visible";
        setTimeout(function () {
            toast.style.opacity = "0";
            toast.style.visibility = "hidden";
        }, 3000);
    }
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