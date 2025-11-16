package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.PuestoElectoral
import com.elecciones.viewmodel.EleccionViewModel

/**
 * Pantalla para registrar o editar un puesto electoral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarPuestoScreen(
    eleccionViewModel: EleccionViewModel,
    eleccionId: Int,
    puestoId: Int? = null,
    onGuardarClick: () -> Unit
) {
    LaunchedEffect(eleccionId) {
        eleccionViewModel.setEleccionId(eleccionId)
    }

    val eleccion by eleccionViewModel.eleccionActual.collectAsState()
    val puestoActual = if (puestoId != null) {
        eleccionViewModel.puestoActual.collectAsState().value
    } else null

    var nombrePuesto by remember { mutableStateOf(puestoActual?.nombre_puesto ?: "") }

    LaunchedEffect(puestoActual) {
        if (puestoActual != null) {
            nombrePuesto = puestoActual.nombre_puesto
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (puestoId == null) "Registrar Puesto" else "Editar Puesto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Elección: ${eleccion?.gestion ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = nombrePuesto,
                onValueChange = { nombrePuesto = it },
                label = { Text("Nombre del Puesto") },
                placeholder = { Text("Ej: Director de Carrera") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (nombrePuesto.isNotBlank()) {
                        val puesto = if (puestoId != null && puestoActual != null) {
                            puestoActual.copy(nombre_puesto = nombrePuesto)
                        } else {
                            PuestoElectoral(
                                id_eleccion = eleccionId,
                                nombre_puesto = nombrePuesto,
                                estado = "Abierto"
                            )
                        }
                        if (puestoId != null) {
                            eleccionViewModel.actualizarPuesto(puesto)
                        } else {
                            eleccionViewModel.insertarPuesto(puesto)
                        }
                        onGuardarClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = nombrePuesto.isNotBlank()
            ) {
                Text("Guardar Puesto")
            }
        }
    }
}

