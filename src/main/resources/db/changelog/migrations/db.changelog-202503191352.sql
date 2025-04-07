--liquibase formatted sql
--changeset artur:202503191129
--comment: cards table create

CREATE TABLE CARDS (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    board_column_id BIGINT NOT NULL,
    CONSTRAINT cards__boards_columns_fk FOREIGN KEY (board_column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE CASCADE
);

--rollback DROP TABLE CARDS;