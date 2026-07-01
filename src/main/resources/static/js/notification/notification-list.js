(function () {
    'use strict';

    function csrfHeader() {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        var headers = {};
        if (token && header) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    // 헤더 종 아이콘 옆 뱃지를 실시간으로 갱신 (notification-header.js가 그리는 영역을 직접 갱신)
    function updateHeaderBadge(count) {
        var badge = document.getElementById('notifUnreadBadge');
        if (!badge) return;
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : String(count);
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }

    // 현재 페이지의 필터 상태 (all / read / unread)
    var alarmPage = document.querySelector('.alarm-page');
    var currentStatus = alarmPage ? alarmPage.getAttribute('data-status') : 'all';

    var totalCountLabel = document.getElementById('alarmTotalCount');

    function updateTotalCountLabel(delta) {
        if (!totalCountLabel) return;
        var current = parseInt(totalCountLabel.textContent, 10);
        if (isNaN(current)) return;
        totalCountLabel.textContent = (current + delta) + '건';
    }

    function showEmptyMessageIfNeeded() {
        var list = document.querySelector('.alarm-list');
        if (list && list.children.length === 0) {
            var emptyMsg = document.createElement('div');
            emptyMsg.className = 'alarm-empty-message';
            emptyMsg.textContent = '조회된 알림이 없습니다.';
            list.parentNode.insertBefore(emptyMsg, list);
            list.remove();
        }
    }

    // confirm()을 대체하는 확인/취소 토스트 (화면 중앙). onConfirm은 '예'를 눌렀을 때만 실행됨
    function showConfirmToast(message, onConfirm) {
        var overlay = document.createElement('div');
        overlay.className = 'notif-center-overlay';

        var toast = document.createElement('div');
        toast.className = 'notif-toast notif-toast-confirm notif-toast-center';
        toast.innerHTML =
            '<div class="notif-toast-header">' +
                '<span class="notif-toast-tag">확인</span>' +
            '</div>' +
            '<div class="notif-toast-content"></div>' +
            '<div class="notif-toast-actions">' +
                '<button type="button" class="notif-toast-btn notif-toast-btn-cancel">취소</button>' +
                '<button type="button" class="notif-toast-btn notif-toast-btn-confirm">확인</button>' +
            '</div>';

        toast.querySelector('.notif-toast-content').textContent = message;

        function close() {
            overlay.remove();
        }

        toast.querySelector('.notif-toast-btn-cancel').addEventListener('click', close);
        toast.querySelector('.notif-toast-btn-confirm').addEventListener('click', function () {
            close();
            onConfirm();
        });
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) close(); // 바깥 영역 클릭 시 취소
        });

        overlay.appendChild(toast);
        document.body.appendChild(overlay);
    }

    // alert()를 대체하는 결과 안내 토스트 (success / error, 화면 중앙). 일정 시간 후 자동으로 사라짐
    function showResultToast(message, type) {
        var overlay = document.createElement('div');
        overlay.className = 'notif-center-overlay';

        var toast = document.createElement('div');
        toast.className = 'notif-toast notif-toast-result notif-toast-center ' +
            (type === 'error' ? 'notif-toast-result-error' : 'notif-toast-result-success');
        toast.innerHTML =
            '<div class="notif-toast-header">' +
                '<span class="notif-toast-tag">' + (type === 'error' ? '오류' : '완료') + '</span>' +
                '<button type="button" class="notif-toast-close" aria-label="닫기">&times;</button>' +
            '</div>' +
            '<div class="notif-toast-content"></div>';

        toast.querySelector('.notif-toast-content').textContent = message;

        function close() {
            overlay.remove();
        }

        toast.querySelector('.notif-toast-close').addEventListener('click', close);
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) close();
        });

        overlay.appendChild(toast);
        document.body.appendChild(overlay);

        setTimeout(close, 4000);
    }

    // 행 클릭 시 읽음 처리 + 이동 (토글 스위치 클릭은 제외)
    document.querySelectorAll('.alarm-row-main').forEach(function (main) {
        main.addEventListener('click', function () {
            var row = main.closest('.alarm-row');
            var url = row.getAttribute('data-go-url');
            if (url) {
                window.location.href = url;
            }
        });
    });

    // 개별 읽음/읽지 않음 토글
    document.querySelectorAll('.alarm-toggle-input').forEach(function (input) {
        input.addEventListener('click', function (e) {
            e.stopPropagation();
        });

        input.addEventListener('change', function () {
            var alarmId = input.getAttribute('data-alarm-id');
            var row = input.closest('.alarm-row');
            var statusLabel = row.querySelector('.alarm-status-label');

            fetch('/notification/' + alarmId + '/toggle', {
                method: 'POST',
                headers: csrfHeader()
            })
                .then(function (res) {
                    if (!res.ok) throw new Error('toggle failed');
                    return res.json();
                })
                .then(function (data) {
                    var isUnread = data.checkYn === 'N';

                    // 현재 필터(읽음/읽지 않음)와 토글 후 상태가 맞지 않으면 화면에서 제거
                    var noLongerMatchesFilter =
                        (currentStatus === 'unread' && !isUnread) ||
                        (currentStatus === 'read' && isUnread);

                    if (noLongerMatchesFilter) {
                        row.remove();
                        updateTotalCountLabel(-1);
                        showEmptyMessageIfNeeded();
                    } else {
                        input.checked = isUnread;
                        row.classList.toggle('alarm-row-unread', isUnread);
                        statusLabel.textContent = isUnread ? '읽지 않음' : '읽음';
                    }

                    updateHeaderBadge(data.unreadCount);
                })
                .catch(function () {
                    // 실패 시 원래 상태로 되돌림
                    input.checked = !input.checked;
                    showResultToast('상태 변경에 실패했습니다. 다시 시도해 주세요.', 'error');
                });
        });
    });

    // 모두 읽음
    var readAllBtn = document.getElementById('readAllBtn');
    if (readAllBtn) {
        readAllBtn.addEventListener('click', function () {
            showConfirmToast('조회된 알림을 모두 읽음 처리하시겠습니까?', function () {
                fetch('/notification/read-all', {
                    method: 'POST',
                    headers: csrfHeader()
                })
                    .then(function (res) {
                        if (!res.ok) throw new Error('read-all failed');
                        return res.json();
                    })
                    .then(function () {
                        showResultToast('모든 알림을 읽음 처리했습니다.', 'success');
                        setTimeout(function () {
                            window.location.reload();
                        }, 800);
                    })
                    .catch(function () {
                        showResultToast('처리에 실패했습니다. 다시 시도해 주세요.', 'error');
                    });
            });
        });
    }
})();