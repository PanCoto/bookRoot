-- ============================================================
--  bookRoot – Flyway Migration V1
--  Inicjalna schema bazy PostgreSQL
--  Wersja: 1.0.0
--  Generowana na podstawie modelu domenowego Hibernate
-- ============================================================

-- ──────────────────────────────────────────────────────────────
--  Typy ENUM jako VARCHAR – Hibernate enkoduje je jako STRING
-- ──────────────────────────────────────────────────────────────

-- ──────────────────────────────────────────────────────────────
--  Tabela: users
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(20)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    login           VARCHAR(20)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    age             INT          NOT NULL,
    display_name    VARCHAR(80),
    email           VARCHAR(255) UNIQUE,
    anonymous_mode  BOOLEAN      NOT NULL DEFAULT FALSE,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP,
    last_login_at   TIMESTAMP
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: categories
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: tasks
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tasks
(
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(150)  NOT NULL,
    content            TEXT          NOT NULL,
    image_url          VARCHAR(500),
    source_url         VARCHAR(500),
    status             VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    task_type          VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    options_json       TEXT,
    is_official        BOOLEAN       NOT NULL DEFAULT FALSE,
    view_count         INT           NOT NULL DEFAULT 0,
    anonymous          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date       DATE          NOT NULL,
    last_modified_date TIMESTAMP,
    author_id          BIGINT        REFERENCES users (id) ON DELETE SET NULL,
    category_id        BIGINT        REFERENCES categories (id) ON DELETE SET NULL,
    approved_by_id     BIGINT        REFERENCES users (id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: answers
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS answers
(
    id           BIGSERIAL PRIMARY KEY,
    content      TEXT          NOT NULL,
    created_date DATE          NOT NULL,
    upvotes      INT           NOT NULL DEFAULT 0,
    downvotes    INT           NOT NULL DEFAULT 0,
    anonymous    BOOLEAN       NOT NULL DEFAULT TRUE,
    is_official  BOOLEAN       NOT NULL DEFAULT FALSE,
    author_id    BIGINT        REFERENCES users (id) ON DELETE SET NULL,
    task_id      BIGINT        NOT NULL REFERENCES tasks (id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: comments
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS comments
(
    id           BIGSERIAL PRIMARY KEY,
    content      TEXT     NOT NULL,
    anonymous    BOOLEAN  NOT NULL DEFAULT FALSE,
    created_date DATE     NOT NULL,
    author_id    BIGINT   REFERENCES users (id) ON DELETE SET NULL,
    task_id      BIGINT   REFERENCES tasks (id) ON DELETE CASCADE,
    answer_id    BIGINT   REFERENCES answers (id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: questions (pytania wewnątrz zadania)
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS questions
(
    id           BIGSERIAL PRIMARY KEY,
    content      TEXT         NOT NULL,
    question_type VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    options_json TEXT,
    correct_answer TEXT,
    task_id      BIGINT       NOT NULL REFERENCES tasks (id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: votes
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS votes
(
    id         BIGSERIAL PRIMARY KEY,
    vote_type  VARCHAR(20) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    voter_id   BIGINT      REFERENCES users (id) ON DELETE SET NULL,
    answer_id  BIGINT      NOT NULL REFERENCES answers (id) ON DELETE CASCADE,
    UNIQUE (voter_id, answer_id)
);

-- ──────────────────────────────────────────────────────────────
--  Tabela: shares (tokeny udostępniania zadań)
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS shares
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(64)  NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP,
    task_id     BIGINT       NOT NULL REFERENCES tasks (id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────────────────────
--  Indeksy dla poprawy wydajności zapytań
-- ──────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_tasks_status      ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_category    ON tasks (category_id);
CREATE INDEX IF NOT EXISTS idx_tasks_author      ON tasks (author_id);
CREATE INDEX IF NOT EXISTS idx_tasks_created     ON tasks (created_date DESC);
CREATE INDEX IF NOT EXISTS idx_answers_task      ON answers (task_id);
CREATE INDEX IF NOT EXISTS idx_comments_task     ON comments (task_id);
CREATE INDEX IF NOT EXISTS idx_votes_answer      ON votes (answer_id);
CREATE INDEX IF NOT EXISTS idx_shares_token      ON shares (token);
CREATE INDEX IF NOT EXISTS idx_users_login       ON users (login);
