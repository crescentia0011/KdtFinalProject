let isSubmitting = false;
document.getElementById('moduleSettingForm').addEventListener('submit', function (e) {
    if (isSubmitting) {
        e.preventDefault();
        return;
    }
    isSubmitting = true;
    document.querySelector('.btn-submit').disabled = true;
});

// 전체선택 / 전체해제
function toggleAllChecks(containerId, checked) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.querySelectorAll('input[type="checkbox"]:not(:disabled)').forEach(function (chk) {
        chk.checked = checked;
    });
}

// 토스트 알림
function showToast(msg, type) {
    var old = document.getElementById('_toastWrap');
    if (old) old.remove();
    var wrap = document.createElement('div');
    wrap.id = '_toastWrap';
    wrap.style.cssText = 'position:fixed;top:24px;left:50%;transform:translate(-50%,0);z-index:99999;';
    var box = document.createElement('div');
    box.style.cssText = 'padding:16px 28px;border-radius:8px;font-size:15px;font-weight:500;color:#fff;box-shadow:0 4px 20px rgba(0,0,0,0.25);text-align:center;min-width:220px;';
    box.style.backgroundColor = (type === 'error') ? '#e53e3e' : (type === 'info' ? '#3b82f6' : '#38a169');
    box.textContent = msg;
    wrap.appendChild(box);
    document.body.appendChild(wrap);
    setTimeout(function () {
        wrap.style.transition = 'opacity 0.4s';
        wrap.style.opacity = '0';
        setTimeout(function () { wrap.remove(); }, 400);
    }, 2500);
}

// 취소 버튼: 토스트 띄우고 약간의 딜레이 후 이동
document.getElementById('cancelBtn').addEventListener('click', function () {
    showToast('취소되었습니다.', 'info');
    setTimeout(function () {
        history.back();
    }, 600);
});

// 서버에서 내려준 결과 플래시 메시지 처리
window.addEventListener('DOMContentLoaded', function () {
    const toastType = /*[[${toastType}]]*/ null;
    const toastMessage = /*[[${toastMessage}]]*/ null;
    if (toastMessage) {
        showToast(toastMessage, toastType);
    }
});