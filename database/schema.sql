-- Runts / PostgreSQL (Neon). Execute com uma conta administrativa apenas no ambiente remoto.
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(32) NOT NULL CHECK (user_type IN ('COACH', 'ATHLETE')),
    coach_id VARCHAR(64) REFERENCES users(id),
    invite_code VARCHAR(32) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    timezone VARCHAR(80) NOT NULL DEFAULT 'America/Sao_Paulo'
);
CREATE TABLE IF NOT EXISTS workouts (
    id VARCHAR(64) PRIMARY KEY,
    athlete_id VARCHAR(64) NOT NULL REFERENCES users(id),
    target_date VARCHAR(32) NOT NULL,
    workout_type VARCHAR(32) NOT NULL,
    target_distance_km DOUBLE PRECISION NOT NULL CHECK (target_distance_km >= 0),
    target_duration_minutes INT NOT NULL CHECK (target_duration_minutes >= 0),
    target_pace VARCHAR(32) NOT NULL,
    target_hr_zone VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    name VARCHAR(160),
    route_type VARCHAR(32),
    effort INT CHECK (effort BETWEEN 1 AND 10),
    coach_id VARCHAR(64) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_workouts_athlete_date ON workouts(athlete_id, target_date);
CREATE TABLE IF NOT EXISTS workout_segments (
    id UUID PRIMARY KEY,
    workout_id VARCHAR(64) NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    position INT NOT NULL CHECK (position >= 0),
    segment_type VARCHAR(32) NOT NULL,
    duration_type VARCHAR(16) NOT NULL CHECK (duration_type IN ('TIME', 'DISTANCE')),
    duration_value DOUBLE PRECISION NOT NULL CHECK (duration_value > 0),
    intensity VARCHAR(80) NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    UNIQUE(workout_id, position)
);
CREATE TABLE IF NOT EXISTS favorite_workouts (
    id UUID PRIMARY KEY,
    coach_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    route_type VARCHAR(32) NOT NULL,
    effort INT NOT NULL CHECK (effort BETWEEN 1 AND 10),
    workout_type VARCHAR(32) NOT NULL,
    target_distance_km DOUBLE PRECISION NOT NULL CHECK (target_distance_km >= 0),
    target_duration_minutes INT NOT NULL CHECK (target_duration_minutes >= 0),
    target_pace VARCHAR(32) NOT NULL,
    target_hr_zone VARCHAR(32) NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS favorite_workout_segments (
    id UUID PRIMARY KEY,
    favorite_id UUID NOT NULL REFERENCES favorite_workouts(id) ON DELETE CASCADE,
    position INT NOT NULL CHECK (position >= 0),
    segment_type VARCHAR(32) NOT NULL,
    duration_type VARCHAR(16) NOT NULL CHECK (duration_type IN ('TIME', 'DISTANCE')),
    duration_value DOUBLE PRECISION NOT NULL CHECK (duration_value > 0),
    intensity VARCHAR(80) NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    UNIQUE(favorite_id, position)
);
CREATE TABLE IF NOT EXISTS race_events (
    id VARCHAR(64) PRIMARY KEY,
    athlete_id VARCHAR(64) NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    date VARCHAR(32) NOT NULL,
    modality VARCHAR(32) NOT NULL,
    target_time VARCHAR(32),
    priority VARCHAR(32) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_races_athlete_date ON race_events(athlete_id, date);
CREATE TABLE IF NOT EXISTS workout_executions (
    id VARCHAR(64) PRIMARY KEY,
    prescribed_workout_id VARCHAR(64) REFERENCES workouts(id),
    athlete_id VARCHAR(64) NOT NULL REFERENCES users(id),
    execution_date VARCHAR(32) NOT NULL,
    actual_distance_km DOUBLE PRECISION NOT NULL CHECK (actual_distance_km >= 0),
    actual_duration_seconds INT NOT NULL CHECK (actual_duration_seconds >= 0),
    actual_pace VARCHAR(32) NOT NULL,
    actual_avg_hr INT,
    pse INT NOT NULL CHECK (pse BETWEEN 1 AND 10),
    encrypted_gps_data_json TEXT,
    comments TEXT
);
CREATE TABLE IF NOT EXISTS training_sheets (
    id VARCHAR(64) PRIMARY KEY,
    coach_id VARCHAR(64) NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at VARCHAR(32) NOT NULL
);
CREATE TABLE IF NOT EXISTS sheet_workouts (
    id VARCHAR(64) PRIMARY KEY,
    sheet_id VARCHAR(64) NOT NULL REFERENCES training_sheets(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    workout_type VARCHAR(32) NOT NULL,
    target_distance_km DOUBLE PRECISION NOT NULL,
    target_duration_minutes INT NOT NULL,
    target_pace VARCHAR(32) NOT NULL,
    target_hr_zone VARCHAR(32) NOT NULL,
    description TEXT NOT NULL
);
