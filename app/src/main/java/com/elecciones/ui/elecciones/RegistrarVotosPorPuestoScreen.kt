package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Postulacion
import com.elecciones.data.entities.PostulacionConCandidato
import com.elecciones.ui.componentes.ConfirmacionEliminarDialog
import com.elecciones.viewmodel.EleccionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

/**
 * Pantalla para registrar los votos de un puesto electoral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarVotosPorPuestoScreen(
    eleccionViewModel: EleccionViewModel,
    puestoId: Int,
    onVotosRegistrados: () -> Unit
) {
    LaunchedEffect(puestoId) {
        eleccionViewModel.setPuestoId(puestoId)
    }

    val puesto by eleccionViewModel.puestoActual.collectAsState()
    val postulaciones by eleccionViewModel.postulacionesPorPuesto.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para los votos de cada postulación
    val votosPorPostulacion = remember {
        mutableStateOf(
            postulaciones.associate { it.postulacion.id_postulacion to it.postulacion.votos.toString() }
        )
    }
    var votosNulos by remember { mutableStateOf(puesto?.votos_nulos?.toString() ?: "0") }
    var votosBlancos by remember { mutableStateOf(puesto?.votos_blancos?.toString() ?: "0") }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    // Actualizar cuando cambien las postulaciones
    LaunchedEffect(postulaciones) {
        votosPorPostulacion.value = postulaciones.associate { it.postulacion.id_postulacion to it.postulacion.votos.toString() }
    }
    
    // Validaciones
    val todosLosVotosValidos = votosPorPostulacion.value.values.all { votos ->
        votos.toIntOrNull()?.let { it >= 0 } ?: false
    } && votosNulos.toIntOrNull()?.let { it >= 0 } ?: false &&
       votosBlancos.toIntOrNull()?.let { it >= 0 } ?: false
    
    // Calcular suma total de votos
    val sumaTotal = remember(votosPorPostulacion.value, votosNulos, votosBlancos) {
        val votosCandidatos = votosPorPostulacion.value.values.sumOf { it.toIntOrNull() ?: 0 }
        val nulos = votosNulos.toIntOrNull() ?: 0
        val blancos = votosBlancos.toIntOrNull() ?: 0
        votosCandidatos + nulos + blancos
    }
    
    val puedeGuardar = todosLosVotosValidos && sumaTotal > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Votos - ${puesto?.nombre_puesto ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                itemsIndexed(postulaciones) { index, postulacionConCandidato ->
                    val postulacionId = postulacionConCandidato.postulacion.id_postulacion
                    val votosActuales = votosPorPostulacion.value[postulacionId] ?: "0"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = postulacionConCandidato.getNombreCompleto(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Frente: ${postulacionConCandidato.frente.nombre}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            OutlinedTextField(
                                value = votosActuales,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        votosPorPostulacion.value = votosPorPostulacion.value.toMutableMap().apply {
                                            put(postulacionId, newValue)
                                        }
                                    }
                                },
                                label = { Text("Votos obtenidos *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                isError = votosActuales.toIntOrNull()?.let { it < 0 } ?: false,
                                supportingText = {
                                    if (votosActuales.toIntOrNull()?.let { it < 0 } == true) {
                                        Text("El valor debe ser mayor o igual a 0")
                                    } else {
                                        Text("Valor por defecto: 0")
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(
                        value = votosNulos,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                votosNulos = newValue
                            }
                        },
                        label = { Text("Votos Nulos *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = votosNulos.toIntOrNull()?.let { it < 0 } ?: false,
                        supportingText = {
                            if (votosNulos.toIntOrNull()?.let { it < 0 } == true) {
                                Text("El valor debe ser mayor o igual a 0")
                            } else {
                                Text("Valor por defecto: 0")
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = votosBlancos,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                votosBlancos = newValue
                            }
                        },
                        label = { Text("Votos Blancos *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = votosBlancos.toIntOrNull()?.let { it < 0 } ?: false,
                        supportingText = {
                            if (votosBlancos.toIntOrNull()?.let { it < 0 } == true) {
                                Text("El valor debe ser mayor o igual a 0")
                            } else {
                                Text("Valor por defecto: 0")
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Total de votos: $sumaTotal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (sumaTotal == 0) {
                        Text(
                            text = "Debe haber al menos un voto para cerrar el puesto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            val isLoading by eleccionViewModel.isLoading.collectAsState()
            Button(
                onClick = {
                    mostrarDialogoConfirmacion = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = puedeGuardar && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Votos y Cerrar Puesto")
                }
            }
            
            // Diálogo de confirmación
            if (mostrarDialogoConfirmacion) {
                ConfirmacionEliminarDialog(
                    titulo = "Confirmar Cierre de Puesto",
                    mensaje = "Una vez cerradas las votaciones, los resultados no se podrán modificar. ¿Está seguro de que desea guardar y cerrar este puesto?",
                    textoConfirmar = "CONFIRMAR",
                    textoCancelar = "CANCELAR",
                    onConfirmar = {
                        val postulacionesActualizadas = postulaciones.map { postulacionConCandidato ->
                            val postulacionId = postulacionConCandidato.postulacion.id_postulacion
                            val votos = votosPorPostulacion.value[postulacionId]?.toIntOrNull() ?: 0
                            postulacionConCandidato.postulacion.copy(votos = votos)
                        }
                        val nulos = votosNulos.toIntOrNull() ?: 0
                        val blancos = votosBlancos.toIntOrNull() ?: 0

                        scope.launch {
                            try {
                                // Cerrar el diálogo primero para evitar problemas de estado
                                mostrarDialogoConfirmacion = false
                                
                                // Validar que hay postulaciones
                                if (postulacionesActualizadas.isEmpty()) {
                                    snackbarHostState.showSnackbar(
                                        message = "Debe haber al menos un candidato postulado",
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                
                                // Validar que el puesto existe y está en estado correcto (usar el estado del composable)
                                if (puesto == null) {
                                    snackbarHostState.showSnackbar(
                                        message = "Error: No se pudo encontrar el puesto",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                    return@launch
                                }
                                
                                if (puesto?.estado != "Abierto") {
                                    snackbarHostState.showSnackbar(
                                        message = "Error: El puesto ya está cerrado",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                    return@launch
                                }
                                
                                // Registrar los votos
                                eleccionViewModel.registrarVotosPorPuesto(
                                    puestoId = puestoId,
                                    postulaciones = postulacionesActualizadas,
                                    votosNulos = nulos,
                                    votosBlancos = blancos
                                )
                                
                                // Esperar un momento para que la operación se complete y la UI se actualice
                                kotlinx.coroutines.delay(500)
                                
                                // Navegar de vuelta de forma segura
                                try {
                                    onVotosRegistrados()
                                } catch (e: Exception) {
                                    // Si hay error en la navegación, mostrar mensaje
                                    snackbarHostState.showSnackbar(
                                        message = "Votos registrados correctamente. Use el botón atrás para volver.",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                }
                            } catch (e: IllegalArgumentException) {
                                // Error de validación
                                mostrarDialogoConfirmacion = false
                                snackbarHostState.showSnackbar(
                                    message = "Error: ${e.message}",
                                    duration = androidx.compose.material3.SnackbarDuration.Long
                                )
                            } catch (e: Exception) {
                                // Cualquier otro error
                                mostrarDialogoConfirmacion = false
                                snackbarHostState.showSnackbar(
                                    message = "Error al registrar votos: ${e.message ?: "Error desconocido"}",
                                    duration = androidx.compose.material3.SnackbarDuration.Long
                                )
                                e.printStackTrace() // Para debugging
                            }
                        }
                    },
                    onCancelar = { mostrarDialogoConfirmacion = false }
                )
            }
        }
    }
}
