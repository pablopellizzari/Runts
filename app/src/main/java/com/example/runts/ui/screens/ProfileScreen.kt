package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsDarkSurface
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextWhite

/**
 * Tela "athlete-profile" / "trainer-profile" com dados reais do usuário logado.
 */
@Composable
fun ProfileScreen(
    currentUser: User? = null,
    userType: UserType = UserType.ATHLETE,
    onNavigateToStats: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToIntegrations: () -> Unit,
    onNavigateToLinkCoach: () -> Unit,
    onLogout: () -> Unit
) {
    val userName = currentUser?.name ?: "Atleta"
    val userEmail = currentUser?.email ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(24.dp)
                    .background(RuntsRedPrimary, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("RUNTS", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RuntsDarkCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (userType == UserType.ATHLETE) "ATLETA" else "TREINADOR",
                    color = RuntsRedPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(RuntsDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName,
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userEmail,
                color = TextGray,
                fontSize = 13.sp
            )

            currentUser?.inviteCode?.let { code ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Código de Convite: $code",
                    color = RuntsRedPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("MINHA CONTA", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuItem(title = "Editar Perfil", icon = Icons.Default.Person, onClick = {})
        ProfileMenuItem(title = "Estatísticas", icon = Icons.Default.BarChart, onClick = onNavigateToStats)
        ProfileMenuItem(title = "Calendário de Provas", icon = Icons.Default.CalendarMonth, onClick = onNavigateToCalendar)
        ProfileMenuItem(title = "Integrações", icon = Icons.Default.Link, onClick = onNavigateToIntegrations)

        if (userType == UserType.ATHLETE) {
            ProfileMenuItem(title = "Vínculo com Treinador", icon = Icons.Default.Person, onClick = onNavigateToLinkCoach)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("CONFIGURAÇÕES", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuItem(title = "Notificações", icon = Icons.Default.Notifications, onClick = {})
        ProfileMenuItem(title = "Sair", icon = Icons.AutoMirrored.Filled.ExitToApp, isLogout = true, onClick = onLogout)
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = RuntsDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isLogout) RuntsRedPrimary else TextGray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = title,
                color = if (isLogout) RuntsRedPrimary else TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextGray
            )
        }
    }
}
