-- Seed a group meeting for testing
INSERT INTO group_meetings (name, start_time, end_time)
VALUES ('Team Standup', '2026-05-05 09:00:00+00', '2026-05-05 10:00:00+00')
ON CONFLICT DO NOTHING;

-- Add participants to the group meeting
INSERT INTO group_meeting_participants (group_meeting_id, user_id)
SELECT gm.id, u.id
FROM group_meetings gm
CROSS JOIN users u
WHERE gm.name = 'Team Standup'
  AND u.email IN ('alice@example.com', 'bob@example.com')
ON CONFLICT DO NOTHING;
