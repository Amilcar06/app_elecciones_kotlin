package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlin.collections.mutableSetOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Candidato
import com.elecciones.data.entities.Frente
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.EleccionViewModel
import com.elecciones.viewmodel.FrenteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

/**
 * Pantalla para seleccionar candidatos para postular a un puesto.
 * Permite selección múltiple para agregar varios candidatos a la vez.
 * Muestra todos los candidatos agrupados por frente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SeleccionarCandidatoScreen(
    candidatoViewModel: CandidatoViewModel,
    frenteViewModel: FrenteViewModel,
    eleccionViewModel: EleccionViewModel,
    puestoId: Int,
    onCandidatoSeleccionado: (Int) -> Unit
) {
    val todosLosCandidatos by candidatoViewModel.todosLosCandidatos.collectAsState()
    val todosLosFrentes by frenteViewModel.todosLosFrentes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Candidatos ya postulados a este puesto (para filtrarlos)
    val postulaciones by eleccionViewModel.postulacionesPorPuesto.collectAsState()
    val candidatosYaPostulados = remember(postulaciones) {
        postulaciones.map { it.postulacion.id_candidato }.toSet()
    }
    
    // Candidatos seleccionados para agregar
    val candidatosSeleccionados = remember { mutableSetOf<Int>() }
    var modoSeleccionMultiple by remember { mutableStateOf(false) }

    // Agrupar candidatos por frente y filtrar los ya postulados
    val candidatosPorFrente = remember(todosLosCandidatos, candidatosYaPostulados) {
        todosLosCandidatos
            .filter { it.id_candidato !in candidatosYaPostulados }
            .groupBy { it.id_frente }
            .mapKeys { (frenteId, _) ->
                todosLosFrentes.find { it.id_frente == frenteId }
            }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (modoSeleccionMultiple) {
                            "Seleccionar Candidatos (${candidatosSeleccionados.size} seleccionados)"
                        } else {
                            "Seleccionar Candidato"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (modoSeleccionMultiple) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                modoSeleccionMultiple = false
                                candidatosSeleccionados.clear()
                            }
                        ) {
                            Text("Cancelar")
                        }
                    } else {
                        androidx.compose.material3.TextButton(
                            onClick = { modoSeleccionMultiple = true }
                        ) {
                            Text("Múltiple")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (modoSeleccionMultiple && candidatosSeleccionados.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            var agregados = 0
                            var errores = 0
                            
                            candidatosSeleccionados.forEach { candidatoId ->
                                try {
                                    val candidato = todosLosCandidatos.find { it.id_candidato == candidatoId }
                                    if (candidato != null) {
                                        val postulacion = com.elecciones.data.entities.Postulacion(
                                            id_puesto = puestoId,
                                            id_candidato = candidato.id_candidato,
                                            votos = 0
                                        )
                                        eleccionViewModel.insertarPostulacion(postulacion)
                                        agregados++
                                    }
                                } catch (e: IllegalStateException) {
                                    errores++
                                } catch (e: Exception) {
                                    errores++
                                }
                            }
                            
                            delay(300) // Esperar que se completen las inserciones
                            
                            // Forzar actualización del Flow de postulaciones
                            val puestoIdActual = puestoId
                            eleccionViewModel.setPuestoId(null)
                            delay(100)
                            eleccionViewModel.setPuestoId(puestoIdActual)
                            delay(200) // Esperar a que se recargue el Flow
                            
                            val mensaje = when {
                                agregados > 0 && errores == 0 -> "Se agregaron $agregados candidato(s) correctamente"
                                agregados > 0 && errores > 0 -> "Se agregaron $agregados candidato(s). $errores no se pudieron agregar (ya estaban postulados)"
                                else -> "No se pudieron agregar los candidatos (ya están postulados)"
                            }
                            
                            snackbarHostState.showSnackbar(
                                message = mensaje,
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            
                            candidatosSeleccionados.clear()
                            modoSeleccionMultiple = false
                            onCandidatoSeleccionado(0) // Navegar de vuelta
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Agregar seleccionados")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            if (candidatosPorFrente.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Todos los candidatos ya están postulados para este puesto",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                candidatosPorFrente.forEach { (frente, candidatos) ->
                    item {
                        Text(
                            text = frente.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(candidatos) { candidato ->
                        CandidatoSeleccionableCard(
                            candidato = candidato,
                            frente = frente,
                            seleccionado = candidato.id_candidato in candidatosSeleccionados,
                            modoSeleccionMultiple = modoSeleccionMultiple,
                            onClick = {
                                if (modoSeleccionMultiple) {
                                    if (candidato.id_candidato in candidatosSeleccionados) {
                                        candidatosSeleccionados.remove(candidato.id_candidato)
                                    } else {
                                        candidatosSeleccionados.add(candidato.id_candidato)
                                    }
                                } else {
                                    // Modo simple: agregar inmediatamente
                                    scope.launch {
                                        try {
                                            val postulacion = com.elecciones.data.entities.Postulacion(
                                                id_puesto = puestoId,
                                                id_candidato = candidato.id_candidato,
                                                votos = 0
                                            )
                                            eleccionViewModel.insertarPostulacion(postulacion)
                                            delay(200) // Esperar que se complete la inserción
                                            
                                            // Forzar actualización del Flow de postulaciones
                                            val puestoIdActual = puestoId
                                            eleccionViewModel.setPuestoId(null)
                                            delay(100)
                                            eleccionViewModel.setPuestoId(puestoIdActual)
                                            delay(200) // Esperar a que se recargue el Flow
                                            
                                            onCandidatoSeleccionado(candidato.id_candidato)
                                        } catch (e: IllegalStateException) {
                                            snackbarHostState.showSnackbar(
                                                message = "Este candidato ya está postulado para este puesto.",
                                                duration = androidx.compose.material3.SnackbarDuration.Short
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "Error al agregar candidato: ${e.message}",
                                                duration = androidx.compose.material3.SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidatoSeleccionableCard(
    candidato: Candidato,
    frente: Frente,
    seleccionado: Boolean,
    modoSeleccionMultiple: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (modoSeleccionMultiple) {
                Checkbox(
                    checked = seleccionado,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${candidato.nombre} ${candidato.paterno} ${candidato.materno?.let { " $it" } ?: ""}".trim(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Frente: ${frente.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                candidato.profesion?.let {
                    Text(
                        text = "Profesión: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
