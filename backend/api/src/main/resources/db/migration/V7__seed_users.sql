-- Seed users with bcrypt-hashed password "123456"
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (email, password)
VALUES
    ('alice@example.com', crypt('123456', gen_salt('bf', 10))),
    ('bob@example.com', crypt('123456', gen_salt('bf', 10))),
    ('charlie@example.com', crypt('123456', gen_salt('bf', 10))),
    ('diana@example.com', crypt('123456', gen_salt('bf', 10))),
    ('eric@example.com', crypt('123456', gen_salt('bf', 10)))
ON CONFLICT (email) DO NOTHING;
