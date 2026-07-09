# WEPLE - 김병완 담당 파트

<p align="center">
  <strong>인증 · 사용자관리 · GitHub 저장소 연동 · 커밋 기반 일감 연결 · 배포</strong>
</p>

---

## 담당 개요

WEPLE 프로젝트에서 **회사별 인증 흐름**, **가입승인 기반 사용자관리**, **GitHub 저장소 연동**, **커밋 메시지 기반 일감 연결**, **배포 환경 구성**을 담당했습니다.

단순 화면 구현보다 사용자가 서비스에 진입하고, 관리자가 계정을 승인하며, 프로젝트 저장소와 일감이 연결되는 흐름을 중심으로 구현했습니다.

---

## 담당 기능 요약

| 구분 | 구현 내용 |
| --- | --- |
| 인증 | 회사 코드 기반 로그인, 회원가입, 자동 로그인 |
| 가입 관리 | 가입승인, 승인/취소 처리 |
| 사용자관리 | 사용자 목록, 검색, 페이징, 활성/비활성 처리 |
| 사용자 상세 | 기본정보, 그룹, 참여 프로젝트 조회 및 수정 |
| 저장소 관리 | GitHub 저장소 등록, 수정, 삭제, 주 저장소 설정 |
| GitHub 연동 | 파일트리, 파일 내용, 커밋 내역, 커밋 상세 조회 |
| 일감 연결 | 커밋 메시지 기반 일감 코드 인식 및 매칭 |
| 배포 | Jenkins, Docker, EC2, GitHub Actions 기반 배포 |

---

## 담당 파트 기술 스택

### Backend

<p>
  <img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jasypt-4B5563?style=for-the-badge"/>
</p>

### Frontend

<p>
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
</p>

### External API & Infra

<p>
  <img src="https://img.shields.io/badge/GitHub REST API-181717?style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/DockerHub-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
</p>

---

## 1. 로그인 / 회사별 접속 처리

![로그인 화면](docs/images/login.png)

회사별 URL을 통해 로그인 화면에 접근하도록 구성했습니다.

