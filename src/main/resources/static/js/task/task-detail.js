function openTab(evt, tabName) {

	// 1. 모든 탭 콘텐츠 영역 숨기기
	const tabContents = document.getElementsByClassName("tab-content");

	for (let i = 0; i < tabContents.length; i++) {

		tabContents[i].style.display = "none";

	}
	// 2. 모든 탭 버튼 활성화 스타일 초기화
	const tabButtons = document.getElementsByClassName("tab-button");

	for (let i = 0; i < tabButtons.length; i++) {

		tabButtons[i].classList.remove("active");

	}



	// 3. 사용자가 클릭한 특정 탭 콘텐츠 노출
	document.getElementById(tabName).style.display = "block";


	// 4. 클릭된 버튼에 활성화(Active) CSS 적용
	evt.currentTarget.classList.add("active");


}

document.addEventListener("DOMContentLoaded", function() {

	const taskDeleteDialog = document.getElementById('taskDeleteDialog');
	const taskDeleteForm = document.getElementById('taskDeleteForm'); // 💡 변수명은 'taskDeleteForm'임
	const taskDeleteName = document.getElementById('taskDeleteName');
	const taskDeletePrompt = document.getElementById('taskDeletePrompt');
	const confirmInput = document.getElementById('taskDeleteConfirmationName');
	const submitBtn = document.getElementById('taskDeleteSubmitBtn');

	if (!taskDeleteDialog) return;

	// 1. 삭제 버튼 클릭 시 모달 오픈 및 데이터 세팅
	document.querySelectorAll('.task-delete-trigger').forEach(function(button) {
		button.addEventListener('click', function() {
			const taskId = button.dataset.taskId;
			const taskTitle = button.dataset.taskTitle;
			const projectId = button.dataset.projectId;

			// 텍스트 가이드 변경
			taskDeleteName.textContent = taskTitle;
			taskDeletePrompt.textContent = '삭제하려면 ' + taskTitle + '을(를) 입력하세요.';
			confirmInput.value = '';

			// 버튼 비활성화 초기화
			submitBtn.disabled = true;

			taskDeleteForm.action = `/project/task/delete/${taskId}?projectId=${projectId}`;

			// 실시간 입력값 비교 체크 (일감명이 정확히 일치할 때만 버튼 활성화)
			confirmInput.oninput = function() {
				if (this.value.trim() === taskTitle.trim()) {
					submitBtn.disabled = false;
				} else {
					submitBtn.disabled = true;
				}
			};

			taskDeleteDialog.showModal();
		});
	});
	document.querySelectorAll('[data-task-delete-close]').forEach(function(button) {
		button.addEventListener('click', function() {
			taskDeleteDialog.close();
			});
		});
    
});



// 1. 답글 폼 열기 및 이동 (수정됨)
function toggleReplyForm(rootId, targetUserName, btnElement) {
    const formElement = document.getElementById('reply-form-' + rootId);
    const replyTextArea = document.getElementById('reply-content-' + rootId);

    const replyWrapper = btnElement.closest('.reply-wrapper');
    const rootCommentItem = btnElement.closest('.root-comment');

    // 1. 입력창 이동 및 마진 정렬 위치 잡기
    if (replyWrapper) {
        // 대댓글의 답글 버튼을 누른 경우 -> 대댓글 묶음 박스 아래에 배치하고 대댓글과 라인(40px)을 맞춤
        replyWrapper.after(formElement);
        formElement.style.marginLeft = '40px'; 
    } else if (rootCommentItem) {
        // 원댓글의 답글 버튼을 누른 경우 -> 원댓글 아래에 배치하고 들여쓰기 없음
        rootCommentItem.after(formElement);
        formElement.style.marginLeft = '0px';  
    }

    // 2. 열기/닫기 토글 및 태그(@) 삽입 규칙 일치화
    if (formElement.style.display === 'none' || formElement.getAttribute('data-target-user') !== targetUserName) {
        formElement.style.display = 'block';
        
        if (targetUserName && targetUserName !== 'null' && targetUserName !== '') {
            replyTextArea.value = `@${targetUserName} `;
        } else {
            replyTextArea.value = '';
        }
        
        formElement.setAttribute('data-target-user', targetUserName);
        replyTextArea.focus();
    } else {
        closeReplyForm(rootId);
    }
}



