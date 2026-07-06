var searchTimer   = null;
var deleteTargets = [];
var currentTab    = 'user';
var pendingRoleChanges = {}; // { memberId: roleId }

/* ══ 토스트 (task-detail.js와 동일한 방식으로 통일) ══ */
function showToast(message, type) {
    var toastWrap = document.getElementById('dynamicToast');
    if (toastWrap) toastWrap.remove();

    toastWrap = document.createElement('div');
    toastWrap.id = 'dynamicToast';
    toastWrap.className = 'toast-wrap';

    var cls = (type === 'error' || type === 'warning') ? 'toast-' + type : 'toast-success';
    toastWrap.innerHTML = '<div class="toast-msg ' + cls + '">' + message + '</div>';
    document.body.appendChild(toastWrap);

    setTimeout(function () {
        if (document.body.contains(toastWrap)) toastWrap.remove();
    }, 3500);
}

/* ══ 체크박스 ══ */
function toggleCheckAll(chk) {
    document.querySelectorAll('.mem-row-chk').forEach(function(c){ c.checked = chk.checked; });
    syncDeleteBtn();
}
function onRowCheck() {
    var all = document.querySelectorAll('.mem-row-chk');
    var chk = document.querySelectorAll('.mem-row-chk:checked');
    document.getElementById('checkAll').checked = all.length > 0 && chk.length === all.length;
    syncDeleteBtn();
}
function syncDeleteBtn() {
    var cnt = document.querySelectorAll('.mem-row-chk:checked').length;
    var btn = document.getElementById('deleteSelectedBtn');
    btn.textContent = cnt > 0 ? '삭제 (' + cnt + ')' : '삭제';
    btn.classList.toggle('disabled', cnt === 0);
}

/* ══ 선택 삭제 ══ */
function confirmDeleteSelected() {
    var checks = document.querySelectorAll('.mem-row-chk:checked');
    if (!checks.length) return;
    deleteTargets = [];
    checks.forEach(function(cb) {
        deleteTargets.push({ memberId: cb.value, projectId: cb.getAttribute('data-project-id') || String(projectId) });
    });
    document.getElementById('deleteConfirmModal').style.display = 'flex';
}
function closeDeleteModal() {
    document.getElementById('deleteConfirmModal').style.display = 'none';
    deleteTargets = [];
}
function executeDelete() {
    if (!deleteTargets.length) return;
    var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    headers[csrfHeader] = csrfToken;
    var chain = Promise.resolve();
    deleteTargets.forEach(function(t) {
        chain = chain.then(function() {
            var p = new URLSearchParams();
            p.append('memberId', t.memberId);
            p.append('projectId', t.projectId);
            return fetch('/project/settings/members/delete', { method:'POST', headers:headers, body:p.toString() })
                .then(function(r){ if (!r.ok) throw new Error(); });
        });
    });
    chain.then(function() {
        showToast('삭제되었습니다.', 'success');
        closeDeleteModal();
        setTimeout(function(){ location.reload(); }, 800);
    }).catch(function(){ showToast('삭제 중 오류가 발생했습니다.', 'error'); });
}

/* ══ 추가 모달 ══ */
function openAddModal() {
    document.getElementById('addMemberModal').style.display = 'flex';
    document.getElementById('userSearchInput').value = '';
    document.querySelectorAll('input[name="selectedRole"]').forEach(function(r){ r.checked = false; });
    document.querySelectorAll('input[name="selectedGroup"]').forEach(function(r){ r.checked = false; });
    document.getElementById('groupUserListWrap').innerHTML = '<p class="modal-empty">그룹을 선택해주세요.</p>';
    switchTab('user');
    searchUsers('');
}
function closeAddModal() { document.getElementById('addMemberModal').style.display = 'none'; }

