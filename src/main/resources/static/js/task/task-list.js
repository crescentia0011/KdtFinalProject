function redirectToInsertTask() {
    const selectElement = document.getElementById('modalProjectId');
    const selectedId = selectElement.value;

    if (!selectedId) {
        alert('프로젝트를 선택해주세요.');
        return;
    }

    // HTML에서 글로벌 전역 변수로 할당한 타임리프 컨텍스트 주소를 참조합니다.
    const baseUrl = window.insertTaskBaseUrl || '/project/task/insert';
    location.href = `${baseUrl}?projectId=${selectedId}`;
}



    // 팝업 토글 함수
    function toggleDropdown(button) {
        const currentMenu = button.nextElementSibling;
        
        // 다른 행에 열려있는 팝업이 있다면 모두 닫기
        document.querySelectorAll('.dropdown-menu-box').forEach(menu => {
            if (menu !== currentMenu) {
                menu.classList.remove('show');
            }
        });
        
        // 현재 누른 팝업 켜고 끄기
        currentMenu.classList.toggle('show');
    }

    // 바탕화면이나 다른 곳 클릭 시 팝업 닫기
    window.addEventListener('click', function(e) {
        if (!e.target.matches('.more-options-btn')) {
            document.querySelectorAll('.dropdown-menu-box').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    });

    // 삭제 처리 함수 예시
    function handleDelete(taskId, projectId) {
        if (confirm("정말 이 일감을 삭제하시겠습니까?")) {
            // REST API 혹은 컨트롤러 매핑 주소로 요청 전송
            location.href = `/project/task/delete/${taskId}?projectId=${projectId}`;
        }
    }
	document.addEventListener('DOMContentLoaded', function() {
	    const element = document.getElementById('memberChoices');
		const choices = new Choices(element, {
		    removeItemButton: true,
		    searchPlaceholderValue: '담당자 검색...',
		    noResultsText: '검색 결과 없음',
		    itemSelectText: '', // 마우스 올렸 때 뜨는 'Press to select'라는 무거운 텍스트를 제거합니다.
		    shouldSort: false,
		    searchFloor: 1,     // 1글자만 쳐도 검색 시작
		    placeholder: true,
		    placeholderValue: '담당자를 선택하세요'
		});
	});
	
	function goPage(pageNumber) {
	            document.getElementById('pageInput').value = pageNumber;
	            document.getElementById('searchForm').submit();
	        }
