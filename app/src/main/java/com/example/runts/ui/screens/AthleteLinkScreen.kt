package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.AuthViewModel

/**
 * Tela "athlete-link" do Figma (Vincular Treinador por código gerado pelo treinador no app).
 */
@Composable
fun AthleteLinkScreen(
    authViewModel: AuthViewModel,
    athleteId: String,
    onBack: () -> Unit,
    onLinkSuccess: () -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }

        Text(
            text = "VINCULAR TREINADOR",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Conecte seu perfil ao treinador usando o código de convite gerado por ele no aplicativo.",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Text("Código de Convite do Treinador", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            placeholder = { Text("Ex: RNTS-4829", color = TextMuted) },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
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
                authViewModel.linkCoach(athleteId, inviteCode)
                onLinkSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)
        ) {
            Text("VINCULAR AGORA", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
        }
    }
}
