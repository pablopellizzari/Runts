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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.WorkoutLogViewModel
import com.example.runts.ui.viewmodel.WorkoutLogUiState
import com.example.runts.domain.model.TrainingRules
import com.example.runts.ui.format.displayName

private fun durationMask(input: String): String {
    val digits = input.filter(Char::isDigit).takeLast(6).padStart(6, '0')
    return "${digits.take(2)}:${digits.substring(2, 4)}:${digits.takeLast(2)}"
}

private fun paceMask(input: String): String {
    val digits = input.filter(Char::isDigit).takeLast(4)
    if (digits.isEmpty()) return ""
    return when {
        digits.length <= 2 -> digits
        else -> "${digits.dropLast(2)}:${digits.takeLast(2)}"
    }
}

/**
 * Tela "training-log" do Figma com salvamento real no Neon DB.
 */
@Composable
fun WorkoutLogScreen(
    viewModel: WorkoutLogViewModel,
    athleteId: String,
    prescribedWorkoutId: String,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val prescribedWorkout by viewModel.prescribedWorkout.collectAsState()
    val suggestedStravaActivity by viewModel.suggestedStravaActivity.collectAsState()
    LaunchedEffect(state) { if (state is WorkoutLogUiState.Saved) onSaveSuccess() }
    LaunchedEffect(prescribedWorkoutId) { viewModel.loadPrescription(prescribedWorkoutId) }
    var isLinked by remember { mutableStateOf(true) }
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var pace by remember { mutableStateOf("") }
    var hr by remember { mutableStateOf("") }
    var selectedPse by remember { mutableIntStateOf(5) }
    var comments by remember { mutableStateOf("") }
    var defaultsLoaded by remember { mutableStateOf(false) }
    var appliedStravaActivityId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(prescribedWorkout?.id, suggestedStravaActivity?.externalId) {
        val activity = suggestedStravaActivity
        if (activity != null && appliedStravaActivityId != activity.externalId) {
            distance = (activity.distanceMeters / 1000.0).toString()
            duration = TrainingRules.durationText(activity.movingTimeSeconds)
            pace = activity.pace.orEmpty()
            hr = activity.averageHeartRate?.toString().orEmpty()
            appliedStravaActivityId = activity.externalId
            defaultsLoaded = true
        } else prescribedWorkout?.takeIf { !defaultsLoaded }?.let { workout ->
            distance = workout.targetDistanceKm.toString()
            duration = TrainingRules.durationText(workout.targetDurationMinutes * 60)
            pace = workout.targetPace
            defaultsLoaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }

        Text("REGISTRAR TREINO", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Text("Grave sua atividade física executada hoje", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Card Vincular à Prescrição
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔗 Vincular à prescrição", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isLinked,
                        onCheckedChange = { isLinked = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = RuntsRedPrimary)
                    )
                }

                if (isLinked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        prescribedWorkout?.let { "• ${it.workoutType.displayName()} - ${it.targetDistanceKm} km (previsto)" }
                            ?: "Carregando prescrição...",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        suggestedStravaActivity?.let { activity ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("ATIVIDADE ENCONTRADA NO STRAVA", color = RuntsRedPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(activity.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Text("Os dados abaixo foram preenchidos automaticamente. Revise e salve para concluir o treino.", color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Linha Distância e Duração
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Distância (KM)", color = TextGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RuntsDarkCard,
                        unfocusedContainerColor = RuntsDarkCard,
                        focusedBorderColor = RuntsRedPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Duração (HH:MM:SS)", color = TextGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = durationMask(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RuntsDarkCard,
                        unfocusedContainerColor = RuntsDarkCard,
                        focusedBorderColor = RuntsRedPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Linha Pace Médio e FC Média
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ritmo Médio (Pace)", color = TextGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = pace,
                    onValueChange = { pace = paceMask(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RuntsDarkCard,
                        unfocusedContainerColor = RuntsDarkCard,
                        focusedBorderColor = RuntsRedPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("FC Média (BPM)", color = TextGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = hr,
                    onValueChange = { hr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RuntsDarkCard,
                        unfocusedContainerColor = RuntsDarkCard,
                        focusedBorderColor = RuntsRedPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card PERCEPÇÃO DE ESFORÇO (PSE 1-10)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PERCEPÇÃO DE ESFORÇO (PSE)", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("1 = Muito Leve | 10 = Esforço Máximo", color = TextMuted, fontSize = 10.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..10).forEach { num ->
                        val isSelected = selectedPse == num
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) RuntsRedPrimary else RuntsDarkCard)
                                .clickable { selectedPse = num },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = num.toString(),
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Anotações do Atleta
        Text("Anotações do Atleta", color = TextGray, fontSize = 12.sp)
        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            placeholder = { Text("Como você se sentiu hoje? Calor, dores, vento forte...", color = TextMuted) },
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RuntsDarkCard,
                unfocusedContainerColor = RuntsDarkCard,
                focusedBorderColor = RuntsRedPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val distVal = distance.toDoubleOrNull() ?: 0.0
                val hrVal = hr.toIntOrNull()

                viewModel.saveExecution(
                    prescribedWorkoutId = if (isLinked) prescribedWorkoutId else null,
                    athleteId = athleteId,
                    distanceKm = distVal,
                    durationSeconds = runCatching { TrainingRules.duration(duration) }.getOrDefault(0),
                    pace = pace,
                    avgHr = hrVal,
                    pse = selectedPse,
                    comments = comments.ifBlank { null },
                    sourceProvider = suggestedStravaActivity?.provider,
                    sourceActivityId = suggestedStravaActivity?.externalId
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)
        ) {
            Text("SALVAR REGISTRO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }
        if (state is WorkoutLogUiState.Error) Text((state as WorkoutLogUiState.Error).message, color = RuntsRedPrimary, modifier = Modifier.padding(top = 12.dp))
    }
}
