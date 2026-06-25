-- 1단계: 최상위 마스터
CREATE TABLE companies (company_id NUMBER PRIMARY KEY, company_code VARCHAR2(50) UNIQUE NOT NULL, company_name VARCHAR2(100) UNIQUE NOT NULL, created_at DATE DEFAULT SYSDATE NOT NULL);
CREATE TABLE common_code (common_id VARCHAR2(50) PRIMARY KEY, common_code VARCHAR2(50) NOT NULL, default_describe VARCHAR2(1000));
CREATE TABLE permissions (permission_code VARCHAR2(50) PRIMARY KEY, permission_tag VARCHAR2(50), permission_name VARCHAR2(100) UNIQUE NOT NULL);
CREATE TABLE project (project_id NUMBER PRIMARY KEY, project_title VARCHAR2(100) UNIQUE NOT NULL, project_describe VARCHAR2(1000), project_identifier VARCHAR2(50) UNIQUE NOT NULL, start_date DATE NOT NULL, finish_date DATE NOT NULL, created_at DATE DEFAULT SYSDATE);
CREATE TABLE files (file_id NUMBER PRIMARY KEY, container_tag VARCHAR2(50), container_id VARCHAR2(50), file_version NUMBER DEFAULT 1, file_tag VARCHAR2(50), file_name VARCHAR2(255), file_path VARCHAR2(500), file_size VARCHAR2(50), file_type VARCHAR2(100), uploader VARCHAR2(50), created_at DATE DEFAULT SYSDATE);

-- 2단계: 기업 종속 마스터
CREATE TABLE groups (group_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, group_name VARCHAR2(100) UNIQUE NOT NULL, created_at DATE DEFAULT SYSDATE);
CREATE TABLE roles (role_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, role_name VARCHAR2(100) UNIQUE NOT NULL);
CREATE TABLE task_priority (task_priority_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, priority_name VARCHAR2(50) UNIQUE NOT NULL, default_yn VARCHAR2(1), using_yn VARCHAR2(1));
CREATE TABLE task_types (type_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, type_name VARCHAR2(100) UNIQUE NOT NULL, task_type_describe VARCHAR2(1000));
CREATE TABLE work_classification (task_classification_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, work_name VARCHAR2(100) UNIQUE NOT NULL, default_yn VARCHAR2(1), using_yn VARCHAR(2));
CREATE TABLE repository_manage_setting (repository_manage_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, commit_auto_yn VARCHAR2(1) DEFAULT 'Y', commit_text_yn VARCHAR2(1) DEFAULT 'Y', task_keyword VARCHAR2(255));
CREATE TABLE Auth_manage_setting (auth_manage_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, automatic_login_yn VARCHAR2(1) DEFAULT 'N' NOT NULL, min_security_length NUMBER DEFAULT 8);
CREATE TABLE modules (module_id VARCHAR2(50) PRIMARY KEY, module_name VARCHAR2(100) NOT NULL, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE);

-- 3단계: 매핑 및 유저
CREATE TABLE role_permissions (role_id NUMBER REFERENCES roles(role_id) ON DELETE CASCADE, permission_code VARCHAR2(50) REFERENCES permissions(permission_code) ON DELETE CASCADE, PRIMARY KEY (role_id, permission_code));
CREATE TABLE project_manage_setting (project_manage_id NUMBER PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, type_id NUMBER NOT NULL REFERENCES task_types(type_id) ON DELETE CASCADE, module_name VARCHAR2(100));
CREATE TABLE module_mapping (module_map_id NUMBER, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, module_name VARCHAR2(100), PRIMARY KEY (module_map_id, project_id));
CREATE TABLE users (user_code VARCHAR2(50) PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, login_id VARCHAR2(50) UNIQUE NOT NULL, password VARCHAR2(100) NOT NULL, user_name VARCHAR2(50) NOT NULL, email VARCHAR2(100) NOT NULL, phone_number VARCHAR2(50) NOT NULL, approval VARCHAR2(1) DEFAULT '0', create_date DATE DEFAULT SYSDATE, signup_requested_at DATE, status VARCHAR2(50) NOT NULL, profile_image VARCHAR2(255), owner_yn NUMBER(1) NOT NULL, admin_yn NUMBER(1), web_notification_yn VARCHAR2(1) DEFAULT 'Y', email_notification_yn VARCHAR2(1) DEFAULT 'N', notification_area VARCHAR2(50) DEFAULT 'ALL', last_login_time DATE, group_id NUMBER REFERENCES groups(group_id) ON DELETE SET NULL);

