import { createCipheriv, createDecipheriv, createHash, randomBytes } from 'node:crypto'
import { requireDatabase } from './db.js'

const STRAVA_OAUTH_URL = 'https://www.strava.com/oauth/mobile/authorize'
const STRAVA_TOKEN_URL = 'https://www.strava.com/api/v3/oauth/token'
const STRAVA_API_URL = 'https://www.strava.com/api/v3'
let schemaPromise

export function ensureStravaSchema() {
  schemaPromise ||= (async () => {
    const database = requireDatabase()
    await database.query(`CREATE TABLE IF NOT EXISTS strava_connections (
      athlete_id VARCHAR(64) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
      strava_athlete_id BIGINT NOT NULL UNIQUE, athlete_name VARCHAR(255) NOT NULL,
      access_token_encrypted TEXT NOT NULL, refresh_token_encrypted TEXT NOT NULL,
      expires_at BIGINT NOT NULL, scope TEXT NOT NULL, connected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      last_sync_at TIMESTAMPTZ, updated_at TIMESTAMPTZ NOT NULL DEFAULT now())`)
    await database.query(`CREATE TABLE IF NOT EXISTS strava_activities (
      strava_activity_id BIGINT PRIMARY KEY, athlete_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      name VARCHAR(255) NOT NULL, sport_type VARCHAR(64) NOT NULL, start_date TIMESTAMPTZ NOT NULL,
      start_date_local TIMESTAMP NOT NULL, distance_meters DOUBLE PRECISION NOT NULL DEFAULT 0,
      moving_time_seconds INT NOT NULL DEFAULT 0, elapsed_time_seconds INT NOT NULL DEFAULT 0,
      average_heartrate INT, average_speed_mps DOUBLE PRECISION NOT NULL DEFAULT 0,
      matched_workout_id VARCHAR(64) REFERENCES workouts(id) ON DELETE SET NULL,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now())`)
    await database.query('CREATE TABLE IF NOT EXISTS integration_settings (key VARCHAR(80) PRIMARY KEY,value TEXT NOT NULL,updated_at TIMESTAMPTZ NOT NULL DEFAULT now())')
    await database.query('ALTER TABLE workout_executions ADD COLUMN IF NOT EXISTS source_provider VARCHAR(32)')
    await database.query('ALTER TABLE workout_executions ADD COLUMN IF NOT EXISTS source_activity_id VARCHAR(128)')
    await database.query('CREATE INDEX IF NOT EXISTS idx_strava_activities_athlete_date ON strava_activities(athlete_id,start_date_local)')
    await database.query('CREATE INDEX IF NOT EXISTS idx_strava_activities_workout ON strava_activities(matched_workout_id)')
    await database.query(`CREATE UNIQUE INDEX IF NOT EXISTS idx_execution_source ON workout_executions(source_provider,source_activity_id)
      WHERE source_provider IS NOT NULL AND source_activity_id IS NOT NULL`)
  })().catch(error => {
    schemaPromise = null
    throw error
  })
  return schemaPromise
}

function configuration() {
  const values = {
    clientId: process.env.STRAVA_CLIENT_ID,
    clientSecret: process.env.STRAVA_CLIENT_SECRET,
    redirectUri: process.env.STRAVA_REDIRECT_URI,
    verifyToken: process.env.STRAVA_WEBHOOK_VERIFY_TOKEN,
    encryptionKey: process.env.STRAVA_TOKEN_ENCRYPTION_KEY,
  }
  const missing = Object.entries(values).filter(([, value]) => !value).map(([key]) => key)
  if (missing.length) {
    throw Object.assign(new Error(`Integração Strava incompleta: ${missing.join(', ')}.`), { status: 503 })
  }
  return values
}

function encryptionKey() {
  const value = configuration().encryptionKey
  if (value.length < 32) throw Object.assign(new Error('STRAVA_TOKEN_ENCRYPTION_KEY precisa ter pelo menos 32 caracteres.'), { status: 503 })
  return createHash('sha256').update(value).digest()
}

function encryptToken(value) {
  const iv = randomBytes(12)
  const cipher = createCipheriv('aes-256-gcm', encryptionKey(), iv)
  const encrypted = Buffer.concat([cipher.update(value, 'utf8'), cipher.final()])
  return `v1:${iv.toString('base64url')}:${cipher.getAuthTag().toString('base64url')}:${encrypted.toString('base64url')}`
}

