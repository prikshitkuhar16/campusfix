-- V3__replace_title_desc_with_complaint_and_add_availability.sql
-- Replace title and description with a single complaint field
-- Add available timings fields for student availability

-- Add the new complaint column
ALTER TABLE complaints ADD COLUMN complaint TEXT;

-- Migrate existing data: concatenate title and description into complaint
UPDATE complaints SET complaint = COALESCE(title, '') || CASE WHEN description IS NOT NULL AND description != '' THEN ' - ' || description ELSE '' END;

-- Make complaint column NOT NULL after migration
ALTER TABLE complaints ALTER COLUMN complaint SET NOT NULL;

-- Drop old columns
ALTER TABLE complaints DROP COLUMN title;
ALTER TABLE complaints DROP COLUMN description;

-- Add availability fields
ALTER TABLE complaints ADD COLUMN available_from TIME;
ALTER TABLE complaints ADD COLUMN available_to TIME;
ALTER TABLE complaints ADD COLUMN available_anytime BOOLEAN NOT NULL DEFAULT TRUE;

