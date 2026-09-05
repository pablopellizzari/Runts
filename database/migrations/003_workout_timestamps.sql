-- Compatibilidade com bancos inicialmente criados pelo aplicativo Android.
ALTER TABLE workouts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE workouts SET updated_at = now() WHERE updated_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_workouts_updated_at ON workouts(updated_at);