-- 4단계: 업무 기능
CREATE TABLE milestone (milestone_id NUMBER, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, login_id VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL, milestone_title VARCHAR2(100) UNIQUE, milestone_describe VARCHAR2(1000) NOT NULL, finish_date DATE, milestone_status VARCHAR2(50), created_at DATE DEFAULT SYSDATE, PRIMARY KEY (milestone_id, project_id));
CREATE TABLE alarm (alarm_id NUMBER PRIMARY KEY, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, alarm_tag VARCHAR2(50), alarm_content VARCHAR2(1000), check_yn VARCHAR2(1) DEFAULT 'N', alarm_date DATE DEFAULT SYSDATE, target_type VARCHAR2(50) NOT NULL, target_id VARCHAR2(50) NOT NULL, alarm_chk DATE);
CREATE TABLE board (board_id NUMBER PRIMARY KEY, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, board_name VARCHAR2(100) UNIQUE NOT NULL, board_describe VARCHAR2(1000), active_yn VARCHAR2(1) DEFAULT 'Y' NOT NULL);
CREATE TABLE members (member_id NUMBER PRIMARY KEY, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE);
CREATE TABLE file_history (history_id NUMBER PRIMARY KEY, file_id NUMBER NOT NULL REFERENCES files(file_id) ON DELETE CASCADE, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, action_type VARCHAR2(50), action_at DATE DEFAULT SYSDATE);

-- 5단계: 결합 및 트래킹
CREATE TABLE repository_setting (repository_id VARCHAR2(50) PRIMARY KEY, company_id NUMBER NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, alarm_id NUMBER NOT NULL REFERENCES alarm(alarm_id) ON DELETE CASCADE, repository_manage_id NUMBER NOT NULL REFERENCES repository_manage_setting(repository_manage_id) ON DELETE CASCADE, repository_identifier VARCHAR2(50) UNIQUE NOT NULL, repository_url VARCHAR2(255) NOT NULL, access_token VARCHAR2(255) NOT NULL, repository_main_yn VARCHAR2(1) NOT NULL, created_at DATE DEFAULT SYSDATE);
CREATE TABLE commit_logs (commit_id VARCHAR2(50) PRIMARY KEY, repository_id VARCHAR2(50) NOT NULL REFERENCES repository_setting(repository_id) ON DELETE CASCADE, commit_hash VARCHAR2(100), commit_user VARCHAR2(50), commit_message CLOB, committed_at DATE);
CREATE TABLE member_roles (member_id NUMBER REFERENCES members(member_id) ON DELETE CASCADE, role_id NUMBER REFERENCES roles(role_id) ON DELETE CASCADE, PRIMARY KEY (member_id, role_id));
CREATE TABLE post (post_id NUMBER PRIMARY KEY, board_id NUMBER NOT NULL REFERENCES board(board_id) ON DELETE CASCADE, writer VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, post_title VARCHAR2(100) NOT NULL, post_content VARCHAR2(4000), views NUMBER DEFAULT 0, created_at DATE DEFAULT SYSDATE, updated_at DATE);
CREATE TABLE post_comment (id NUMBER PRIMARY KEY, post_id NUMBER NOT NULL REFERENCES post(post_id) ON DELETE CASCADE, login_id VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL, comment_content VARCHAR2(1000), created_at DATE DEFAULT SYSDATE);

