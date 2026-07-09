# WEPLE - 김병완 담당 파트

<p align="center">
  <strong>회사별 인증, 사용자 관리, GitHub 저장소 연동, 커밋-일감 연결 기능 구현</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Boot-3.5.16-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/MyBatis-000000?style=flat-square"/>
  <img src="https://img.shields.io/badge/Oracle-F80000?style=flat-square&logo=oracle&logoColor=white"/>
</p>

<br/>

## Overview

WEPLE 프로젝트에서 **인증 및 사용자 관리**, **GitHub 저장소 연동**, **커밋 내역 기반 일감 연결** 기능을 담당했습니다.

회사별 URL을 통한 로그인 흐름부터 회원가입 승인, 사용자 상태 제어, GitHub 저장소 파일 조회, 커밋 메시지 기반 일감 연결, 커밋 상세 Diff 조회까지 하나의 흐름으로 이어지는 기능들을 구현했습니다.

<br/>

## 담당 기능 요약

| 구분 | 주요 구현 내용 |
|---|---|
| 인증 | 회사별 URL 로그인, 자동 로그인, 로그인 성공 후 회사별 화면 이동 |
| 회원가입 | 회원가입 입력 검증, 아이디/이메일 중복 체크, 승인대기 상태 등록 |
| 가입승인 | 관리자 권한 기반 승인 처리, 승인 시 계정 활성화, 취소 시 요청 삭제 |
| 사용자관리 | 사용자 목록 조회, 검색, 활성/비활성 전환, 상세조회 및 정보 수정 |
| 저장소 관리 | 프로젝트별 GitHub 저장소 등록, 수정, 삭제, 주 저장소 표시 |
| GitHub 연동 | GitHub API 기반 브랜치/파일트리/파일 내용/커밋 내역 조회 |
| 커밋 연결 | 커밋 메시지에서 일감 코드 인식, 실제 일감과 매칭, 미등록 상태 표시 |
| 커밋 상세 | 커밋 상세 정보, 변경 파일 트리, Diff 형태의 변경 내용 조회 |
| 배포 | Docker, Jenkins, GitHub Actions 기반 운영 배포 흐름 구성 |

<br/>

## Tech Stack

### Backend

<p>
  <img src="https://img.shields.io/badge/Java 21-007396?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Boot 3.5.16-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge"/>
</p>

### Database

<p>
  <img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white"/>
</p>

### Frontend

<p>
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/CSS-663399?style=for-the-badge&logo=css&logoColor=white"/>
</p>

### External API & Deploy

<p>
  <img src="https://img.shields.io/badge/GitHub API-181717?style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
</p>

<br/>

## 1. 회사별 로그인

<p align="center">
  <img src="docs/images/01-login.png" width="850" alt="회사별 로그인 화면"/>
</p>

회사별 전용 URL을 통해 로그인 페이지에 접근하도록 구성했습니다.

`/c/{companyCode}/login` 형식으로 접근하면 URL의 회사 코드가 로그인 화면에 자동으로 반영되며, 로그인 시 입력한 계정의 회사 정보와 URL의 회사 코드가 일치하는지 확인합니다.

이를 통해 같은 로그인 페이지를 사용하더라도 회사 단위로 사용자를 명확하게 구분할 수 있도록 처리했습니다.

### 주요 구현 포인트

- 회사별 URL 기반 로그인 페이지 접근
- URL의 회사 코드 자동 반영
- 로그인 계정의 회사 코드와 URL 회사 코드 검증
- 로그인 성공 시 해당 회사의 프로젝트 화면으로 이동
- Spring Security 기반 로그인 처리 적용

<details>
<summary>자동 로그인 구현 코드 보기</summary>

```java
.rememberMe(remember -> remember
    .rememberMeParameter("rememberMe")
    .tokenValiditySeconds(60 * 60 * 24 * 14)
    .tokenRepository(persistentTokenRepository)
    .rememberMeCookieName("WEPLE_REMEMBER_ME")
    .alwaysRemember(false)
)
```

자동 로그인 선택 시 Remember-Me 토큰을 DB에 저장하고, 쿠키를 통해 인증 상태를 유지하도록 구성했습니다.

</details>

<br/>

## 2. 회원가입 및 승인대기 처리

<p align="center">
  <img src="docs/images/02-signup.png" width="850" alt="회원가입 화면"/>
</p>

회원가입은 회사별 URL을 통해 접근하며, 입력값 검증과 중복 체크를 거친 뒤 바로 활성 계정으로 등록하지 않고 **승인대기 상태**로 저장되도록 구현했습니다.

가입 요청자는 `a1` 상태로 저장되며, 관리자의 승인을 받은 뒤에만 실제 서비스 이용이 가능한 `a2` 상태로 전환됩니다.

