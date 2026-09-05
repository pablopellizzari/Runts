package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.User
import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.ui.theme.BadgeRodagem
import com.example.runts.ui.theme.BadgeSuccess
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.AthleteDashboardViewModel
import com.example.runts.ui.format.displayName
import com.example.runts.ui.format.formatShortDate
import com.example.runts.ui.format.workoutDescriptionPresentation
import java.time.LocalDate

data class DayCalendarModel(
    val dayName: String,
    val dayNumber: String,
    val fullDateStr: String
)

/**
 * Tela "athlete-home" com dados reais do usuário logado.
 */
@Composable
fun AthleteDashboardScreen(
    viewModel: AthleteDashboardViewModel,
    currentUser: User? = null,
    onWorkoutClick: (String) -> Unit
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today.toString()) }

    val workouts by viewModel.workouts.collectAsState()
    val loadedUser by viewModel.currentUser.collectAsState()
    val races by viewModel.races.collectAsState()

    val userName = loadedUser?.name ?: currentUser?.name ?: "Atleta"
    val userInitials = if (userName.isNotBlank()) userName.take(2).uppercase() else "TS"

    val weekDays = remember {
        val currentMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val dayNames = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        (0..6).map { i ->
            val date = currentMonday.plusDays(i.toLong())
            DayCalendarModel(
                dayName = dayNames[i],
                dayNumber = date.dayOfMonth.toString(),
                fullDateStr = date.toString()
            )
        }
    }

    val selectedDayNumber = weekDays.find { it.fullDateStr == selectedDate }?.dayNumber ?: today.dayOfMonth.toString()
    val workoutForSelectedDay = workouts.find { it.targetDate == selectedDate }
    val raceForSelectedDay = races.find { it.date == selectedDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header com Nome Real do Usuário Logado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RuntsRedPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(userInitials, color = TextWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("E aí, campeão!", color = TextGray, fontSize = 12.sp)
                Text(userName, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RuntsDarkCard)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = TextWhite)
            }
        }

        Text("${today.month.name} ${today.year}", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        // Fita Semanal
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDays) { day ->
                val isSelected = selectedDate == day.fullDateStr
                val hasWorkoutForDay = workouts.any { it.targetDate == day.fullDateStr }
                val hasRaceForDay = races.any { it.date == day.fullDateStr }
                val isCompleted = workouts.find { it.targetDate == day.fullDateStr }?.status == WorkoutStatus.COMPLETED

                val bg = if (isSelected) RuntsRedPrimary else RuntsDarkCard
                val textColor = if (isSelected) TextWhite else TextGray

                Column(
                    modifier = Modifier
                        .size(width = 46.dp, height = 68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { selectedDate = day.fullDateStr },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(day.dayName, color = textColor, fontSize = 11.sp)
                    Text(day.dayNumber, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    if (hasWorkoutForDay || hasRaceForDay) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) BadgeSuccess else if (isSelected) TextWhite else RuntsRedPrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        raceForSelectedDay?.let { race ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = com.example.runts.ui.theme.BadgeWarning.copy(alpha = 0.18f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = com.example.runts.ui.theme.BadgeWarning)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("PROVA", color = com.example.runts.ui.theme.BadgeWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(race.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(race.modality, color = TextGray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "TREINO DO DIA $selectedDayNumber",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text("Planejado pelo Coach Marcelo", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (workoutForSelectedDay != null) {
            WorkoutCard(workout = workoutForSelectedDay, onClick = { onWorkoutClick(workoutForSelectedDay.id) })
        } else {
            RestDayCard(dayNumber = selectedDayNumber)
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout, onClick: () -> Unit) {
    val isCompleted = workout.status == WorkoutStatus.COMPLETED
    val presentation = workoutDescriptionPresentation(workout.description, workout.workoutType.displayName())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) RuntsDarkCard else RuntsDarkSurface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCompleted) BadgeSuccess else BadgeRodagem)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCompleted) "CONCLUÍDO ✔" else workout.workoutType.displayName().uppercase(),
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(formatShortDate(workout.targetDate), color = TextMuted, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = presentation.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                maxLines = 2
            )

            presentation.notes?.let { notes ->
                Text(
                    text = notes,
                    color = TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (presentation.segments.isNotEmpty()) {
                Text(
                    "ETAPAS DO TREINO",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presentation.segments.forEach { segment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RuntsDarkCard)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) BadgeSuccess else RuntsRedPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(segment.order, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(segment.type, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (segment.details.isNotBlank()) {
                                    Text(segment.details, color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (isCompleted) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BadgeSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Treino Realizado com Sucesso!", color = BadgeSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DISTÂNCIA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${workout.targetDistanceKm} KM", color = if (isCompleted) BadgeSuccess else RuntsRedPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("PACE ALVO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(workout.targetPace, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("min/km", color = TextMuted, fontSize = 11.sp)
                }

                Column {
                    Text("ZONA FC", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(workout.targetHeartRateZone, color = RuntsRedPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun RestDayCard(dayNumber: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("😴", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Dia de Descanso / Off", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Nenhum treino prescrito para o dia $dayNumber.", color = TextGray, fontSize = 13.sp)
        }
    }
}
