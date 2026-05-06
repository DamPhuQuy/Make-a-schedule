-- Seed a group meeting for testing
INSERT INTO appointments (name, start_time, end_time, appointment_type)
VALUES ('Team Standup', '2026-05-05 09:00:00+00'::TIMESTAMP WITH TIME ZONE, '2026-05-05 10:00:00+00'::TIMESTAMP WITH TIME ZONE, 'GROUP')
ON CONFLICT DO NOTHING;

-- Add participants to the group meeting
INSERT INTO group_meeting_participants (group_meeting_id, user_id)
SELECT a.id, u.id
FROM appointments a
CROSS JOIN users u
WHERE a.name = 'Team Standup'
  AND a.appointment_type = 'GROUP'
  AND u.email IN ('alice@example.com', 'bob@example.com')
ON CONFLICT DO NOTHING;
