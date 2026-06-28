document.addEventListener("DOMContentLoaded", function() {
    const taskModal = document.getElementById("taskModal");
    const btnOpenModal = document.getElementById("btnOpenModal");
    const btnCloseModal = document.getElementById("btnCloseModal");
    const btnCancelModal = document.getElementById("btnCancelModal");
    const btnConfirmTasks = document.getElementById("btnConfirmTasks");
    const btnSearchTasks = document.getElementById("btnSearchTasks");
    
    const thCheckAll = document.getElementById("thCheckAll");
    const unassignedTaskListBody = document.getElementById("unassignedTaskListBody");
    const taskPaginationNav = document.getElementById("taskPaginationNav");
    
    const hiddenTasksContainer = document.getElementById("hiddenTasksContainer");
    const selectedTasksPreview = document.getElementById("selectedTasksPreview");
    const selectedCountBadge = document.getElementById("selectedCount");

    let tempSelectedTasks = {};
    let confirmedTasks = {};
    
    // 현재 모달창 내 상태 추적용 변수
    let currentPage = 1;
    const pageSize = 10;

    btnOpenModal.addEventListener("click", function() {
        taskModal.classList.add("show");
        currentPage = 1; // 열릴 때 1페이지 초기화
        fetchUnassignedTasks(currentPage);
    });

    function closeModal() {
        taskModal.classList.remove("show");
        tempSelectedTasks = { ...confirmedTasks }; 
    }
    btnCloseModal.addEventListener("click", closeModal);
    btnCancelModal.addEventListener("click", closeModal);

    // 검색 시 항상 1페이지로 새로고침
    btnSearchTasks.addEventListener("click", function() {
        currentPage = 1;
        fetchUnassignedTasks(currentPage);
    });

    // 10개씩 페이징 처리된 데이터 비동기 요청 호출
	function fetchUnassignedTasks(page) {
	    currentPage = page;
	    const taskStatus = document.getElementById("filterStatus").value;
	    const priority = document.getElementById("filterPriority").value;
	    const taskManager = document.getElementById("filterManager").value; // 텍스트 입력값 꺼내기
	    const typeId = document.getElementById("filterTypeId").value;       // [변경] 드롭다운의 ID 값 꺼내기

	    // URL 파라미터 빌드 구성 조정
	    let url = `/project/milestone/unassigned-tasks?projectId=${projectId}&page=${page}`;
	    if (taskStatus) url += `&taskStatus=${encodeURIComponent(taskStatus)}`;
	    if (priority) url += `&priority=${encodeURIComponent(priority)}`;
	    if (taskManager) url += `&taskManager=${encodeURIComponent(taskManager)}`; // 백엔드 u.user_name 매핑 데이터
	    if (typeId) url += `&typeId=${encodeURIComponent(typeId)}`;               // [변경] typeName 문자열 대신 고유 ID 전송

	    unassignedTaskListBody.innerHTML = `<tr><td colspan="6" class="text-center py-4"><div class="spinner-border text-primary spinner-border-sm"></div> 로딩 중...</td></tr>`;

	    fetch(url)
	        .then(response => {
	            if (!response.ok) throw new Error();
	            return response.json();
	        })
	        .then(data => {
	            renderTaskList(data.list || []);
	            renderPagination(data.totalCount || 0);
	        })
	        .catch(() => {
	            unassignedTaskListBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">조회 실패</td></tr>`;
	        });
	}

    // 리스트 가공 함수
    function renderTaskList(taskList) {
        unassignedTaskListBody.innerHTML = "";
        thCheckAll.checked = false;

        if (taskList.length === 0) {
            unassignedTaskListBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">조회된 미지정 일감이 없습니다.</td></tr>`;
            return;
        }

        taskList.forEach(task => {
            const tr = document.createElement("tr");
            const isChecked = tempSelectedTasks[task.taskId] ? "checked" : "";

            tr.innerHTML = `
                <td><input type="checkbox" class="td-task-check" data-id="${task.taskId}" data-title="${task.taskTitle}" ${isChecked}></td>
                <td class="fw-semibold text-dark">${task.taskTitle}</td>
                <td><span class="badge bg-light text-secondary">${task.taskStatus || '-'}</span></td>
                <td><span class="text-warning">${task.priority || '-'}</span></td>
                <td>${task.taskManager || '없음'}</td>
                <td><span class="text-info">${task.typeName || '-'}</span></td>
            `;

            const checkbox = tr.querySelector(".td-task-check");
            checkbox.addEventListener("change", function() {
                if (this.checked) {
                    tempSelectedTasks[task.taskId] = task.taskTitle;
                } else {
                    delete tempSelectedTasks[task.taskId];
                }
            });

            unassignedTaskListBody.appendChild(tr);
        });
    }

    // 전체 체크 토글 제어
    thCheckAll.addEventListener("change", function() {
        const checkboxes = unassignedTaskListBody.querySelectorAll(".td-task-check");
        checkboxes.forEach(cb => {
            cb.checked = this.checked;
            const id = cb.getAttribute("data-id");
            const title = cb.getAttribute("data-title");
            if (this.checked) {
                tempSelectedTasks[id] = title;
            } else {
                delete tempSelectedTasks[id];
            }
        });
    });

    // [핵심] 10개 기준 동적 페이징 UI 생성 처리 함수
    function renderPagination(totalCount) {
        taskPaginationNav.innerHTML = "";
        if (totalCount <= 0) return;

        const totalPages = Math.ceil(totalCount / pageSize);
        const pageBlockSize = 5; // 한 번에 표시할 페이지 번호 개수
        const currentBlock = Math.ceil(currentPage / pageBlockSize);
        
        const startPage = (currentBlock - 1) * pageBlockSize + 1;
        let endPage = currentBlock * pageBlockSize;
        if (endPage > totalPages) endPage = totalPages;

        // 이전 블록 이동 버튼
        if (startPage > 1) {
            const prevLi = document.createElement("li");
            prevLi.className = "page-item";
            prevLi.innerHTML = `<a class="page-link">&laquo;</a>`;
            prevLi.addEventListener("click", () => fetchUnassignedTasks(startPage - 1));
            taskPaginationNav.appendChild(prevLi);
        }

        // 페이지 번호 링크 나열 루프
        for (let i = startPage; i <= endPage; i++) {
            const li = document.createElement("li");
            li.className = `page-item ${i === currentPage ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link">${i}</a>`;
            
            li.addEventListener("click", function() {
                if (i !== currentPage) fetchUnassignedTasks(i);
            });
            taskPaginationNav.appendChild(li);
        }

        // 다음 블록 이동 버튼
        if (endPage < totalPages) {
            const nextLi = document.createElement("li");
            nextLi.className = "page-item";
            nextLi.innerHTML = `<a class="page-link">&raquo;</a>`;
            nextLi.addEventListener("click", () => fetchUnassignedTasks(endPage + 1));
            taskPaginationNav.appendChild(nextLi);
        }
    }

    // 메인화면 최종 제출 연결 확정
    btnConfirmTasks.addEventListener("click", function() {
        confirmedTasks = { ...tempSelectedTasks };
        hiddenTasksContainer.innerHTML = "";
        selectedTasksPreview.innerHTML = "";
        
        const taskIds = Object.keys(confirmedTasks);
        selectedCountBadge.innerText = `${taskIds.length}개`;

        if (taskIds.length === 0) {
            selectedTasksPreview.innerHTML = "선택된 일감이 없습니다. '일감 등록하기' 버튼을 눌러 일감을 선택해주세요.";
            taskModal.classList.remove("show");
            return;
        }

        taskIds.forEach(id => {
            const hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = "taskIds";
            hiddenInput.value = id;
            hiddenTasksContainer.appendChild(hiddenInput);

            const span = document.createElement("span");
            span.className = "selected-task-badge";
            span.innerText = confirmedTasks[id];
            selectedTasksPreview.appendChild(span);
        });

        taskModal.classList.remove("show");
    });
});