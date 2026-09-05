package com.example.runts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.AuthUiState
import com.example.runts.ui.viewmodel.AuthViewModel

/**
 * Tela de Criação de Conta (Atleta ou Treinador) conectada ao Neon DB e Room.
 */
@Composable
fun RegisterAccountScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: (User) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            val user = (uiState as AuthUiState.Authenticated).user
            onRegisterSuccess(user)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RuntsDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
        }

        Text("CRIAR CONTA", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Text("Crie sua conta de atleta", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        if (localError.isNotBlank()) {
            Text(localError, color = RuntsRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
        }

        if (uiState is AuthUiState.Error) {
            Text((uiState as AuthUiState.Error).message, color = RuntsRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
        }

        // Nome Completo
        Text("Nome Completo", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Seu nome completo", color = TextMuted) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
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

        // E-mail
        Text("E-mail", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("seu@email.com", color = TextMuted) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
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

        // Senha
        Text("Senha", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Mínimo 6 caracteres", color = TextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
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

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.length < 6) {
                    localError = "Preencha todos os campos corretamente (senha mín. 6 caracteres)."
                } else {
                    localError = ""
                    authViewModel.register(name.trim(), email.trim(), password.trim(), UserType.ATHLETE)
                }
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = TextWhite, modifier = Modifier.height(24.dp))
            } else {
                Text("CADASTRAR E ENTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}