-- 6단계: 일감 및 소요시간
CREATE TABLE task (task_id VARCHAR2(50) PRIMARY KEY, task_type VARCHAR2(50) NOT NULL, task_title VARCHAR2(100) NOT NULL, task_describe VARCHAR2(1000), task_status VARCHAR2(50) NOT NULL, priority VARCHAR2(50) NOT NULL, task_manager VARCHAR2(50), start_date DATE, finish_date DATE, estimated_time NUMBER, task_progress NUMBER DEFAULT 0, parent_task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE SET NULL, spent_hours_sum NUMBER DEFAULT 0, created_at DATE DEFAULT SYSDATE, updated_at DATE, milestone_id NUMBER NOT NULL, project_id NUMBER NOT NULL, CONSTRAINT FK_MILE_TASK FOREIGN KEY (milestone_id, project_id) REFERENCES milestone(milestone_id, project_id) ON DELETE CASCADE);
CREATE TABLE task_history (history_id NUMBER PRIMARY KEY, task_id VARCHAR2(50) NOT NULL REFERENCES task(task_id) ON DELETE CASCADE, action_type VARCHAR2(50), changed_by VARCHAR2(50), action_at DATE DEFAULT SYSDATE);
CREATE TABLE task_history_detail (detail_id NUMBER PRIMARY KEY, history_id NUMBER NOT NULL REFERENCES task_history(history_id) ON DELETE CASCADE, field_name VARCHAR2(50), old_value VARCHAR2(255), new_value VARCHAR2(255));
CREATE TABLE task_comment (comment_id NUMBER PRIMARY KEY, task_id VARCHAR2(50) NOT NULL REFERENCES task(task_id) ON DELETE CASCADE, parent_comment_id NUMBER REFERENCES task_comment(comment_id) ON DELETE SET NULL, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, task_comment VARCHAR2(1000), created_at DATE DEFAULT SYSDATE);
CREATE TABLE test_case (test_id VARCHAR2(50) PRIMARY KEY, task_id VARCHAR2(50) NOT NULL REFERENCES task(task_id) ON DELETE CASCADE, milestone_id NUMBER NOT NULL, project_id NUMBER NOT NULL, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, test_name VARCHAR2(100) NOT NULL, created_at DATE DEFAULT SYSDATE, progress VARCHAR2(50), test_date DATE, priority VARCHAR2(50) NOT NULL, test_yn VARCHAR2(1), test_manager VARCHAR2(50), manager VARCHAR2(50), test_content VARCHAR2(1000), test_describe VARCHAR2(1000), CONSTRAINT FK_MILE_TEST FOREIGN KEY (milestone_id, project_id) REFERENCES milestone(milestone_id, project_id) ON DELETE CASCADE);
CREATE TABLE work_time (work_id NUMBER PRIMARY KEY, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE SET NULL, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, work_name NUMBER NOT NULL REFERENCES work_classification(task_classification_id) ON DELETE CASCADE, work_date DATE DEFAULT SYSDATE, spent_content VARCHAR2(1000), spent_hour NUMBER, created_at DATE, updated_at DATE);

-- 7단계: 위키
CREATE TABLE wiki_pages (wiki_page_id VARCHAR2(50) PRIMARY KEY, project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE, parent_page_id VARCHAR2(50) REFERENCES wiki_pages(wiki_page_id) ON DELETE SET NULL, title VARCHAR2(255) NOT NULL, content CLOB NOT NULL, current_version NUMBER NOT NULL, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, lock_user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL, locked_at DATE, created_at DATE, update_at DATE);
CREATE TABLE wiki_histories (wiki_history_id NUMBER PRIMARY KEY, wiki_page_id VARCHAR2(50) NOT NULL REFERENCES wiki_pages(wiki_page_id) ON DELETE CASCADE, wiki_version NUMBER, title VARCHAR2(255) NOT NULL, content CLOB NOT NULL, user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE CASCADE, created_at DATE NOT NULL);
CREATE TABLE wiki_relations (wiki_relation_id NUMBER PRIMARY KEY, wiki_page_id VARCHAR2(50) NOT NULL REFERENCES wiki_pages(wiki_page_id) ON DELETE CASCADE, target_type VARCHAR2(20) NOT NULL, project_id NUMBER REFERENCES project(project_id) ON DELETE CASCADE, target_task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE CASCADE, target_wiki_id VARCHAR2(50) REFERENCES wiki_pages(wiki_page_id) ON DELETE CASCADE);

SELECT COUNT(*) FROM user_tables;

-- 1단계: 마스터 데이터 삽입 (참조할 부모들)
INSERT INTO companies (company_id, company_code, company_name) VALUES (1, 'CP-001', '구글 코리아');
INSERT INTO companies (company_id, company_code, company_name) VALUES (2, 'CP-002', '네이버 파이낸셜');

INSERT INTO permissions (permission_code, permission_name) VALUES ('ADM', '관리자');
INSERT INTO permissions (permission_code, permission_name) VALUES ('DEV', '개발자');

INSERT INTO project (project_id, project_title, project_identifier, start_date, finish_date) 
VALUES (100, '협업툴 개발 프로젝트', 'COL-DEV', SYSDATE, SYSDATE + 30);

INSERT INTO task_types (type_id, company_id, type_name) VALUES (1, 1, '결함');
INSERT INTO task_types (type_id, company_id, type_name) VALUES (2, 1, '새 기능');

