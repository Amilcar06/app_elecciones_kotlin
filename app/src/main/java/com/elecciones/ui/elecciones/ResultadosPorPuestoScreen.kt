package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.elecciones.ui.componentes.PieChart
import com.elecciones.ui.componentes.PieChartData
import com.elecciones.viewmodel.EleccionViewModel

/**
 * Pantalla que muestra los resultados de un puesto electoral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResultadosPorPuestoScreen(
    eleccionViewModel: EleccionViewModel,
    puestoId: Int
) {
    LaunchedEffect(puestoId) {
        eleccionViewModel.setPuestoId(puestoId)
    }

    val puestoConPostulaciones by eleccionViewModel.puestoConPostulaciones.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados - ${puestoConPostulaciones?.puesto?.nombre_puesto ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        puestoConPostulaciones?.let { puestoConPost ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Gráfico circular
                val totalVotos = puestoConPost.getTotalVotos()
                if (totalVotos > 0) {
                    val datosGrafico = puestoConPost.postulaciones.map { postulacion ->
                        PieChartData(
                            label = postulacion.getNombreCompleto(),
                            value = postulacion.postulacion.votos.toFloat(),
                            color = androidx.compose.ui.graphics.Color(
                                android.graphics.Color.parseColor(postulacion.frente.color)
                            )
                        )
                    }
                    PieChart(
                        data = datosGrafico,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                // Lista de resultados
                LazyColumn {
                    items(puestoConPost.postulaciones) { postulacion ->
                        ResultadoCard(
                            postulacion = postulacion,
                            totalVotos = totalVotos,
                            esGanador = puestoConPost.getGanador()?.postulacion?.id_postulacion == postulacion.postulacion.id_postulacion
                        )
                    }

                    item {
                        // Resumen de votos nulos y blancos
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Resumen",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Votos Válidos: ${puestoConPost.getTotalVotosValidos()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Votos Nulos: ${puestoConPost.puesto.votos_nulos}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Votos Blancos: ${puestoConPost.puesto.votos_blancos}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Total: $totalVotos",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cargando resultados...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ResultadoCard(
    postulacion: PostulacionConCandidato,
    totalVotos: Int,
    esGanador: Boolean
) {
    val porcentaje = if (totalVotos > 0) {
        (postulacion.postulacion.votos.toFloat() / totalVotos * 100)
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esGanador) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esGanador) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
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
                        color = if (esGanador) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = "Frente: ${postulacion.frente.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esGanador) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (esGanador) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "GANADOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Text(
                text = "Votos: ${postulacion.postulacion.votos} (${String.format("%.2f", porcentaje)}%)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (esGanador) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

