
        document.addEventListener("DOMContentLoaded", function () {
        	const tableBody = document.getElementById("sortable-list");
        	
        	// 1. 드래그 앤 드롭 정렬
        	Sortable.create(tableBody, {
        		animation: 150
        		, handle : '.drag-handle'
        		, onEnd: function() {
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
        				if (!response.ok) alert("순서 저장 중 오류가 발생했습니다.");
        			})
        			.catch(error => {
        				console.error("Error:", error);
        				alert("서버와 통신할 수 없습니다.");
        			});
        		}
        	});

        	// 2. 비동기 삭제 기능 추가 (이벤트 위임 방식)
        	tableBody.addEventListener("click", function(e) {
        		// 클릭된 요소가 삭제 버튼이거나 삭제 버튼의 자식(아이콘 등)일 때
        		const deleteBtn = e.target.closest(".btn-delete");
        		if (!deleteBtn) return;

        		const row = deleteBtn.closest("tr");
        		const typeId = row.getAttribute("data-id");
        		const typeName = row.querySelector("td").textContent.trim();

        		if (confirm(`[${typeName}] 일감 유형을 정말로 삭제하시겠습니까?`)) {
        			const token = document.getElementById("_csrf").getAttribute("content");
        		    const header = document.getElementById("_csrf_header").getAttribute("content");
        			
        			fetch(`/system/taskType/delete/${typeId}`, {
        				method: 'POST',
        				headers: {
        		            'Content-Type': 'application/json',
        		            [header]: token 
        		        }
        			})
        			.then(response => {
        				if (response.ok) {
        					alert("성공적으로 삭제되었습니다.");
        					row.remove();
        				} else {
        					alert("삭제 중 오류가 발생했습니다.");
        				}
        			})
        			.catch(error => {
        				console.error("Error:", error);
        				alert("서버와 통신할 수 없습니다.");
        			});
        		}
        	});
        });