package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.PuestoElectoral
import com.elecciones.viewmodel.EleccionViewModel
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val isLoading by eleccionViewModel.isLoading.collectAsState()
    var operacionCompletada by remember { mutableStateOf(false) }
    
    LaunchedEffect(eleccionId) {
        eleccionViewModel.setEleccionId(eleccionId)
    }
    
    // Navegar cuando la operación termine
    LaunchedEffect(isLoading) {
        if (!isLoading && operacionCompletada) {
            operacionCompletada = false
            onGuardarClick()
        }
    }

    val eleccion by eleccionViewModel.eleccionActual.collectAsState()
    val puestoActual = if (puestoId != null) {
        eleccionViewModel.puestoActual.collectAsState().value
    } else null

    var nombrePuesto by remember { mutableStateOf(puestoActual?.nombre_puesto ?: "") }
    var mostrarErrorNombre by remember { mutableStateOf(false) }
    var mensajeErrorNombre by remember { mutableStateOf("") }
    var verificarNombre by remember { mutableStateOf(false) }

    LaunchedEffect(puestoActual) {
        if (puestoActual != null) {
            nombrePuesto = puestoActual.nombre_puesto
        }
    }
    
    // Validaciones
    val nombreValido = nombrePuesto.trim().length >= 5 && nombrePuesto.trim().length <= 100
    val isFormularioValido = nombreValido && !mostrarErrorNombre

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
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Elección: ${eleccion?.gestion ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nombrePuesto,
                    onValueChange = { 
                        nombrePuesto = it
                        verificarNombre = false
                        mostrarErrorNombre = false
                    },
                    label = { Text("Nombre del Puesto *") },
                    placeholder = { Text("Ej: Director de Carrera") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = (nombrePuesto.isNotBlank() && !nombreValido) || mostrarErrorNombre,
                    supportingText = {
                        when {
                            nombrePuesto.isBlank() -> Text("Campo obligatorio")
                            !nombreValido -> {
                                if (nombrePuesto.trim().length < 5) {
                                    Text("Mínimo 5 caracteres")
                                } else {
                                    Text("Máximo 100 caracteres")
                                }
                            }
                            mostrarErrorNombre -> Text(mensajeErrorNombre)
                            else -> {}
                        }
                    }
                )
            }
            
            Button(
                onClick = {
                    scope.launch {
                        verificarNombre = true
                        val nombreTrimmed = nombrePuesto.trim()
                        
                        val existeNombre = if (puestoId != null) {
                            eleccionViewModel.existeNombrePuestoEnEleccionExcluyendo(eleccionId, nombreTrimmed, puestoId)
                        } else {
                            eleccionViewModel.existeNombrePuestoEnEleccion(eleccionId, nombreTrimmed)
                        }
                        
                        mostrarErrorNombre = existeNombre
                        mensajeErrorNombre = if (existeNombre) {
                            "Ya existe un puesto con este nombre en esta elección."
                        } else {
                            ""
                        }
                        
                        if (!existeNombre && isFormularioValido) {
                            val puesto = if (puestoId != null && puestoActual != null) {
                                puestoActual.copy(nombre_puesto = nombreTrimmed)
                            } else {
                                PuestoElectoral(
                                    id_eleccion = eleccionId,
                                    nombre_puesto = nombreTrimmed,
                                    estado = "Abierto"
                                )
                            }
                            if (puestoId != null) {
                                eleccionViewModel.actualizarPuesto(puesto)
                            } else {
                                eleccionViewModel.insertarPuesto(puesto)
                            }
                            operacionCompletada = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = isFormularioValido && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (puestoId == null) "Guardar Puesto" else "Actualizar Puesto")
                }
            }
        }
    }
}
