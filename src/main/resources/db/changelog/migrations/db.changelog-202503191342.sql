--liquibase formatted sql
--changeset artur:202503191128
--comment: boards_columns table create

CREATE TABLE BOARDS_COLUMNS(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    "order" int NOT NULL,
    kind VARCHAR(7) NOT NULL,
    board_id BIGINT NOT NULL,
    CONSTRAINT boards__boards_columns_fk FOREIGN KEY (board_id) REFERENCES BOARDS(id) ON DELETE CASCADE,
    CONSTRAINT unique_board_id_order UNIQUE (board_id, "order")
);

--rollback DROP TABLE BOARDS