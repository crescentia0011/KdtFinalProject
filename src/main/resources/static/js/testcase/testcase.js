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
			
// 상세조회 페이지 연결			
function goDetail(element) {
    // element.dataset을 통해 html의 data-* 속성에 접근합니다.
    var projectId = element.dataset.projectId;
    var testId = element.dataset.testId;
    
    location.href = '/project/testcase/detail?projectId=' + projectId + '&testId=' + testId;
}	



function openTcDeleteModal(btn) {
    // 버튼에 심어둔 data-* 값 가져오기
    const testId = btn.getAttribute('data-test-id');
    const testName = btn.getAttribute('data-test-name');
    const projectId = btn.getAttribute('data-project-id');

    const dialog = document.getElementById('tcDeleteDialog');
    
    // 만약 HTML에서 모달 코드를 못 찾으면 경고창 띄우기 (먹통 방지)
    if (!dialog) {
        alert('모달 창 코드를 찾을 수 없습니다. HTML 하단에 <dialog> 태그가 있는지 확인해주세요.');
        return;
    }

    // 텍스트 가이드 및 폼 세팅
    document.getElementById('tcDeleteName').textContent = testName;
    document.getElementById('tcDeletePrompt').textContent = '삭제하려면 [' + testName + '] 을(를) 정확히 입력하세요.';
    
    const confirmInput = document.getElementById('tcDeleteConfirmationName');
    const submitBtn = document.getElementById('tcDeleteSubmitBtn');
    
    confirmInput.value = '';
    submitBtn.disabled = true;

    // Action 주소 세팅
    document.getElementById('tcDeleteForm').action = `/project/testcase/delete/${testId}?projectId=${projectId}`;

    // 실시간 글자 비교
    confirmInput.oninput = function() {
        if (this.value.trim() === testName.trim()) {
            submitBtn.disabled = false;
        } else {
            submitBtn.disabled = true;
        }
    };

    // 모달 띄우기!
    dialog.showModal();
}

// 2. 모달 닫기 함수
function closeTcDeleteModal() {
    const dialog = document.getElementById('tcDeleteDialog');
    if (dialog) {
        dialog.close();
    }
}