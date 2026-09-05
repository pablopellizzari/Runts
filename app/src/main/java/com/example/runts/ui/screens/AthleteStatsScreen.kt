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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.AthleteStatsViewModel

/**
 * Tela "athlete-stats" do Figma com estatísticas reais calculadas do Neon DB.
 */
@Composable
fun AthleteStatsScreen(
    viewModel: AthleteStatsViewModel,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Mês") }

    val stats by viewModel.stats.collectAsState()

    val totalDistStr = stats?.let { "${it.totalDistanceKm} KM" } ?: "—"
    val avgPaceStr = stats?.avgPace ?: "—"
    val adherenceStr = stats?.let { "${it.adherenceRatePercent}%" } ?: "—"

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

        Text("ESTATÍSTICAS", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Text("Acompanhe sua evolução física", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor de Período
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semana", "Mês", "Ano", "Custom").forEach { period ->
                val isSelected = selectedFilter == period
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = period },
                    label = { Text(period) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RuntsDarkCard,
                        selectedLabelColor = TextWhite,
                        containerColor = RuntsDarkSurface,
                        labelColor = TextGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Linha 1 de métricas
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DISTÂNCIA TOTAL ↗", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(totalDistStr, color = RuntsRedPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Período selecionado", color = TextMuted, fontSize = 10.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TEMPO DE TREINO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stats?.let { "${it.totalDurationSeconds / 3600}h ${(it.totalDurationSeconds / 60) % 60}m" } ?: "—", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Tempo registrado", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Linha 2 de métricas
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PACE MÉDIO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(avgPaceStr, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ADESÃO À PLANILHA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(adherenceStr, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(" 🔄", fontSize = 14.sp)
                    }
                    Text("Calculado pelas prescrições", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card DISTRIBUIÇÃO DE ZONAS CARDÍACAS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DISTRIBUIÇÃO DE ZONAS CARDÍACAS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                val zones = stats?.hrZoneDistributionPercent.orEmpty()
                val total = zones.values.sum().coerceAtLeast(1)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("Z1" to RuntsDarkCard, "Z2" to RuntsDarkCard, "Z3" to RuntsRedPrimary, "Z4" to com.example.runts.ui.theme.BadgeTiros, "Z5" to com.example.runts.ui.theme.BadgeTiros).forEach { (zone, color) ->
                        Box(modifier = Modifier.weight((zones[zone] ?: 0).coerceAtLeast(1).toFloat() / total).height(12.dp).background(color))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Z1 ${zones["Z1"] ?: 0}%", color = TextMuted, fontSize = 10.sp)
                    Text("Z3 ${zones["Z3"] ?: 0}%", color = RuntsRedPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Z5 ${zones["Z5"] ?: 0}%", color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
