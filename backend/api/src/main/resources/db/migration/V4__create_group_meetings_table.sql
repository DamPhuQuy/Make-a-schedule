-- Create group_meetings table
CREATE TABLE group_meetings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_group_meetings_start_time ON group_meetings(start_time);
CREATE INDEX idx_group_meetings_end_time ON group_meetings(end_time);
