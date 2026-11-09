CREATE TABLE IF NOT EXISTS doctor (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    name             VARCHAR(30)   NOT NULL,
    hospital         VARCHAR(50)   NOT NULL,
    department       VARCHAR(30)   NOT NULL,
    memo             VARCHAR(500)  NULL,
    next_visit_date  DATE          NULL,
    deleted_at       TIMESTAMP     NULL,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS visit (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    doctor_id   BIGINT         NOT NULL,
    visit_date  DATE           NOT NULL,
    memo        VARCHAR(1000)  NULL,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

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
    answered_at    TIMESTAMP      NULL,
    delivered_at   TIMESTAMP      NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_question_visit  FOREIGN KEY (visit_id)  REFERENCES visit (id)
);

CREATE TABLE IF NOT EXISTS commitment (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    doctor_id   BIGINT        NOT NULL,
    visit_id    BIGINT        NOT NULL,
    content     VARCHAR(300)  NOT NULL,
    due_date    DATE          NOT NULL,
    done        BOOLEAN       NOT NULL DEFAULT FALSE,
    done_at     TIMESTAMP     NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_commitment_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_commitment_visit  FOREIGN KEY (visit_id)  REFERENCES visit (id)
);

CREATE INDEX IF NOT EXISTS idx_doctor_deleted_next  ON doctor (deleted_at, next_visit_date);
CREATE INDEX IF NOT EXISTS idx_visit_doctor_date    ON visit (doctor_id, visit_date);
CREATE INDEX IF NOT EXISTS idx_question_doctor_stat ON question (doctor_id, status);
CREATE INDEX IF NOT EXISTS idx_question_status      ON question (status);
CREATE INDEX IF NOT EXISTS idx_question_visit       ON question (visit_id);
CREATE INDEX IF NOT EXISTS idx_commitment_done_due  ON commitment (done, due_date);
CREATE INDEX IF NOT EXISTS idx_commitment_doctor    ON commitment (doctor_id, done);
CREATE INDEX IF NOT EXISTS idx_commitment_visit     ON commitment (visit_id);
