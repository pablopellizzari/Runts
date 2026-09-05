package com.example.runts.data.remote.database

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.runts.BuildConfig
import com.example.runts.data.repository.resultOf
import com.example.runts.domain.model.*
import com.google.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Android-compatible HTTPS transport. Direct DB credentials are for this study build only. */
@Singleton
class NeonPostgresManager @Inject constructor() {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).callTimeout(45, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private fun encoded(value: String) = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    private data class Query(val query: String, val params: List<Any?> = emptyList())
    private fun query(sql: String, vararg args: Any?) = Query(sql, args.toList())

    private suspend fun request(payload: Any): JsonObject = withContext(Dispatchers.IO) {
        check(BuildConfig.NEON_HOST.isNotBlank() && BuildConfig.NEON_PASSWORD.isNotBlank()) { "Conexão Neon não configurada em local.properties." }
        val host = BuildConfig.NEON_HOST.trim()
        val connection = "postgresql://${encoded(BuildConfig.NEON_USER)}:${encoded(BuildConfig.NEON_PASSWORD)}@$host/${encoded(BuildConfig.NEON_DATABASE)}?sslmode=require"
        val request = Request.Builder().url("https://$host/sql")
            .header("Neon-Connection-String", connection)
            .header("Neon-Raw-Text-Output", "true").header("Neon-Array-Mode", "false")
            .post(gson.toJson(payload).toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val code = runCatching { JsonParser.parseString(body).asJsonObject.get("code")?.asString }.getOrNull()
                val message = when (code) {
                    "23505" -> "Já existe um cadastro ou registro com estes dados."
                    "23503" -> "O usuário ou treino associado não existe mais. Atualize os dados."
                    "23514" -> "Os dados não atendem às regras do banco."
                    "28P01" -> "O banco recusou as credenciais configuradas."
                    else -> "Não foi possível concluir a operação no Neon (HTTP ${response.code}, código ${code ?: "indisponível"})."
                }
                throw IllegalStateException(message)
            }
            check(body.isNotBlank()) { "Neon retornou uma resposta vazia." }
            JsonParser.parseString(body).asJsonObject
        }
    }
    private fun rows(response: JsonObject): List<JsonObject> {
        check(response.has("rows") && response.get("rows").isJsonArray) { "Resposta inválida do Neon." }
        val fields = response.getAsJsonArray("fields")?.map { it.asJsonObject.get("name").asString }.orEmpty()
        return response.getAsJsonArray("rows").map { row ->
            if (row.isJsonObject) row.asJsonObject
            else {
                check(row.isJsonArray && row.asJsonArray.size() == fields.size) { "Colunas inválidas na resposta Neon." }
                JsonObject().apply { fields.forEachIndexed { i, name -> add(name, row.asJsonArray[i]) } }
            }
        }
    }
    private suspend fun sql(text: String, vararg params: Any?) = rows(request(query(text, *params)))
    private suspend fun transaction(queries: List<Query>) {
        if (queries.isEmpty()) return
        val response = request(mapOf("queries" to queries))
        check(response.getAsJsonArray("results")?.size() == queries.size) { "Transação sem confirmação do Neon." }
        response.getAsJsonArray("results").forEach { rows(it.asJsonObject) }
    }
    private fun upsert(table: String, values: Map<String, Any?>, immutable: Set<String> = setOf("id")): Query {
        // Table/column names come only from constants below; all user values are parameters.
        val columns = values.keys.toList()
        val slots = columns.indices.joinToString(",") { "$" + (it + 1) }
        val updates = columns.filterNot { it in immutable }.joinToString(",") { "$it=EXCLUDED.$it" }
        return Query("INSERT INTO $table (${columns.joinToString(",")}) VALUES ($slots) ON CONFLICT (id) DO UPDATE SET $updates", values.values.toList())
    }
    suspend fun checkConnection(): Result<Unit> = resultOf { check(sql("SELECT 1 AS ok").single().int("ok") == 1) }
    suspend fun initDatabaseTables(): Result<Unit> = resultOf { transaction(SCHEMA.map { Query(it) }) }

    suspend fun registerUser(name: String, email: String, rawPassword: String, type: UserType): Result<User> = withContext(Dispatchers.IO) { resultOf {
        require(name.trim().length in 2..255) { "Informe seu nome (2 a 255 caracteres)." }
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()) && email.trim().length <= 255) { "Informe um e-mail válido." }
        require(rawPassword.length >= 8 && rawPassword.toByteArray().size <= 72) { "A senha deve ter de 8 a 72 bytes." }
        val normalized = email.trim().lowercase(java.util.Locale.ROOT)
        require(sql("SELECT id FROM users WHERE lower(email)=lower($1)", normalized).isEmpty()) { "Este e-mail já está cadastrado." }
        val u = User(UUID.randomUUID().toString(), name.trim(), normalized, type,
            inviteCode = if (type == UserType.COACH) "RNTS-${UUID.randomUUID().toString().take(8).uppercase()}" else null)
        sql("INSERT INTO users (id,name,email,password_hash,user_type,invite_code) VALUES ($1,$2,$3,$4,$5,$6)",
            u.id,u.name,u.email,BCrypt.withDefaults().hashToString(12,rawPassword.toCharArray()),u.userType.name,u.inviteCode)
        u
    } }
    suspend fun loginUser(email: String, rawPassword: String): Result<User> = withContext(Dispatchers.IO) { resultOf {
        require(email.isNotBlank() && rawPassword.isNotBlank()) { "Informe e-mail e senha." }
        val row = sql("SELECT * FROM users WHERE lower(email)=lower($1) LIMIT 1",email.trim()).firstOrNull()
            ?: error("E-mail ou senha incorretos.")
        check(BCrypt.verifyer().verify(rawPassword.toCharArray(),row.string("password_hash")).verified) { "E-mail ou senha incorretos." }
        user(row)
    } }
    suspend fun sendTemporaryPasswordEmail(email: String): Result<String> = Result.failure(UnsupportedOperationException("Envio de recuperação não configurado. Nenhum e-mail foi enviado."))
    suspend fun getUser(id: String): User? = sql("SELECT * FROM users WHERE id=$1",id).firstOrNull()?.let(::user)
    suspend fun getAthletesByCoachId(coachId: String) = sql("SELECT * FROM users WHERE coach_id=$1 AND user_type='ATHLETE' ORDER BY name",coachId).map(::user)
    suspend fun linkAthleteToCoach(athleteId: String, inviteCode: String): Result<User> = resultOf {
        val row = sql("""UPDATE users AS athlete SET coach_id=coach.id FROM users AS coach
            WHERE athlete.id=$1 AND athlete.user_type='ATHLETE' AND coach.user_type='COACH'
            AND upper(coach.invite_code)=upper($2) RETURNING athlete.*""",athleteId,inviteCode.trim()).firstOrNull()
            ?: error("Código de convite inválido ou conta não é de atleta.")
        user(row)
    }
    suspend fun getWorkoutsByAthlete(id: String) = sql("SELECT * FROM workouts WHERE athlete_id=$1 ORDER BY target_date",id).map(::workout)
    private fun workoutQuery(w: Workout) = upsert("workouts", linkedMapOf("id" to w.id,"athlete_id" to w.athleteId,"target_date" to w.targetDate,
        "workout_type" to w.workoutType.name,"target_distance_km" to w.targetDistanceKm,"target_duration_minutes" to w.targetDurationMinutes,
        "target_pace" to w.targetPace,"target_hr_zone" to w.targetHeartRateZone,"description" to w.description,"status" to w.status.name),
        setOf("id","athlete_id","status"))
    suspend fun saveWorkout(w: Workout): Result<Unit> = saveWorkouts(listOf(w))
    suspend fun saveWorkouts(workouts: List<Workout>): Result<Unit> = resultOf {
        workouts.forEach(TrainingRules::prescription)
        transaction(workouts.map(::workoutQuery))
    }
    suspend fun getRaceEventsByAthlete(id: String) = sql("SELECT * FROM race_events WHERE athlete_id=$1 ORDER BY date",id).map(::race)
    suspend fun saveRaceEvent(r: RaceEvent): Result<Unit> = resultOf {
        TrainingRules.race(r)
        transaction(listOf(upsert("race_events",linkedMapOf("id" to r.id,"athlete_id" to r.athleteId,"name" to r.name,
            "date" to r.date,"modality" to r.modality,"target_time" to r.targetTime,"priority" to r.priority.name),setOf("id","athlete_id"))))
    }
    suspend fun deleteRaceEvent(raceId: String, athleteId: String): Result<Unit> = resultOf {
        sql("DELETE FROM race_events WHERE id=$1 AND athlete_id=$2", raceId, athleteId)
    }
    suspend fun saveWorkoutExecution(e: WorkoutExecution): Result<Unit> = resultOf {
        val w = e.prescribedWorkoutId?.let { sql("SELECT * FROM workouts WHERE id=$1",it).firstOrNull()?.let(::workout) }
        TrainingRules.execution(e,w)
        // Deterministic linked IDs and a transaction make retries idempotent.
        val insert = upsert("workout_executions",linkedMapOf("id" to e.id,"prescribed_workout_id" to e.prescribedWorkoutId,
            "athlete_id" to e.athleteId,"execution_date" to e.executionDate,"actual_distance_km" to e.actualDistanceKm,
            "actual_duration_seconds" to e.actualDurationSeconds,"actual_pace" to e.actualPace,
            "actual_avg_hr" to e.actualAvgHeartRate,"pse" to e.pse,"encrypted_gps_data_json" to e.encryptedGpsDataJson,"comments" to e.comments),
            setOf("id","athlete_id","prescribed_workout_id"))
        transaction(listOfNotNull(insert,e.prescribedWorkoutId?.let {
            query("UPDATE workouts SET status='COMPLETED' WHERE id=$1 AND athlete_id=$2",it,e.athleteId)
        }))
    }
    suspend fun getExecutionsByAthlete(id: String) = sql("SELECT * FROM workout_executions WHERE athlete_id=$1 ORDER BY execution_date DESC",id).map(::execution)
    suspend fun getSheetsByCoach(coachId: String): List<TrainingSheet> {
        val children = sql("SELECT sw.* FROM sheet_workouts sw JOIN training_sheets s ON s.id=sw.sheet_id WHERE s.coach_id=$1 ORDER BY day_of_week",coachId)
            .map(::sheetWorkout).groupBy { it.sheetId }
        return sql("SELECT * FROM training_sheets WHERE coach_id=$1 ORDER BY created_at DESC",coachId).map {
            TrainingSheet(it.string("id"),it.string("coach_id"),it.string("title"),it.nullable("description"),it.string("created_at"),children[it.string("id")].orEmpty())
        }
    }
    suspend fun saveTrainingSheet(s: TrainingSheet): Result<Unit> = resultOf {
        TrainingRules.sheet(s)
        transaction(listOf(upsert("training_sheets",linkedMapOf("id" to s.id,"coach_id" to s.coachId,"title" to s.title,
            "description" to s.description,"created_at" to s.createdAt),setOf("id","coach_id","created_at")),
            query("DELETE FROM sheet_workouts WHERE sheet_id=$1",s.id)) + s.workouts.map {
            upsert("sheet_workouts",linkedMapOf("id" to it.id,"sheet_id" to it.sheetId,"day_of_week" to it.dayOfWeek,
                "workout_type" to it.workoutType.name,"target_distance_km" to it.targetDistanceKm,"target_duration_minutes" to it.targetDurationMinutes,
                "target_pace" to it.targetPace,"target_hr_zone" to it.targetHeartRateZone,"description" to it.description))
        })
    }

    private fun user(r: JsonObject) = User(r.string("id"),r.string("name"),r.string("email"),UserType.valueOf(r.string("user_type")),r.nullable("coach_id"),r.nullable("invite_code"))
    private fun workout(r: JsonObject) = Workout(r.string("id"),r.string("athlete_id"),r.string("target_date"),WorkoutType.valueOf(r.string("workout_type")),r.double("target_distance_km"),r.int("target_duration_minutes"),r.string("target_pace"),r.string("target_hr_zone"),r.string("description"),WorkoutStatus.valueOf(r.string("status")))
    private fun race(r: JsonObject) = RaceEvent(r.string("id"),r.string("athlete_id"),r.string("name"),r.string("date"),r.string("modality"),r.nullable("target_time"),RacePriority.valueOf(r.string("priority")))
    private fun execution(r: JsonObject) = WorkoutExecution(r.string("id"),r.nullable("prescribed_workout_id"),r.string("athlete_id"),r.string("execution_date"),r.double("actual_distance_km"),r.int("actual_duration_seconds"),r.string("actual_pace"),r.nullable("actual_avg_hr")?.toIntOrNull(),r.int("pse"),r.nullable("encrypted_gps_data_json"),r.nullable("comments"))
    private fun sheetWorkout(r: JsonObject) = SheetWorkout(r.string("id"),r.string("sheet_id"),r.int("day_of_week"),WorkoutType.valueOf(r.string("workout_type")),r.double("target_distance_km"),r.int("target_duration_minutes"),r.string("target_pace"),r.string("target_hr_zone"),r.string("description"))
    private fun JsonObject.string(name: String) = nullable(name) ?: error("Resposta Neon sem $name")
    private fun JsonObject.nullable(name: String) = get(name)?.takeUnless { it.isJsonNull }?.asString
    private fun JsonObject.int(name: String) = string(name).toInt()
    private fun JsonObject.double(name: String) = string(name).toDouble()

    private companion object { val SCHEMA = listOf(
        "CREATE TABLE IF NOT EXISTS users (id VARCHAR(64) PRIMARY KEY,name VARCHAR(255) NOT NULL,email VARCHAR(255) UNIQUE NOT NULL,password_hash VARCHAR(255) NOT NULL,user_type VARCHAR(32) NOT NULL,coach_id VARCHAR(64),invite_code VARCHAR(32) UNIQUE)",
        "CREATE TABLE IF NOT EXISTS workouts (id VARCHAR(64) PRIMARY KEY,athlete_id VARCHAR(64) NOT NULL,target_date VARCHAR(32) NOT NULL,workout_type VARCHAR(32) NOT NULL,target_distance_km DOUBLE PRECISION NOT NULL,target_duration_minutes INT NOT NULL,target_pace VARCHAR(32) NOT NULL,target_hr_zone VARCHAR(32) NOT NULL,description TEXT NOT NULL,status VARCHAR(32) NOT NULL,updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),name VARCHAR(160),route_type VARCHAR(32),effort INT,coach_id VARCHAR(64))",
        "CREATE TABLE IF NOT EXISTS race_events (id VARCHAR(64) PRIMARY KEY,athlete_id VARCHAR(64) NOT NULL,name VARCHAR(255) NOT NULL,date VARCHAR(32) NOT NULL,modality VARCHAR(32) NOT NULL,target_time VARCHAR(32),priority VARCHAR(32) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS workout_executions (id VARCHAR(64) PRIMARY KEY,prescribed_workout_id VARCHAR(64),athlete_id VARCHAR(64) NOT NULL,execution_date VARCHAR(32) NOT NULL,actual_distance_km DOUBLE PRECISION NOT NULL,actual_duration_seconds INT NOT NULL,actual_pace VARCHAR(32) NOT NULL,actual_avg_hr INT,pse INT NOT NULL,encrypted_gps_data_json TEXT,comments TEXT)",
        "CREATE TABLE IF NOT EXISTS training_sheets (id VARCHAR(64) PRIMARY KEY,coach_id VARCHAR(64) NOT NULL,title VARCHAR(255) NOT NULL,description TEXT,created_at VARCHAR(32) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS sheet_workouts (id VARCHAR(64) PRIMARY KEY,sheet_id VARCHAR(64) NOT NULL,day_of_week INT NOT NULL,workout_type VARCHAR(32) NOT NULL,target_distance_km DOUBLE PRECISION NOT NULL,target_duration_minutes INT NOT NULL,target_pace VARCHAR(32) NOT NULL,target_hr_zone VARCHAR(32) NOT NULL,description TEXT NOT NULL)"
    ) }
}
