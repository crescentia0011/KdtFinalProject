(function () {
    // yyyy-MM-dd (네이티브 date input 값) <-> yyyy.MM.dd (서버/매퍼가 기대하는 포맷) 상호 변환
    function toDotFormat(dashStr) {
        return dashStr ? dashStr.replaceAll('-', '.') : '';
    }
    function toDashFormat(date) {
        var y = date.getFullYear();
        var m = String(date.getMonth() + 1).padStart(2, '0');
        var d = String(date.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + d;
    }
    function parseDash(str) {
        if (!str) return null;
        var parts = str.split('-');
        if (parts.length !== 3) return null;
        var d = new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
        return isNaN(d.getTime()) ? null : d;
    }
    function addDays(date, days) {
        var d = new Date(date.getTime());
        d.setDate(d.getDate() + days);
        return d;
    }

    var startInput  = document.getElementById('startDateInput');
    var endInput    = document.getElementById('endDateInput');
    var startHidden = document.getElementById('startDateHidden');
    var endHidden   = document.getElementById('endDateHidden');
    var searchForm  = document.getElementById('searchForm');
    var searchFlag  = document.getElementById('searchFlag');

    function syncHiddenAndPeriod() {
        startHidden.value = toDotFormat(startInput.value);
        endHidden.value   = toDotFormat(endInput.value);

        var periodEl = document.querySelector('.time-period');
        if (periodEl && startHidden.value && endHidden.value) {
            periodEl.textContent = startHidden.value + ' ~ ' + endHidden.value;
        }
    }

    function setRange(start, end) {
        startInput.value = toDashFormat(start);
        endInput.value   = toDashFormat(end);
        endInput.min   = startInput.value;
        startInput.max = endInput.value;
        syncHiddenAndPeriod();
    }

    function applyRecentDays(days) {
        var today = new Date();
        setRange(addDays(today, -(days - 1)), today);
    }

    startInput.addEventListener('change', function () {
        endInput.min = startInput.value;
        syncHiddenAndPeriod();
    });
    endInput.addEventListener('change', function () {
        startInput.max = endInput.value;
        syncHiddenAndPeriod();
    });

    var initStart = parseDash(startInput.value);
    var initEnd   = parseDash(endInput.value);
    if (initStart && initEnd) {
        setRange(initStart, initEnd);
    } else {
        applyRecentDays(5);
    }

    document.getElementById('recentPeriodBtn').addEventListener('click', function () {
        applyRecentDays(5);
        searchFlag.value = 'true';
        searchForm.submit();
    });

    document.getElementById('dateBackBtn').addEventListener('click', function () {
        var curStart = parseDash(startInput.value);
        var curEnd   = parseDash(endInput.value);
        if (!curStart || !curEnd) {
            applyRecentDays(5);
        } else {
            var rangeDays = Math.round((curEnd.getTime() - curStart.getTime()) / (24 * 60 * 60 * 1000)) + 1;
            var newEnd    = addDays(curStart, -1);
            var newStart  = addDays(newEnd, -(rangeDays - 1));
            setRange(newStart, newEnd);
        }
        searchFlag.value = 'true';
        searchForm.submit();
    });

    document.getElementById('resetBtn').addEventListener('click', function () {
        var projectSelect = searchForm.querySelector('select[name="projectId"]');
        if (projectSelect) projectSelect.value = '';
        var userSelect = searchForm.querySelector('select[name="userCode"]');
        if (userSelect) userSelect.value = '';
        searchForm.querySelectorAll('input[type="checkbox"]').forEach(function (cb) {
            cb.checked = false;
        });
        applyRecentDays(5);
        searchFlag.value = 'false';
        searchForm.submit();
    });

    searchForm.addEventListener('submit', syncHiddenAndPeriod);
})();