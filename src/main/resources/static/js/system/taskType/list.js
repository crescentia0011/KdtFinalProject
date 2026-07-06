document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("sortable-list");
    
    if (!tableBody) return;

    // 1. 드래그 앤 드랍 정렬 (SortableJS)
    Sortable.create(tableBody, {
        animation: 150,
        handle: '.drag-handle',
        onEnd: function() {
            const rows = tableBody.querySelectorAll('tr');
            const sortedIds = Array.from(rows).map(row => parseInt(row.getAttribute('data-id')));
            
            const token = document.getElementById("_csrf").getAttribute("content");
            const header = document.getElementById("_csrf_header").getAttribute("content");
            
            fetch('/system/taskType/reorder', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    [header]: token 
                },
                body: JSON.stringify(sortedIds)
            })
            .then(response => {
                if (!response.ok) {
                    showToast("순서 저장 중 오류가 발생했습니다.", true);
                }
            })
            .catch(error => {
                console.error("Error:", error);
                showToast("서버와 통신할 수 없습니다.", true);
            });
        }
    });

    // 2. 비동기 삭제 기능 (showConfirm 커스텀 모달 반영)
    tableBody.addEventListener('click', function (event) {
        const deleteBtn = event.target.closest('.delete-btn');
        if (!deleteBtn) return;

        const row = deleteBtn.closest('tr');
        const typeId = row.getAttribute('data-id');
        const typeName = row.querySelector('td').textContent.trim();

        // 커스텀 모달 호출
        showConfirm('일감 유형 삭제', `[${typeName}] 일감 유형을 정말 삭제하시겠습니까?`).then((isConfirmed) => {
            if (isConfirmed) {
                const csrfToken = document.getElementById('_csrf').getAttribute('content');
                const csrfHeader = document.getElementById('_csrf_header').getAttribute('content');

                fetch(`/system/taskType/${typeId}`, {
                    method: 'DELETE',
                    headers: {
                        [csrfHeader]: csrfToken,
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => {
                    if (response.ok) {
                        showToast('성공적으로 삭제되었습니다.', false); 
                        row.remove(); // 화면에서 해당 행 즉시 제거
                    } else {
                        showToast('삭제에 실패했습니다. 다시 시도해 주세요.', true);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('오류가 발생했습니다.', true);
                });
            }
        });
    });

    /**
     * 3. 동적 토스트 생성 함수
     */
    function showToast(message, isError) {
        const toastWrap = document.createElement('div');
        toastWrap.className = 'toast-wrap';
        
        const toastMsg = document.createElement('div');
        toastMsg.className = `toast-msg ${isError ? 'toast-error' : 'toast-success'}`;
        toastMsg.textContent = message;
        
        toastWrap.appendChild(toastMsg);
        document.body.appendChild(toastWrap);
        
        setTimeout(() => {
            toastWrap.remove();
        }, 3500);
    }

    /**
     * 4. 커스텀 삭제 확인 모달 (Promise 기반) - 추가된 부분
     */
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

            // CSS 트랜지션 트리거용 (0.01초 뒤 클래스 추가)
            setTimeout(() => overlay.classList.add('show'), 10);

            const close = (result) => {
                overlay.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(overlay)) overlay.remove();
                    resolve(result); // 사용자가 누른 결과값(true/false)을 반환
                }, 200); 
            };

            document.getElementById('modalCancelBtn').onclick = () => close(false);
            document.getElementById('modalDeleteBtn').onclick = () => close(true);
        });
    }
});