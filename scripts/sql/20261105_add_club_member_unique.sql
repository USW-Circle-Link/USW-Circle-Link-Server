-- Manual migration for existing MySQL schemas (production ddl-auto is none).
-- First inspect the actual table/column names and existing indexes:
SHOW CREATE TABLE club_members_table;
SHOW INDEX FROM club_members_table;

-- STOP if this query returns any rows. Resolve duplicates after checking references
-- and taking a backup; this script deliberately does not delete any membership.
SELECT club_id, profile_id, COUNT(*) AS duplicate_count
FROM club_members_table
GROUP BY club_id, profile_id
HAVING COUNT(*) > 1;

-- Execute separately only after the duplicate query is empty, and only if the
-- equivalent unique index does not already exist. ALTER is not auto-run by the app.
ALTER TABLE club_members_table
    ADD CONSTRAINT uk_club_member UNIQUE (club_id, profile_id);
