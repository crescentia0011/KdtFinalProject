(() => {
    const searchInput = document.getElementById('repositorySearch');
    const repositoryRows = document.querySelectorAll('[data-repository-row]');
    const emptyResultRow = document.getElementById('repositorySearchEmpty');

    if (!searchInput || !emptyResultRow) {
        return;
    }

    // 입력·붙여넣기·모바일 자동완성 모두 반영하기 위해 keydown 대신 input 이벤트 사용함.
    searchInput.addEventListener('input', () => {
        const keyword = searchInput.value.trim().toLowerCase();
        let visibleCount = 0;

        repositoryRows.forEach((row) => {
            const repositoryName = row.dataset.repositoryName.toLowerCase();
            const repositoryUrl = row.dataset.repositoryUrl.toLowerCase();
            const isMatched = !keyword || repositoryName.includes(keyword) || repositoryUrl.includes(keyword);
            row.hidden = !isMatched;
            if (isMatched) {
                visibleCount += 1;
            }
        });

        emptyResultRow.hidden = repositoryRows.length === 0 || visibleCount > 0;
    });
})();