```text
/c/{companyCode}/login
URL에 포함된 회사 코드가 로그인 화면에 자동 반영되며, 로그인 시 계정 정보와 회사 코드를 함께 검증합니다.
구현 포인트
회사별 URL 기반 로그인 페이지 접근
URL의 회사 코드 자동 반영
로그인 ID + 회사 코드 기준 사용자 검증
Spring Security Remember-Me 기반 자동 로그인
.rememberMe(remember -> remember
    .rememberMeParameter("rememberMe")
    .tokenValiditySeconds(60 * 60 * 24 * 14)
    .tokenRepository(persistentTokenRepository)
    .rememberMeCookieName("WEPLE_REMEMBER_ME")
)
2. 회원가입 / 승인대기 처리

회원가입 시 아이디와 이메일 중복 여부를 확인하고, 비밀번호 확인 및 입력값 유효성 검사를 수행합니다.
가입 요청은 즉시 활성 계정으로 등록하지 않고 a1 상태로 저장했습니다.
a1: 승인대기
a2: 활성
a3: 비활성
구현 포인트
회사 코드 기반 회원가입
아이디/이메일 중복 검증
비밀번호 확인
승인대기 상태 저장
3. 가입승인 / 계정 활성화

관리자는 승인대기 상태의 사용자를 조회하고, 승인 또는 취소 처리를 할 수 있습니다.
처리 흐름
회원가입 요청
→ status = a1 저장
→ 관리자 가입승인 페이지 조회
→ 승인 시 status = a2 변경
→ 취소 시 가입요청 데이터 삭제
구현 포인트
관리자 권한 사용자만 접근 가능
승인대기 사용자 목록 조회
승인 시 활성 계정으로 전환
취소 시 가입 요청 데이터 삭제
4. 사용자관리 / 계정 상태 제어

관리자는 사용자 목록을 조회하고 역할, 아이디, 이름 기준으로 검색할 수 있습니다.
또한 사용자 계정을 활성 또는 비활성 상태로 전환할 수 있습니다.
구현 포인트
사용자 목록 조회
검색 및 페이징
활성/비활성 상태 변경
최고관리자/관리자 제어 범위 제한
5. 사용자 상세조회 / 정보 수정

사용자의 기본정보, 계정 상태, 최근 로그인 일시, 소속 그룹, 참여 프로젝트 정보를 조회할 수 있습니다.
수정 화면에서는 이름, 이메일, 연락처, 그룹 정보를 변경할 수 있도록 구성했습니다.
구현 포인트
사용자 기본정보 조회
최근 로그인 일시 조회
소속 그룹 조회
참여 프로젝트 조회
사용자 정보 수정
6. 저장소 등록 및 관리

프로젝트 설정의 저장소 탭에서 GitHub 저장소를 등록하고 관리할 수 있습니다.
구현 포인트
프로젝트별 GitHub 저장소 등록
주 저장소 지정
저장소 수정 및 삭제
삭제 시 저장소명 재입력 검증
같은 회사 내 저장소 URL 중복 방지
7. GitHub 저장소 조회

등록된 저장소 URL을 기반으로 GitHub REST API를 호출하여 브랜치, 파일트리, 파일 내용을 조회했습니다.
처리 흐름
저장소 URL 등록
→ GitHub API 호출
→ 브랜치 목록 조회
→ 파일트리 조회
→ 선택 파일 내용 미리보기
구현 포인트
GitHub 저장소 메타데이터 조회
브랜치 목록 조회
브랜치별 파일트리 조회
폴더/파일명 검색
파일 내용 미리보기
8. 저장소 서비스 설정 / 커밋 인식 규칙

커밋과 일감 연결 규칙은 관리자가 회사 단위로 설정할 수 있도록 구성했습니다.
refs, fixes, related
위와 같은 참조 키워드를 기준으로 커밋 메시지에서 일감 코드를 인식합니다.
구현 포인트
커밋 자동 수집 여부 설정
커밋 메시지 인식 여부 설정
참조 키워드 쉼표 기반 관리
설정값 기반 커밋-일감 연결 처리
9. 커밋 조회 / 일감 연결

커밋 메시지에 참조 키워드와 일감 코드가 포함되어 있으면 실제 일감과 매칭하여 표시합니다.
refs TSK-260707_3
fixes TSK-260707_10
related TSK-260707_16
핵심 로직
private static final Pattern TASK_CODE_PATTERN = Pattern.compile("TSK-\\d{6}_\\d+");
Pattern taskReferencePattern = Pattern.compile(
    "(?i)(?:^|\\s)(?:" + keywordPattern + ")\\s+("
    + TASK_CODE_PATTERN.pattern() + ")(?=\\s|$)"
);
처리 흐름
커밋 메시지 조회
→ 참조 키워드 확인
→ 일감 코드 추출
→ 실제 일감과 매칭
→ 존재하면 일감 제목 표시
→ 없으면 미등록 상태 표시
10. 커밋 상세조회 / 변경 내역 확인

커밋 상세 화면에서는 커밋 메시지, 해시코드, 작성자, 작성일시를 확인할 수 있습니다.
또한 변경 파일 목록과 파일별 Diff 내용을 확인할 수 있도록 구성했습니다.
구현 포인트
커밋 상세 정보 조회
변경 파일 수 및 추가/삭제 라인 요약
변경 파일 트리 구성
파일별 Diff 내용 표시
11. 배포 환경 구성
WEPLE 배포는 GitHub, Jenkins, Docker, DockerHub, AWS EC2 기반으로 구성했습니다.
GitHub push
→ GitHub Actions
→ Jenkins 원격 빌드 실행
→ Maven Build
→ Docker Image Build
→ DockerHub Push
→ EC2 운영 서버 배포
구현 포인트
Jenkins Pipeline 구성
Docker 이미지 빌드 및 DockerHub Push
EC2 운영 서버 배포
GitHub Actions를 통한 Jenkins 원격 빌드 트리거
환경 변수 기반 민감정보 관리
담당 파트 성과
회사 코드 기반 인증 흐름 구현
승인대기 기반 회원가입 프로세스 구현
사용자 계정 상태 관리 및 권한 제어 처리
GitHub 저장소 파일/커밋 조회 기능 구현
커밋 메시지 기반 일감 연결 기능 구현
Jenkins, Docker, EC2 기반 배포 흐름 구성
아쉬운 점 및 개선 방향
제한된 기간 안에 인증, 사용자관리, 저장소 연동, 배포까지 함께 구현하면서 일부 화면의 사용성과 예외 안내는 더 개선할 여지가 남았습니다.
추후에는 GitHub API 호출 제한과 커밋 조회 성능을 보완하고, 커밋-일감 연결 규칙을 더 유연하게 확장할 수 있도록 개선하고 싶습니다.
느낀점
이번 프로젝트를 통해 로그인, 권한, 사용자 상태, 외부 API 연동, 배포 과정이 서로 밀접하게 연결된다는 것을 체감했습니다.
단순히 기능을 구현하는 것에서 끝나는 것이 아니라, 데이터 흐름과 예외 상황, 운영 환경까지 함께 고려해야 실제 서비스에 가까워진다는 점을 배울 수 있었습니다.
