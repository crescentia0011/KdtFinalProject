(function () {
    'use strict';

    var POLL_INTERVAL_MS = 15000;
    var TOAST_AUTO_CLOSE_MS = 5000;

    var bellBtn = document.getElementById('notifBellBtn');
    var dropdown = document.getElementById('notifDropdown');
    var unreadBadge = document.getElementById('notifUnreadBadge');
    var toastContainer = document.getElementById('notifToastContainer');
    var notifWrap = document.getElementById('notifWrap');

    if (!bellBtn || !dropdown || !toastContainer) {
        // 헤더 마크업이 없는 페이지(예: 로그인 화면)에서는 동작하지 않음
        return;
    }

    var LAST_SEEN_KEY = 'weple_notif_last_seen_alarm_id';
    var TOAST_FRESHNESS_MS = 20000; // 알림 발생 후 20초 이내면 '방금 생긴 알림'으로 간주하여 토스트 표시

    function getLastSeenAlarmId() {
        try {
            var v = sessionStorage.getItem(LAST_SEEN_KEY);
            return v ? Number(v) : null;
        } catch (e) {
            return null; // sessionStorage 접근 불가 환경(시크릿 모드 제한 등) 대비
        }
    }

    function setLastSeenAlarmId(id) {
        try {
            sessionStorage.setItem(LAST_SEEN_KEY, String(id));
        } catch (e) {
            // 저장 실패 시에도 동작은 계속되어야 하므로 무시
        }
    }

    function csrfHeader() {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        var headers = {};
        if (token && header && token.content && header.content) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    function updateBadge(count) {
        if (!unreadBadge) return;
        if (count > 0) {
            unreadBadge.textContent = count > 99 ? '99+' : String(count);
            unreadBadge.style.display = 'flex';
        } else {
            unreadBadge.style.display = 'none';
        }
    }

    function closeDropdown() {
        dropdown.style.display = 'none';
    }

    function openDropdown() {
        dropdown.style.display = 'block';
        fetch('/notification/popover')
            .then(function (res) { return res.text(); })
            .then(function (html) {
                dropdown.innerHTML = html;
            })
            .catch(function () {
                dropdown.innerHTML = '<div class="notif-dropdown-empty">알림을 불러오지 못했습니다.</div>';
            });
    }

    bellBtn.addEventListener('click', function (e) {
        e.preventDefault();
        var isOpen = dropdown.style.display === 'block';
        if (isOpen) {
            closeDropdown();
        } else {
            openDropdown();
        }
    });

    // 바깥 영역 클릭 시 닫기
    document.addEventListener('click', function (e) {
        if (notifWrap && !notifWrap.contains(e.target)) {
            closeDropdown();
        }
    });

    var MAX_VISIBLE_TOASTS = 4;

    function showToast(alarm) {
        // 토스트가 너무 많이 쌓이지 않도록 오래된 것부터 정리
        while (toastContainer.children.length >= MAX_VISIBLE_TOASTS) {
            toastContainer.removeChild(toastContainer.firstElementChild);
        }

        var toast = document.createElement('div');
        toast.className = 'notif-toast';
        toast.innerHTML =
            '<div class="notif-toast-header">' +
                '<span class="notif-toast-tag">' + (alarm.alarmTag || '알림') + '</span>' +
                '<button type="button" class="notif-toast-close" aria-label="닫기">&times;</button>' +
            '</div>' +
            '<div class="notif-toast-content"></div>' +
            '<div class="notif-toast-time"></div>';

        // alarmContent는 서버(AlarmTextUtil)에서 <strong> 강조 태그만 포함하도록
        // 생성되며, 사용자 입력값(일감명 등)은 서버단에서 이미 HTML 이스케이프 처리되어 있음
        toast.querySelector('.notif-toast-content').innerHTML = alarm.alarmContent || '';
        toast.querySelector('.notif-toast-time').textContent = alarm.relativeTime || '방금 전';

        toast.addEventListener('click', function (e) {
            if (e.target.classList.contains('notif-toast-close')) {
                e.stopPropagation();
                toast.remove();
                return;
            }
            window.location.href = '/notification/' + alarm.alarmId + '/go';
        });

        toastContainer.appendChild(toast);

        setTimeout(function () {
            if (toast.parentNode) toast.remove();
        }, TOAST_AUTO_CLOSE_MS);
    }

    function poll() {
        fetch('/notification/latest')
            .then(function (res) {
                if (!res.ok) throw new Error('poll failed');
                return res.json();
            })
            .then(function (data) {
                updateBadge(data.unreadCount);

                if (!data.latest) {
                    return;
                }

                var alarmId = data.latest.alarmId;

                // 이미 토스트로 보여줬거나 확인한 알림이면 다시 띄우지 않음 (다른 페이지로 이동해도 유지됨)
                if (alarmId === getLastSeenAlarmId()) {
                    return;
                }
                setLastSeenAlarmId(alarmId);

                // 페이지 진입/리다이렉트 시점과 무관하게, 발생한 지 얼마 안 된(신선한) 알림이면 토스트 표시.
                // 오래된 알림(예: 페이지를 새로 열었는데 한참 전 알림이 최신인 경우)은 토스트를 띄우지 않음
                if (isFresh(data.latest.alarmDate)) {
                    showToast(data.latest);
                }
            })
            .catch(function () {
                // 폴링 실패는 조용히 무시 (다음 주기에 재시도)
            });
    }

    function isFresh(alarmDateStr) {
        if (!alarmDateStr) return false;
        var alarmTime = new Date(alarmDateStr).getTime();
        if (isNaN(alarmTime)) return false;
        return (Date.now() - alarmTime) <= TOAST_FRESHNESS_MS;
    }

    poll();
    setInterval(poll, POLL_INTERVAL_MS);
})();