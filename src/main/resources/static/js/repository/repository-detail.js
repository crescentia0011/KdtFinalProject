(() => {
    const branchSelect = document.getElementById('githubBranch');
    const fileSearchInput = document.getElementById('githubFileSearch');
    const treeItems = document.querySelectorAll('[data-github-tree-item]');
    const emptyResult = document.getElementById('githubFileSearchEmpty');
    const repositoryDetail = document.getElementById('repositoryDetail');
    const commitRows = document.querySelectorAll('[data-github-commit-row]');
    const commitLinks = document.querySelectorAll('[data-github-commit-link]');

    // 외부 GitHub 상세를 연 커밋 SHA를 저장해 새로고침 후에도 열람 표시를 유지함.
    const viewedCommitStorageKey = repositoryDetail && repositoryDetail.dataset.repositoryId
        ? `weple-viewed-github-commits:${repositoryDetail.dataset.repositoryId}`
        : null;
    let storedCommitShas = [];
    if (viewedCommitStorageKey) {
        try {
            storedCommitShas = JSON.parse(localStorage.getItem(viewedCommitStorageKey) || '[]');
        } catch (error) {
            // 브라우저 저장값이 손상된 경우 열람 표시만 초기화하고 커밋 조회 기능은 유지함.
            storedCommitShas = [];
        }
    }
    const viewedCommitShas = new Set(storedCommitShas);

    const markCommitAsViewed = (commitSha) => {
        if (!commitSha || !viewedCommitStorageKey) {
            return;
        }
        viewedCommitShas.add(commitSha);
        localStorage.setItem(viewedCommitStorageKey, JSON.stringify([...viewedCommitShas]));
        commitRows.forEach((row) => {
            if (row.dataset.commitSha === commitSha) {
                row.classList.add('is-github-viewed');
            }
        });
    };

    commitRows.forEach((row) => {
        if (viewedCommitShas.has(row.dataset.commitSha)) {
            row.classList.add('is-github-viewed');
        }
    });
    commitLinks.forEach((link) => {
        link.addEventListener('click', () => {
            markCommitAsViewed(link.closest('[data-github-commit-row]').dataset.commitSha);
        });
    });

    if (branchSelect) {
        // 브랜치가 달라지면 기존 파일 경로가 없을 수 있어 루트부터 다시 조회함.
        branchSelect.addEventListener('change', () => {
            const detailUrl = new URL(window.location.href);
            detailUrl.searchParams.set('branch', branchSelect.value);
            detailUrl.searchParams.delete('directoryPath');
            detailUrl.searchParams.delete('filePath');
            window.location.href = detailUrl.toString();
        });
    }

    if (!fileSearchInput || !emptyResult) {
        return;
    }

    // 현재 API로 불러온 폴더 항목만 즉시 필터링함. 저장소 전체 검색은 별도 API 기능으로 분리 필요함.
    fileSearchInput.addEventListener('input', () => {
        const keyword = fileSearchInput.value.trim().toLowerCase();
        let visibleCount = 0;

        treeItems.forEach((item) => {
            const fileName = item.dataset.githubFileName.toLowerCase();
            const isMatched = !keyword || fileName.includes(keyword);
            // 트리 항목의 display:flex 스타일과 hidden 속성 충돌을 피하려고 표시 상태를 직접 제어함.
            item.style.display = isMatched ? '' : 'none';
            item.setAttribute('aria-hidden', String(!isMatched));
            if (isMatched) {
                visibleCount += 1;
            }
        });

        emptyResult.hidden = treeItems.length === 0 || visibleCount > 0;
    });
})();
