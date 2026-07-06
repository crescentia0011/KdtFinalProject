(function () {
    'use strict';

    var POLL_INTERVAL_MS = 5000;
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

    // 마이페이지(Dv-044) "실시간 웹 알림 수신" 토글. N이면 새 알림을 자동으로 감지해
    // 배지를 갱신하거나 토스트를 띄우지 않는다. (종 아이콘을 눌러 직접 확인하는 것은 항상 가능)
    var WEB_NOTIF_ENABLED = notifWrap ? notifWrap.getAttribute('data-web-notif-yn') !== 'N' : true;

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

    var POPOVER_CACHE_KEY = 'weple_notif_popover_html';

    function getCachedPopover() {
        try { return sessionStorage.getItem(POPOVER_CACHE_KEY); } catch (e) { return null; }
    }
    function setCachedPopover(html) {
        try { sessionStorage.setItem(POPOVER_CACHE_KEY, html); } catch (e) {}
    }

    function closeDropdown() {
        dropdown.style.display = 'none';
    }

    function openDropdown() {
        dropdown.style.display = 'block';

        // sessionStorage 캐시가 있으면 즉시 표시 (페이지 이동 후에도 유지됨)
        var cached = getCachedPopover();
        if (cached) {
            dropdown.innerHTML = cached;
        } else {
            dropdown.innerHTML =
                '<div class="notif-dropdown-loading">' +
                    '<span class="notif-loading-dot"></span>' +
                    '<span class="notif-loading-dot"></span>' +
                    '<span class="notif-loading-dot"></span>' +
                '</div>';
        }

        // 백그라운드에서 항상 최신 데이터로 갱신
        fetch('/notification/popover')
            .then(function (res) { return res.text(); })
            .then(function (html) {
                setCachedPopover(html);
                if (dropdown.style.display === 'block') {
                    dropdown.innerHTML = html;
                }
            })
            .catch(function () {
                if (dropdown.style.display === 'block') {
                    dropdown.innerHTML = '<div class="notif-dropdown-empty">알림을 불러오지 못했습니다.</div>';
                }
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

    var TOAST_TAG_COLORS = {
        '일감 배정':    { border: '#3B82F6', color: '#3B82F6' },
        '상태 변경':    { border: '#22C55E', color: '#22C55E' },
        '댓글 등록':    { border: '#F59E0B', color: '#F59E0B' },
        '첨부파일 등록': { border: '#6D5EF7', color: '#6D5EF7' },
        '프로젝트 초대': { border: '#EF4444', color: '#EF4444' }
    };

    var MAX_VISIBLE_TOASTS = 4;

	function showToast(alarm) {
	    // 토스트가 너무 많이 쌓이지 않도록 오래된 것부터 정리 (맨 아래 = 가장 오래된 것)
	    while (toastContainer.children.length >= MAX_VISIBLE_TOASTS) {
	        toastContainer.removeChild(toastContainer.lastElementChild);
	    }

	    var tag = alarm.alarmTag || '알림';
	    var tagStyle = TOAST_TAG_COLORS[tag] || { border: '#6D5EF7', color: '#6D5EF7' };

	    var toast = document.createElement('div');
	    toast.className = 'notif-toast';
	    toast.style.borderLeftColor = tagStyle.border;
	    toast.innerHTML =
	        '<div class="notif-toast-header">' +
	            '<span class="notif-toast-tag" style="color:' + tagStyle.color + '">' + tag + '</span>' +
	            '<button type="button" class="notif-toast-close" aria-label="닫기">&times;</button>' +
	        '</div>' +
	        '<div class="notif-toast-content"></div>' +
	        '<div class="notif-toast-time"></div>';

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

	    // 최신 알림이 맨 위로 쌓이도록 컨테이너 맨 앞에 삽입 (컨테이너는 화면 하단 고정 + column 방향이라
	    // 맨 앞(첫 자식)이 시각적으로 가장 위에 위치함)
	    toastContainer.insertBefore(toast, toastContainer.firstChild);

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

	            var list = data.latestList || [];
	            if (!list.length) return;

	            var lastSeen = getLastSeenAlarmId();
	            var newestId = list[0].alarmId;

	            // list는 최신순(0=가장 최근)으로 오므로, 아직 안 본 것만 골라서
	            // 오래된 것부터 순서대로 토스트를 띄워야 최종적으로 가장 최근 알림이 맨 위에 남는다.
	            var unseen = list.filter(function (a) {
	                return lastSeen === null || a.alarmId > lastSeen;
	            });

	            if (unseen.length) {
	                // 팝오버 캐시 무효화 (다음에 종 클릭하면 최신 목록으로 갱신됨)
	                try { sessionStorage.removeItem(POPOVER_CACHE_KEY); } catch (e) {}

	                unseen.slice().reverse().forEach(function (alarm) {
	                    if (isFresh(alarm.alarmDate)) {
	                        showToast(alarm);
	                    }
	                });
	            }

	            setLastSeenAlarmId(newestId);
	        })
	        .catch(function () {
	            // 폴링 실패는 조용히 무시 (다음 주기에 재시도)
	        });
	}

    function isFresh(alarmDateStr) {
        if (!alarmDateStr) return false;
        var alarmTime = new Date(alarmDateStr).getTime();
        if (isNaN(alarmTime)) return false;
        var diff = Date.now() - alarmTime;
        // diff가 음수라는 건 알림 시각이 "미래"라는 뜻 -> 서버/DB 시간 오차 등 비정상 상황이므로
        // 신선한 알림으로 취급하지 않는다. (과거 이 방어 코드가 없어서 알림 시각이 미래로 잘못
        // 저장되었을 때 몇 시간이 지나도 팝업이 계속 뜨는 버그가 있었음 - 근본 원인은 SQL 수정으로 해결됨)
        return diff >= 0 && diff <= TOAST_FRESHNESS_MS;
    }

    if (WEB_NOTIF_ENABLED) {
        poll();
        setInterval(poll, POLL_INTERVAL_MS);
    }
})();