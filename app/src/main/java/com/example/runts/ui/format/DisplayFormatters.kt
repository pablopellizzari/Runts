package com.example.runts.ui.format

import com.example.runts.domain.model.TrainingRules
import com.example.runts.domain.model.WorkoutType
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy", Locale("pt", "BR"))

fun formatShortDate(value: String): String = runCatching {
    TrainingRules.date(value).format(shortDateFormatter)
}.getOrDefault(value)

fun WorkoutType.displayName(): String = when (this) {
    WorkoutType.RODAGEM -> "Rodagem"
    WorkoutType.TIROS -> "Tiros"
    WorkoutType.TEMPO_RUN -> "Treino de ritmo"
    WorkoutType.LONGAO -> "Longão"
    WorkoutType.FORTALECIMENTO -> "Fortalecimento"
    WorkoutType.PROVA -> "Prova"
}

fun localizeSegmentNames(value: String): String {
    val translations = linkedMapOf(
        "COOLDOWN" to "Desaquecimento",
        "WARMUP" to "Aquecimento",
        "INTERVAL" to "Tiro",
        "RECOVERY" to "Recuperação",
        "RUN" to "Corrida",
        "REST" to "Pausa"
    )
    return translations.entries.fold(value) { text, (source, target) ->
        text.replace(Regex("\\b$source\\b", RegexOption.IGNORE_CASE), target)
    }
}

data class WorkoutSegmentPresentation(
    val order: String,
    val type: String,
    val details: String
)

data class WorkoutDescriptionPresentation(
    val title: String,
    val notes: String?,
    val segments: List<WorkoutSegmentPresentation>
)

/** Converte a descrição legada compartilhada com o site em conteúdo próprio para a interface. */
fun workoutDescriptionPresentation(value: String, fallbackTitle: String): WorkoutDescriptionPresentation {
    val lines = localizeSegmentNames(value).lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
    val segmentPattern = Regex("^(\\d+)[.)]\\s*(.+)$")
    val segmentLines = lines.mapNotNull { line ->
        val match = segmentPattern.matchEntire(line) ?: return@mapNotNull null
        val parts = match.groupValues[2].split(Regex("\\s+[—–-]\\s+")).map(String::trim).filter(String::isNotEmpty)
        WorkoutSegmentPresentation(
            order = match.groupValues[1],
            type = parts.firstOrNull() ?: "Segmento",
            details = parts.drop(1).joinToString(" · ")
        )
    }
    val descriptiveLines = lines.filterNot { segmentPattern.matches(it) }
    return WorkoutDescriptionPresentation(
        title = descriptiveLines.firstOrNull() ?: fallbackTitle,
        notes = descriptiveLines.drop(1).joinToString(" ").takeIf(String::isNotBlank),
        segments = segmentLines
    )
}
