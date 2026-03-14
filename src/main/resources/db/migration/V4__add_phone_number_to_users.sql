-- V4__add_phone_number_to_users.sql
-- Add phone_number column to users table for profile contact info

ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);

