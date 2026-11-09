CREATE TABLE IF NOT EXISTS doctor (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    name             VARCHAR(30)   NOT NULL,
    hospital         VARCHAR(50)   NOT NULL,
    department       VARCHAR(30)   NOT NULL,
    memo             VARCHAR(500)  NULL,
    next_visit_date  DATE          NULL,
    deleted_at       DATETIME(6)   NULL,
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_doctor_deleted_next (deleted_at, next_visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS visit (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    doctor_id   BIGINT         NOT NULL,
    visit_date  DATE           NOT NULL,
    memo        VARCHAR(1000)  NULL,
    created_at  DATETIME(6)    NOT NULL,
    updated_at  DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_visit_doctor_date (doctor_id, visit_date),
    CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS question (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    doctor_id      BIGINT         NOT NULL,
    visit_id       BIGINT         NOT NULL,
    content        VARCHAR(500)   NOT NULL,
    product_tag    VARCHAR(20)    NULL,
    status         VARCHAR(20)    NOT NULL,
    answer         VARCHAR(1000)  NULL,
    source_type    VARCHAR(20)    NULL,
    source_detail  VARCHAR(200)   NULL,
    answered_at    DATETIME(6)    NULL,
    delivered_at   DATETIME(6)    NULL,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_question_doctor_stat (doctor_id, status),
    INDEX idx_question_status (status),
    INDEX idx_question_visit (visit_id),
    CONSTRAINT fk_question_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_question_visit  FOREIGN KEY (visit_id)  REFERENCES visit (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS commitment (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    doctor_id   BIGINT        NOT NULL,
    visit_id    BIGINT        NOT NULL,
    content     VARCHAR(300)  NOT NULL,
    due_date    DATE          NOT NULL,
    done        BIT(1)        NOT NULL DEFAULT b'0',
    done_at     DATETIME(6)   NULL,
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_commitment_done_due (done, due_date),
    INDEX idx_commitment_doctor (doctor_id, done),
    INDEX idx_commitment_visit (visit_id),
    CONSTRAINT fk_commitment_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_commitment_visit  FOREIGN KEY (visit_id)  REFERENCES visit (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
