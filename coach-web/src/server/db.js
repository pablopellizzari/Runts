import pg from 'pg'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const { Pool } = pg

function databaseUrlFromEnvironment() {
  if (process.env.DATABASE_URL) return process.env.DATABASE_URL
  const localFile = resolve('../local.properties')
  const local = existsSync(localFile)
    ? Object.fromEntries(readFileSync(localFile, 'utf8').split(/\r?\n/).filter(line => line && !line.startsWith('#') && line.includes('=')).map(line => {
      const index = line.indexOf('=')
      return [line.slice(0, index).trim(), line.slice(index + 1).trim()]
    }))
    : {}
  const { NEON_HOST, NEON_DATABASE, NEON_USER, NEON_PASSWORD } = { ...local, ...process.env }
  if (!NEON_HOST || !NEON_DATABASE || !NEON_USER || !NEON_PASSWORD) return null
  return `postgresql://${encodeURIComponent(NEON_USER)}:${encodeURIComponent(NEON_PASSWORD)}@${NEON_HOST}/${encodeURIComponent(NEON_DATABASE)}?sslmode=verify-full`
}

const connectionString = databaseUrlFromEnvironment()

export const pool = connectionString
  ? new Pool({ connectionString, max: 10 })
  : null

export function requireDatabase() {
  if (!pool) {
    const error = new Error('Banco não configurado. Defina DATABASE_URL no arquivo .env do painel.')
    error.status = 503
    throw error
  }
  return pool
}

export async function transaction(work) {
  const client = await requireDatabase().connect()
  try {
    await client.query('BEGIN')
    const result = await work(client)
    await client.query('COMMIT')
    return result
  } catch (error) {
    await client.query('ROLLBACK')
    throw error
  } finally {
    client.release()
  }
}
