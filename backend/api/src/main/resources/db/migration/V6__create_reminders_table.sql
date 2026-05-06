-- Create reminders table
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    minutes_before INTEGER NOT NULL,
    CONSTRAINT fk_reminders_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- Create index for better query performance
CREATE INDEX idx_reminders_appointment_id ON reminders(appointment_id);
