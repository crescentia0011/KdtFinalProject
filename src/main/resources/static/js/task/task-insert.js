// 제이쿼리 에러 강제 우회 방어벽 검토후 필요없으면 삭제
window.onerror = function(message, source, lineno, colno, error) {
    if (message && (message.indexOf('indexOf') !== -1 || (source && (source.indexOf('jquery') !== -1 || source.indexOf('custom') !== -1)))) {
        return true; 
    }
};

const projectStart = document.getElementById("projectStartDate").value;
const projectFinish = document.getElementById("projectFinishDate").value;

const startDate = document.getElementById("startDate");
const finishDate = document.getElementById("finishDate");

startDate.min = projectStart;
startDate.max = projectFinish;

finishDate.min = projectStart;
finishDate.max = projectFinish;


const startDateInput = document.getElementById('startDate');
    const finishDateInput = document.getElementById('finishDate');

    startDateInput.addEventListener('change', function() {
        // 시작일이 선택되면 완료일의 최소 선택 가능일(min)을 시작일로 설정
        if (this.value) {
            finishDateInput.min = this.value;
            
            // 만약 기존에 입력된 완료일이 새로운 시작일보다 앞선다면 완료일 초기화
            if (finishDateInput.value && finishDateInput.value < this.value) {
                finishDateInput.value = '';
				showToast('완료일은 시작일보다 빠를 수 없습니다. 다시 선택해주세요.', true);
            }
        } else {
            finishDateInput.removeAttribute('min');
        }
    });

document.addEventListener("DOMContentLoaded", function() {
    // ==========================================
    // 1. 상위 일감 찾는 검색창 함수
    // ==========================================
    const parentTasks = window.globalParentTaskList || [];
    const searchInput = document.getElementById("parentTaskSearch");
    const hiddenInput = document.getElementById("parentTaskId");
    const autocompleteList = document.getElementById("autocomplete-list");

    if (searchInput && autocompleteList) {
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
    }

    // ==========================================
    // 2. 신규 파일 첨부 및 드래그 앤 드롭 로직
    // ==========================================
    const fileInput = document.getElementById('fileInput');
    const dropZone = document.getElementById('dropZone');
    const fileListDisplay = document.getElementById('fileListDisplay');
    
    let selectedFiles = [];
    const MAX_SIZE = 5 * 1024 * 1024; // 5MB

    if (fileInput && dropZone) {
        // 파일 클릭 첨부
        fileInput.addEventListener('change', (e) => {
            handleFiles(e.target.files);
        });

        // 드래그 앤 드롭
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('dragover');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
            handleFiles(e.dataTransfer.files);
        });

        // 파일 검증 및 배열 추가 함수
        function handleFiles(files) {
            for (let file of files) {
                if (file.size > MAX_SIZE) {
                    alert(`${file.name} 파일이 5MB를 초과합니다.`);
                    continue;
                }
                // 중복 방지
                if (selectedFiles.some(f => f.name === file.name && f.size === file.size)) {
                    continue;
                }
                selectedFiles.push(file);
            }
            renderFileList();
        }

        // ⭐️ 중요: 파일 목록 렌더링 및 실제 <input> 태그 동기화
        function renderFileList() {
            fileListDisplay.innerHTML = '';
            
            // 일반 Form Submit을 위해 JS 배열의 파일을 실제 <input type="file">에 동기화해주는 객체
            const dataTransfer = new DataTransfer(); 

            selectedFiles.forEach((file, index) => {
                // 1. 실제 input에 전송될 파일 목록에 추가
                dataTransfer.items.add(file);

                // 2. 화면에 보여줄 UI 생성
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';
                fileItem.innerHTML = `
                    <span>📄 <strong>${file.name}</strong> (${(file.size / 1024).toFixed(1)} KB)</span>
                    <span class="btn-remove" data-index="${index}" style="color: #ff4d4f; cursor: pointer; font-weight: bold; margin-left: 10px;">✕</span>
                `;
                fileListDisplay.appendChild(fileItem);
            });

            // ⭐️ 가장 핵심: 동기화된 파일 목록을 실제 input 태그에 덮어씌움
            fileInput.files = dataTransfer.files;

            // 취소 버튼 이벤트
            const removeButtons = fileListDisplay.querySelectorAll('.btn-remove');
            removeButtons.forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.stopPropagation(); 
                    const indexToRemove = parseInt(btn.getAttribute('data-index'));
                    selectedFiles.splice(indexToRemove, 1);
                    renderFileList(); // 삭제 후 재렌더링 (이때 input.files도 다시 갱신됨)
                });
            });
        }
    }

    // ==========================================
    // 3. 기존 첨부된 파일 삭제 로직 (수정 화면용)
    // ==========================================
    const existingFileItems = document.querySelectorAll('.existing-file-item');
    const deletedFilesContainer = document.getElementById('deletedFilesContainer');

    if (existingFileItems.length > 0 && deletedFilesContainer) {
        existingFileItems.forEach(item => {
            const removeBtn = item.querySelector('.btn-remove-existing');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    const fileId = item.getAttribute('data-file-id');

                    // 서버로 전송할 hidden input 생성
                    const hiddenInput = document.createElement('input');
                    hiddenInput.type = 'hidden';
                    hiddenInput.name = 'deletedFileIds'; // 백엔드 DTO와 일치해야 함
                    hiddenInput.value = fileId;
                    deletedFilesContainer.appendChild(hiddenInput);

                    // 화면에서 숨김
                    item.style.display = 'none';
                });
            }
        });
    }
});

function showToast(message, isError = false) {
        let toastWrap = document.getElementById('dynamicToast');
        if (toastWrap) toastWrap.remove(); 

        toastWrap = document.createElement('div');
        toastWrap.id = 'dynamicToast';
        toastWrap.className = 'toast-wrap';
        
        toastWrap.innerHTML = `<div class="toast-msg ${isError ? 'toast-error' : 'toast-success'}">${message}</div>`;
        document.body.appendChild(toastWrap);

    
        setTimeout(() => {
            if (document.body.contains(toastWrap)) toastWrap.remove();
        }, 3500); 
    }