INSERT INTO work_classification (task_classification_id, company_id, work_name) VALUES (10, 1, '개발');
INSERT INTO work_classification (task_classification_id, company_id, work_name) VALUES (20, 1, '미팅');

-- 2단계: 종속 데이터 삽입
INSERT INTO groups (group_id, company_id, group_name) VALUES (10, 1, '인프라개발팀');

INSERT INTO users (user_code, company_id, login_id, password, user_name, email, phone_number, status, owner_yn, group_id) 
VALUES ('USR-001', 1, 'admin_user', 'pwd123', '김관리', 'admin@google.com', '010-1111-1111', 'ACT', 1, 10);

INSERT INTO users (user_code, company_id, login_id, password, user_name, email, phone_number, status, owner_yn, group_id) 
VALUES ('USR-002', 1, 'dev_user', 'pwd456', '이개발', 'dev@google.com', '010-2222-2222', 'ACT', 0, 10);

COMMIT;
-- 1. 기업과 부서, 유저 관계 확인 (JOIN)
SELECT 
    c.company_name AS "회사명", 
    g.group_name AS "부서명", 
    u.user_name AS "사용자명", 
    u.login_id AS "로그인ID"
FROM users u
JOIN companies c ON u.company_id = c.company_id
JOIN groups g ON u.group_id = g.group_id;

-- 2. 프로젝트 마스터 확인
SELECT * FROM project;

-- 3. 일감 유형 및 작업 분류 확인
SELECT * FROM task_types;
SELECT * FROM work_classification;

-- 4. 전체 데이터 건수 확인 (데이터가 들어갔는지 카운트)
SELECT 'USERS' AS TABLE_NAME, COUNT(*) AS CNT FROM users
UNION ALL
SELECT 'COMPANIES', COUNT(*) FROM companies
UNION ALL
SELECT 'GROUPS', COUNT(*) FROM groups
UNION ALL
SELECT 'PROJECT', COUNT(*) FROM project;



-- 1. 기존 테이블 삭제 (오류 방지)
DROP TABLE task CASCADE CONSTRAINTS;
DROP TABLE milestone CASCADE CONSTRAINTS;

-- 2. 마일스톤 테이블 생성 (PK 단순화)
CREATE TABLE milestone (
    milestone_id NUMBER PRIMARY KEY,  -- 복합키가 아닌 단일키로 변경
    project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    login_id VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    milestone_title VARCHAR2(100) UNIQUE,
    milestone_describe VARCHAR2(1000) NOT NULL,
    finish_date DATE,
    milestone_status VARCHAR2(50),
    created_at DATE DEFAULT SYSDATE
);

-- 3. 일감 테이블 생성 (마일스톤 참조 시 SET NULL 적용)
CREATE TABLE task (
    task_id VARCHAR2(50) PRIMARY KEY,
    task_type VARCHAR2(50) NOT NULL,
    task_title VARCHAR2(100) NOT NULL,
    task_describe VARCHAR2(1000),
    task_status VARCHAR2(50) NOT NULL,
    priority VARCHAR2(50) NOT NULL,
    task_manager VARCHAR2(50),
    start_date DATE,
    finish_date DATE,
    estimated_time NUMBER,
    task_progress NUMBER DEFAULT 0,
    parent_task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE SET NULL,
    spent_hours_sum NUMBER DEFAULT 0,
    created_at DATE DEFAULT SYSDATE,
    updated_at DATE,
    milestone_id NUMBER,  -- nullable로 설정
    project_id NUMBER NOT NULL,
    -- 프로젝트 삭제 시 일감 삭제(CASCADE), 마일스톤 삭제 시 일감은 유지(SET NULL)
    CONSTRAINT FK_TASK_PROJECT FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
    CONSTRAINT FK_TASK_MILESTONE FOREIGN KEY (milestone_id) REFERENCES milestone(milestone_id) ON DELETE SET NULL
);
-- 기존 task 테이블 삭제
DROP TABLE task CASCADE CONSTRAINTS;

