document.addEventListener("DOMContentLoaded", function () {
	    
	    // =======================================================
	    // 1. 드롭다운 셀렉트 박스 전환 제어 및 펼침 리셋 로직
	    // =======================================================
	    const selectEl = document.getElementById("statSectionSelect");
	    
	    if (selectEl) {
	        selectEl.addEventListener("change", function () {
	            const selectedValue = this.value;
	            
	            document.querySelectorAll(".stat-toggle-content").forEach(function (content) {
	                content.classList.remove("active");
	                
	                // 다른 드롭다운으로 전환할 때, 기존에 펼쳐져 있던 더보기 버튼이 있다면 다시 접어두는 센스
	                const openBtn = content.querySelector(".stat-more-btn");
	                if (openBtn && openBtn.textContent.includes("접기")) {
	                    openBtn.click(); 
	                }
	            });
	            
	            const targetContent = document.getElementById("stat-" + selectedValue);
	            if (targetContent) {
	                targetContent.classList.add("active");
	            }
	        });
	    }
	
	    // =======================================================
	    // 2. 무한 확장 대응: 5개 초과 시 '더보기' 버튼 동적 생성 로직
	    // =======================================================
	    document.querySelectorAll(".stat-toggle-content").forEach(function (content) {
	        const rows = content.querySelectorAll(".stat-item-row");
	        
	        // 등록된 통계 아이템이 5개를 초과하는 경우에만 작동
	        if (rows.length > 5) {
	            
	            // 1) 5번째(인덱스 5) 데이터부터 끝까지 숨김 클래스 부여
	            for (let i = 5; i < rows.length; i++) {
	                rows[i].classList.add("hidden-stat-row");
	            }
	
	            // 2) 버튼을 담을 컨테이너 세팅
	            const btnWrap = document.createElement("div");
	            btnWrap.className = "stat-more-btn-wrap";
	            
	            // 3) 버튼 엘리먼트 생성 및 텍스트 커스텀 (담당자 탭일 때는 문구 분기 처리)
	            const btn = document.createElement("button");
	            btn.className = "stat-more-btn";
	            btn.type = "button";
	            
	            const isManagerTab = (content.id === "stat-manager");
	            const hiddenCount = rows.length - 5;
	            btn.textContent = isManagerTab ? `담당자 더보기 ▽ (${hiddenCount}명 더보기)` : `더보기 ▽ (${hiddenCount}개 더보기)`;
	
	            // 4) 조립 후 화면 부착
	            btnWrap.appendChild(btn);
	            content.appendChild(btnWrap);
	
	            // 5) 더보기 버튼 클릭 토글 이벤트 바인딩
	            btn.addEventListener("click", function () {
	                // 현재 접혀있는 상태인지 여부 확인
	                const isCollapsed = rows[5].classList.contains("hidden-stat-row");
	                
	                // 숨겨진 열들을 토글 토글
	                for (let i = 5; i < rows.length; i++) {
	                    if (isCollapsed) {
	                        rows[i].classList.remove("hidden-stat-row");
	                    } else {
	                        rows[i].classList.add("hidden-stat-row");
	                    }
	                }
	
	                // 토글 상태에 맞춰 버튼 텍스트 변경
	                if (isCollapsed) {
	                    btn.textContent = "접기 △";
	                } else {
	                    btn.textContent = isManagerTab ? `담당자 더보기 ▽ (${hiddenCount}명 더보기)` : `더보기 ▽ (${hiddenCount}개 더보기)`;
	                }
	            });
	        }
	    });
	});
	
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