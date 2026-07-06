document.addEventListener("DOMContentLoaded", function () {

    // 시작일 선택 시 마감일의 최소 선택 가능일을 시작일로 제한 (네이티브 date input)
    const startInput = document.getElementById("startDate");
    const finishInput = document.getElementById("finishDate");

    startInput.addEventListener("change", function () {
        finishInput.min = startInput.value;
    });

	// 설정 모듈: 클릭해도 해제 안 되도록
    const settingsCheckbox = document.getElementById("moduleSettings");
    settingsCheckbox.addEventListener("click", function (e) {
        e.preventDefault();
        this.checked = true;
    });
    
	// 개요 모듈: 클릭해도 해제 안 되도록
    const overviewCheckbox = document.getElementById("moduleOverview");
    overviewCheckbox.addEventListener("click", function (e) {
        e.preventDefault();
        this.checked = true;
    });

 	// 전체선택
    document.getElementById("selectAll").addEventListener("click", function () {
        document.querySelectorAll('input[name="moduleNames"]')
            .forEach(cb => cb.checked = true);
    });

 	// 전체해제 (개요, 설정 제외)
    document.getElementById("deselectAll").addEventListener("click", function () {
        document.querySelectorAll('input[name="moduleNames"]')
            .forEach(cb => {
                if (cb.value !== "b1" && cb.value !== "b11") cb.checked = false;
            });
    });


    // 토스트 자동 제거
    const toastWrap = document.getElementById("toastWrap");
    if (toastWrap) {
        setTimeout(() => toastWrap.remove(), 3500);
    }

});