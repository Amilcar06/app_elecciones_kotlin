package com.elecciones.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Componente DatePicker simple para seleccionar fecha.
 * Devuelve la fecha en formato YYYY-MM-DD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    fechaInicial: String? = null,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fechaDefault = fechaInicial?.let { 
        try {
            LocalDate.parse(it, formatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    } ?: LocalDate.now()

    var añoSeleccionado by remember { mutableStateOf(fechaDefault.year) }
    var mesSeleccionado by remember { mutableStateOf(fechaDefault.monthValue) }
    var diaSeleccionado by remember { mutableStateOf(fechaDefault.dayOfMonth) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Seleccionar Fecha",
                    style = MaterialTheme.typography.titleLarge
                )

                // Selector de año
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Año:")
                    OutlinedTextField(
                        value = añoSeleccionado.toString(),
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                if (it >= 1900 && it <= 2100) {
                                    añoSeleccionado = it
                                }
                            }
                        },
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

                // Selector de mes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mes:")
                    var mesExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { mesExpanded = true },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(obtenerNombreMes(mesSeleccionado))
                        }
                        DropdownMenu(
                            expanded = mesExpanded,
                            onDismissRequest = { mesExpanded = false }
                        ) {
                            (1..12).forEach { mes ->
                                DropdownMenuItem(
                                    text = { Text(obtenerNombreMes(mes)) },
                                    onClick = {
                                        mesSeleccionado = mes
                                        mesExpanded = false
                                        // Ajustar día si es necesario
                                        val maxDias = obtenerMaxDiasMes(mesSeleccionado, añoSeleccionado)
                                        if (diaSeleccionado > maxDias) {
                                            diaSeleccionado = maxDias
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de día
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Día:")
                    var diaExpanded by remember { mutableStateOf(false) }
                    val maxDias = obtenerMaxDiasMes(mesSeleccionado, añoSeleccionado)
                    
                    Box {
                        OutlinedButton(
                            onClick = { diaExpanded = true },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(diaSeleccionado.toString())
                        }
                        DropdownMenu(
                            expanded = diaExpanded,
                            onDismissRequest = { diaExpanded = false }
                        ) {
                            (1..maxDias).forEach { dia ->
                                DropdownMenuItem(
                                    text = { Text(dia.toString()) },
                                    onClick = {
                                        diaSeleccionado = dia
                                        diaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCELAR")
                    }
                    Button(
                        onClick = {
                            val fechaSeleccionada = LocalDate.of(añoSeleccionado, mesSeleccionado, diaSeleccionado)
                            onDateSelected(fechaSeleccionada.format(formatter))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ACEPTAR")
                    }
                }
            }
        }
    }
}

private fun obtenerNombreMes(mes: Int): String {
    return when (mes) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        12 -> "Diciembre"
        else -> mes.toString()
    }
}

private fun obtenerMaxDiasMes(mes: Int, año: Int): Int {
    val diasPorMes = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    if (mes == 2 && esAñoBisiesto(año)) {
        return 29
    }
    return diasPorMes[mes - 1]
}

private fun esAñoBisiesto(año: Int): Boolean {
    return (año % 4 == 0 && año % 100 != 0) || (año % 400 == 0)
}

