import 'dotenv/config'
import bcrypt from 'bcryptjs'
import cors from 'cors'
import express from 'express'
import { randomUUID } from 'node:crypto'
import { authenticate, createToken } from './auth.js'
import { requireDatabase, transaction } from './db.js'
import { credentialsSchema, registrationSchema, workoutSchema } from './validation.js'

const app = express()
const port = Number(process.env.PORT || 8787)

app.use(cors({ origin: process.env.CLIENT_ORIGIN?.split(',') || ['http://localhost:5173'] }))
app.use(express.json({ limit: '256kb' }))

const publicUser = ({ password_hash: _, ...user }) => user

app.get('/api/health', async (_req, res, next) => {
  try {
    await requireDatabase().query('SELECT updated_at FROM workouts LIMIT 0')
    res.json({ ok: true })
  } catch (error) { next(error) }
})

app.post('/api/auth/register', async (req, res, next) => {
  try {
    const data = registrationSchema.parse(req.body)
    const id = randomUUID()
    const inviteCode = `RNTS-${randomUUID().slice(0, 8).toUpperCase()}`
    const passwordHash = await bcrypt.hash(data.password, 12)
    const { rows } = await requireDatabase().query(
      `INSERT INTO users (id,name,email,password_hash,user_type,invite_code,timezone)
       VALUES ($1,$2,lower($3),$4,'COACH',$5,$6)
       RETURNING id,name,email,user_type,invite_code,timezone`,
      [id, data.name, data.email, passwordHash, inviteCode, data.timezone],
    )
    res.status(201).json({ token: createToken(rows[0]), user: rows[0] })
  } catch (error) { next(error) }
})

app.post('/api/auth/login', async (req, res, next) => {
  try {
    const data = credentialsSchema.parse(req.body)
    const { rows } = await requireDatabase().query('SELECT * FROM users WHERE lower(email)=lower($1) LIMIT 1', [data.email])
    const user = rows[0]
    if (!user || user.user_type !== 'COACH' || !(await bcrypt.compare(data.password, user.password_hash))) {
      throw Object.assign(new Error('E-mail ou senha incorretos.'), { status: 401 })
    }
    res.json({ token: createToken(user), user: publicUser(user) })
  } catch (error) { next(error) }
})

app.use('/api', authenticate)

app.get('/api/me', async (req, res, next) => {
  try {
    const { rows } = await requireDatabase().query(
      'SELECT id,name,email,user_type,invite_code,timezone FROM users WHERE id=$1 AND user_type=\'COACH\'', [req.coachId],
    )
    if (!rows[0]) throw Object.assign(new Error('Treinador não encontrado.'), { status: 404 })
    res.json(rows[0])
  } catch (error) { next(error) }
})

app.get('/api/students', async (req, res, next) => {
  try {
    const { rows } = await requireDatabase().query(
      `SELECT u.id,u.name,u.email,
        count(w.id) FILTER (WHERE w.target_date >= CURRENT_DATE::text) AS upcoming_workouts,
        min(w.target_date) FILTER (WHERE w.target_date >= CURRENT_DATE::text) AS next_workout_date
       FROM users u LEFT JOIN workouts w ON w.athlete_id=u.id
       WHERE u.coach_id=$1 AND u.user_type='ATHLETE'
       GROUP BY u.id ORDER BY u.name`, [req.coachId],
    )
    res.json(rows)
  } catch (error) { next(error) }
})

async function assertStudent(coachId, athleteId, executor = requireDatabase()) {
  const { rowCount } = await executor.query(
    "SELECT 1 FROM users WHERE id=$1 AND coach_id=$2 AND user_type='ATHLETE'", [athleteId, coachId],
  )
  if (!rowCount) throw Object.assign(new Error('Aluno não encontrado ou não vinculado a você.'), { status: 404 })
}

app.get('/api/students/:athleteId/workouts', async (req, res, next) => {
  try {
    await assertStudent(req.coachId, req.params.athleteId)
    const start = req.query.start
    const { rows } = await requireDatabase().query(
      `SELECT w.*, COALESCE(json_agg(json_build_object(
         'id',s.id,'type',s.segment_type,'durationType',s.duration_type,'durationValue',s.duration_value,
         'intensity',s.intensity,'notes',s.notes,'position',s.position
       ) ORDER BY s.position) FILTER (WHERE s.id IS NOT NULL),'[]') AS segments,
       (SELECT json_build_object(
          'id',e.id,'executionDate',e.execution_date,'distanceKm',e.actual_distance_km,
          'durationSeconds',e.actual_duration_seconds,'pace',e.actual_pace,
          'avgHeartRate',e.actual_avg_hr,'pse',e.pse,'comments',e.comments
        ) FROM workout_executions e WHERE e.prescribed_workout_id=w.id
          ORDER BY e.execution_date DESC LIMIT 1) AS execution
       FROM workouts w LEFT JOIN workout_segments s ON s.workout_id=w.id
       WHERE w.athlete_id=$1 AND w.target_date::date BETWEEN $2::date AND ($2::date + 6)
       GROUP BY w.id ORDER BY w.target_date`, [req.params.athleteId, start],
    )
    res.json(rows)
  } catch (error) { next(error) }
})

