-- Create group_meeting_participants table
CREATE TABLE group_meeting_participants (
    id BIGSERIAL PRIMARY KEY,
    group_meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_participants_group_meeting FOREIGN KEY (group_meeting_id) REFERENCES group_meetings(id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_group_meeting_user UNIQUE (group_meeting_id, user_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_participants_group_meeting_id ON group_meeting_participants(group_meeting_id);
CREATE INDEX idx_participants_user_id ON group_meeting_participants(user_id);
