-- V5__update_complaint_status_workflow.sql

-- Ensure any existing 'IN_PROGRESS' records are migrated to 'ASSIGNED'
UPDATE complaints SET status = 'ASSIGNED' WHERE status = 'IN_PROGRESS';

UPDATE complaint_status_history SET previous_status = 'ASSIGNED' WHERE previous_status = 'IN_PROGRESS';
UPDATE complaint_status_history SET new_status = 'ASSIGNED' WHERE new_status = 'IN_PROGRESS';

-- Update the CHECK constraint on complaints table
ALTER TABLE complaints DROP CONSTRAINT chk_status;

ALTER TABLE complaints ADD CONSTRAINT chk_status
    CHECK (status IN ('CREATED', 'ASSIGNED', 'RESOLVED', 'VERIFIED'));