### 주요 구현 포인트

- 회사 코드 기반 회원가입 페이지 접근
- 아이디 중복 체크
- 이메일 중복 체크
- 비밀번호 확인 및 입력값 유효성 검증
- 회원가입 요청 시 `a1` 승인대기 상태로 저장

<details>
<summary>회원가입 저장 흐름 보기</summary>

```sql
INSERT INTO users (
    user_code,
    company_id,
    login_id,
    password,
    user_name,
    email,
    phone_number,
    signup_requested_at,
    status,
    owner_yn,
    admin_yn
)
VALUES (
    #{userCode},
    #{companyId},
    #{loginId},
    #{encodedPassword},
    #{userName},
    #{email},
    #{phoneNumber},
    SYSDATE,
    'a1',
    'N',
    'N'
)
```

회원가입 직후에는 활성 계정이 아니라 승인대기 상태로 저장하여 관리자 승인 절차를 거치도록 구성했습니다.

</details>

<br/>

## 3. 가입승인 및 계정 활성화

<p align="center">
  <img src="docs/images/03-signup-approval.png" width="850" alt="가입승인 화면"/>
</p>

가입승인 페이지에서는 승인대기 상태인 사용자를 조회하고, 관리자가 승인 또는 취소 처리를 할 수 있습니다.

승인 시 사용자 상태를 `a1`에서 `a2`로 변경하여 활성 계정으로 전환하고, 취소 시 가입요청 데이터를 삭제하도록 처리했습니다.

### 주요 구현 포인트

- 관리자 권한 사용자만 접근 가능
- 승인대기 상태(`a1`) 사용자 목록 조회
- 승인 시 활성 상태(`a2`)로 변경
- 취소 시 가입요청 데이터 삭제
- 회사 단위로 가입요청 데이터 분리

<details>
<summary>가입승인 처리 SQL 보기</summary>

```sql
UPDATE users
   SET status = 'a2'
 WHERE user_code = #{userCode}
   AND company_id = #{companyId}
   AND status = 'a1'
```

같은 회사의 승인대기 사용자만 승인되도록 조건을 지정했습니다.

</details>

<br/>

## 4. 사용자 관리

<p align="center">
  <img src="docs/images/04-user-list.png" width="850" alt="사용자 관리 화면"/>
</p>

가입승인이 완료된 사용자는 사용자관리 화면에서 조회할 수 있습니다.

관리자는 역할, 아이디, 이름 기준으로 사용자를 검색할 수 있으며, 계정 상태를 활성 또는 비활성으로 전환할 수 있습니다.

단, 최고관리자와 관리자 계정은 권한 오남용을 막기 위해 제어 범위를 제한했습니다.

### 주요 구현 포인트

- 활성/비활성 사용자 목록 조회
- 역할, 아이디, 이름 기준 검색
- 사용자 계정 활성/비활성 전환
- 최고관리자 및 관리자 계정 제어 제한
- 페이징 처리 적용

<br/>

## 5. 사용자 상세조회 및 정보 수정

<p align="center">
  <img src="docs/images/05-user-detail.png" width="850" alt="사용자 상세조회 화면"/>
</p>

사용자 상세조회 화면에서는 기본정보, 계정 상태, 최근 로그인 일시, 소속 그룹, 참여 프로젝트 정보를 확인할 수 있습니다.

수정 화면에서는 사용자 이름, 이메일, 연락처, 그룹 정보를 변경할 수 있도록 연결했습니다.

### 주요 구현 포인트

- 사용자 기본정보 조회
- 계정 상태 및 최근 로그인 일시 조회
- 소속 그룹 정보 조회
- 참여 프로젝트 정보 조회
- 사용자 정보 수정 화면 연결

<br/>

## 6. GitHub 저장소 등록 및 관리

<p align="center">
  <img src="docs/images/06-repository-management.png" width="850" alt="저장소 등록 및 관리 화면"/>
</p>

프로젝트 설정의 저장소 탭에서 GitHub 저장소를 등록하고 관리할 수 있도록 구현했습니다.

관리자는 저장소를 추가, 수정, 삭제할 수 있으며, 프로젝트에서 주로 사용하는 저장소는 주 저장소로 표시되도록 처리했습니다.

삭제 시에는 실수 방지를 위해 저장소명을 다시 입력해야 삭제가 가능하도록 구성했습니다.

### 주요 구현 포인트

- 프로젝트별 GitHub 저장소 등록
- 저장소명, URL, 주 저장소 여부 관리
- 저장소 수정 시 URL 수정 제한
- 저장소 삭제 시 이름 재입력 확인
- 같은 회사 내 동일 저장소 URL 중복 방지

<br/>

## 7. GitHub 저장소 파일 조회

