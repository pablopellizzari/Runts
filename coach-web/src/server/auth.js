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

export function authenticate(req, _res, next) {
  try {
    const token = req.headers.authorization?.replace(/^Bearer\s+/i, '')
    if (!token) throw Object.assign(new Error('Sessão necessária.'), { status: 401 })
    const payload = jwt.verify(token, secret(), { issuer: 'runts-coach' })
    if (payload.role !== 'COACH') throw Object.assign(new Error('Acesso exclusivo para treinadores.'), { status: 403 })
    req.coachId = payload.sub
    next()
  } catch (error) {
    error.status ||= 401
    next(error)
  }
}