-- 상위 일감 삭제 시 하위 일감도 같이 삭제되도록 수정
CREATE TABLE task (
    task_id VARCHAR2(50) PRIMARY KEY,
    task_type VARCHAR2(50) NOT NULL,
    task_title VARCHAR2(100) NOT NULL,
    task_describe VARCHAR2(1000),
    task_status VARCHAR2(50) NOT NULL,
    priority VARCHAR2(50) NOT NULL,
    task_manager VARCHAR2(50),
    start_date DATE,
    finish_date DATE,
    estimated_time NUMBER,
    task_progress NUMBER DEFAULT 0,
    -- [변경사항] ON DELETE CASCADE 적용
    parent_task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE CASCADE,
    spent_hours_sum NUMBER DEFAULT 0,
    created_at DATE DEFAULT SYSDATE,
    updated_at DATE,
    milestone_id NUMBER,
    project_id NUMBER NOT NULL,
    
    -- 프로젝트 삭제 시 일감 삭제, 마일스톤 삭제 시 일감은 유지(SET NULL)
    CONSTRAINT FK_TASK_PROJECT FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
    CONSTRAINT FK_TASK_MILESTONE FOREIGN KEY (milestone_id) REFERENCES milestone(milestone_id) ON DELETE SET NULL
);

-- 1. 회원 정보 참조 제약조건 수정 (기존 CASCADE를 삭제하고 SET NULL로 재설정)
-- 먼저 테이블을 살짝 수정해야 하므로, 관련 테이블들을 다시 정리합니다.

-- 회원 참조 테이블들 일괄 삭제 후 재생성 (안전하게)
DROP TABLE wiki_histories CASCADE CONSTRAINTS;
DROP TABLE wiki_pages CASCADE CONSTRAINTS;
DROP TABLE work_time CASCADE CONSTRAINTS;
DROP TABLE test_case CASCADE CONSTRAINTS;
DROP TABLE post_comment CASCADE CONSTRAINTS;
DROP TABLE post CASCADE CONSTRAINTS;
DROP TABLE file_history CASCADE CONSTRAINTS;
DROP TABLE alarm CASCADE CONSTRAINTS;
DROP TABLE milestone CASCADE CONSTRAINTS;

-- 1. 마일스톤 (담당자 삭제 시 담당자만 NULL)
CREATE TABLE milestone (
    milestone_id NUMBER PRIMARY KEY,
    project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    login_id VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL, 
    milestone_title VARCHAR2(100) UNIQUE,
    milestone_describe VARCHAR2(1000) NOT NULL,
    finish_date DATE,
    milestone_status VARCHAR2(50),
    created_at DATE DEFAULT SYSDATE
);

-- 2. 알람 (사용자 삭제 시 알람 기록 유지)
CREATE TABLE alarm (
    alarm_id NUMBER PRIMARY KEY,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    alarm_tag VARCHAR2(50),
    alarm_content VARCHAR2(1000),
    check_yn VARCHAR2(1) DEFAULT 'N',
    alarm_date DATE DEFAULT SYSDATE,
    target_type VARCHAR2(50) NOT NULL,
    target_id VARCHAR2(50) NOT NULL,
    alarm_chk DATE
);

-- 3. 파일 이력 (파일 기록은 무조건 살려야 함)
CREATE TABLE file_history (
    history_id NUMBER PRIMARY KEY,
    file_id NUMBER NOT NULL REFERENCES files(file_id) ON DELETE CASCADE,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    action_type VARCHAR2(50),
    action_at DATE DEFAULT SYSDATE
);

-- 4. 게시글 (작성자 탈퇴해도 글은 유지)
CREATE TABLE post (
    post_id NUMBER PRIMARY KEY,
    board_id NUMBER NOT NULL REFERENCES board(board_id) ON DELETE CASCADE,
    writer VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    post_title VARCHAR2(100) NOT NULL,
    post_content VARCHAR2(4000),
    views NUMBER DEFAULT 0,
    created_at DATE DEFAULT SYSDATE,
    updated_at DATE
);

-- 5. 게시글 댓글
CREATE TABLE post_comment (
    id NUMBER PRIMARY KEY,
    post_id NUMBER NOT NULL REFERENCES post(post_id) ON DELETE CASCADE,
    login_id VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    comment_content VARCHAR2(1000),
    created_at DATE DEFAULT SYSDATE
);


