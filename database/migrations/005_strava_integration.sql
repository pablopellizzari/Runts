CREATE TABLE IF NOT EXISTS strava_connections (
    athlete_id VARCHAR(64) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    strava_athlete_id BIGINT NOT NULL UNIQUE,
    athlete_name VARCHAR(255) NOT NULL,
    access_token_encrypted TEXT NOT NULL,
    refresh_token_encrypted TEXT NOT NULL,
    expires_at BIGINT NOT NULL,
    scope TEXT NOT NULL,
    connected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_sync_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS strava_activities (
    strava_activity_id BIGINT PRIMARY KEY,
    athlete_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    sport_type VARCHAR(64) NOT NULL,
    start_date TIMESTAMPTZ NOT NULL,
    start_date_local TIMESTAMP NOT NULL,
    distance_meters DOUBLE PRECISION NOT NULL DEFAULT 0,
    moving_time_seconds INT NOT NULL DEFAULT 0,
    elapsed_time_seconds INT NOT NULL DEFAULT 0,
    average_heartrate INT,
    average_speed_mps DOUBLE PRECISION NOT NULL DEFAULT 0,
    matched_workout_id VARCHAR(64) REFERENCES workouts(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS integration_settings (
    key VARCHAR(80) PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE workout_executions ADD COLUMN IF NOT EXISTS source_provider VARCHAR(32);
ALTER TABLE workout_executions ADD COLUMN IF NOT EXISTS source_activity_id VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_strava_activities_athlete_date ON strava_activities(athlete_id,start_date_local);
CREATE INDEX IF NOT EXISTS idx_strava_activities_workout ON strava_activities(matched_workout_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_execution_source ON workout_executions(source_provider,source_activity_id)
WHERE source_provider IS NOT NULL AND source_activity_id IS NOT NULL;