app.get('/api/students/:athleteId/races', async (req, res, next) => {
  try {
    await assertStudent(req.coachId, req.params.athleteId)
    const { rows } = await requireDatabase().query(
      `SELECT id,name,date,modality,target_time,priority FROM race_events
       WHERE athlete_id=$1 AND date::date BETWEEN $2::date AND ($2::date + 6)
       ORDER BY date`, [req.params.athleteId, req.query.start],
    )
    res.json(rows)
  } catch (error) { next(error) }
})

app.get('/api/favorites', async (req, res, next) => {
  try {
    const { rows } = await requireDatabase().query(
      `SELECT f.*, COALESCE(json_agg(json_build_object(
        'id',s.id,'type',s.segment_type,'durationType',s.duration_type,'durationValue',s.duration_value,
        'intensity',s.intensity,'notes',s.notes,'position',s.position
      ) ORDER BY s.position) FILTER (WHERE s.id IS NOT NULL),'[]') AS segments
      FROM favorite_workouts f LEFT JOIN favorite_workout_segments s ON s.favorite_id=f.id
      WHERE f.coach_id=$1 GROUP BY f.id ORDER BY f.name`, [req.coachId],
    )
    res.json(rows)
  } catch (error) { next(error) }
})

function legacyDescription(data) {
  const segmentNames = { WARMUP: 'Aquecimento', RUN: 'Corrida', INTERVAL: 'Tiro', RECOVERY: 'Recuperação', COOLDOWN: 'Desaquecimento', REST: 'Pausa' }
  const segmentText = data.segments.map((segment, index) => {
    const unit = segment.durationType === 'TIME' ? 'min' : 'km'
    return `${index + 1}. ${segmentNames[segment.type] || segment.type} — ${segment.durationValue} ${unit} — ${segment.intensity}${segment.notes ? ` — ${segment.notes}` : ''}`
  }).join('\n')
  return [data.name, data.notes, segmentText].filter(Boolean).join('\n\n')
}

async function replaceSegments(client, workoutId, segments) {
  await client.query('DELETE FROM workout_segments WHERE workout_id=$1', [workoutId])
  for (const [position, segment] of segments.entries()) {
    await client.query(
      `INSERT INTO workout_segments (id,workout_id,position,segment_type,duration_type,duration_value,intensity,notes)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`,
      [segment.id || randomUUID(), workoutId, position, segment.type, segment.durationType, segment.durationValue, segment.intensity, segment.notes],
    )
  }
}

