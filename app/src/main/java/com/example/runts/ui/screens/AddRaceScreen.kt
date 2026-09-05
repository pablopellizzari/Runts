package com.example.runts.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runts.domain.model.RacePriority
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsDarkCard
import com.example.runts.ui.theme.RuntsRedPrimary
import com.example.runts.ui.theme.TextGray
import com.example.runts.ui.theme.TextMuted
import com.example.runts.ui.theme.TextWhite
import com.example.runts.ui.viewmodel.RaceCalendarViewModel
import com.example.runts.ui.format.formatShortDate
import java.util.Calendar
import java.util.Locale

/**
 * Tela "add-race-form" com DatePicker nativo, campo livre de distância e sem a opção Trail.
 */
@Composable
fun AddRaceScreen(
    viewModel: RaceCalendarViewModel,
    athleteId: String,
    raceId: String?,
    onClose: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var name by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var dateIso by remember { mutableStateOf("") }
    var customDistanceKm by remember { mutableStateOf("21.0") }
    var targetTime by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(RacePriority.PROVA_A) }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val editingRace by viewModel.editingRace.collectAsState()

    LaunchedEffect(raceId) { viewModel.loadRace(raceId) }
    LaunchedEffect(editingRace?.id) {
        editingRace?.let { race ->
            name = race.name
            dateIso = race.date
            dateInput = formatShortDate(race.date)
            customDistanceKm = race.modality.substringBefore(" ").replace(',', '.')
            targetTime = race.targetTime.orEmpty()
            selectedPriority = race.priority
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth)
                val formattedMonth = String.format(Locale.getDefault(), "%02d", month + 1)
                dateInput = "$formattedDay-$formattedMonth-${year.toString().takeLast(2)}"
                dateIso = "$year-$formattedMonth-$formattedDay"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

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
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextWhite)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(if (raceId == null) "NOVA PROVA" else "EDITAR PROVA", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextWhite)
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = RuntsRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Nome do Evento
        Text("Nome do Evento", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Ex: Corrida de São Silvestre", color = TextMuted) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor de Data Padrão (DatePicker)
        Text("Data da Prova", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = dateInput,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Selecione a data no calendário", color = TextMuted) },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar Data", tint = RuntsRedPrimary)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clickable { datePickerDialog.show() },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Distância em KM (Campo Livre Numérico)
        Text("Distância Livre (KM)", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = customDistanceKm,
            onValueChange = { customDistanceKm = it },
            placeholder = { Text("Ex: 21.0 ou 10.5", color = TextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Meta de Tempo (Opcional)
        Text("Meta de Tempo (Opcional)", color = TextWhite, fontSize = 14.sp)
        OutlinedTextField(
            value = targetTime,
            onValueChange = { targetTime = it },
            placeholder = { Text("Ex: 01:45:00", color = TextMuted) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Importância da Prova
        Text("Importância da Prova", color = TextWhite, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { selectedPriority = RacePriority.PROVA_A },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPriority == RacePriority.PROVA_A) RuntsRedPrimary else RuntsDarkCard
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PROVA A", fontWeight = FontWeight.Bold)
                    Text("Foco principal do ano", fontSize = 10.sp, color = TextGray)
                }
            }

            Button(
                onClick = { selectedPriority = RacePriority.PROVA_B },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPriority == RacePriority.PROVA_B) RuntsRedPrimary else RuntsDarkCard
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PROVA B", fontWeight = FontWeight.Bold)
                    Text("Treino de ritmo", fontSize = 10.sp, color = TextGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "Por favor, digite o nome do evento."
                } else if (dateInput.isBlank()) {
                    errorMessage = "Por favor, selecione a data no seletor."
                } else if (customDistanceKm.replace(',', '.').toDoubleOrNull()?.let { it <= 0 } != false) {
                    errorMessage = "Informe uma distância válida maior que zero."
                } else {
                    isSaving = true
                    viewModel.saveRace(
                        raceId = raceId,
                        athleteId = athleteId,
                        name = name.trim(),
                        date = dateIso,
                        modality = "${customDistanceKm.replace(',', '.')} km",
                        targetTime = targetTime.ifBlank { null },
                        priority = selectedPriority,
                        onResult = { result ->
                            isSaving = false
                            result.onSuccess { onSaveSuccess() }
                                .onFailure { errorMessage = it.message ?: "Não foi possível salvar a prova." }
                        }
                    )
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RuntsRedPrimary)
        ) {
            Text(if (isSaving) "SALVANDO..." else "SALVAR PROVA", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }

        if (raceId != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    isSaving = true
                    viewModel.deleteRace(raceId, athleteId) { result ->
                        isSaving = false
                        result.onSuccess { onSaveSuccess() }
                            .onFailure { errorMessage = it.message ?: "Não foi possível excluir a prova." }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RuntsDarkCard)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = RuntsRedPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXCLUIR PROVA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RuntsRedPrimary)
            }
        }
    }
}