-- 7. 테스트 케이스 (담당자 NULL)
CREATE TABLE test_case (
    test_id VARCHAR2(50) PRIMARY KEY,
    task_id VARCHAR2(50) NOT NULL REFERENCES task(task_id) ON DELETE CASCADE,
    milestone_id NUMBER NOT NULL,
    project_id NUMBER NOT NULL,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    test_name VARCHAR2(100) NOT NULL,
    created_at DATE DEFAULT SYSDATE,
    progress VARCHAR2(50),
    test_date DATE,
    priority VARCHAR2(50) NOT NULL,
    test_yn VARCHAR2(1),
    test_manager VARCHAR2(50),
    manager VARCHAR2(50),
    test_content VARCHAR2(1000),
    test_describe VARCHAR2(1000),
    CONSTRAINT FK_MILE_TEST FOREIGN KEY (milestone_id, project_id) REFERENCES milestone(milestone_id, project_id) ON DELETE CASCADE
);

-- 8. 업무 시간 (가장 중요! 시간 기록은 반드시 보존)
CREATE TABLE work_time (
    work_id NUMBER PRIMARY KEY,
    project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    task_id VARCHAR2(50) REFERENCES task(task_id) ON DELETE SET NULL,
    user_code VARCHAR2(50) NOT NULL REFERENCES users(user_code) ON DELETE SET NULL,
    work_name NUMBER NOT NULL REFERENCES work_classification(task_classification_id) ON DELETE CASCADE,
    work_date DATE DEFAULT SYSDATE,
    spent_content VARCHAR2(1000),
    spent_hour NUMBER,
    created_at DATE,
    updated_at DATE
);

-- 9. 위키 (작성자/잠금자 정보만 NULL)
CREATE TABLE wiki_pages (
    wiki_page_id VARCHAR2(50) PRIMARY KEY,
    project_id NUMBER NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    parent_page_id VARCHAR2(50) REFERENCES wiki_pages(wiki_page_id) ON DELETE SET NULL,
    title VARCHAR2(255) NOT NULL,
    content CLOB NOT NULL,
    current_version NUMBER NOT NULL,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    lock_user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    locked_at DATE,
    created_at DATE,
    update_at DATE
);

CREATE TABLE wiki_histories (
    wiki_history_id NUMBER PRIMARY KEY,
    wiki_page_id VARCHAR2(50) NOT NULL REFERENCES wiki_pages(wiki_page_id) ON DELETE CASCADE,
    wiki_version NUMBER,
    title VARCHAR2(255) NOT NULL,
    content CLOB NOT NULL,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    created_at DATE NOT NULL
);

-- 1. 혹시 생성 시도하다 남은 테이블이 있다면 삭제
DROP TABLE test_case CASCADE CONSTRAINTS;

-- 2. 수정된 테이블 생성 (복합키 참조 제거)
CREATE TABLE test_case (
    test_id VARCHAR2(50) PRIMARY KEY,
    task_id VARCHAR2(50) NOT NULL REFERENCES task(task_id) ON DELETE CASCADE,
    milestone_id NUMBER NOT NULL,
    project_id NUMBER NOT NULL,
    user_code VARCHAR2(50) REFERENCES users(user_code) ON DELETE SET NULL,
    test_name VARCHAR2(100) NOT NULL,
    created_at DATE DEFAULT SYSDATE,
    progress VARCHAR2(50),
    test_date DATE,
    priority VARCHAR2(50) NOT NULL,
    test_yn VARCHAR2(1),
    test_manager VARCHAR2(50),
    manager VARCHAR2(50),
    test_content VARCHAR2(1000),
    test_describe VARCHAR2(1000),
    -- [수정] 복합키 참조를 제거하고, milestone_id만 참조하도록 변경
    CONSTRAINT FK_MILE_TEST FOREIGN KEY (milestone_id) REFERENCES milestone(milestone_id) ON DELETE CASCADE
);

SELECT COUNT(*) AS "총 테이블 개수" FROM user_tables;
SELECT table_name FROM user_tables ORDER BY table_name;


DESC groups;
SELECT *FROM groups ORDER BY group_id;
select * from groups;
select * from users;

update users
set group_id = null
where user_name = '다그닥';

SELECT g.group_id
     , g.company_id
     , g.group_name
     , g.created_at
FROM groups g
JOIN companies c ON g.company_id = c.company_id
ORDER BY g.group_id ASC;

SELECT * FROM files;
SELECT * FROM users;
SELECT NVL(MAX(group_id), 0) + 10 FROM groups;

DELETE FROM groups WHERE group_id = 30;

INSERT INTO groups (group_id, company_id, group_name)
VALUES (10, 1, '인프라개발팀');

INSERT INTO groups (group_id, company_id, group_name)
VALUES(20, 1, '디자인팀');

