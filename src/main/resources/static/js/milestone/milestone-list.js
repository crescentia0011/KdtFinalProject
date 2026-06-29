// 1. 토글 스위치 상태에 따른 안내 텍스트 동적 변경
	    function toggleStatusText(checkbox) {
	        const container = checkbox.closest('.edit-mode-container');
	        const textSpan = container.querySelector('.switch-text');
	        const progress = parseInt(textSpan.getAttribute('data-progress') || '0', 10);
	        
	        if (progress < 100) {
	            textSpan.textContent = '진척도 100% 필수';
	            return;
	        }
	        
	        textSpan.textContent = checkbox.checked ? '완료 처리' : '진행 중';
	    }
	
	    // 2. 편집 모드 진입 (기존 데이터 백업)
	    function enableEditMode(button) {
	        const header = button.closest('.milestone-header');
	        
	        // 데이터 오염 방지를 위해 현재 입력값들을 원본 데이터로 저장
	        header.dataset.oriTitle = header.querySelector('.edit-title-input').value;
	        header.dataset.oriDate = header.querySelector('.edit-date-input').value;
	        header.dataset.oriDescribe = header.querySelector('.edit-describe-input').value;
	        header.dataset.oriChecked = header.querySelector('.edit-status-toggle').checked;
	
	        header.querySelector('.view-mode-container').style.display = 'none';
	        header.querySelector('.edit-mode-container').style.display = 'flex';
	    }
	    
	    // 3. 편집 취소 (수정 중이던 값 초기화 및 복구)
	    function cancelEditMode(button) {
	        const header = button.closest('.milestone-header');
	        
	        // 백업해둔 기존 데이터로 복원
	        header.querySelector('.edit-title-input').value = header.dataset.oriTitle || '';
	        header.querySelector('.edit-date-input').value = header.dataset.oriDate || '';
	        header.querySelector('.edit-describe-input').value = header.dataset.oriDescribe || '';
	        
	        const toggle = header.querySelector('.edit-status-toggle');
	        toggle.checked = header.dataset.oriChecked === 'true';
	        toggleStatusText(toggle); 
	
	        header.querySelector('.edit-mode-container').style.display = 'none';
	        header.querySelector('.view-mode-container').style.display = 'flex';
	    }
	    
	    // 4. 비동기 저장 처리 (공백 및 날짜 검증 포함)
	    function saveMilestone(button) {
	        const header = button.closest('.milestone-header');
	        const milestoneId = header.getAttribute('data-milestone-id');
	        
	        const updatedTitle = header.querySelector('.edit-title-input').value;
	        const updatedDate = header.querySelector('.edit-date-input').value;
	        const isChecked = header.querySelector('.edit-status-toggle').checked;
	        const updatedStatus = isChecked ? 'g2' : 'g1';
	        const milestoneDescribe = header.querySelector('.edit-describe-input').value;
	        
	        const csrfParameter = document.querySelector('meta[name="_csrf_parameter"]').getAttribute('content');
	        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
	    
	        // 필수 값 검증 (Validation)
	        if (!updatedTitle.trim()) {
	            alert("마일스톤 명칭은 공백일 수 없습니다.");
	            return;
	        }
	        
	        if (!updatedDate) {
	            alert("목표 완료일을 입력해 주세요.");
	            return;
	        }
	    
	        const formData = new URLSearchParams();
	        formData.append('milestoneId', milestoneId);
	        formData.append('projectId', new URLSearchParams(window.location.search).get('projectId'));
	        formData.append('milestoneTitle', updatedTitle);
	        formData.append('finishDate', updatedDate);
	        formData.append('milestoneStatus', updatedStatus);
	        formData.append('milestoneDescribe', milestoneDescribe);
	        formData.append(csrfParameter, csrfToken);
	    
	        fetch('/project/milestone/update', {
	            method: 'POST',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            body: formData
	        })
	        .then(response => {
	            if (response.ok) {
	                alert('마일스톤 수정이 완료되었습니다.');
	                location.reload();
	            } else {
	                alert('수정 작업 처리에 실패하였습니다. 데이터 구조를 확인해 주세요.');
	            }
	        })
	        .catch(error => {
	            console.error('AJAX 전송 처리 에러:', error);
	            alert('네트워크 연결이 원활하지 않습니다.');
	        });
	    }
	    
	    document.addEventListener("DOMContentLoaded", function() {
		    const dialog = document.getElementById('milestoneDeleteDialog');
		    const confirmInput = document.getElementById('milestoneDeleteConfirmationName');
		    const submitBtn = document.getElementById('milestoneDeleteSubmitBtn');
		    const deleteNameSpan = document.getElementById('milestoneDeleteName');
		    const promptSpan = document.getElementById('milestoneDeletePrompt');
		    const milestoneIdInput = document.getElementById('milestoneDeleteId');
	
		    if (!dialog) return;
	
		    // 1. 삭제 버튼 클릭 시 모달 오픈 및 데이터 세팅
		    document.querySelectorAll('.milestone-delete-trigger').forEach(function (button) {
		        button.addEventListener('click', function () {
		            const milestoneId = button.dataset.milestoneId;
		            const milestoneTitle = button.dataset.milestoneTitle;
	
		            // 데이터 동적 주입
		            milestoneIdInput.value = milestoneId;
		            deleteNameSpan.textContent = milestoneTitle;
		            promptSpan.textContent = '삭제하려면 ' + milestoneTitle + '을(를) 입력하세요.';
		            confirmInput.value = '';
		            
		            // 버튼 상태 초기화 (비활성화)
		            submitBtn.disabled = true;
	
		            // 실시간 텍스트 일치 여부 검사
		            confirmInput.oninput = function() {
		                if (this.value.trim() === milestoneTitle.trim()) {
		                    submitBtn.disabled = false;
		                } else {
		                    submitBtn.disabled = true;
		                }
		            };
	
		            dialog.showModal();
		        });
		    });
	
		    // 2. 닫기 버튼 이벤트 처리 (취소 및 X 버튼)
		    document.querySelectorAll('[data-milestone-delete-close]').forEach(function (button) {
		        button.addEventListener('click', function () {
		            dialog.close();
		        });
		    });
		});