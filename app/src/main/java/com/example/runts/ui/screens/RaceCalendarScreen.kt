package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.model.RacePriority
import com.example.runts.ui.theme.BadgeProvaA
import com.example.runts.ui.theme.BadgeProvaB
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.RaceCalendarViewModel
import com.example.runts.ui.format.formatShortDate

/**
 * Tela "race-calendar" do Figma conectada em tempo real ao Neon DB.
 */
@Composable
fun RaceCalendarScreen(
    viewModel: RaceCalendarViewModel,
    onAddRaceClick: () -> Unit,
    onRaceClick: (String) -> Unit
) {
    val racesList by viewModel.races.collectAsState()

    Scaffold(
        containerColor = RuntsDarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRaceClick,
                containerColor = RuntsRedPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Prova", tint = TextWhite)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "CALENDÁRIO DE PROVAS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Suas metas do ano mapeadas pelo treinador para focar na periodização certa.",
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (racesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma prova cadastrada ainda.", color = TextGray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(racesList) { race ->
                        RaceEventCard(race = race, onClick = { onRaceClick(race.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceEventCard(race: RaceEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(race.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(formatShortDate(race.date), color = TextGray, fontSize = 12.sp)
                }
            }

            Text(
                text = race.modality,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (race.priority == RacePriority.PROVA_A) BadgeProvaA else BadgeProvaB)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (race.priority == RacePriority.PROVA_A) "Prova A" else "Prova B",
                    color = TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
