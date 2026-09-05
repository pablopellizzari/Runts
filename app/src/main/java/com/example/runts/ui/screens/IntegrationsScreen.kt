package com.example.runts.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.runts.ui.theme.*
import com.example.runts.ui.viewmodel.IntegrationsViewModel

private data class PendingIntegration(val name: String, val description: String)

@Composable
fun IntegrationsScreen(viewModel: IntegrationsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val authorizationUrl by viewModel.authorizationUrl.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.loadStatus() }
    LaunchedEffect(authorizationUrl) {
        authorizationUrl?.let {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
            viewModel.consumeAuthorizationUrl()
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.loadStatus() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().background(RuntsDarkBackground).padding(16.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }
        Text("INTEGRAÇÕES", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Text("Sincronize automaticamente seus treinos executados", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                StravaCard(state.strava.connected, state.strava.athleteName, state.strava.lastSyncAt, state.loading,
                    viewModel::connectStrava, viewModel::syncStrava, viewModel::disconnectStrava)
            }
            state.message?.let { item { Text(it, color = TextWhite, fontSize = 12.sp) } }
            state.error?.let { item { Text(it, color = RuntsRedPrimary, fontSize = 12.sp) } }
            items(listOf(
                PendingIntegration("Garmin Connect", "Sincronização com relógios Garmin."),
                PendingIntegration("Polar Flow", "Métricas avançadas e recuperação."),
                PendingIntegration("Coros Active", "Carga de treino e ritmo por satélite."),
                PendingIntegration("Samsung Health", "Sincronização com relógios Galaxy Watch.")
            )) { PendingIntegrationCard(it) }
        }
    }
}

@Composable
private fun StravaCard(connected: Boolean, athleteName: String?, lastSyncAt: String?, loading: Boolean,
                       onConnect: () -> Unit, onSync: () -> Unit, onDisconnect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(if (connected) RuntsRedPrimary else RuntsDarkCard), contentAlignment = Alignment.Center) {
                    Text("S", color = TextWhite, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Strava", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(if (connected) "Conectado${athleteName?.let { " como $it" } ?: ""}" else "Importe distância, duração, ritmo e frequência cardíaca.", color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                    if (connected && lastSyncAt != null) Text("Sincronização automática ativa", color = RuntsRedPrimary, fontSize = 10.sp)
                }
                if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = RuntsRedPrimary, strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(12.dp))
            if (!connected) {
                Button(onClick = onConnect, enabled = !loading, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)) {
                    Text("CONECTAR COM STRAVA", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSync, enabled = !loading, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)) {
                        Icon(Icons.Default.Sync, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("SINCRONIZAR", fontSize = 11.sp)
                    }
                    Button(onClick = onDisconnect, enabled = !loading, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RuntsDarkCard)) {
                        Icon(Icons.Default.LinkOff, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("DESCONECTAR", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingIntegrationCard(item: PendingIntegration) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(RuntsDarkCard), contentAlignment = Alignment.Center) { Text(item.name.take(1), color = TextWhite, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(item.name, color = TextWhite, fontWeight = FontWeight.Bold); Text(item.description, color = TextGray, fontSize = 11.sp) }
            Text("EM BREVE", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
