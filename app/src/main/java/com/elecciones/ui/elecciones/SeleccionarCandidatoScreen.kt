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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Candidato
import com.elecciones.data.entities.Frente
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.EleccionViewModel
import com.elecciones.viewmodel.FrenteViewModel

/**
 * Pantalla para seleccionar un candidato para postular a un puesto.
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

    // Agrupar candidatos por frente
    val candidatosPorFrente = todosLosCandidatos.groupBy { it.id_frente }
        .mapKeys { (frenteId, _) ->
            todosLosFrentes.find { it.id_frente == frenteId }
        }
        .filterKeys { it != null }
        .mapKeys { it.key!! }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Candidato") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
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
                        onClick = {
                            // Crear la postulación
                            val postulacion = com.elecciones.data.entities.Postulacion(
                                id_puesto = puestoId,
                                id_candidato = candidato.id_candidato,
                                votos = 0
                            )
                            eleccionViewModel.insertarPostulacion(postulacion)
                            onCandidatoSeleccionado(candidato.id_candidato)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidatoSeleccionableCard(
    candidato: Candidato,
    frente: Frente,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${candidato.nombre} ${candidato.paterno} ${candidato.materno}",
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

