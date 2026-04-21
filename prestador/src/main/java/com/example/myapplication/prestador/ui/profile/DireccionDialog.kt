package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun DireccionDialog(
    direccion: DireccionEntity? = null,
    titulo: String = "Dirección",
    onDismiss: () -> Unit,
    onConfirm: (
            pais: String,
            provincia: String,
            localidad: String,
            codigoPostal: String,
            calle: String,
            numer: String
            ) -> Unit
) {
    val colors = getPrestadorColors()
    var pais by remember { mutableStateOf(direccion?.pais ?: "Argentina") }
    var provincia by remember { mutableStateOf(direccion?.provincia ?: "") }
    var localidad by remember { mutableStateOf(direccion?.localidad ?: "") }
    var codigoPostal by remember { mutableStateOf(direccion?.codigoPostal ?: "") }
    var calle by remember { mutableStateOf(direccion?.calle ?: "") }
    var numero by remember { mutableStateOf(direccion?.numero ?: "") }

    var calleError by remember { mutableStateOf(false) }
    var provinciaError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingLabelTextField(
                    value = pais,
                    onValueChange = { pais = it },
                    label = "País",
                    leadingIcon = Icons.Default.Public
                )

                Spacer(modifier = Modifier.height(12.dp))

                FloatingLabelTextField(
                    value = provincia,
                    onValueChange = {
                        provincia = it; provinciaError = false },
                    label = "Provincia / Estado *",
                    leadingIcon = Icons.Default.Map
                )
                if (provinciaError) {
                    Text("La provincia es requerida", color = Color.Red, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                    }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingLabelTextField(
                    value = localidad,
                    onValueChange = {localidad = it },
                    label = "Localidad / Ciudad",
                    leadingIcon = Icons.Default.LocationCity
                )

                Spacer(modifier = Modifier.height(12.dp))
                FloatingLabelTextField(
                    value = codigoPostal,
                    onValueChange = {
                        codigoPostal = it
                    },
                    label = "Codigo Postal",
                    leadingIcon = Icons.Default.MarkunreadMailbox,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                FloatingLabelTextField(
                    value = calle,
                    onValueChange = { calle = it; calleError = false},
                    label = "Calle *",
                    leadingIcon = Icons.Default.Home
                )
                if (calleError) {
                    Text("La calle es requerida", color = Color.Red, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingLabelTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = "Numero / Altura ",
                    leadingIcon = Icons.Default.Tag,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) {Text("Cancelar") }

                    Button(
                        onClick = {
                            var hasError = false
                            if (provincia.isBlank()) { provinciaError = true; hasError = true }
                            if (calle.isBlank()) { calleError = true; hasError = true }
                            if (!hasError) {
                                onConfirm(pais, provincia, localidad, codigoPostal, calle, numero)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) { Text("Guardar") }
                }
            }
        }
    }
}