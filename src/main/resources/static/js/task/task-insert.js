// 제이쿼리 에러 강제 우회 방어벽 검토후 필요없으면 삭제
window.onerror = function(message, source, lineno, colno, error) {
    if (message && (message.indexOf('indexOf') !== -1 || (source && (source.indexOf('jquery') !== -1 || source.indexOf('custom') !== -1)))) {
        return true; 
    }
};

//상위 일감 찾는 검색창 함수
document.addEventListener("DOMContentLoaded", function() {
    // HTML 화면단에서 전역 바인딩한 부모 일감 리스트를 가져옵니다.
    const parentTasks = window.globalParentTaskList || [];

    const searchInput = document.getElementById("parentTaskSearch");
    const hiddenInput = document.getElementById("parentTaskId");
    const autocompleteList = document.getElementById("autocomplete-list");

    if (!searchInput || !autocompleteList) return;

    // 검색창 입력 이벤트
    searchInput.addEventListener("input", function(e) {
        e.stopPropagation(); 
        
        const val = this.value.trim().toLowerCase();
        autocompleteList.innerHTML = ""; 

        if (!val) {
            autocompleteList.style.display = "none";
            hiddenInput.value = ""; 
            return;
        }

        const filtered = parentTasks.filter(task => {
            if (task && task.taskTitle) {
                const titleStr = String(task.taskTitle).toLowerCase();
                return titleStr.includes(val);
            }
            return false;
        });

        if (filtered.length > 0) {
            let htmlContent = "";
            
            filtered.forEach(task => {
                htmlContent += `
                    <button type="button" 
                            class="list-group-item list-group-item-action small text-start py-2 parent-task-item" 
                            style="cursor: pointer; background-color: #ffffff; border-top: none; border-left: 1px solid #dee2e6; border-right: 1px solid #dee2e6; border-bottom: 1px solid #dee2e6;"
                            data-id="${task.taskId}" 
                            data-title="${task.taskTitle}">
                        ${task.taskTitle}
                    </button>
                `;
            });
            
            autocompleteList.innerHTML = htmlContent;
            
            const firstItem = autocompleteList.querySelector(".parent-task-item");
            if (firstItem) firstItem.style.borderTop = "1px solid #dee2e6";
            
            autocompleteList.style.display = "block"; 
            
            const items = autocompleteList.querySelectorAll(".parent-task-item");
            items.forEach(item => {
                item.addEventListener("click", function(clickEvent) {
                    clickEvent.stopPropagation(); 
                    
                    searchInput.value = this.getAttribute("data-title");  
                    hiddenInput.value = this.getAttribute("data-id");    
                    autocompleteList.style.display = "none"; 
                });
            });
            
        } else {
            autocompleteList.style.display = "none"; 
        }
    });

    document.addEventListener("click", function(e) {
        if (e.target !== searchInput && e.target !== autocompleteList) {
            autocompleteList.style.display = "none";
        }
    });
});