// 💡 CSRF 토큰을 가져오는 공통 함수
function getCsrfToken() {
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        return {
            header: headerMeta ? headerMeta.getAttribute('content') : 'X-CSRF-TOKEN',
            token: tokenMeta ? tokenMeta.getAttribute('content') : ''
        };
    }
	  
	  function showToast(message, isError = false) {
	          let toastWrap = document.getElementById('dynamicToast');
	          if (toastWrap) toastWrap.remove(); // 기존에 떠있는 토스트가 있다면 제거

	          toastWrap = document.createElement('div');
	          toastWrap.id = 'dynamicToast';
	          toastWrap.className = 'toast-wrap';
	          
	          toastWrap.innerHTML = `<div class="toast-msg ${isError ? 'toast-error' : 'toast-success'}">${message}</div>`;
	          document.body.appendChild(toastWrap);

	          // 애니메이션(3.5s)이 끝난 후 DOM에서 깔끔하게 삭제
	          setTimeout(() => {
	              if (document.body.contains(toastWrap)) toastWrap.remove();
	          }, 3500); 
	      }

	      // 2. 커스텀 삭제 확인 모달 (Promise 기반)
	      function showConfirm(title, desc) {
	          return new Promise((resolve) => {
	              const overlay = document.createElement('div');
	              overlay.className = 'custom-modal-overlay';
	              
	              overlay.innerHTML = `
	                  <div class="custom-modal-box">
	                      <div class="custom-modal-title">${title}</div>
	                      <div class="custom-modal-desc">${desc}</div>
	                      <div class="custom-modal-btns">
	                          <button class="modal-btn-cancel" id="modalCancelBtn">취소</button>
	                          <button class="modal-btn-delete" id="modalDeleteBtn">삭제</button>
	                      </div>
	                  </div>
	              `;
	              document.body.appendChild(overlay);

	              // CSS 트랜지션 트리거
	              setTimeout(() => overlay.classList.add('show'), 10);

	              const close = (result) => {
	                  overlay.classList.remove('show');
	                  setTimeout(() => {
	                      if (document.body.contains(overlay)) overlay.remove();
	                      resolve(result);
	                  }, 200); // 닫기 애니메이션 대기
	              };

	              document.getElementById('modalCancelBtn').onclick = () => close(false);
	              document.getElementById('modalDeleteBtn').onclick = () => close(true);
	          });
	      }	  
	  
	  
	  
	  
		  // 💡 새롭게 추가하는 부분 새로고침 함수
		  function reloadCommentList() {

		      const projectId = document.getElementById('currentProjectId').value; 

		      fetch(`/api/task/comments/fragment/${TaskId}?projectId=${projectId}`)
		          .then(response => response.text())
		          .then(html => {

		              document.getElementById('commentArea').outerHTML = html;

		              const commentCount = document.querySelectorAll('#commentArea .comment-item').length;
		              document.getElementById('commentCount').textContent = commentCount;
		          })
		          .catch(err => console.error("댓글 목록 갱신 실패:", err));
		  }

		  // 1. 댓글 등록
		  function submitComment(parentCommentId) {
		      let contentId = parentCommentId ? 'reply-content-' + parentCommentId : 'mainCommentContent';
		      let contentElement = document.getElementById(contentId);
		      let content = contentElement.value;

		      if (!content.trim()) {
		          showToast('댓글 내용을 입력해주세요.', true);
		          contentElement.focus();
		          return;
		      }

		      let requestData = {
		          taskId: TaskId,
		          taskComment: content.trim(),
		          parentCommentId: parentCommentId 
		      };

		      fetch(`/api/task/comments/${TaskId}`, {
		          method: 'POST',
		          headers: { 
		              'Content-Type': 'application/json',
		              [getCsrfToken().header]: getCsrfToken().token  
		          },
		          body: JSON.stringify(requestData)
		      })
		      .then(response => {
		          if (response.ok) {
		              showToast('댓글이 등록되었습니다.', false); 
		              contentElement.value = ''; // 💡 새로고침을 안 하므로 입력창을 수동으로 비워줌
		              reloadCommentList();       // 💡 전체 새로고침 대신 부분 새로고침 호출!
		          } else {
		              showToast('댓글 등록에 실패했습니다.', true);
		          }
		      });
		  }

		  // 2. 댓글 수정
		  function updateComment(commentId) {
		      const newContent = document.getElementById('edit-content-' + commentId).value;
		      if (!newContent.trim()) { 
		          showToast('수정할 내용을 입력해주세요.', true); 
		          return; 
		      }

		      fetch(`/api/task/comments/${commentId}`, {
		          method: 'PUT',
		          headers: { 
		              'Content-Type': 'application/json',
		              [getCsrfToken().header]: getCsrfToken().token  
		          },
		          body: JSON.stringify({ taskComment: newContent })
		      })
		      .then(response => {
		          if (response.ok) { 
		              showToast('수정되었습니다.', false);
		              reloadCommentList(); // 💡 부분 새로고침!
		          } else {
		              showToast('수정 권한이 없거나 실패했습니다.', true);
		          }
		      });
		  }

		  // 3. 댓글 삭제
		  function deleteComment(commentId) {
		      showConfirm('댓글 삭제', '정말 이 댓글을 삭제하시겠습니까?').then((isConfirmed) => {
		          if (isConfirmed) {
		              fetch(`/api/task/comments/${commentId}`, {
		                  method: 'DELETE',
		                  headers: {
		                      [getCsrfToken().header]: getCsrfToken().token  
		                  }
		              })
		              .then(response => {
		                  if (response.ok) { 
		                      showToast('삭제되었습니다.', false);
		                      reloadCommentList(); // 💡 부분 새로고침!
		                  } else {
		                      showToast('삭제 권한이 없거나 실패했습니다.', true);
		                  }
		              });
		          }
		      });
		  }


	    // 3. 댓글 수정 폼 열기/닫기 토글 
	    function toggleEditForm(commentId) {
	        const textElement = document.getElementById('comment-text-' + commentId);
	        const formElement = document.getElementById('edit-form-' + commentId);
	        const editTextArea = document.getElementById('edit-content-' + commentId);

	        if (formElement.style.display === 'none') {
	            textElement.style.display = 'none';
	            formElement.style.display = 'block';
	            editTextArea.focus();
	        } else {
	            textElement.style.display = 'block';
	            formElement.style.display = 'none';
	            editTextArea.value = textElement.innerText;
	        }
	    }

		//답글 취소
		// 취소 버튼을 눌렀을 때 완전히 창을 닫는 함수
		function closeReplyForm(rootId) {
		    const formElement = document.getElementById('reply-form-' + rootId);
		    const replyTextArea = document.getElementById('reply-content-' + rootId);
		    
		    if(formElement) {
		        formElement.style.display = 'none';
		        formElement.removeAttribute('data-target-user');
		    }
		    if(replyTextArea) {
		        replyTextArea.value = '';
		    }
		}
		// '@' 태그된 대댓글 들여쓰기 처리
		function applyNestedReplyIndentation() {
		    document.querySelectorAll('.reply-comment .comment-body').forEach(function(body) {
		        if (body.innerText.trim().startsWith('@')) {
		            const wrapper = body.closest('.reply-wrapper');
		            if (wrapper) {
		                wrapper.style.marginLeft = '40px'; // 들여쓰기 픽셀 조정 가능
		            }
		        }
		    });
		}

		// 최초 화면 로드 시 실행
		document.addEventListener("DOMContentLoaded", function() {
		    applyNestedReplyIndentation();
		    // ... 기존 코드 ...
		});
		

	  