/* ══ 탭 ══ */
function switchTab(tab) {
    currentTab = tab;
    document.getElementById('panelUser').style.display  = tab === 'user'  ? '' : 'none';
    document.getElementById('panelGroup').style.display = tab === 'group' ? '' : 'none';
    document.getElementById('tabUser').classList.toggle('active',  tab === 'user');
    document.getElementById('tabGroup').classList.toggle('active', tab === 'group');
}

/* ══ 사용자 검색 ══ */
function searchUsers(kw) {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(function() {
        fetch('/project/settings/members/search?projectId=' + projectId + '&keyword=' + encodeURIComponent(kw))
            .then(function(r){ return r.json(); })
            .then(function(list){ renderChips(list, 'userListWrap', 'selectedUser', true); })
            .catch(function(){ renderChips([], 'userListWrap', 'selectedUser', true); });
    }, 250);
}

/* ══ 그룹 검색 ══ */
function filterGroups(keyword) {
    var kw = keyword.trim().toLowerCase();
    document.querySelectorAll('#groupListWrap .chip-item').forEach(function(item) {
        var name = (item.getAttribute('data-group-name') || '').toLowerCase();
        item.style.display = (!kw || name.indexOf(kw) !== -1) ? '' : 'none';
    });
}

/* ══ 그룹 내 사용자 ══ */
function loadGroupUsers(groupId) {
    document.getElementById('groupUserListWrap').innerHTML = '<p class="modal-empty">불러오는 중...</p>';
    document.getElementById('groupToggleAllBtn').textContent = '전체 선택';
    fetch('/project/settings/members/group?groupId=' + groupId + '&projectId=' + projectId)
        .then(function(r){ return r.json(); })
        .then(function(list){ renderChips(list, 'groupUserListWrap', 'selectedGroupUser', true); })
        .catch(function(){ renderChips([], 'groupUserListWrap', 'selectedGroupUser', true); });
}

/* ══ 그룹 내 사용자 전체 선택/해제 ══ */
function toggleAllGroupUsers() {
    var checks = document.querySelectorAll('#groupUserListWrap input[name="selectedGroupUser"]');
    if (!checks.length) return;
    var allChecked = Array.prototype.every.call(checks, function(c){ return c.checked; });
    checks.forEach(function(c){ c.checked = !allChecked; });
    document.getElementById('groupToggleAllBtn').textContent = allChecked ? '전체 선택' : '전체 해제';
}

function renderChips(list, wrapId, name, showAvatar) {
    var wrap = document.getElementById(wrapId);
    if (!list || !list.length) { wrap.innerHTML = '<p class="modal-empty">검색 결과가 없습니다.</p>'; return; }
    wrap.innerHTML = list.map(function(u) {
        var img = showAvatar ? '<img src="' + (u.profileImage||'/images/default-profile.svg') + '" class="chip-avatar" onerror="this.src=\'/images/default-profile.svg\'">' : '';
        var already = u.isAlreadyMember === true || u.isAlreadyMember === 1;
        var checkedAttr  = already ? 'checked' : '';
        var extraClass   = already ? ' chip-already' : '';
        var memberIdAttr = u.memberId != null ? u.memberId : '';
        return '<label class="chip-item chip-check' + extraClass + '">'
        + '<input type="checkbox" name="' + name + '" value="' + u.userCode + '" '
        + checkedAttr + ' data-already="' + (already ? '1' : '0') + '" data-member-id="' + memberIdAttr + '">'
        + img + '<span>' + u.userName + (already && u.roleName ? ' <small style="opacity:.6">('+u.roleName+')</small>' : '') + '</span></label>';
    }).join('');
}

