package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.TrainingRules
import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.ui.format.displayName
import com.example.runts.ui.format.formatShortDate
import com.example.runts.ui.theme.BadgeSuccess
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.AthleteDashboardViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val planWeekLabel = DateTimeFormatter.ofPattern("dd-MM-yy", Locale("pt", "BR"))
private val planDayNames = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")

@Composable
fun AthletePlanScreen(
    viewModel: AthleteDashboardViewModel,
    onWorkoutClick: (String) -> Unit
) {
    val workouts by viewModel.workouts.collectAsState()
    val today = remember { LocalDate.now() }
    var weekStart by remember {
        mutableStateOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))
    }
    val days = (0L..6L).map(weekStart::plusDays)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { weekStart = weekStart.minusWeeks(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior", tint = TextWhite)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLANILHAS", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(
                    "${weekStart.format(planWeekLabel)} — ${weekStart.plusDays(6).format(planWeekLabel)}",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { weekStart = weekStart.plusWeeks(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Próxima semana", tint = TextWhite)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        days.forEachIndexed { index, date ->
            val workout = workouts.firstOrNull { runCatching { TrainingRules.date(it.targetDate) }.getOrNull() == date }
            PlanDayCard(
                dayName = planDayNames[index],
                date = date,
                workout = workout,
                isToday = date == today,
                onClick = { workout?.let { onWorkoutClick(it.id) } }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PlanDayCard(
    dayName: String,
    date: LocalDate,
    workout: Workout?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val completed = workout?.status == WorkoutStatus.COMPLETED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (workout != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isToday) RuntsDarkSurface else RuntsDarkCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.36f)) {
                Text(dayName.uppercase(), color = if (isToday) RuntsRedPrimary else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(formatShortDate(date.toString()), color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (isToday) Text("HOJE", color = RuntsRedPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            if (workout == null) {
                Column(modifier = Modifier.weight(0.64f)) {
                    Text("Descanso", color = TextGray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Nenhum treino planejado", color = TextMuted, fontSize = 11.sp)
                }
            } else {
                Icon(
                    if (completed) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = if (completed) BadgeSuccess else RuntsRedPrimary
                )
                Column(modifier = Modifier.weight(0.64f).padding(start = 12.dp)) {
                    Text(
                        if (completed) "CONCLUÍDO" else workout.workoutType.displayName().uppercase(),
                        color = if (completed) BadgeSuccess else RuntsRedPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        workout.description.lineSequence().firstOrNull().orEmpty().ifBlank { workout.workoutType.displayName() },
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Text(
                        "${workout.targetDistanceKm} km · ${workout.targetDurationMinutes} min · ${workout.targetPace}/km",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
