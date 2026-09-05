import assert from 'node:assert/strict'
import test from 'node:test'
import { createStravaState, verifyStravaState } from '../src/server/auth.js'
import { authorizationUrl } from '../src/server/strava.js'

process.env.JWT_SECRET = 'runts-test-secret-with-more-than-32-characters'
process.env.STRAVA_CLIENT_ID = '12345'
process.env.STRAVA_CLIENT_SECRET = 'test-client-secret'
process.env.STRAVA_REDIRECT_URI = 'https://runts.example/api/integrations/strava/callback'
process.env.STRAVA_WEBHOOK_VERIFY_TOKEN = 'test-webhook-token'
process.env.STRAVA_TOKEN_ENCRYPTION_KEY = 'test-encryption-key-with-more-than-32-characters'

test('estado OAuth identifica o atleta e rejeita adulterações', () => {
  const state = createStravaState('athlete-123')
  assert.equal(verifyStravaState(state), 'athlete-123')
  assert.throws(() => verifyStravaState(`${state}changed`))
})

test('URL de autorização solicita somente leitura de atividades', () => {
  const url = new URL(authorizationUrl('signed-state'))
  assert.equal(url.origin, 'https://www.strava.com')
  assert.equal(url.searchParams.get('client_id'), '12345')
  assert.equal(url.searchParams.get('response_type'), 'code')
  assert.equal(url.searchParams.get('scope'), 'read,activity:read')
  assert.equal(url.searchParams.get('state'), 'signed-state')
})
