# WEPLE - 김병완 담당 파트

<p align="center">
  <strong>회사별 인증 · 사용자 관리 · GitHub 저장소 연동 · 커밋 기반 일감 연결 · 배포</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Boot-3.5.16-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/MyBatis-000000?style=flat-square"/>
  <img src="https://img.shields.io/badge/Oracle-F80000?style=flat-square&logo=oracle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/GitHub API-181717?style=flat-square&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white"/>
</p>

---

## Overview

WEPLE은 프로젝트 협업을 위한 웹 기반 플랫폼입니다.  
이 중에서 저는 **서비스 진입 흐름**, **사용자 계정 관리**, **GitHub 저장소 연동**, **커밋과 일감 연결**, **배포 환경 구성**을 담당했습니다.

담당 기능은 서로 독립된 화면이 아니라 다음 흐름으로 연결됩니다.

```text
회사별 접속
→ 로그인 / 회원가입
→ 관리자 승인
→ 사용자 관리
→ 프로젝트 저장소 등록
→ GitHub 커밋 조회
→ 커밋 메시지 기반 일감 연결
→ 커밋 상세 및 변경 내역 확인
My Role
영역	구현 내용
인증	회사 코드 기반 로그인, 회원가입, 자동 로그인
계정 관리	가입승인, 사용자 목록, 상태 변경, 상세조회, 정보 수정
저장소	GitHub 저장소 등록/관리, 파일트리 조회, 커밋 조회
일감 연동	커밋 메시지에서 일감 코드 추출 후 실제 일감과 매칭
배포	GitHub Actions, Jenkins, Docker, EC2 기반 배포 흐름 구성

Screenshot Guide
이미지는 아래 경로에 저장하면 README에서 자동으로 표시됩니다.
docs/images/01-login.png
docs/images/02-signup.png
docs/images/03-signup-approval.png
docs/images/04-user-list.png
docs/images/05-user-detail.png
docs/images/06-repository-management.png
docs/images/07-repository-detail.png
docs/images/08-repository-setting.png
docs/images/09-commit-list.png
docs/images/10-commit-detail.png
1. 회사 단위 인증 흐름
같은 서비스 안에서 여러 회사의 사용자를 구분하기 위해 회사 코드 기반 진입 구조를 적용했습니다.

<p align="center">
  <img src="docs/images/01-login.png" width="850" alt="회사 코드 기반 로그인 화면"/>
</p>

Why
초기 로그인은 단순히 아이디와 비밀번호만으로 사용자를 구분하는 구조였습니다.
하지만 WEPLE은 회사 단위로 사용자를 관리해야 하기 때문에, 로그인 시점부터 회사 정보를 함께 검증할 필요가 있었습니다.
How
/c/{companyCode}/login
/c/{companyCode}/join
URL에 포함된 회사 코드를 로그인/회원가입 화면에 자동 반영하고, 로그인 검증 시 사용자 계정의 회사 코드와 비교하도록 처리했습니다.
Result
회사별 로그인 URL 제공
회사 코드 자동 반영
회사 코드와 계정 정보 이중 검증
Spring Security Remember-Me 기반 자동 로그인 지원
.rememberMe(remember -> remember
    .rememberMeParameter("rememberMe")
    .tokenValiditySeconds(60 * 60 * 24 * 14)
    .tokenRepository(persistentTokenRepository)
    .rememberMeCookieName("WEPLE_REMEMBER_ME")
)
2. 승인 기반 회원가입 프로세스
사용자가 가입 즉시 서비스를 이용하지 않고, 관리자의 승인 후 활성화되도록 구성했습니다.

<p align="center">
  <img src="docs/images/02-signup.png" width="850" alt="회원가입 화면"/>
</p>

Why
회사 내부 협업 서비스 특성상 아무 사용자가 바로 접근하면 안 되기 때문에, 회원가입 이후 관리자 검토 단계가 필요했습니다.
How
회원가입 요청 시 사용자 정보를 저장하되, 계정 상태를 바로 활성화하지 않고 승인대기 상태로 저장했습니다.
a1: 승인대기
a2: 활성
a3: 비활성
<p align="center">
  <img src="docs/images/03-signup-approval.png" width="850" alt="가입승인 화면"/>
</p>

관리자가 가입승인 페이지에서 요청자를 확인하고 승인하면 a2 상태로 변경됩니다.
취소 시에는 가입 요청 데이터가 삭제됩니다.
Result
아이디/이메일 중복 검증
비밀번호 확인 및 입력값 유효성 검사
승인대기 상태 저장
관리자 승인 후 활성 계정 전환
3. 사용자 계정 관리
관리자가 사용자 상태를 제어하고, 사용자 정보를 조회/수정할 수 있도록 구성했습니다.

<p align="center">
  <img src="docs/images/04-user-list.png" width="850" alt="사용자관리 화면"/>
</p>

Key Features
사용자 목록 조회
역할, 아이디, 이름 기준 검색
페이징 처리
활성/비활성 상태 변경
최고관리자/관리자 제어 범위 제한
사용자 계정은 단순히 조회만 하는 것이 아니라, 운영 중 필요한 상태 전환까지 가능해야 했습니다.
다만 관리자 계정끼리 임의로 비활성화하는 상황을 막기 위해 권한별 제어 범위를 제한했습니다.
<p align="center">
  <img src="docs/images/05-user-detail.png" width="850" alt="사용자 상세조회 화면"/>
</p>

상세조회 화면에서는 기본정보, 계정 상태, 최근 로그인 일시, 소속 그룹, 참여 프로젝트 정보를 함께 확인할 수 있도록 구성했습니다.
4. GitHub 저장소 등록 및 조회
프로젝트별 GitHub 저장소를 등록하고, 시스템 내부에서 파일과 커밋 정보를 확인할 수 있도록 구현했습니다.

<p align="center">
  <img src="docs/images/06-repository-management.png" width="850" alt="저장소 등록 및 관리 화면"/>
</p>

Why
프로젝트 관리 시스템 안에서 개발 산출물과 커밋 이력을 함께 확인하기 위해 GitHub 저장소 연동이 필요했습니다.
How
프로젝트 설정에서 GitHub 저장소 URL을 등록하고, GitHub REST API를 통해 저장소 정보를 조회했습니다.
<p align="center">
  <img src="docs/images/07-repository-detail.png" width="850" alt="GitHub 저장소 파일트리 조회 화면"/>
</p>

Result
프로젝트별 GitHub 저장소 등록
주 저장소 지정
저장소 수정/삭제
저장소명 재입력 기반 삭제 확인
브랜치별 파일트리 조회
선택 파일 내용 미리보기
Repository URL
→ GitHub REST API
→ Branch List
→ File Tree
→ File Content
5. 커밋 메시지 기반 일감 연결
커밋 메시지에 포함된 일감 코드를 인식해 실제 일감과 연결했습니다.

<p align="center">
  <img src="docs/images/08-repository-setting.png" width="850" alt="저장소 서비스 설정 화면"/>
</p>

Problem
커밋 내역만 보여주면 어떤 작업과 관련된 변경인지 파악하기 어렵습니다.
따라서 커밋 메시지에 작성된 일감 코드를 읽어 실제 일감과 연결하는 기능을 구현했습니다.
Rule
관리자는 저장소 서비스 설정에서 커밋 인식 규칙을 설정할 수 있습니다.
refs, fixes, related
위와 같은 참조 키워드 뒤에 일감 코드가 작성된 경우 연결 대상으로 인식합니다.
refs TSK-260707_3
fixes TSK-260707_10
related TSK-260707_16
<p align="center">
  <img src="docs/images/09-commit-list.png" width="850" alt="커밋 조회 및 일감 연결 화면"/>
</p>

Core Logic
private static final Pattern TASK_CODE_PATTERN = Pattern.compile("TSK-\\d{6}_\\d+");
Pattern taskReferencePattern = Pattern.compile(
    "(?i)(?:^|\\s)(?:" + keywordPattern + ")\\s+("
    + TASK_CODE_PATTERN.pattern() + ")(?=\\s|$)"
);
Result
기간 조건 기반 커밋 조회
메시지, 이메일, 해시 검색
참조 키워드 기반 일감 코드 추출
실제 일감 존재 여부 확인
존재하지 않는 코드는 미등록 상태로 표시
6. 커밋 상세 및 변경 내역 확인
GitHub로 이동하지 않아도 시스템 내부에서 커밋 상세와 변경 파일을 확인할 수 있도록 구성했습니다.

<p align="center">
  <img src="docs/images/10-commit-detail.png" width="850" alt="커밋 상세 및 변경 내역 확인 화면"/>
</p>

커밋 상세 화면에서는 선택한 커밋의 메시지, 해시코드, 작성자, 작성일시를 확인할 수 있습니다.
또한 변경 파일 수와 추가/삭제 라인 수를 요약하고, 파일별 변경 내역을 Diff 형태로 확인할 수 있습니다.
Result
커밋 상세 정보 조회
변경 파일 트리 구성
파일별 추가/삭제 라인 표시
코드 변경 내역 확인
7. 배포 환경 구성
GitHub push 이후 Jenkins와 Docker를 통해 운영 서버에 배포되는 흐름을 구성했습니다.

GitHub push
→ GitHub Actions
→ Jenkins Build Trigger
→ Maven Build
→ Docker Image Build
→ DockerHub Push
→ EC2 Pull & Run
Result
Jenkins Pipeline 구성
Docker 이미지 빌드
DockerHub 이미지 Push
EC2 운영 서버 배포
GitHub Actions를 통한 Jenkins 원격 빌드 실행
환경 변수 기반 민감정보 관리
What I Learned
이번 프로젝트를 통해 인증, 권한, 사용자 상태, 외부 API 연동, 배포가 서로 독립된 기능이 아니라 하나의 서비스 흐름 안에서 연결된다는 점을 체감했습니다.
특히 GitHub 저장소와 일감을 연결하면서 단순 조회 기능을 넘어, 개발 이력과 업무 흐름을 함께 추적할 수 있는 구조를 고민해볼 수 있었습니다.
Retrospective
잘한 점
로그인부터 저장소 연동까지 여러 기능이 연결된 흐름을 구현
회사 코드, 사용자 상태, 관리자 권한 등 예외 상황을 고려
GitHub API와 커밋 메시지를 활용해 일감 연결 기능 구현
Jenkins, Docker, EC2 기반 배포 흐름 직접 구성
개선할 점
GitHub API 호출 제한과 커밋 조회 성능 보완
사용자관리와 저장소 화면의 예외 안내 개선
커밋-일감 연결 규칙을 더 유연하게 확장
