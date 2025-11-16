package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.elecciones.data.entities.PuestoElectoral
import com.elecciones.viewmodel.EleccionViewModel

/**
 * Pantalla que muestra los puestos electorales de una elección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PuestosElectoralesScreen(
    eleccionViewModel: EleccionViewModel,
    eleccionId: Int,
    onAddPuestoClick: () -> Unit,
    onPuestoClick: (Int) -> Unit
) {
    LaunchedEffect(eleccionId) {
        eleccionViewModel.setEleccionId(eleccionId)
    }

    val eleccion by eleccionViewModel.eleccionActual.collectAsState()
    val puestos by eleccionViewModel.puestosPorEleccion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elección ${eleccion?.gestion ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPuestoClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Puesto")
            }
        }
    ) { paddingValues ->
        if (puestos.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "No hay puestos electorales registrados.\nPulsa el botón + para añadir uno.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(puestos) { puesto ->
                    PuestoElectoralCard(
                        puesto = puesto,
                        onClick = { onPuestoClick(puesto.id_puesto) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PuestoElectoralCard(
    puesto: PuestoElectoral,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = puesto.nombre_puesto,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Estado: ${puesto.estado}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