/* ══ 등록 ══ */
function submitAddMember() {
    var roleInput = document.querySelector('input[name="selectedRole"]:checked');

    var checkboxSelector = currentTab === 'user'
        ? '#userListWrap input[name="selectedUser"]'
        : '#groupUserListWrap input[name="selectedGroupUser"]';

    var toAdd = [];
    var toRemove = [];

    document.querySelectorAll(checkboxSelector).forEach(function(cb) {
        var wasAlready = cb.getAttribute('data-already') === '1';
        var isChecked  = cb.checked;

        if (isChecked && !wasAlready) {
            toAdd.push(cb.value);
        } else if (!isChecked && wasAlready) {
            toRemove.push({ memberId: cb.getAttribute('data-member-id') });
        }
        // 체크 유지 or 미체크 유지 → 아무 것도 안 함 (member_id 그대로 보존)
    });

    if (!toAdd.length && !toRemove.length) {
        showToast('변경된 내용이 없습니다.', 'warning');
        return;
    }
    if (toAdd.length && !roleInput) {
        showToast('새로 추가할 구성원의 역할을 선택해주세요.', 'warning');
        return;
    }

    var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    headers[csrfHeader] = csrfToken;
    var chain = Promise.resolve();

    toAdd.forEach(function(code) {
        chain = chain.then(function() {
            var p = new URLSearchParams();
            p.append('projectId', String(projectId));
            p.append('userCode', code);
            p.append('roleId', roleInput.value);
            return fetch('/project/settings/members/add', { method:'POST', headers:headers, body:p.toString() })
                .then(function(r){ if (!r.ok) throw new Error(); });
        });
    });

    toRemove.forEach(function(t) {
        chain = chain.then(function() {
            var p = new URLSearchParams();
            p.append('memberId', t.memberId);
            p.append('projectId', String(projectId));
            return fetch('/project/settings/members/delete', { method:'POST', headers:headers, body:p.toString() })
                .then(function(r){ if (!r.ok) throw new Error(); });
        });
    });

    chain.then(function() {
        showToast('구성원 정보가 저장되었습니다.', 'success');
        closeAddModal();
        setTimeout(function(){ location.reload(); }, 800);
    }).catch(function(){ showToast('저장 중 오류가 발생했습니다.', 'error'); });
}

/* ══ 외부 클릭 닫기 ══ */
document.addEventListener('click', function(e) {
    if (e.target.id === 'addMemberModal')     closeAddModal();
    if (e.target.id === 'deleteConfirmModal') closeDeleteModal();
});

/* ══ 역할 편집 (배치 저장) ══ */
function onRoleSelectChange(sel) {
    var memberId     = sel.getAttribute('data-member-id');
    var originalRole = sel.getAttribute('data-original-role') || '';
    var newRole      = sel.value || '';

    if (newRole === originalRole) {
        delete pendingRoleChanges[memberId];
        sel.classList.remove('role-changed');
    } else {
        pendingRoleChanges[memberId] = newRole;
        sel.classList.add('role-changed');
    }
    syncSaveRolesBtn();
}

function syncSaveRolesBtn() {
    var btn = document.getElementById('saveRolesBtn');
    if (!btn) return;
    var cnt = Object.keys(pendingRoleChanges).length;
    btn.textContent = cnt > 0 ? '저장 (' + cnt + ')' : '저장';
    btn.classList.toggle('disabled', cnt === 0);
}

function saveMemberRoles() {
    var memberIds = Object.keys(pendingRoleChanges);
    if (!memberIds.length) return;

    var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    headers[csrfHeader] = csrfToken;
    var chain = Promise.resolve();
    var failed = false;

    memberIds.forEach(function(memberId) {
        chain = chain.then(function() {
            var p = new URLSearchParams();
            p.append('memberId', memberId);
            p.append('projectId', String(projectId));
            p.append('roleId', pendingRoleChanges[memberId]);
            return fetch('/project/settings/members/updateRole', { method:'POST', headers:headers, body:p.toString() })
                .then(function(r){ if (!r.ok) throw new Error(); })
                .catch(function(){ failed = true; });
        });
    });

    chain.then(function() {
        if (failed) {
            showToast('일부 역할 변경 중 오류가 발생했습니다.', 'error');
        } else {
            showToast('역할이 저장되었습니다.', 'success');
        }
        pendingRoleChanges = {};
        setTimeout(function(){ location.reload(); }, 800);
    });
}