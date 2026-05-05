-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create appointment table
CREATE TABLE IF NOT EXISTS appointment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    meeting_date DATE NOT NULL,
    start_hour INTEGER NOT NULL CHECK (start_hour >= 0 AND start_hour <= 23),
    end_hour INTEGER NOT NULL CHECK (end_hour >= 0 AND end_hour <= 23),
    type_appointment VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_hours CHECK (start_hour < end_hour)
);

-- Create reminder table
CREATE TABLE IF NOT EXISTS reminder (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create take table (many-to-many relationship between users and appointments)
CREATE TABLE IF NOT EXISTS take (
    user_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, appointment_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE
);

-- Create take_rmd table (many-to-many relationship between appointments and reminders)
CREATE TABLE IF NOT EXISTS take_rmd (
    appointment_id BIGINT NOT NULL,
    reminder_id BIGINT NOT NULL,
    PRIMARY KEY (appointment_id, reminder_id),
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_appointment_meeting_date ON appointment(meeting_date);
CREATE INDEX IF NOT EXISTS idx_appointment_type ON appointment(type_appointment);
CREATE INDEX IF NOT EXISTS idx_take_user_id ON take(user_id);
CREATE INDEX IF NOT EXISTS idx_take_appointment_id ON take(appointment_id);
CREATE INDEX IF NOT EXISTS idx_take_rmd_appointment_id ON take_rmd(appointment_id);
CREATE INDEX IF NOT EXISTS idx_take_rmd_reminder_id ON take_rmd(reminder_id);

-- Insert sample reminders
INSERT INTO reminder (title, created_at, updated_at) VALUES
    ('15 phút trước', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('30 phút trước', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('1 giờ trước', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('1 ngày trước', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (title) DO NOTHING;