async function saveFavorite(client, coachId, data) {
  const favoriteId = randomUUID()
  await client.query(
    `INSERT INTO favorite_workouts
      (id,coach_id,name,route_type,effort,workout_type,target_distance_km,target_duration_minutes,target_pace,target_hr_zone,notes)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
    [favoriteId, coachId, data.name, data.routeType, data.effort, data.workoutType, data.targetDistanceKm,
      data.targetDurationMinutes, data.targetPace, data.targetHeartRateZone, data.notes],
  )
  for (const [position, segment] of data.segments.entries()) {
    await client.query(
      `INSERT INTO favorite_workout_segments
        (id,favorite_id,position,segment_type,duration_type,duration_value,intensity,notes)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`,
      [randomUUID(), favoriteId, position, segment.type, segment.durationType, segment.durationValue, segment.intensity, segment.notes],
    )
  }
  return favoriteId
}

app.post('/api/students/:athleteId/workouts', async (req, res, next) => {
  try {
    const data = workoutSchema.parse(req.body)
    const result = await transaction(async client => {
      await assertStudent(req.coachId, req.params.athleteId, client)
      const existing = await client.query('SELECT id,status FROM workouts WHERE athlete_id=$1 AND target_date=$2 ORDER BY updated_at DESC LIMIT 1', [req.params.athleteId, data.targetDate])
      if (existing.rows[0]?.status === 'COMPLETED') throw Object.assign(new Error('Um treino concluído não pode ser editado.'), { status: 409 })
      const workoutId = existing.rows[0]?.id || randomUUID()
      await client.query(
        `INSERT INTO workouts
          (id,athlete_id,target_date,workout_type,target_distance_km,target_duration_minutes,target_pace,target_hr_zone,description,status,name,route_type,effort,coach_id,updated_at)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,'PENDING',$10,$11,$12,$13,now())
         ON CONFLICT (id) DO UPDATE SET workout_type=EXCLUDED.workout_type,target_distance_km=EXCLUDED.target_distance_km,
          target_duration_minutes=EXCLUDED.target_duration_minutes,target_pace=EXCLUDED.target_pace,target_hr_zone=EXCLUDED.target_hr_zone,
          description=EXCLUDED.description,name=EXCLUDED.name,route_type=EXCLUDED.route_type,effort=EXCLUDED.effort,updated_at=now()`,
        [workoutId, req.params.athleteId, data.targetDate, data.workoutType, data.targetDistanceKm,
          data.targetDurationMinutes, data.targetPace, data.targetHeartRateZone, legacyDescription(data), data.name,
          data.routeType, data.effort, req.coachId],
      )
      await replaceSegments(client, workoutId, data.segments)
      const favoriteId = data.favorite ? await saveFavorite(client, req.coachId, data) : null
      return { id: workoutId, favoriteId }
    })
    res.status(201).json(result)
  } catch (error) { next(error) }
})

app.delete('/api/students/:athleteId/workouts/:workoutId', async (req, res, next) => {
  try {
    await assertStudent(req.coachId, req.params.athleteId)
    const result = await requireDatabase().query(
      `DELETE FROM workouts WHERE id=$1 AND athlete_id=$2 AND coach_id=$3 AND status='PENDING'`,
      [req.params.workoutId, req.params.athleteId, req.coachId],
    )
    if (!result.rowCount) throw Object.assign(new Error('Treino não encontrado ou já concluído.'), { status: 404 })
    res.status(204).end()
  } catch (error) { next(error) }
})

app.post('/api/students/:athleteId/workouts/from-favorite', async (req, res, next) => {
  try {
    const { favoriteId, targetDate } = req.body
    if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(targetDate || '')) {
      throw Object.assign(new Error('Data do treino inválida.'), { status: 400 })
    }
    const result = await transaction(async client => {
      await assertStudent(req.coachId, req.params.athleteId, client)
      const favorite = await client.query('SELECT * FROM favorite_workouts WHERE id=$1 AND coach_id=$2', [favoriteId, req.coachId])
      if (!favorite.rows[0]) throw Object.assign(new Error('Treino favorito não encontrado.'), { status: 404 })
      const segments = await client.query('SELECT * FROM favorite_workout_segments WHERE favorite_id=$1 ORDER BY position', [favoriteId])
      const data = favorite.rows[0]
      const existing = await client.query('SELECT id,status FROM workouts WHERE athlete_id=$1 AND target_date=$2 ORDER BY updated_at DESC LIMIT 1', [req.params.athleteId, targetDate])
      if (existing.rows[0]?.status === 'COMPLETED') throw Object.assign(new Error('Um treino concluído não pode ser substituído.'), { status: 409 })
      const workoutId = existing.rows[0]?.id || randomUUID()
      const segmentModels = segments.rows.map(s => ({ type: s.segment_type, durationType: s.duration_type, durationValue: Number(s.duration_value), intensity: s.intensity, notes: s.notes }))
      await client.query(
        `INSERT INTO workouts
          (id,athlete_id,target_date,workout_type,target_distance_km,target_duration_minutes,target_pace,target_hr_zone,description,status,name,route_type,effort,coach_id)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,'PENDING',$10,$11,$12,$13)
         ON CONFLICT (id) DO UPDATE SET workout_type=EXCLUDED.workout_type,target_distance_km=EXCLUDED.target_distance_km,
          target_duration_minutes=EXCLUDED.target_duration_minutes,target_pace=EXCLUDED.target_pace,target_hr_zone=EXCLUDED.target_hr_zone,
          description=EXCLUDED.description,name=EXCLUDED.name,route_type=EXCLUDED.route_type,effort=EXCLUDED.effort,coach_id=EXCLUDED.coach_id,updated_at=now()`,
        [workoutId, req.params.athleteId, targetDate, data.workout_type, data.target_distance_km,
          data.target_duration_minutes, data.target_pace, data.target_hr_zone,
          legacyDescription({ ...data, name: data.name, notes: data.notes, segments: segmentModels }), data.name, data.route_type, data.effort, req.coachId],
      )
      await replaceSegments(client, workoutId, segmentModels)
      return { id: workoutId }
    })
    res.status(201).json(result)
  } catch (error) { next(error) }
})

app.use((error, _req, res, _next) => {
  const isValidation = error?.name === 'ZodError'
  const status = error.status || (error.code === '23505' ? 409 : isValidation ? 400 : 500)
  const message = error.code === '23505' ? 'Já existe um cadastro ou treino com estes dados.' : isValidation ? 'Confira os campos informados.' : error.message
  if (status >= 500) console.error(error)
  res.status(status).json({ error: message, details: isValidation ? error.issues : undefined })
})

export default app

if (!process.env.VERCEL) {
  app.listen(port, '127.0.0.1', () => console.log(`Runts Coach API em http://127.0.0.1:${port}`))
}
