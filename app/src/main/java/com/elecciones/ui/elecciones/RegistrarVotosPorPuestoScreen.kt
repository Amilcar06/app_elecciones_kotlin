package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Postulacion
import com.elecciones.data.entities.PostulacionConCandidato
import com.elecciones.viewmodel.EleccionViewModel

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

    // Estado para los votos de cada postulación
    val votosPorPostulacion = remember {
        mutableStateOf(
            postulaciones.associate { it.postulacion.id_postulacion to it.postulacion.votos.toString() }
        )
    }
    var votosNulos by remember { mutableStateOf(puesto?.votos_nulos?.toString() ?: "0") }
    var votosBlancos by remember { mutableStateOf(puesto?.votos_blancos?.toString() ?: "0") }

    // Actualizar cuando cambien las postulaciones
    LaunchedEffect(postulaciones) {
        votosPorPostulacion.value = postulaciones.associate { it.postulacion.id_postulacion to it.postulacion.votos.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Votos - ${puesto?.nombre_puesto ?: ""}") },
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
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
                                label = { Text("Votos obtenidos") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
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
                        label = { Text("Votos Nulos") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
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
                        label = { Text("Votos Blancos") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }

            Button(
                onClick = {
                    val postulacionesActualizadas = postulaciones.map { postulacionConCandidato ->
                        val postulacionId = postulacionConCandidato.postulacion.id_postulacion
                        val votos = votosPorPostulacion.value[postulacionId]?.toIntOrNull() ?: 0
                        postulacionConCandidato.postulacion.copy(votos = votos)
                    }
                    val nulos = votosNulos.toIntOrNull() ?: 0
                    val blancos = votosBlancos.toIntOrNull() ?: 0

                    eleccionViewModel.registrarVotosPorPuesto(
                        puestoId = puestoId,
                        postulaciones = postulacionesActualizadas,
                        votosNulos = nulos,
                        votosBlancos = blancos
                    )
                    onVotosRegistrados()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar Votos y Cerrar Puesto")
            }
        }
    }
}

