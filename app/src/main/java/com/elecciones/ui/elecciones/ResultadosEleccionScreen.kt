package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.PuestoConPostulaciones
import com.elecciones.viewmodel.EleccionViewModel
import kotlinx.coroutines.flow.first

/**
 * Pantalla que muestra los resultados de una elección finalizada.
 * Muestra todos los puestos con sus ganadores de forma resumida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResultadosEleccionScreen(
    eleccionViewModel: EleccionViewModel,
    eleccionId: Int,
    onVerDetallePuesto: (Int) -> Unit
) {
    LaunchedEffect(eleccionId) {
        eleccionViewModel.setEleccionId(eleccionId)
    }

    val eleccion by eleccionViewModel.eleccionActual.collectAsState()
    val puestos by eleccionViewModel.puestosPorEleccion.collectAsState()
    
    // Cargar datos completos de cada puesto con sus postulaciones
    var puestosConResultados by remember { mutableStateOf<List<PuestoConPostulaciones>>(emptyList()) }
    
    LaunchedEffect(puestos.map { it.id_puesto }) {
        val resultados = mutableListOf<PuestoConPostulaciones>()
        puestos.forEach { puesto ->
            try {
                eleccionViewModel.setPuestoId(puesto.id_puesto)
                kotlinx.coroutines.delay(200)
                val puestoCompleto = eleccionViewModel.puestoConPostulaciones.first()
                puestoCompleto?.let { resultados.add(it) }
            } catch (e: Exception) {
                // Ignorar errores
            }
        }
        puestosConResultados = resultados
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados - Gestión ${eleccion?.gestion ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (puestosConResultados.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Cargando resultados...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(puestosConResultados) { puestoConPost ->
                    PuestoResultadoCard(
                        puestoConPostulaciones = puestoConPost,
                        onVerDetalle = { onVerDetallePuesto(puestoConPost.puesto.id_puesto) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PuestoResultadoCard(
    puestoConPostulaciones: PuestoConPostulaciones,
    onVerDetalle: () -> Unit
) {
    val ganador = puestoConPostulaciones.getGanador()
    val totalVotos = puestoConPostulaciones.getTotalVotos()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onVerDetalle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título del puesto
            Text(
                text = puestoConPostulaciones.puesto.nombre_puesto,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Ganador destacado
            if (ganador != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Ganador",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFD700) // Dorado
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ganador.getNombreCompleto(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Frente: ${ganador.frente.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "${ganador.postulacion.votos} votos (${if (totalVotos > 0) String.format("%.1f", ganador.postulacion.votos.toFloat() / totalVotos * 100) else "0"}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Si no hay ganador (empate o sin votos)
                Text(
                    text = if (puestoConPostulaciones.postulaciones.isEmpty()) {
                        "Sin candidatos postulados"
                    } else {
                        "Empate o sin votos registrados"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información adicional
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Candidatos",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "${puestoConPostulaciones.postulaciones.size} candidato${if (puestoConPostulaciones.postulaciones.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "Total votos: $totalVotos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón "Ver más" - más discreto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onVerDetalle)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver más detalles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Ver más",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

