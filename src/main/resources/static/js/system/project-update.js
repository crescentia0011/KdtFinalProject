document.addEventListener("DOMContentLoaded", function () {

    // 시작일/마감일 서로의 선택 가능 범위를 제한 (네이티브 date input)
    const startInput = document.getElementById("startDate");
    const finishInput = document.getElementById("finishDate");

    startInput.addEventListener("change", function () {
        finishInput.min = startInput.value;
    });
    finishInput.addEventListener("change", function () {
        startInput.max = finishInput.value;
    });

    // 페이지 진입 시 기존 값 기준으로도 범위 제한 적용
    if (startInput.value) finishInput.min = startInput.value;
    if (finishInput.value) startInput.max = finishInput.value;

    // 전체선택
    document.getElementById("selectAll").addEventListener("click", function () {
        document.querySelectorAll('input[name="moduleNames"]')
            .forEach(cb => cb.checked = true);
    });

    // 전체해제
    document.getElementById("deselectAll")
	.addEventListener("click", function () {
	    document.querySelectorAll('input[name="moduleNames"]')
	        .forEach(cb => {
	            if(cb.value !== "b1" && cb.value !== "b11"){
	                cb.checked = false;
	            }
	        });
	});

    // 폼 제출 시 최소 1개 체크 검증
    document.getElementById("projectForm").addEventListener("submit", function (e) {
        const checked = document.querySelectorAll('input[name="moduleNames"]:checked');
        if (checked.length === 0) {
            e.preventDefault();
            const wrap = document.createElement("div");
            wrap.className = "toast-wrap";
            wrap.innerHTML = '<div class="toast-msg toast-error">최소 1개 이상의 모듈을 선택해야 합니다.</div>';
            document.querySelector(".project-page").prepend(wrap);
            setTimeout(() => wrap.remove(), 3500);
        }
    });

    // 토스트 자동 제거
    const toastWrap = document.getElementById("toastWrap");
    if (toastWrap) {
        setTimeout(() => toastWrap.remove(), 3500);
    }

});