function decryptToken(value) {
  const [version, iv, tag, encrypted] = value.split(':')
  if (version !== 'v1' || !iv || !tag || !encrypted) throw new Error('Token Strava armazenado em formato inválido.')
  const decipher = createDecipheriv('aes-256-gcm', encryptionKey(), Buffer.from(iv, 'base64url'))
  decipher.setAuthTag(Buffer.from(tag, 'base64url'))
  return Buffer.concat([decipher.update(Buffer.from(encrypted, 'base64url')), decipher.final()]).toString('utf8')
}

async function stravaRequest(url, options = {}) {
  const response = await fetch(url, options)
  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const message = body?.message || body?.error || `Strava respondeu HTTP ${response.status}.`
    throw Object.assign(new Error(message), { status: response.status >= 500 ? 502 : 400 })
  }
  return body
}

function tokenBody(values) {
  return new URLSearchParams(Object.entries(values).filter(([, value]) => value != null).map(([key, value]) => [key, String(value)]))
}

export function authorizationUrl(state) {
  const config = configuration()
  const url = new URL(STRAVA_OAUTH_URL)
  url.searchParams.set('client_id', config.clientId)
  url.searchParams.set('redirect_uri', config.redirectUri)
  url.searchParams.set('response_type', 'code')
  url.searchParams.set('approval_prompt', 'auto')
  url.searchParams.set('scope', 'read,activity:read')
  url.searchParams.set('state', state)
  return url.toString()
}

export async function exchangeAuthorizationCode(code) {
  const config = configuration()
  return stravaRequest(STRAVA_TOKEN_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: tokenBody({ client_id: config.clientId, client_secret: config.clientSecret, code, grant_type: 'authorization_code' }),
  })
}

export async function saveConnection(athleteId, token) {
  await ensureStravaSchema()
  const scopes = String(token.scope || '')
  if (!scopes.split(/[ ,]+/).includes('activity:read') && !scopes.split(/[ ,]+/).includes('activity:read_all')) {
    throw Object.assign(new Error('Autorize a leitura de atividades para conectar o Strava.'), { status: 400 })
  }
  const athlete = token.athlete || {}
  const athleteName = [athlete.firstname, athlete.lastname].filter(Boolean).join(' ').trim() || 'Atleta Strava'
  await requireDatabase().query(
    `INSERT INTO strava_connections
      (athlete_id,strava_athlete_id,athlete_name,access_token_encrypted,refresh_token_encrypted,expires_at,scope,connected_at,updated_at)
     VALUES ($1,$2,$3,$4,$5,$6,$7,now(),now())
     ON CONFLICT (athlete_id) DO UPDATE SET strava_athlete_id=EXCLUDED.strava_athlete_id,
      athlete_name=EXCLUDED.athlete_name,access_token_encrypted=EXCLUDED.access_token_encrypted,
      refresh_token_encrypted=EXCLUDED.refresh_token_encrypted,expires_at=EXCLUDED.expires_at,
      scope=EXCLUDED.scope,updated_at=now()`,
    [athleteId, athlete.id, athleteName, encryptToken(token.access_token), encryptToken(token.refresh_token), token.expires_at, scopes],
  )
}

async function connectionForAthlete(athleteId) {
  await ensureStravaSchema()
  const { rows } = await requireDatabase().query('SELECT * FROM strava_connections WHERE athlete_id=$1', [athleteId])
  if (!rows[0]) throw Object.assign(new Error('Conta Strava não conectada.'), { status: 404 })
  return rows[0]
}

async function accessTokenFor(connection) {
  if (Number(connection.expires_at) > Math.floor(Date.now() / 1000) + 3600) return decryptToken(connection.access_token_encrypted)
  const config = configuration()
  const refreshed = await stravaRequest(STRAVA_TOKEN_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: tokenBody({
      client_id: config.clientId,
      client_secret: config.clientSecret,
      grant_type: 'refresh_token',
      refresh_token: decryptToken(connection.refresh_token_encrypted),
    }),
  })
  await requireDatabase().query(
    `UPDATE strava_connections SET access_token_encrypted=$2,refresh_token_encrypted=$3,expires_at=$4,updated_at=now()
     WHERE athlete_id=$1`,
    [connection.athlete_id, encryptToken(refreshed.access_token), encryptToken(refreshed.refresh_token), refreshed.expires_at],
  )
  return refreshed.access_token
}

