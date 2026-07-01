(function () {
    'use strict';

    var projectId = document.getElementById('projectId').value;
    var currentUserCode = document.getElementById('currentUserCode').value;
    var saveBtn = document.getElementById('saveBtn');
    var resetBtn = document.getElementById('resetBtn');

    // taskId -> { taskStatusId, fromStatusId }
    var pendingChanges = {};

    function csrfHeader() {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        var headers = {};
        if (token && header) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    function updateColumnCounts() {
        document.querySelectorAll('.kanban-column').forEach(function (col) {
            var list = col.querySelector('.kanban-card-list');
            var countEl = col.querySelector('.kanban-column-count');
            if (list && countEl) {
                countEl.textContent = list.querySelectorAll('.kanban-card').length;
            }
        });
    }

    function toggleSaveButton() {
        saveBtn.disabled = Object.keys(pendingChanges).length === 0;
    }

    function showAlert(message) {
        // 간단한 알림. 프로젝트 공통 토스트가 있다면 그쪽으로 교체 가능.
        alert(message);
    }

    // 드래그 가능한 카드 목록(.kanban-card-list)마다 Sortable 적용
    document.querySelectorAll('.kanban-card-list').forEach(function (list) {
        new Sortable(list, {
            group: 'kanban',
            animation: 150,
            ghostClass: 'dragging',
            emptyInsertThreshold: 20,

            onMove: function (evt) {
                // 빈 컬럼 안내 문구는 드래그 중 가려도 무방
            },

            onEnd: function (evt) {
                var card = evt.item;
                var fromListEl = evt.from;
                var toListEl = evt.to;

                var taskId = card.getAttribute('data-task-id');
                var managerId = card.getAttribute('data-manager-id');
                var fromStatusId = fromListEl.getAttribute('data-status-id');
                var toStatusId = toListEl.getAttribute('data-status-id');

                // 같은 컬럼 내 순서 변경은 상태 변경이 아니므로 무시
                if (fromStatusId === toStatusId) {
                    return;
                }

                // 본인이 담당자가 아닌 일감은 이동 제한
                if (managerId !== currentUserCode) {
                    showAlert('본인이 담당자로 등록된 일감만 상태를 변경할 수 있습니다.');
                    // 원래 컬럼으로 되돌리기
                    if (evt.oldIndex >= fromListEl.children.length) {
                        fromListEl.appendChild(card);
                    } else {
                        fromListEl.insertBefore(card, fromListEl.children[evt.oldIndex]);
                    }
                    updateColumnCounts();
                    return;
                }

                // 변경사항 임시 저장 (등록 누르기 전까지는 화면에서만 이동된 상태)
                var originalStatusId = pendingChanges[taskId]
                    ? pendingChanges[taskId].originalStatusId
                    : fromStatusId;

                if (toStatusId === originalStatusId) {
                    // 원래 컬럼으로 되돌아온 경우 변경사항 제거
                    delete pendingChanges[taskId];
                    card.classList.remove('kanban-card-changed');
                } else {
                    pendingChanges[taskId] = {
                        taskStatusId: toStatusId,
                        originalStatusId: originalStatusId
                    };
                    card.classList.add('kanban-card-changed');
                }

                card.setAttribute('data-status-id', toStatusId);
                updateColumnCounts();
                toggleSaveButton();
            }
        });
    });

    // 등록 버튼 - 변경된 일감들을 배열로 모아 한번에 저장
    saveBtn.addEventListener('click', function () {
        var moveList = Object.keys(pendingChanges).map(function (taskId) {
            return {
                taskId: taskId,
                taskStatusId: pendingChanges[taskId].taskStatusId
            };
        });

        if (moveList.length === 0) {
            return;
        }

        if (!confirm('변경사항을 저장하시겠습니까?')) {
            return;
        }

        fetch('/project/kanban/move', {
            method: 'POST',
            headers: Object.assign(
                { 'Content-Type': 'application/json' },
                csrfHeader()
            ),
            body: JSON.stringify(moveList)
        })
            .then(function (res) {
                return res.json().then(function (body) {
                    return { ok: res.ok, body: body };
                });
            })
            .then(function (result) {
                if (result.ok) {
                    pendingChanges = {};
                    showAlert(result.body.message || '변경사항이 저장되었습니다.');
                    window.location.href = '/project/kanban?projectId=' + projectId;
                } else {
                    showAlert(result.body.message || '저장에 실패했습니다.');
                }
            })
            .catch(function () {
                showAlert('저장 중 오류가 발생했습니다. 다시 시도해 주세요.');
            });
    });

    // 초기화 버튼 - 화면상의 이동을 모두 되돌리고 새로고침
    resetBtn.addEventListener('click', function () {
        if (Object.keys(pendingChanges).length === 0) {
            window.location.reload();
            return;
        }
        if (confirm('변경사항을 초기화하시겠습니까?')) {
            window.location.href = '/project/kanban?projectId=' + projectId;
        }
    });

    // 저장하지 않고 페이지를 벗어나는 경우 경고
    window.addEventListener('beforeunload', function (e) {
        if (Object.keys(pendingChanges).length > 0) {
            e.preventDefault();
            e.returnValue = '';
        }
    });
})();