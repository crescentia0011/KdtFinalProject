(function () {
    'use strict';

    // ------------------------------------------------------------
    // Dv-045 프로필 사진 관리
    // 화면설명: "사진 변경 버튼을 클릭하기 전까지 파일 첨부 영역은 표시하지 않는다."
    //          "프로필 사진 변경 및 삭제는 등록(적용) 버튼 클릭 시 최종 반영된다."
    // ------------------------------------------------------------

    var form = document.getElementById('profileForm');
    if (!form) {
        return;
    }

    var fileInput = document.getElementById('profileFileInput');
    var changeBtn = document.getElementById('profileChangeBtn');
    var deleteBtn = document.getElementById('profileDeleteBtn');
    var removeFlagInput = document.getElementById('profileRemoveFlag');
    var previewWrap = document.getElementById('profilePreviewWrap');
    var originalImageUrl = previewWrap ? previewWrap.getAttribute('data-original-image') : '';

    var DEFAULT_PROFILE_IMAGE = '/images/default-profile.svg';
    var ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif'];

    function extensionOf(fileName) {
        var idx = fileName.lastIndexOf('.');
        return idx === -1 ? '' : fileName.substring(idx + 1).toLowerCase();
    }

    function renderPreview(src) {
        if (!previewWrap) return;

        previewWrap.innerHTML =
            '<img src="' + (src || DEFAULT_PROFILE_IMAGE) + '" alt="프로필 미리보기">';
    }

    // 사진변경 버튼
    if (changeBtn && fileInput) {
        changeBtn.addEventListener('click', function () {
            fileInput.click();
        });
    }

    // 파일 선택
    if (fileInput) {
        fileInput.addEventListener('change', function () {
            var file = fileInput.files && fileInput.files[0];
            if (!file) return;

            var ext = extensionOf(file.name);

            if (ALLOWED_EXTENSIONS.indexOf(ext) === -1) {
                window.alert('이미지 파일(jpg, jpeg, png, gif)만 등록할 수 있습니다.');
                fileInput.value = '';
                return;
            }

            if (removeFlagInput) {
                removeFlagInput.value = 'N';
            }

            var reader = new FileReader();
            reader.onload = function (e) {
                renderPreview(e.target.result);
            };
            reader.readAsDataURL(file);
        });
    }

    // 삭제 버튼
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function () {
            if (fileInput) {
                fileInput.value = '';
            }

            if (removeFlagInput) {
                removeFlagInput.value = 'Y';
            }

            renderPreview(null);
        });
    }

    // 등록 버튼
    form.addEventListener('submit', function (e) {
        var hasNewFile = fileInput && fileInput.files && fileInput.files.length > 0;
        var willRemove = removeFlagInput && removeFlagInput.value === 'Y';

        if (!hasNewFile && !willRemove) {
            e.preventDefault();
            window.location.href = form.getAttribute('data-cancel-url') || '/mypage';
        }
    });
})();