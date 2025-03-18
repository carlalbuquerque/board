--liquibase formatted sql
--changeset root:202503172004
--comment: set unblock_reason nullable

ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason VARCHAR(255) NULL;

-- rollback ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason VARCHAR(255);