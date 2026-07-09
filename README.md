# WEPLE : 함께 만드는 연결된 협업 플랫폼
## 팀 구성 및 역할

<table align="center">
  <tr>
    <td align="center">
      <a href="https://github.com/time1014">
        <img src="https://avatars.githubusercontent.com/u/64236748?v=4" width="100px;" alt="방진영"/><br />
        <sub><b>방진영</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/crescentia0011">
        <img src="https://avatars.githubusercontent.com/u/254889839?v=4" width="100px;" alt="김병완"/><br />
        <sub><b>김병완</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/kimeunji806">
        <img src="https://avatars.githubusercontent.com/u/258710580?v=4" width="100px;" alt="김은지"/><br />
        <sub><b>김은지</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/kimminji28">
        <img src="https://github.com/kimminji28.png" width="100px;" alt="김민지"/><br />
      <sub><b>김민지</b></sub>
        </a>
    </td>
    <td align="center">
      <a href="https://github.com/smk412">
       <img src="https://github.com/smk412.png" width="100px;" alt="송민규"/><br />
      <sub><b>송민규</b></sub>
        </a>
    </td>
  </tr>

  <tr>
    <th align="center">팀장</th>
    <th align="center">부팀장</th>
    <th align="center">팀원</th>
    <th align="center">팀원</th>
    <th align="center">팀원</th>
  </tr>

  <tr>
    <td align="center">프로젝트 총괄</td>
    <td align="center">배포</td>
    <td align="center">DB</td>
    <td align="center">개발환경</td>
    <td align="center">Git</td>
  </tr>
</table>

# WEPLE

> 함께 만드는 연결된 협업 플랫폼

WEPLE은 프로젝트 수행 과정에서 발생하는 일정, 일감, 산출물, 커뮤니케이션, 저장소 이력을 하나의 흐름으로 관리할 수 있는 웹 기반 협업 플랫폼입니다.  
팀 단위 프로젝트를 진행하면서 구성원, 역할, 권한, 일감, 파일, 위키, 알림, 저장소 정보를 통합적으로 관리하는 것을 목표로 합니다.

## 주요 기능

### 관리

- 회사별 사용자 가입승인
- 사용자 등록 및 계정 상태 관리
- 그룹 관리
- 역할 및 권한 관리
- 코드값 및 서비스 설정 관리

### 프로젝트

- 프로젝트 생성 및 관리
- 프로젝트 구성원 등록 및 관리
- 프로젝트별 모듈 설정
- 버전 및 마일스톤 관리
- 로드맵 관리

### 일감 / 테스트

- 일감 등록, 조회, 수정, 삭제
- 일감 댓글 및 작업내역 관리
- 일감 유형, 우선순위, 상태 관리
- 테스트 케이스 관리
- 요구사항 커버리지 확인

### 일정 / 현황

- 통합 캘린더 및 프로젝트 캘린더
- 작업 시간 등록 및 소요시간 관리
- 간트차트
- 칸반보드
- 프로젝트 진행 현황 확인

### 협업 / 산출물

- 위키 등록 및 관리
- 게시판
- 파일 업로드 및 다운로드
- 다운로드 이력 조회
- 알림 및 마이페이지

### 저장소

- GitHub 저장소 등록 및 관리
- 저장소 파일트리 및 파일 내용 조회
- 커밋 내역 조회
- 커밋 상세 및 변경 내역 확인
- 커밋 메시지 기반 일감 연결

## 기술 스택

### Backend

- Java 21
- Spring Boot 3.5.16
- Spring MVC
- Spring Security
- MyBatis
- Oracle Database
- Jasypt

### Frontend

- Thymeleaf
- JavaScript
- HTML5
- CSS3

### External / Infra

- GitHub REST API
- AWS S3
- AWS EC2
- Docker
- Jenkins
- GitHub Actions

## 프로젝트 구조

```text
src/main/java/com/weple/cloud
├── auth          # 인증 관련 기능
├── system        # 시스템 및 관리자 기능
├── project       # 프로젝트 관련 기능
├── repository    # 저장소 연동 기능
└── ...           # 일감, 파일, 위키, 알림 등 협업 기능

src/main/resources
├── mapper        # MyBatis Mapper XML
├── static        # CSS, JavaScript, 이미지 등 정적 리소스
└── templates     # Thymeleaf 화면 템플릿


실행 방법

1. 환경 변수 설정
실행 환경에 맞게 필요한 값을 설정합니다.
JASYPT_PASSWORD
GITHUB_API_TOKEN
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

2. 애플리케이션 실행
./mvnw spring-boot:run
Windows 환경에서는 다음 명령을 사용할 수 있습니다.
mvnw.cmd spring-boot:run

3. 빌드
./mvnw clean package
배포 구조
WEPLE은 GitHub, Jenkins, Docker, DockerHub, AWS EC2를 기반으로 배포 환경을 구성했습니다.
GitHub push
→ GitHub Actions
→ Jenkins 원격 빌드 실행
→ Maven Build
→ Docker Image Build
→ DockerHub Push
→ 운영 EC2에서 Docker Image Pull & Run

팀 구성
이름	  주요 담당
방진영	  일감, 테스트 케이스, 캘린더
김병완	  로그인 및 인증, 사용자관리, 저장소 연동, 배포
김은지	  프로젝트, 위키, 알림, 마이페이지, 칸반보드
김민지	  그룹, 코드값, 소요시간, 파일관리
송민규	  공통 UI, 프로젝트 보조 기능

프로젝트 목표
WEPLE은 프로젝트 수행에 필요한 업무 관리, 일정 관리, 산출물 관리, 협업 기능을 하나의 플랫폼에서 제공하는 것을 목표로 합니다.
프로젝트 구성원은 일감과 일정, 파일, 위키, 저장소 이력을 함께 확인할 수 있고, 관리자는 사용자와 권한, 프로젝트 설정을 통합적으로 관리할 수 있습니다.
