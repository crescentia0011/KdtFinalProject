function redirectToInsertTask() {
    const selectElement = document.getElementById('modalProjectId');
    const selectedId = selectElement.value;

    if (!selectedId) {
        alert('프로젝트를 선택해주세요.');
        return;
    }

    // HTML에서 글로벌 전역 변수로 할당한 타임리프 컨텍스트 주소를 참조합니다.
    const baseUrl = window.insertTaskBaseUrl || '/project/task/insert';
    location.href = `${baseUrl}?projectId=${selectedId}`;
}