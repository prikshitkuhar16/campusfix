-- V2__add_job_type_and_room_to_complaints.sql
-- Add job_type and room columns to complaints table

ALTER TABLE complaints ADD COLUMN job_type VARCHAR(50);
ALTER TABLE complaints ADD COLUMN room VARCHAR(255);

CREATE INDEX idx_complaints_job_type ON complaints(job_type);
CREATE INDEX idx_complaints_created_by ON complaints(created_by);
CREATE INDEX idx_complaints_assigned_to ON complaints(assigned_to);