<p align="center">
  <img src="docs/images/07-repository-detail.png" width="850" alt="GitHub 저장소 조회 화면"/>
</p>

등록된 저장소 URL을 기반으로 GitHub API를 호출하여 브랜치, 파일트리, 파일 내용을 조회했습니다.

브랜치를 변경하면 해당 브랜치 기준의 파일 목록이 다시 조회되며, 파일을 선택하면 오른쪽 영역에서 파일 내용을 미리볼 수 있도록 구성했습니다.

### 주요 구현 포인트

- GitHub API 기반 저장소 정보 조회
- 브랜치 목록 조회 및 선택
- 폴더/파일 트리 조회
- 파일명 검색 지원
- 선택 파일 내용 미리보기

<details>
<summary>GitHub 파일 조회 흐름 보기</summary>

```java
String contentsPath = directory.isBlank()
    ? "/contents"
    : "/contents/" + directory;

JsonNode contents = getJson(
    GITHUB_API + repositoryPath + contentsPath
    + "?ref=" + encodeQueryValue(branch)
);
```

선택한 브랜치와 경로를 기준으로 GitHub Contents API를 호출하여 파일 목록을 조회합니다.

</details>

<br/>

## 8. 저장소 서비스 설정

<p align="center">
  <img src="docs/images/08-repository-setting.png" width="850" alt="저장소 서비스 설정 화면"/>
</p>

커밋과 일감 연결 규칙은 고정값이 아니라 관리자가 설정할 수 있도록 구성했습니다.

관리자는 커밋 자동 수집 여부, 커밋 메시지 인식 여부, 참조 키워드를 설정할 수 있습니다.

예를 들어 `refs`, `fixes`, `related` 같은 키워드를 등록하면 해당 키워드 뒤에 있는 일감 코드를 인식 대상으로 처리합니다.

### 주요 구현 포인트

- 커밋 자동 수집 여부 설정
- 커밋 메시지 텍스트 인식 여부 설정
- 일감 참조 키워드 관리
- 설정 변경 전 이탈 시 미저장 안내
- 회사 단위 저장소 설정 관리

<br/>

## 9. 커밋 조회 및 일감 연결

<p align="center">
  <img src="docs/images/09-commit-list.png" width="850" alt="커밋 조회 및 일감 연결 화면"/>
</p>

커밋 내역은 기간 조건과 검색어를 기준으로 조회할 수 있도록 구성했습니다.

검색은 커밋 메시지, 이메일, 해시코드를 기준으로 처리됩니다.

커밋 메시지에 설정된 참조 키워드와 `TSK-날짜_번호` 형식의 일감 코드가 포함되어 있으면, 해당 코드를 추출해 실제 일감과 매칭합니다.

실제 일감이 존재하면 일감 제목과 함께 표시하고, 존재하지 않는 경우에는 미등록 상태로 구분했습니다.

### 주요 구현 포인트

- 기간 조건 기반 커밋 조회
- 메시지, 이메일, 해시 기준 검색
- 커밋 메시지에서 일감 코드 추출
- 실제 일감 정보와 매칭
- 미등록 일감 코드 별도 표시

<details>
<summary>일감 코드 추출 로직 보기</summary>

```java
private static final Pattern TASK_CODE_PATTERN = Pattern.compile("TSK-\\d{6}_\\d+");

String keywordPattern = keywords.stream()
    .map(Pattern::quote)
    .collect(Collectors.joining("|"));

Pattern taskReferencePattern = Pattern.compile(
    "(?i)(?:^|\\s)(?:" + keywordPattern + ")\\s+("
        + TASK_CODE_PATTERN.pattern()
        + ")(?=\\s|$)"
);

Matcher matcher = taskReferencePattern.matcher(commitMessage);

return matcher.find() ? matcher.group(1) : null;
```

참조 키워드와 일감 코드 패턴을 조합하여 커밋 메시지에서 실제 연결 대상 일감 코드를 추출합니다.

</details>

<br/>

## 10. 커밋 상세조회 및 변경 파일 확인

<p align="center">
  <img src="docs/images/10-commit-detail.png" width="850" alt="커밋 상세조회 화면"/>
</p>

커밋 목록에서 특정 커밋을 선택하면 커밋 상세 화면으로 이동합니다.

상단에서는 커밋 메시지, 해시코드, 작성자, 작성일시를 확인할 수 있고, 변경 파일 수와 추가/삭제 라인 수를 요약해서 보여줍니다.

왼쪽 영역에서는 변경된 파일을 폴더 트리 형태로 확인할 수 있으며, 파일을 선택하면 오른쪽에서 변경 내용을 Diff 형태로 비교할 수 있습니다.

### 주요 구현 포인트

