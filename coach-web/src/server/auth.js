import 'dotenv/config'
import jwt from 'jsonwebtoken'

function secret() {
  const value = process.env.JWT_SECRET
  if (!value || value.length < 32) {
    const error = new Error('JWT_SECRET precisa ter pelo menos 32 caracteres.')
    error.status = 503
    throw error
  }
  return value
}

export function createToken(user) {
  return jwt.sign({ sub: user.id, role: user.user_type }, secret(), { expiresIn: '12h', issuer: 'runts-coach' })
}

function roleAuthentication(role, requestProperty, forbiddenMessage) {
  return (req, _res, next) => {
    try {
      const token = req.headers.authorization?.replace(/^Bearer\s+/i, '')
      if (!token) throw Object.assign(new Error('Sessão necessária.'), { status: 401 })
      const payload = jwt.verify(token, secret(), { issuer: 'runts-coach' })
      if (payload.role !== role) throw Object.assign(new Error(forbiddenMessage), { status: 403 })
      req[requestProperty] = payload.sub
      next()
    } catch (error) {
      error.status ||= 401
      next(error)
    }
  }
}

export const authenticate = roleAuthentication('COACH', 'coachId', 'Acesso exclusivo para treinadores.')
export const authenticateAthlete = roleAuthentication('ATHLETE', 'athleteId', 'Acesso exclusivo para atletas.')

export function createStravaState(athleteId) {
  return jwt.sign({ sub: athleteId, purpose: 'strava-oauth' }, secret(), {
    expiresIn: '10m',
    issuer: 'runts-coach',
    audience: 'strava-callback',
  })
}

export function verifyStravaState(state) {
  const payload = jwt.verify(state, secret(), { issuer: 'runts-coach', audience: 'strava-callback' })
  if (payload.purpose !== 'strava-oauth') throw new Error('Estado OAuth inválido.')
  return payload.sub
}
