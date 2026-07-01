document.addEventListener('DOMContentLoaded', function() {
  const calendarEl = document.getElementById('calendar');

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    
    // 🔥 이 부분을 false로 변경해야 기본(안 예쁜) 버튼들이 사라집니다!
    headerToolbar: false, 
    
    locale: 'ko',
    
    events: {
        url: '/scheduleList',
        extraParams: function() {
            const currentProjectId = document.getElementById('hiddenProjectId').value; 
            const checkedValues = Array.from(document.querySelectorAll('.filter-item input:checked'))
                                       .map(cb => cb.value);
            
            return {
                projectId: currentProjectId,
                filterTypes: checkedValues.join(',') 
            };
        }
    },
    
    // 날짜 텍스트 중앙 렌더링
    datesSet: function(info) {
      document.getElementById('calendar-center-title').textContent = info.view.title;
    }
  });

  calendar.render();

  // --- 커스텀 헤더 버튼 이벤트 연동 (이 코드들이 사진 속 예쁜 버튼들을 작동시킵니다) ---
  document.querySelector('.prev-btn').addEventListener('click', () => calendar.prev());
  document.querySelector('.next-btn').addEventListener('click', () => calendar.next());
  document.querySelector('.today-btn').addEventListener('click', () => calendar.today());

  const viewButtons = document.querySelectorAll('.view-btn');
  viewButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      viewButtons.forEach(b => b.classList.remove('active'));
      this.classList.add('active');
      calendar.changeView(this.getAttribute('data-view'));
    });
  });

  // --- 필터(체크박스) 조작 시 캘린더 새로고침 ---
  const filterCheckboxes = document.querySelectorAll('.filter-item input');
  filterCheckboxes.forEach(checkbox => {
    checkbox.addEventListener('change', function() {
      calendar.refetchEvents(); 
    });
  });
});