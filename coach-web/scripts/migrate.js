import 'dotenv/config'
import { readFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import pg from 'pg'

function parseProperties(text) {
  return Object.fromEntries(text.split(/\r?\n/).filter(line => line && !line.startsWith('#') && line.includes('=')).map(line => {
    const index = line.indexOf('=')
    return [line.slice(0, index).trim(), line.slice(index + 1).trim()]
  }))
}

let connectionString = process.env.DATABASE_URL
if (!connectionString) {
  const properties = parseProperties(await readFile(resolve('../local.properties'), 'utf8'))
  connectionString = `postgresql://${encodeURIComponent(properties.NEON_USER)}:${encodeURIComponent(properties.NEON_PASSWORD)}@${properties.NEON_HOST}/${encodeURIComponent(properties.NEON_DATABASE)}?sslmode=verify-full`
}

const client = new pg.Client({ connectionString })
await client.connect()
try {
  await client.query('BEGIN')
  await client.query(await readFile(resolve('../database/migrations/002_coach_web.sql'), 'utf8'))
  await client.query(await readFile(resolve('../database/migrations/003_workout_timestamps.sql'), 'utf8'))
  await client.query(await readFile(resolve('../database/migrations/004_normalize_race_dates.sql'), 'utf8'))
  await client.query(await readFile(resolve('../database/migrations/005_strava_integration.sql'), 'utf8'))
  await client.query('COMMIT')
  console.log('Migrações do painel aplicadas com sucesso.')
} catch (error) {
  await client.query('ROLLBACK')
  throw error
} finally {
  await client.end()
}