- 커밋 상세 정보 조회
- 변경 파일 수 및 추가/삭제 라인 요약
- 변경 파일 트리 표시
- 파일별 Diff 내용 조회
- 추가/삭제 라인 색상 구분

<br/>

## 11. 배포 환경 구성

<p align="center">
  <img src="docs/images/11-deploy.png" width="850" alt="배포 구성 화면"/>
</p>

운영 배포를 위해 AWS EC2, Docker, Jenkins, GitHub Actions를 활용했습니다.

GitHub main 브랜치에 변경사항이 반영되면 GitHub Actions가 Jenkins Job을 호출하고, Jenkins는 프로젝트를 빌드한 뒤 Docker 이미지를 생성하여 DockerHub에 Push합니다.

이후 운영 서버에서 이미지를 Pull 받아 컨테이너를 재실행하는 방식으로 배포 흐름을 구성했습니다.

### 배포 흐름

```text
GitHub main push
        ↓
GitHub Actions
        ↓
Jenkins Pipeline
        ↓
Spring Boot Build
        ↓
Docker Image Build
        ↓
DockerHub Push
        ↓
Production Server Pull & Run
```

<br/>

## 구현 중 중점적으로 고려한 부분

### 1. 회사 단위 데이터 분리

로그인, 회원가입, 가입승인, 사용자관리, 저장소 등록 기능에서 회사 코드를 기준으로 데이터를 구분했습니다.

이를 통해 같은 시스템을 사용하더라도 회사별 사용자가 서로 다른 데이터 범위 안에서 동작하도록 처리했습니다.

### 2. 승인 기반 사용자 흐름

회원가입 직후 바로 서비스를 이용하는 구조가 아니라, 관리자 승인 후 활성화되는 구조로 설계했습니다.

사용자 상태값을 기준으로 승인대기, 활성, 비활성 상태를 분리했습니다.

### 3. GitHub API 호출 안정성

GitHub 저장소 조회 기능은 외부 API를 사용하기 때문에 토큰 관리, 호출 제한, 조회 실패 상황을 고려했습니다.

운영 환경에서는 환경변수 기반으로 GitHub Token을 관리하여 소스코드에 민감정보가 포함되지 않도록 처리했습니다.

### 4. 커밋과 일감의 연결 기준

커밋 메시지에 단순히 일감 코드만 포함되었다고 무조건 연결하지 않고, 관리자가 설정한 참조 키워드가 함께 있는 경우에만 연결되도록 처리했습니다.

이를 통해 의도하지 않은 커밋 연결을 줄이고, 프로젝트에서 정한 커밋 작성 규칙에 맞게 일감을 추적할 수 있도록 했습니다.

<br/>

## 담당 파트 회고

### 잘한 점

로그인, 회원가입, 사용자관리, 저장소 연동처럼 여러 기능이 연결된 흐름을 맡아 예외처리와 권한처리를 포함해 기획한 주요 기능을 구현했습니다.

### 아쉬운 점

제한된 시간 안에 많은 기능을 구현하다 보니 일부 화면 완성도와 코드 효율성 면에서 보완할 부분이 남았습니다.

### 개선 방향

GitHub API 호출 제한과 커밋 조회 성능에 대한 보완이 필요하며, 사용자관리와 저장소 화면에서 예외 상황을 더 명확히 안내할 수 있도록 개선할 수 있습니다.

<br/>

## 담당 기능 한눈에 보기

| 기능 | 상태 | 설명 |
|---|---|---|
| 회사별 로그인 | 완료 | URL 회사 코드 기반 로그인 처리 |
| 자동 로그인 | 완료 | Spring Security Remember-Me 적용 |
| 회원가입 | 완료 | 입력 검증 및 승인대기 등록 |
| 가입승인 | 완료 | 관리자 승인 시 계정 활성화 |
| 사용자관리 | 완료 | 조회, 검색, 상태 변경, 상세조회 |
| 저장소 관리 | 완료 | GitHub 저장소 등록, 수정, 삭제 |
| GitHub 파일 조회 | 완료 | 브랜치별 파일트리 및 파일 내용 조회 |
| 커밋 조회 | 완료 | 기간 및 검색 조건 기반 조회 |
| 일감 연결 | 완료 | 커밋 메시지 기반 일감 코드 매칭 |
| 커밋 상세 | 완료 | 변경 파일 및 Diff 조회 |
| 배포 | 완료 | Jenkins, Docker, GitHub Actions 기반 배포 |

<br/>

## Screenshot Path Guide

이미지는 아래 경로에 배치하는 것을 기준으로 작성했습니다.

```text
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
docs/images/11-deploy.png
```

이미지 파일명을 위 경로와 동일하게 맞추면 README에서 바로 화면이 출력됩니다.
