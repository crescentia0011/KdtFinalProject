window.switchTab = function(tabId, clickedTabElement) {
    //  모든 탭 활성화 해제
    const tabs = document.querySelectorAll('.tc-tab');
    tabs.forEach(tab => tab.classList.remove('active'));
    clickedTabElement.classList.add('active');

    //  모든 탭 컨텐츠 숨기기
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => {
        content.style.display = 'none';
    });

    // 선택한 탭 컨텐츠만 보이기
    const targetContent = document.getElementById(tabId);
    if (targetContent) {
        targetContent.style.display = 'block';
    }

    // 검색창 숨김/보이기 처리
    const searchBox = document.getElementById('search-box');
    if (searchBox) {
        if (tabId === 'tab-coverage') {
            searchBox.style.display = 'none';
        } else {
            searchBox.style.display = 'block';
        }
    }
};

document.addEventListener("DOMContentLoaded", function() {
    // 테스트 케이스 등록용 일감 자동완성 검색 함수
    const tasks = window.globalTaskList || [];
    const searchInput = document.getElementById("taskSearch");
    const hiddenInput = document.getElementById("taskId");
    const autocompleteList = document.getElementById("task-autocomplete-list");

    if (searchInput && autocompleteList) {
        //  키보드 입력 이벤트 핸들러
        searchInput.addEventListener("input", function(e) {
            e.stopPropagation(); 
            
            const val = this.value.trim().toLowerCase();
            autocompleteList.innerHTML = ""; 

            // 입력값이 비어있으면 목록을 숨기고 바인딩된 hidden ID 제거
            if (!val) {
                autocompleteList.style.display = "none";
                hiddenInput.value = ""; 
                return;
            }

            // taskTitle에 검색어가 포함되는 데이터 필터링
            const filtered = tasks.filter(task => {
                if (task && task.taskTitle) {
                    const titleStr = String(task.taskTitle).toLowerCase();
                    return titleStr.includes(val);
                }
                return false;
            });

            // 매칭되는 일감 목록이 존재할 때
            if (filtered.length > 0) {
                let htmlContent = "";
                
                filtered.forEach(task => {
                    htmlContent += `
                        <button type="button" 
                                class="list-group-item list-group-item-action small text-start py-2 task-item" 
                                style="cursor: pointer; background-color: #ffffff; border: none; border-bottom: 1px solid #dee2e6; width: 100%; display: block; padding: 10px; text-align: left;"
                                data-id="${task.taskId}" 
                                data-title="${task.taskTitle}">
                            ${task.taskTitle}
                        </button>
                    `;
                });
                
                autocompleteList.innerHTML = htmlContent;
                
                // 첫 번째 아이템의 상단 테두리 보정
                const firstItem = autocompleteList.querySelector(".task-item");
                if (firstItem) firstItem.style.borderTop = "1px solid #dee2e6";
                
                autocompleteList.style.display = "block"; 
                
                // 동적으로 생성된 항목들에 클릭 이벤트 연결
                const items = autocompleteList.querySelectorAll(".task-item");
                items.forEach(item => {
                    item.addEventListener("click", function(clickEvent) {
                        clickEvent.stopPropagation(); 
                        searchInput.value = this.getAttribute("data-title"); // 검색창엔 타이틀 세팅 
                        hiddenInput.value = this.getAttribute("data-id");    // hidden input엔 ID 할당
                        autocompleteList.style.display = "none";             // 목록 숨기기
                    });
                });
            } else {
                autocompleteList.style.display = "none"; 
            }
        });

        //  화면의 다른 곳을 클릭하면 검색 레이어가 닫히도록 설정
        document.addEventListener("click", function() {
            autocompleteList.style.display = "none";
        });
    }
});

// 페이징
function goPage(pageNumber) {
	            document.getElementById('pageInput').value = pageNumber;
	            document.getElementById('searchForm').submit();
	        }