SELECT sequence_name
FROM user_sequences;
COMMIT;
select * from users;
select * from groups;

update users
set group_id = null
where user_name = '다그닥';

update users
set admin_yn = 1
where user_name = '담당근';

SELECT w.task_classification_id   AS taskClassificationId
     , w.work_name              AS workName
     , NULL                     AS priorityName
     , w.default_yn             AS defaultYn
     , w.using_yn               AS usingYn
FROM work_classification w
JOIN companies c ON c.company_id = w.company_id
UNION ALL
SELECT t.task_priority_id      AS taskPriorityId  , NULL
      , t.priority_name       AS priorityName
      , t.default_yn          AS defaultYn
      , t.using_yn            AS usingYn
FROM task_priority t
JOIN companies c ON c.company_id = t.company_id
ORDER BY 1;

desc companies;
DESC work_classification;
desc task_priority;

UPDATE work_classification
SET using_yn = 'Y'
WHERE task_classification_id = 10;

select * from companies;
select * from work_classification;
select * from task_priority;

DELETE FROM work_classification WHERE task_classification_id = 47;
DELETE FROM task_priority WHERE task_priority_id = 43;

commit;


select * from companies;
select * from permissions;
select * from role_permissions;
select * from roles;

SELECT c.company_id
       , c.company_code
       , c.company_name
       , created_at
       , r.role_id
       , r.role_name
       , rp.permission_code
       , p.permission_tag
       , p.permission_name
FROM companies c
JOIN roles r ON r.company_id = c.company_id
LEFT JOIN role_permissions rp ON rp.role_id = r.role_id
LEFT JOIN permissions p ON p.permission_code = rp.permission_code
ORDER BY 1;

commit;

SELECT task_classification_id,
           company_id,
           work_name,
           priority_name,
           default_yn,
           using_yn
    FROM (
        SELECT w.task_classification_id,
               w.company_id,
               w.work_name,
               CAST(NULL AS VARCHAR2(200)) AS priority_name,
               w.default_yn,
               w.using_yn
        FROM work_classification w
        JOIN companies c ON c.company_id = w.company_id
        WHERE w.task_classification_id = 10
        
        UNION ALL
        
        SELECT t.task_priority_id,
               t.company_id,
               CAST(NULL AS VARCHAR2(200)) AS work_name,
               t.priority_name,
               t.default_yn,
               t.using_yn
        FROM task_priority t
        JOIN companies c ON c.company_id = t.company_id
        WHERE t.task_priority_id = 10
    )
    WHERE ROWNUM <= 1;
    
SELECT t.task_priority_id, t.company_id, t.priority_name, t.default_yn, t.using_yn
FROM task_priority t
JOIN companies c ON c.company_id = t.company_id;

DROP SEQUENCE SEQ_DAY_SEQ;



CREATE SEQUENCE SET_DAY_SEQ
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NOCACHE
    NOCYCLE;

DESC work_time;
select * from work_time;
select * from project;
select * from users;
select * from work_classification;
select * from task;

SELECT wt.work_id
       , p.project_id
       , t.task_id
       , u.user_code
       , wc.work_name   AS work_name_label
       , wt.work_date
       , wt.spent_content
       , wt.spent_hour
       , wt.created_at
       , wt.updated_at
FROM work_time wt
LEFT JOIN project P ON wt.project_id = p.project_id
LEFT JOIN users u ON u.user_code = wt.user_code
LEFT JOIN work_classification wc ON wc.work_name = TO_CHAR(wt.work_name)
LEFT JOIN task t ON t.task_id = wt.task_id
ORDER BY wt.work_id;

INSERT INTO work_time (
                        work_id
                        , project_id
                        , task_id
                        , user_code
                        , work_name
                        , work_date
                        , spent_content
                        , spent_hour
                        , created_at
                        , updated_at
                       )
VALUES (
        ATE_DAY_SEQ.NEXTVAL
        , 1
        , 'TSK-260623_8'
        , 'USR-260621-003'
        , 10
        , SYSDATE
        , '전체 소요시간 데이터 조회 테스트'
        , 6
        , SYSDATE
        , SYSDATE
);

COMMIT; -- 변경사항 저장
SELECT column_name, data_type 
FROM user_tab_columns 
WHERE table_name = 'WORK_TIME'
ORDER BY column_id;

CREATE SEQUENCE ATE_DAY_SEQ
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NOCACHE
    NOCYCLE;
    
    
SELECT * FROM task;