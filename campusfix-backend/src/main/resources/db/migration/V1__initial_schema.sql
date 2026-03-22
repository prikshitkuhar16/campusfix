-- V1__initial_schema.sql
-- CampusFix Backend – Consolidated Schema

-- ========================================
-- CAMPUSES TABLE
-- ========================================
CREATE TABLE campuses (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    description TEXT,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- ========================================
-- BUILDINGS TABLE
-- ========================================
CREATE TABLE buildings (
    id UUID PRIMARY KEY,
    number VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    campus_id UUID NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_buildings_campus FOREIGN KEY (campus_id) REFERENCES campuses(id)
);

CREATE INDEX idx_buildings_campus_id ON buildings(campus_id);

-- ========================================
-- USERS TABLE
-- ========================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    firebase_uid VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    job_type VARCHAR(50),
    phone_number VARCHAR(20),
    campus_id UUID NOT NULL,
    building_id UUID,
    is_active BOOLEAN DEFAULT TRUE,
    invited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_campus FOREIGN KEY (campus_id) REFERENCES campuses(id),
    CONSTRAINT fk_users_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT chk_role CHECK (role IN ('CAMPUS_ADMIN', 'BUILDING_ADMIN', 'STUDENT', 'STAFF'))
);

CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_users_campus_id ON users(campus_id);
CREATE INDEX idx_users_role ON users(role);

-- ========================================
-- OTP VERIFICATIONS TABLE
-- ========================================
CREATE TABLE otp_verifications (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_purpose CHECK (purpose IN ('SIGNUP', 'CREATE_CAMPUS'))
);

CREATE INDEX idx_otp_verifications_email ON otp_verifications(email);
CREATE INDEX idx_otp_verifications_purpose ON otp_verifications(purpose);

-- ========================================
-- INVITE TOKENS TABLE
-- ========================================
CREATE TABLE invite_tokens (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    job_type VARCHAR(50),
    token VARCHAR(255) UNIQUE NOT NULL,
    campus_id UUID NOT NULL,
    building_id UUID,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invite_tokens_campus FOREIGN KEY (campus_id) REFERENCES campuses(id),
    CONSTRAINT fk_invite_tokens_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT chk_invite_role CHECK (role IN ('CAMPUS_ADMIN', 'BUILDING_ADMIN', 'STUDENT', 'STAFF'))
);

-- ========================================
-- COMPLAINTS TABLE
-- ========================================
CREATE TABLE complaints (
    id UUID PRIMARY KEY,
    complaint TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    job_type VARCHAR(50),
    room VARCHAR(255),
    available_from TIME,
    available_to TIME,
    available_anytime BOOLEAN DEFAULT TRUE,
    campus_id UUID NOT NULL,
    building_id UUID NOT NULL,
    created_by UUID NOT NULL,
    assigned_to UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_complaints_campus FOREIGN KEY (campus_id) REFERENCES campuses(id),
    CONSTRAINT fk_complaints_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT fk_complaints_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_complaints_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT chk_status CHECK (status IN ('CREATED', 'ASSIGNED', 'RESOLVED', 'VERIFIED'))
);

CREATE INDEX idx_complaints_campus_id ON complaints(campus_id);
CREATE INDEX idx_complaints_building_id ON complaints(building_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_job_type ON complaints(job_type);
CREATE INDEX idx_complaints_created_by ON complaints(created_by);
CREATE INDEX idx_complaints_assigned_to ON complaints(assigned_to);

-- ========================================
-- COMPLAINT STATUS HISTORY TABLE (Now referencing complaints)
-- ========================================
CREATE TABLE complaint_status_history (
    id UUID PRIMARY KEY,
    complaint_id UUID NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_complaint_status_history_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_complaint_status_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id)
);

