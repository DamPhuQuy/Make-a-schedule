-- Drop old tables
DROP TABLE IF EXISTS reminders CASCADE;
DROP TABLE IF EXISTS group_meeting_participants CASCADE;
DROP TABLE IF EXISTS group_meetings CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- Create appointments table with SINGLE_TABLE inheritance
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    appointment_type VARCHAR(31) NOT NULL,
    location VARCHAR(255),
    owner_id BIGINT,
    CONSTRAINT fk_appointments_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_appointments_owner_id ON appointments(owner_id);
CREATE INDEX idx_appointments_start_time ON appointments(start_time);
CREATE INDEX idx_appointments_end_time ON appointments(end_time);
CREATE INDEX idx_appointments_type ON appointments(appointment_type);

-- Create group_meeting_participants table
CREATE TABLE group_meeting_participants (
    id BIGSERIAL PRIMARY KEY,
    group_meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_group_meeting_participants_appointment FOREIGN KEY (group_meeting_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_meeting_participants_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_group_meeting_user UNIQUE (group_meeting_id, user_id)
);

CREATE INDEX idx_group_meeting_participants_meeting_id ON group_meeting_participants(group_meeting_id);
CREATE INDEX idx_group_meeting_participants_user_id ON group_meeting_participants(user_id);

-- Create reminders table
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    minutes_before INTEGER NOT NULL,
    CONSTRAINT fk_reminders_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_appointment_id ON reminders(appointment_id);
