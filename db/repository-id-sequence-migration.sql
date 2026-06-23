-- 저장소 등록에서 MAX + 1을 제거하기 위한 Oracle 시퀀스 마이그레이션입니다.
-- 각 블록은 기존 데이터의 최대값 다음 번호부터 시퀀스를 시작하며, 이미 생성된 시퀀스는 그대로 둡니다.

DECLARE
    v_start_with NUMBER;
BEGIN
    SELECT NVL(MAX(repository_manage_id), 0) + 1
    INTO v_start_with
    FROM repository_manage_setting;

    EXECUTE IMMEDIATE 'CREATE SEQUENCE repository_manage_setting_seq START WITH '
        || v_start_with || ' INCREMENT BY 1 NOCACHE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN
            RAISE;
        END IF;
END;
/

DECLARE
    v_start_with NUMBER;
BEGIN
    SELECT NVL(MAX(
        CASE
            WHEN REGEXP_LIKE(repository_id, '^REPO-[0-9]{8}-[0-9]+$')
            THEN TO_NUMBER(REGEXP_SUBSTR(repository_id, '[0-9]+$'))
        END
    ), 0) + 1
    INTO v_start_with
    FROM repository_setting;

    EXECUTE IMMEDIATE 'CREATE SEQUENCE repository_setting_seq START WITH '
        || v_start_with || ' INCREMENT BY 1 NOCACHE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN
            RAISE;
        END IF;
END;
/

-- 프로젝트당 주 저장소는 한 건만 허용합니다.
-- 기존 데이터에 주 저장소가 여러 건이면 먼저 정리한 뒤 이 인덱스를 생성해야 합니다.
CREATE UNIQUE INDEX uq_repository_setting_main
ON repository_setting (
    CASE WHEN repository_main_yn = 'Y' THEN company_id END,
    CASE WHEN repository_main_yn = 'Y' THEN project_id END
);
