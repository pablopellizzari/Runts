package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.TrainingRules
import com.example.runts.ui.format.formatShortDate
import com.example.runts.ui.theme.BadgeSuccess
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.WorkoutDetailViewModel

@Composable
fun PrescribedVsExecutedScreen(viewModel: WorkoutDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val workout = state.workout
    val execution = state.execution

    Column(
        modifier = Modifier.fillMaxSize().background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }
        Text("PREVISTO VS REALIZADO", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextWhite)

        if (workout == null || execution == null) {
            Text(
                if (state.isLoading) "Carregando dados reais..." else "O registro concluído não foi encontrado.",
                color = TextGray,
                modifier = Modifier.padding(top = 16.dp)
            )
            return@Column
        }

        Text(formatShortDate(execution.executionDate), color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PREVISTO", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("REALIZADO", color = RuntsRedPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                CompareRow("Distância", "${workout.targetDistanceKm} km", "${execution.actualDistanceKm} km")
                Divider()
                CompareRow("Duração", "${workout.targetDurationMinutes} min", TrainingRules.durationText(execution.actualDurationSeconds))
                Divider()
                CompareRow("Ritmo médio", "${workout.targetPace} /km", "${execution.actualPace} /km")
                Divider()
                CompareRow("Frequência cardíaca", workout.targetHeartRateZone, execution.actualAvgHeartRate?.let { "$it bpm" } ?: "Não informada")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BadgeSuccess)
                    Text("  TREINO CONCLUÍDO", color = BadgeSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("PERCEPÇÃO DE ESFORÇO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${execution.pse}/10", color = RuntsRedPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Text("FEEDBACK DO ATLETA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(execution.comments ?: "Nenhuma observação informada.", color = TextWhite, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun Divider() = Spacer(
    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(1.dp).background(RuntsDarkCard)
)

@Composable
private fun CompareRow(label: String, expected: String, actual: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 10.sp)
            Text(expected, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(label, color = TextMuted, fontSize = 10.sp)
            Text(actual, color = RuntsRedPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
