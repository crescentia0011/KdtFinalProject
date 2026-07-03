document.addEventListener("DOMContentLoaded", function() {
    // 모달 제어 엘리먼트
    const taskModal = document.getElementById("taskModal");
    const btnOpenModal = document.getElementById("btnOpenModal");
    const btnCloseModal = document.getElementById("btnCloseModal");
    const btnCancelModal = document.getElementById("btnCancelModal");
    const btnConfirmTasks = document.getElementById("btnConfirmTasks"); // HTML의 '선택 완료' 버튼
    const btnSearchTasks = document.getElementById("btnSearchTasks");
    
    // 테이블 및 페이징 엘리먼트
    const thCheckAll = document.getElementById("thCheckAll");
    const unassignedTaskListBody = document.getElementById("unassignedTaskListBody");
    const taskPaginationNav = document.getElementById("taskPaginationNav");

    // HTML에 렌더링된 메타 태그에서 CSRF 토큰 정보 추출
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // 버튼 텍스트를 상세페이지 용도에 맞게 '등록'으로 동적 변경
    if (btnConfirmTasks) {
        btnConfirmTasks.textContent = "연결 등록";
    }

    // 데이터 상태 관리
    // HTML 내부 <script th:inline="javascript">에서 정의된 전역 변수(initialTasks, projectId, milestoneId) 사용
    let confirmedTasks = (typeof initialTasks !== 'undefined' && initialTasks) ? { ...initialTasks } : {};
    let tempSelectedTasks = { ...confirmedTasks };
    
    let currentPage = 1;
    const pageSize = 10;

    // 모달 열기
    btnOpenModal.addEventListener("click", function() {
        taskModal.classList.add("show");
        currentPage = 1; 
        fetchUnassignedTasks(currentPage);
    });

    // 모달 닫기 (임시 선택값 초기화)
    function closeModal() {
        taskModal.classList.remove("show");
        tempSelectedTasks = { ...confirmedTasks }; 
    }
    btnCloseModal.addEventListener("click", closeModal);
    btnCancelModal.addEventListener("click", closeModal);

    // 미지정 일감 검색 버튼
    btnSearchTasks.addEventListener("click", function() {
        currentPage = 1;
        fetchUnassignedTasks(currentPage);
    });

    // 백엔드로부터 미지정 일감 목록 로드 (기존 로직 유지)
    function fetchUnassignedTasks(page) {
        currentPage = page;
        const taskStatus = document.getElementById("filterStatus").value;
        const priority = document.getElementById("filterPriority").value;
        const taskManager = document.getElementById("filterManager").value; 
        const typeId = document.getElementById("filterTypeId").value;       

        let url = `/project/milestone/unassigned-tasks?projectId=${projectId}&page=${page}`;
        if (typeof milestoneId !== 'undefined' && milestoneId) {
            url += `&milestoneId=${milestoneId}`;
        }
        if (taskStatus) url += `&taskStatus=${encodeURIComponent(taskStatus)}`;
        if (priority) url += `&priority=${encodeURIComponent(priority)}`;
        if (taskManager) url += `&taskManager=${encodeURIComponent(taskManager)}`; 
        if (typeId) url += `&typeId=${encodeURIComponent(typeId)}`;               

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

    // 일감 목록 렌더링 및 체크박스 이벤트 바인딩 (기존 로직 유지)
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

    // 전체 선택 기능 (기존 로직 유지)
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

    // 페이징 생성 (기존 로직 유지)
    function renderPagination(totalCount) {
        taskPaginationNav.innerHTML = "";
        if (totalCount <= 0) return;

        const totalPages = Math.ceil(totalCount / pageSize);
        const pageBlockSize = 5; 
        const currentBlock = Math.ceil(currentPage / pageBlockSize);
        
        const startPage = (currentBlock - 1) * pageBlockSize + 1;
        let endPage = currentBlock * pageBlockSize;
        if (endPage > totalPages) endPage = totalPages;

        if (startPage > 1) {
            const prevLi = document.createElement("li");
            prevLi.className = "page-item";
            prevLi.innerHTML = `<a class="page-link">&laquo;</a>`;
            prevLi.addEventListener("click", () => fetchUnassignedTasks(startPage - 1));
            taskPaginationNav.appendChild(prevLi);
        }

        for (let i = startPage; i <= endPage; i++) {
            const li = document.createElement("li");
            li.className = `page-item ${i === currentPage ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link">${i}</a>`;
            
            li.addEventListener("click", function() {
                if (i !== currentPage) fetchUnassignedTasks(i);
            });
            taskPaginationNav.appendChild(li);
        }

        if (endPage < totalPages) {
            const nextLi = document.createElement("li");
            nextLi.className = "page-item";
            nextLi.innerHTML = `<a class="page-link">&raquo;</a>`;
            nextLi.addEventListener("click", () => fetchUnassignedTasks(endPage + 1));
            taskPaginationNav.appendChild(nextLi);
        }
    }

    // =======================================================
    // [핵심 변경 포인트] 비동기 즉시 등록 및 화면 갱신 기능
    // =======================================================
    btnConfirmTasks.addEventListener("click", function() {
        // 더블 클릭 방지 및 상태 표시를 위한 버튼 비활성화
        btnConfirmTasks.disabled = true;
        btnConfirmTasks.textContent = "저장 중...";

        // 보낼 파라미터 빌드 (x-www-form-urlencoded 포맷)
        const params = new URLSearchParams();
        params.append("projectId", projectId);
        params.append("milestoneId", milestoneId);
        
        // 현재 체크되어 임시 저장된 일감 ID들을 모두 담음
        const taskIds = Object.keys(tempSelectedTasks);
        if (taskIds.length > 0) {
            taskIds.forEach(id => params.append("taskIds", id));
        } else {
            // 선택된 일감이 하나도 없을 경우에 대비해 공백 값 전송 (전체 해제 목적)
            params.append("taskIds", "");
        }

        // 서버로 비동기 POST 요청
        fetch("/project/milestone/api/update-task-mapping", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrfHeader]: csrfToken // Spring Security CSRF 검증 우회용 헤더 주입
            },
            body: params.toString()
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("네트워크 응답 오류");
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert(data.message);
                taskModal.classList.remove("show");
                // 상세페이지의 통계, 진행바, 일감 리스트를 최신화하기 위해 새로고침 실행
                location.reload();
            } else {
                alert("등록 실패: " + data.message);
                btnConfirmTasks.disabled = false;
                btnConfirmTasks.textContent = "연결 등록";
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("서버 통신 중 문제가 발생했습니다.");
            btnConfirmTasks.disabled = false;
            btnConfirmTasks.textContent = "연결 등록";
        });
    });
});