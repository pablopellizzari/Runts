import { z } from 'zod'

export const credentialsSchema = z.object({
  email: z.string().trim().email().max(255),
  password: z.string().min(8).max(72),
})

export const registrationSchema = credentialsSchema.extend({
  name: z.string().trim().min(2).max(255),
  timezone: z.string().trim().min(1).max(80).default('America/Sao_Paulo'),
})

export const segmentSchema = z.object({
  id: z.string().uuid().optional(),
  type: z.enum(['WARMUP', 'RUN', 'INTERVAL', 'RECOVERY', 'COOLDOWN', 'REST']),
  durationType: z.enum(['TIME', 'DISTANCE']),
  durationValue: z.number().positive().max(10000),
  intensity: z.string().trim().min(1).max(80),
  notes: z.string().trim().max(1000).default(''),
})

export const workoutSchema = z.object({
  name: z.string().trim().min(2).max(160),
  routeType: z.enum(['ROAD', 'TRAIL', 'TRACK', 'TREADMILL', 'MIXED']),
  effort: z.number().int().min(1).max(10),
  workoutType: z.enum(['RODAGEM', 'TIROS', 'TEMPO_RUN', 'LONGAO', 'FORTALECIMENTO', 'PROVA']),
  targetDate: z.string().date(),
  targetDistanceKm: z.number().min(0).max(500),
  targetDurationMinutes: z.number().int().min(0).max(1440),
  targetPace: z.string().trim().min(1).max(32),
  targetHeartRateZone: z.string().trim().min(1).max(32),
  notes: z.string().trim().max(3000).default(''),
  favorite: z.boolean().default(false),
  segments: z.array(segmentSchema).min(1).max(50),
})
