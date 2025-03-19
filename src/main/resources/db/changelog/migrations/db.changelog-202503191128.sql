--liquibase formatted sql
--changeset artur:202503191128
--comment: boards table create

CREATE TABLE BOARDS(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
)

--rollback DROP TABLE BOARDS