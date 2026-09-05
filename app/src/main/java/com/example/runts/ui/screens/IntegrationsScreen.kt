package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class IntegrationModel(
    val name: String,
    val description: String,
    val isConnected: Boolean,
    val lastSyncText: String? = null
)

/**
 * Tela "integrations" do Figma (Strava, Garmin Connect, Apple Health, Polar, Coros, Samsung Health).
 */
@Composable
fun IntegrationsScreen(
    onBack: () -> Unit
) {
    val integrationsList = listOf(
        IntegrationModel("Strava", "Conexão OAuth será disponibilizada em uma próxima versão.", false),
        IntegrationModel("Garmin Connect", "Conexão OAuth será disponibilizada em uma próxima versão.", false),
        IntegrationModel("Apple Health", "Atividades salvas no Apple Watch.", false),
        IntegrationModel("Polar Flow", "Métricas avançadas e recuperação.", false),
        IntegrationModel("Coros Active", "Carga de treino e pace por satélite.", false),
        IntegrationModel("Samsung Health", "Sincronize relógios da linha Galaxy Watch.", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }

        Text("INTEGRAÇÕES", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Text("Sincronize automaticamente seus treinos executados", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.padding(top = 16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(integrationsList) { item ->
                IntegrationCard(item = item)
            }
        }
    }
}

@Composable
private fun IntegrationCard(item: IntegrationModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (item.isConnected) RuntsRedPrimary else RuntsDarkCard),
                contentAlignment = Alignment.Center
            ) {
                Text(item.name.take(1), color = TextWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(item.description, color = TextGray, fontSize = 11.sp, lineHeight = 14.sp)
                item.lastSyncText?.let {
                    Text(it, color = RuntsRedPrimary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isConnected) RuntsDarkCard else TextWhite,
                    contentColor = if (item.isConnected) RuntsRedPrimary else RuntsDarkBackground
                )
            ) {
                Text(
                    text = if (item.isConnected) "Conectado" else "Em breve",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
