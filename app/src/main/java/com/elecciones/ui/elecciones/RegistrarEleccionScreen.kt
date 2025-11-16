package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.elecciones.data.entities.Eleccion
import com.elecciones.ui.utilidades.validarFormatoFecha
import com.elecciones.viewmodel.EleccionViewModel

/**
 * Pantalla de formulario para registrar o editar una elección.
 *
 * @param eleccionViewModel ViewModel para la lógica de negocio de las elecciones.
 * @param eleccionId ID de la elección a editar (null si es nueva).
 * @param onGuardarAccion Acción a ejecutar tras guardar la elección (navegar hacia atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarEleccionScreen(
    eleccionViewModel: EleccionViewModel,
    eleccionId: Int? = null,
    onGuardarAccion: () -> Unit
) {
    val esEdicion = eleccionId != null
    val elecciones: List<Eleccion> by eleccionViewModel.todasLasElecciones.collectAsState()
    val eleccion = if (esEdicion && eleccionId != null) {
        elecciones.find { elec: Eleccion -> elec.id_eleccion == eleccionId }
    } else {
        null
    }

    var gestion by remember(eleccion) { mutableStateOf(eleccion?.gestion?.toString() ?: "") }
    var fecha by remember(eleccion) { mutableStateOf(eleccion?.fecha_eleccion ?: "") }
    var descripcion by remember(eleccion) { mutableStateOf(eleccion?.descripcion ?: "") }
    
    var mostrarErrorFecha by remember { mutableStateOf(false) }
    var mostrarErrorCerrada by remember { mutableStateOf(false) }

    val eleccionCerrada = eleccion?.estado == "Cerrada"
    val fechaValida = fecha.isBlank() || validarFormatoFecha(fecha)
    val isFormularioValido = gestion.isNotBlank() && 
            fecha.isNotBlank() && 
            fechaValida &&
            !eleccionCerrada

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Elección" else "Crear Nueva Elección") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Mostrar mensaje si la elección está cerrada
        if (eleccionCerrada) {
            AlertDialog(
                onDismissRequest = { onGuardarAccion() },
                title = { Text("Elección Cerrada") },
                text = { Text("No se puede modificar una elección que ya está cerrada.") },
                confirmButton = {
                    TextButton(onClick = { onGuardarAccion() }) {
                        Text("CERRAR")
                    }
                }
            )
            return@Scaffold
        }
        
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = gestion,
                onValueChange = { gestion = it },
                label = { Text("Gestión (Año)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !eleccionCerrada
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = { 
                    fecha = it
                    mostrarErrorFecha = false
                },
                label = { Text("Fecha de Elección (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !eleccionCerrada,
                isError = mostrarErrorFecha || (fecha.isNotBlank() && !fechaValida),
                supportingText = if (mostrarErrorFecha || (fecha.isNotBlank() && !fechaValida)) {
                    { Text("Formato de fecha inválido. Use YYYY-MM-DD") }
                } else null
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                enabled = !eleccionCerrada
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (eleccionCerrada) {
                            mostrarErrorCerrada = true
                            return@Button
                        }
                        
                        mostrarErrorFecha = fecha.isNotBlank() && !fechaValida
                        
                        if (!mostrarErrorFecha && isFormularioValido) {
                            if (esEdicion && eleccion != null) {
                                // Actualizar elección existente
                                val eleccionActualizada = eleccion.copy(
                                    gestion = gestion.toIntOrNull() ?: eleccion.gestion,
                                    fecha_eleccion = fecha,
                                    descripcion = descripcion.takeIf { it.isNotBlank() }
                                )
                                eleccionViewModel.actualizarEleccion(eleccionActualizada)
                            } else {
                                // Crear nueva elección
                                val nuevaEleccion = Eleccion(
                                    gestion = gestion.toIntOrNull() ?: 0,
                                    fecha_eleccion = fecha,
                                    estado = "Programada", // Por defecto, una nueva elección está "Programada"
                                    descripcion = descripcion.takeIf { it.isNotBlank() }
                                )
                                eleccionViewModel.insertarEleccion(nuevaEleccion)
                            }
                            onGuardarAccion()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFormularioValido && !eleccionCerrada
                ) {
                    Text("GUARDAR")
                }

                OutlinedButton(
                    onClick = onGuardarAccion,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CANCELAR")
                }
            }
        }
    }
}
