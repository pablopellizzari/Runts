package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.ui.theme.BadgeTiros
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.WorkoutDetailViewModel
import com.example.runts.ui.format.displayName
import com.example.runts.ui.format.formatShortDate
import com.example.runts.ui.format.localizeSegmentNames
import com.example.runts.domain.model.WorkoutStatus

/**
 * Tela "training-detail" do Figma.
 */
@Composable
fun WorkoutDetailScreen(
    viewModel: WorkoutDetailViewModel,
    onBack: () -> Unit,
    onMarkCompletedClick: () -> Unit,
    onViewComparisonClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val workout = state.workout
    if (workout == null) {
        Column(modifier = Modifier.fillMaxSize().background(RuntsDarkBackground).padding(24.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite) }
            Text(if (state.isLoading) "Carregando treino..." else "Treino não encontrado.", color = TextWhite)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BadgeTiros)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(workout.workoutType.displayName().uppercase(), color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(formatShortDate(workout.targetDate), color = TextGray, fontSize = 13.sp)
        Text(
            text = workout.workoutType.displayName(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Linha 1 de Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatCard(
                title = "DISTÂNCIA ALVO",
                value = "${workout.targetDistanceKm} KM",
                subtitle = "Inclui aquec. e desaquec.",
                valueColor = RuntsRedPrimary,
                modifier = Modifier.weight(1f)
            )

            DetailStatCard(
                title = "DURAÇÃO ESTIMADA",
                value = "${workout.targetDurationMinutes} MIN",
                subtitle = "Ritmo forte",
                valueColor = RuntsRedPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Linha 2 de Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatCard(
                title = "PACE ALVO",
                value = workout.targetPace,
                subtitle = "min/km nos tiros",
                valueColor = RuntsRedPrimary,
                modifier = Modifier.weight(1f)
            )

            DetailStatCard(
                title = "FREQUÊNCIA ALVO",
                value = workout.targetHeartRateZone,
                subtitle = "Limiar anaeróbico",
                valueColor = RuntsRedPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Esforço Predominante
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ESFORÇO PREDOMINANTE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(RuntsDarkCard, RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(RuntsRedPrimary, RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(RuntsRedPrimary, RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(RuntsDarkCard, RoundedCornerShape(4.dp)))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Z1 (Leve)", color = TextMuted, fontSize = 10.sp)
                    Text("Z3/Z4 (Forte)", color = RuntsRedPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Z5 (Máximo)", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Instruções do Treinador
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📋 Instruções do Treinador", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeSegmentNames(workout.description),
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão "MARCAR COMO CONCLUÍDO"
        Button(
            onClick = if (workout.status == WorkoutStatus.COMPLETED) onViewComparisonClick else onMarkCompletedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (workout.status == WorkoutStatus.COMPLETED) "VER PREVISTO VS REALIZADO" else "MARCAR COMO CONCLUÍDO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }
    }
}

@Composable
private fun DetailStatCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}
