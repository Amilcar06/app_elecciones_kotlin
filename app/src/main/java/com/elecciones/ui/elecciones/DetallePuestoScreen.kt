package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.PostulacionConCandidato
import com.elecciones.viewmodel.EleccionViewModel

/**
 * Pantalla que muestra las postulaciones de un puesto electoral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetallePuestoScreen(
    eleccionViewModel: EleccionViewModel,
    puestoId: Int,
    onAddPostulacionClick: () -> Unit,
    onRegistrarVotosClick: () -> Unit,
    onVerResultadosClick: () -> Unit,
    onEliminarPostulacion: (Int) -> Unit
) {
    LaunchedEffect(puestoId) {
        eleccionViewModel.setPuestoId(puestoId)
    }

    val puesto by eleccionViewModel.puestoActual.collectAsState()
    val postulaciones by eleccionViewModel.postulacionesPorPuesto.collectAsState()

    // Validaciones de estado: no se puede modificar si está en "Votación" o "Cerrado"
    val puedeModificar = puesto?.estado == "Abierto"
    val puedeRegistrarVotos = puesto?.estado == "Abierto" && postulaciones.isNotEmpty()
    val puedeVerResultados = puesto?.estado == "Cerrado"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puesto: ${puesto?.nombre_puesto ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (puedeModificar) {
                FloatingActionButton(onClick = onAddPostulacionClick) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Postulación")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información del puesto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Estado: ${puesto?.estado ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Candidatos postulados: ${postulaciones.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (postulaciones.isEmpty()) {
                Text(
                    text = "No hay postulaciones registradas.\nPulsa el botón + para añadir una.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(postulaciones) { postulacion ->
                        PostulacionCard(
                            postulacion = postulacion,
                            puedeEliminar = puedeModificar,
                            onEliminar = { onEliminarPostulacion(postulacion.postulacion.id_postulacion) }
                        )
                    }
                }
            }

            if (puesto?.estado == "Abierto") {
                Button(
                    onClick = onRegistrarVotosClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = postulaciones.isNotEmpty()
                ) {
                    Text("Registrar Votos")
                }
            }

            if (puedeVerResultados) {
                OutlinedButton(
                    onClick = onVerResultadosClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Ver Resultados")
                }
            }
        }
    }
}

@Composable
private fun PostulacionCard(
    postulacion: PostulacionConCandidato,
    puedeEliminar: Boolean,
    onEliminar: () -> Unit
) {
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
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postulacion.getNombreCompleto(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Frente: ${postulacion.frente.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (puedeEliminar) {
                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