function localDate(activity) {
  return String(activity.start_date_local || activity.start_date || '').slice(0, 10)
}

async function matchingWorkout(athleteId, date) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return null
  const { rows } = await requireDatabase().query(
    `SELECT id FROM workouts WHERE athlete_id=$1 AND target_date::date=$2::date AND status='PENDING'
     ORDER BY updated_at DESC NULLS LAST`,
    [athleteId, date],
  )
  return rows.length === 1 ? rows[0].id : null
}

async function storeActivity(athleteId, activity) {
  const date = localDate(activity)
  const matchedWorkoutId = await matchingWorkout(athleteId, date)
  await requireDatabase().query(
    `INSERT INTO strava_activities
      (strava_activity_id,athlete_id,name,sport_type,start_date,start_date_local,distance_meters,moving_time_seconds,
       elapsed_time_seconds,average_heartrate,average_speed_mps,matched_workout_id,updated_at)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,now())
     ON CONFLICT (strava_activity_id) DO UPDATE SET name=EXCLUDED.name,sport_type=EXCLUDED.sport_type,
      start_date=EXCLUDED.start_date,start_date_local=EXCLUDED.start_date_local,distance_meters=EXCLUDED.distance_meters,
      moving_time_seconds=EXCLUDED.moving_time_seconds,elapsed_time_seconds=EXCLUDED.elapsed_time_seconds,
      average_heartrate=EXCLUDED.average_heartrate,average_speed_mps=EXCLUDED.average_speed_mps,
      matched_workout_id=COALESCE(strava_activities.matched_workout_id,EXCLUDED.matched_workout_id),updated_at=now()`,
    [activity.id, athleteId, activity.name || 'Atividade no Strava', activity.sport_type || activity.type || 'Workout',
      activity.start_date, activity.start_date_local, Number(activity.distance || 0), Number(activity.moving_time || 0),
      Number(activity.elapsed_time || 0), activity.average_heartrate == null ? null : Math.round(activity.average_heartrate),
      Number(activity.average_speed || 0), matchedWorkoutId],
  )
  return matchedWorkoutId
}

export async function syncActivities(athleteId) {
  const connection = await connectionForAthlete(athleteId)
  const accessToken = await accessTokenFor(connection)
  const url = new URL(`${STRAVA_API_URL}/athlete/activities`)
  url.searchParams.set('after', String(Math.floor(Date.now() / 1000) - 90 * 24 * 60 * 60))
  url.searchParams.set('per_page', '100')
  const activities = await stravaRequest(url, { headers: { authorization: `Bearer ${accessToken}` } })
  let matched = 0
  for (const activity of activities) {
    if (await storeActivity(athleteId, activity)) matched += 1
  }
  await requireDatabase().query('UPDATE strava_connections SET last_sync_at=now(),updated_at=now() WHERE athlete_id=$1', [athleteId])
  return { imported: activities.length, matched }
}

export async function connectionStatus(athleteId) {
  await ensureStravaSchema()
  const { rows } = await requireDatabase().query(
    'SELECT athlete_name,scope,last_sync_at,connected_at FROM strava_connections WHERE athlete_id=$1', [athleteId],
  )
  const row = rows[0]
  return row ? { connected: true, athleteName: row.athlete_name, scope: row.scope, lastSyncAt: row.last_sync_at, connectedAt: row.connected_at } : { connected: false }
}

