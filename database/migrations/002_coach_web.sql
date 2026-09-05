-- Expande o esquema existente sem invalidar dados consumidos pelo aplicativo Android.
ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(80) NOT NULL DEFAULT 'America/Sao_Paulo';
ALTER TABLE workouts ADD COLUMN IF NOT EXISTS name VARCHAR(160);
ALTER TABLE workouts ADD COLUMN IF NOT EXISTS route_type VARCHAR(32);
ALTER TABLE workouts ADD COLUMN IF NOT EXISTS effort INT CHECK (effort BETWEEN 1 AND 10);
ALTER TABLE workouts ADD COLUMN IF NOT EXISTS coach_id VARCHAR(64) REFERENCES users(id);

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

CREATE INDEX IF NOT EXISTS idx_workout_segments_workout ON workout_segments(workout_id, position);
CREATE INDEX IF NOT EXISTS idx_favorite_workouts_coach ON favorite_workouts(coach_id, name);
