document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar');

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        
        // 커스텀 헤더를 사용하므로 기본 헤더는 숨김 처리
        headerToolbar: false, 
        
        // 한 칸에 최대 3개까지만 표시하고 나머지는 +더보기로 처리
        dayMaxEvents: 4, 
        
        // 🔥 기본 팝오버 대신 커스텀 모달 연결
        moreLinkClick: function(info) {
            const modal = document.getElementById('customMoreModal');
            const listContainer = document.getElementById('customMoreModalList');
            const dateTitle = document.getElementById('customMoreModalDate');

            // 1. 이전 리스트 초기화
            listContainer.innerHTML = '';

            // 2. 날짜 텍스트 세팅 (예: 6월 11일 (목) 일정)
            const d = info.date;
            const days = ['일', '월', '화', '수', '목', '금', '토'];
            dateTitle.textContent = `${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]}) 일정`;

            // 3. 해당 날짜의 전체 이벤트 목록 렌더링
            info.allSegs.forEach(seg => {
                const event = seg.event;
                
                // 이벤트 객체 데이터 추출
                const eventId = event.id;
                const title = event.title;
                const bgColor = event.backgroundColor || '#000000'; // 기본색상 검정
                
                // Controller에서 전달한 커스텀 데이터 (일감/마일스톤 구분용)
                const type = event.extendedProps.type; 

                // 리스트 아이템(<li>) 생성 및 스타일 설정
                const li = document.createElement('li');
                li.style.display = 'flex';
                li.style.alignItems = 'center';
                li.style.padding = '10px';
                li.style.border = '1px solid #eee';
                li.style.marginBottom = '5px';
                li.style.borderRadius = '4px';
                li.style.cursor = 'pointer';

                // Hover 효과
                li.onmouseover = () => li.style.backgroundColor = '#f8f9fa';
                li.onmouseout = () => li.style.backgroundColor = '#fff';

                // 🔥 클릭 시 상세 페이지 이동 로직 (상황에 맞게 주석 해제 및 수정해서 사용하세요)
				li.onclick = function() {
				    // 1. 쿼리에서 매핑한 eventType 가져오기 (MY_TASK 또는 MILESTONE)
				    const eventType = event.extendedProps.eventType;
				    
				    // 2. 현재 페이지의 프로젝트 ID 가져오기
				    const projectId = document.getElementById('hiddenProjectId').value;

				    let targetUrl = '';

				    if (eventType === 'MY_TASK') {
				        // 일감 이동: http://localhost:8073/project/task/detail/TSK-260623_8?projectId=1
				        targetUrl = `/project/task/detail/${eventId}?projectId=${projectId}`;
				        
				    } else if (eventType === 'MILESTONE') {
				        // 마일스톤의 경우 id가 'M_49' 형태이므로 'M_'를 제거하여 실제 ID만 추출
				        const realMilestoneId = eventId.replace('M_', '');
				        
				        // 마일스톤 이동: http://localhost:8073/project/milestone/detail?projectId=1&milestoneId=49
				        targetUrl = `/project/milestone/detail?projectId=${projectId}&milestoneId=${realMilestoneId}`;
				    }

				    // URL이 정상적으로 생성되었다면 페이지 이동
				    if (targetUrl) {
				        window.location.href = targetUrl;
				    }
				};

                // 좌측 색상 점 (Color Dot)
                const dot = document.createElement('span');
                dot.style.display = 'inline-block';
                dot.style.minWidth = '12px';
                dot.style.height = '12px';
                dot.style.borderRadius = '50%';
                dot.style.backgroundColor = bgColor;
                dot.style.marginRight = '10px';

                // 텍스트 영역
                const text = document.createElement('span');
                text.textContent = title;
                text.style.whiteSpace = 'nowrap';
                text.style.overflow = 'hidden';
                text.style.textOverflow = 'ellipsis';

                // 요소 조립
                li.appendChild(dot);
                li.appendChild(text);
                listContainer.appendChild(li);
            });

            // 4. 모달 띄우기
            modal.style.display = 'flex';

            // 5. FullCalendar 기본 팝오버 동작 막기
            return 'none';
        },
        
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

    // --- 커스텀 더보기 모달 닫기 이벤트 ---
    const customModal = document.getElementById('customMoreModal');
    if (customModal) {
        // X 버튼 클릭 시 닫기
        document.getElementById('customMoreModalClose').addEventListener('click', function() {
            customModal.style.display = 'none';
        });
        
        // 모달 바깥 영역(배경 검은부분) 클릭 시 닫기
        customModal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.style.display = 'none';
            }
        });
    }

    // --- 커스텀 헤더 버튼 이벤트 연동 ---
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