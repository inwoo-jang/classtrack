-- 기존 DB의 due_date NOT NULL 제약은 Hibernate update가 제거하지 않을 수 있어 명시한다.
ALTER TABLE assignments ALTER COLUMN due_date DROP NOT NULL;