export async function suggestedActivity(athleteId, workoutId) {
  await ensureStravaSchema()
  const workout = await requireDatabase().query('SELECT id,target_date FROM workouts WHERE id=$1 AND athlete_id=$2', [workoutId, athleteId])
  if (!workout.rows[0]) throw Object.assign(new Error('Treino não encontrado.'), { status: 404 })
  const { rows } = await requireDatabase().query(
    `SELECT * FROM strava_activities WHERE athlete_id=$1 AND
      (matched_workout_id=$2 OR (matched_workout_id IS NULL AND start_date_local::date=$3::date))
     AND sport_type IN ('Run','VirtualRun','TrailRun')
     ORDER BY (matched_workout_id=$2) DESC,start_date DESC LIMIT 1`,
    [athleteId, workoutId, workout.rows[0].target_date],
  )
  if (!rows[0]) return null
  const activity = rows[0]
  const distanceKm = Number(activity.distance_meters) / 1000
  const durationSeconds = Number(activity.moving_time_seconds || activity.elapsed_time_seconds)
  const paceSeconds = distanceKm > 0 ? Math.round(durationSeconds / distanceKm) : 0
  return {
    external_id: String(activity.strava_activity_id),
    provider: 'STRAVA',
    name: activity.name,
    distance_meters: Number(activity.distance_meters),
    moving_time_seconds: durationSeconds,
    average_speed_mps: Number(activity.average_speed_mps),
    average_heartrate: activity.average_heartrate,
    start_date_local: activity.start_date_local,
    pace: paceSeconds ? `${Math.floor(paceSeconds / 60)}:${String(paceSeconds % 60).padStart(2, '0')}` : '',
  }
}

export async function disconnectStrava(athleteId) {
  const connection = await connectionForAthlete(athleteId)
  const accessToken = await accessTokenFor(connection)
  const config = configuration()
  const basicCredentials = Buffer.from(`${config.clientId}:${config.clientSecret}`).toString('base64')
  await fetch('https://www.strava.com/oauth/revoke', {
    method: 'POST',
    headers: { authorization: `Basic ${basicCredentials}`, 'content-type': 'application/x-www-form-urlencoded' },
    body: tokenBody({ token: accessToken, token_type_hint: 'access_token' }),
  }).catch(() => null)
  await requireDatabase().query('DELETE FROM strava_connections WHERE athlete_id=$1', [athleteId])
}

export async function processWebhookEvent(event) {
  await ensureStravaSchema()
  const subscription = await requireDatabase().query("SELECT value FROM integration_settings WHERE key='strava_webhook_subscription_id'")
  if (!subscription.rows[0] || String(event.subscription_id) !== subscription.rows[0].value) return
  if (event.object_type === 'athlete' && event.aspect_type === 'update' && event.updates?.authorized === 'false') {
    await requireDatabase().query('DELETE FROM strava_connections WHERE strava_athlete_id=$1', [event.owner_id])
    return
  }
  if (event.object_type !== 'activity') return
  const { rows } = await requireDatabase().query('SELECT * FROM strava_connections WHERE strava_athlete_id=$1', [event.owner_id])
  const connection = rows[0]
  if (!connection) return
  if (event.aspect_type === 'delete') {
    await requireDatabase().query('DELETE FROM strava_activities WHERE strava_activity_id=$1 AND athlete_id=$2', [event.object_id, connection.athlete_id])
    return
  }
  const accessToken = await accessTokenFor(connection)
  const activity = await stravaRequest(`${STRAVA_API_URL}/activities/${event.object_id}`, { headers: { authorization: `Bearer ${accessToken}` } })
  await storeActivity(connection.athlete_id, activity)
}

export function verifyWebhookToken(value) {
  return value && value === configuration().verifyToken
}

export async function ensureWebhookSubscription() {
  const config = configuration()
  const query = new URLSearchParams({ client_id: config.clientId, client_secret: config.clientSecret })
  const subscriptions = await stravaRequest(`${STRAVA_API_URL}/push_subscriptions?${query}`)
  let subscription
  if (Array.isArray(subscriptions) && subscriptions.length) subscription = subscriptions[0]
  const callbackUrl = `${new URL(config.redirectUri).origin}/api/integrations/strava/webhook`
  subscription ||= await stravaRequest(`${STRAVA_API_URL}/push_subscriptions`, {
      method: 'POST',
      headers: { 'content-type': 'application/x-www-form-urlencoded' },
      body: tokenBody({ client_id: config.clientId, client_secret: config.clientSecret, callback_url: callbackUrl, verify_token: config.verifyToken }),
    })
  await ensureStravaSchema()
  await requireDatabase().query(
    `INSERT INTO integration_settings (key,value,updated_at) VALUES ('strava_webhook_subscription_id',$1,now())
     ON CONFLICT (key) DO UPDATE SET value=EXCLUDED.value,updated_at=now()`, [String(subscription.id)],
  )
  return subscription
}
