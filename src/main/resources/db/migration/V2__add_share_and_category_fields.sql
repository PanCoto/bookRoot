-- ============================================================
-- V2 – Share entity corrections + Category.createdAt
-- Adds: share_type, owner_id, expires_at, created_at to shares
-- Adds: created_at to categories
-- ============================================================

-- ── categories ───────────────────────────────────────────────
ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

-- Back-fill existing rows
UPDATE categories SET created_at = NOW() WHERE created_at IS NULL;

-- ── shares ───────────────────────────────────────────────────
ALTER TABLE shares
    ADD COLUMN IF NOT EXISTS share_type  VARCHAR(30) NOT NULL DEFAULT 'PUBLIC_LINK',
    ADD COLUMN IF NOT EXISTS owner_id    BIGINT,
    ADD COLUMN IF NOT EXISTS expires_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMP;

-- Back-fill share owner from task author where possible
UPDATE shares s
SET owner_id   = t.author_id,
    created_at = NOW()
FROM tasks t
WHERE s.task_id = t.id
  AND s.owner_id IS NULL;

-- Set NOT NULL after back-fill (only if existing data was updated)
-- ALTER TABLE shares ALTER COLUMN owner_id SET NOT NULL;

-- Add FK constraint for owner_id if not present
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'shares'
          AND constraint_name = 'fk_shares_owner'
    ) THEN
        ALTER TABLE shares
            ADD CONSTRAINT fk_shares_owner
            FOREIGN KEY (owner_id) REFERENCES users(id);
    END IF;
END $$;
