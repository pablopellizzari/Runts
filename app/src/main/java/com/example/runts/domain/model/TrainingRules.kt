package com.example.runts.domain.model

import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

/** Rules shared by forms, repositories and tests. Persist dates as ISO, never day numbers. */
object TrainingRules {
    fun date(value: String): LocalDate = try {
        LocalDate.parse(value.take(10))
    } catch (_: Exception) {
        try { LocalDate.parse(value.take(10), DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)) }
        catch (_: Exception) { throw IllegalArgumentException("Informe uma data válida (AAAA-MM-DD).") }
    }
    fun weekStart(date: LocalDate) = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun sameWeek(first: String, second: String) = weekStart(date(first)) == weekStart(date(second))
    fun pace(distance: Double, seconds: Int): String {
        if (distance <= 0 || seconds <= 0) return "—"
        val pace = (seconds / distance).roundToInt()
        return String.format(Locale.ROOT, "%d:%02d", pace / 60, pace % 60)
    }
    fun duration(value: String): Int {
        val parts = value.trim().split(":")
        require(parts.size in 2..3 && parts.all { it.matches(Regex("[0-9]{1,3}")) }) { "Use MM:SS ou HH:MM:SS." }
        val n = parts.map(String::toInt)
        require(n.last() < 60 && (n.size != 3 || n[1] < 60)) { "Minutos e segundos devem ser menores que 60." }
        return if (n.size == 3) n[0] * 3600 + n[1] * 60 + n[2] else n[0] * 60 + n[1]
    }
    fun durationText(seconds: Int) = String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
    fun prescription(w: Workout) {
        require(w.athleteId.isNotBlank()) { "Atleta não informado." }
        date(w.targetDate)
        targets(w.targetDistanceKm, w.targetDurationMinutes, w.targetPace, w.targetHeartRateZone)
    }
    fun targets(distance: Double, minutes: Int, pace: String, zone: String) {
        require(distance.isFinite() && distance >= 0 && distance <= 1000 && minutes in 0..10080 && (distance > 0 || minutes > 0)) { "Informe distância ou duração válida." }
        require(pace.isBlank() || (Regex("[0-9]{1,2}:[0-5][0-9]").matches(pace) && duration(pace) > 0)) { "Pace deve ser MM:SS." }
        require(zone.isBlank() || zone in listOf("Z1", "Z2", "Z3", "Z4", "Z5")) { "Zona deve ser Z1 a Z5." }
    }
    fun execution(e: WorkoutExecution, workout: Workout? = null) {
        require(e.athleteId.isNotBlank()) { "Atleta não informado." }
        require(!date(e.executionDate).isAfter(LocalDate.now())) { "Não é possível registrar uma execução futura." }
        require(e.actualDistanceKm.isFinite() && e.actualDistanceKm in 0.0..1000.0) { "Distância inválida." }
        require(e.actualDurationSeconds in 1..604800) { "Informe uma duração maior que zero." }
        require(e.pse in 1..10) { "PSE deve estar entre 1 e 10." }
        require(e.actualAvgHeartRate == null || e.actualAvgHeartRate in 30..250) { "Frequência cardíaca deve estar entre 30 e 250 bpm." }
        if (e.prescribedWorkoutId != null) {
            require(workout != null && workout.athleteId == e.athleteId) { "Prescrição não pertence ao atleta." }
            require(sameWeek(workout.targetDate, e.executionDate)) { "Escolha uma prescrição da mesma semana da execução." }
        }
    }
    fun race(r: RaceEvent) {
        require(r.athleteId.isNotBlank() && r.name.trim().isNotEmpty() && r.modality.trim().isNotEmpty()) { "Preencha nome e modalidade da prova." }
        date(r.date)
        if (r.date.contains('T')) java.time.LocalDateTime.parse(r.date)
        r.targetTime?.takeIf(String::isNotBlank)?.let { require(duration(it) > 0) { "Meta de tempo inválida." } }
    }
    fun sheet(s: TrainingSheet) {
        require(s.coachId.isNotBlank() && s.title.isNotBlank() && s.workouts.isNotEmpty()) { "Informe título e pelo menos um treino." }
        require(s.workouts.map { it.id }.distinct().size == s.workouts.size) { "Treinos duplicados no modelo." }
        s.workouts.forEach {
            require(it.sheetId == s.id && it.dayOfWeek in 1..7) { "Dia ou modelo inválido." }
            targets(it.targetDistanceKm, it.targetDurationMinutes, it.targetPace, it.targetHeartRateZone)
        }
    }
}
