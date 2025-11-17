package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Eleccion
import com.elecciones.ui.componentes.CardEleccion
import com.elecciones.ui.componentes.EleccionStats
import com.elecciones.viewmodel.EleccionViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EleccionesScreen(
    eleccionViewModel: EleccionViewModel,
    onAddEleccionClick: () -> Unit,
    onEleccionClick: (Int) -> Unit,
    onEditEleccionClick: ((Int) -> Unit)? = null
) {
    val elecciones by eleccionViewModel.todasLasElecciones.collectAsState()
    
    // Mapa para almacenar estadísticas de cada elección
    var eleccionesStats by remember { mutableStateOf<Map<Int, EleccionStats>>(emptyMap()) }
    
    // Cargar estadísticas para cada elección
    LaunchedEffect(elecciones.map { it.id_eleccion }) {
        val statsMap = mutableMapOf<Int, EleccionStats>()
        elecciones.forEach { eleccion ->
            try {
                // Obtener puestos de la elección usando el ViewModel
                eleccionViewModel.setEleccionId(eleccion.id_eleccion)
                kotlinx.coroutines.delay(300) // Esperar a que se cargue el StateFlow
                val puestosEleccion = eleccionViewModel.puestosPorEleccion.first()
                
                val totalPuestos = puestosEleccion.size
                val puestosAbiertos = puestosEleccion.count { it.estado == "Abierto" }
                val puestosCerrados = puestosEleccion.count { it.estado == "Cerrado" }
                
                // Contar postulaciones (candidatos) de todos los puestos
                var totalCandidatos = 0
                puestosEleccion.forEach { puesto ->
                    eleccionViewModel.setPuestoId(puesto.id_puesto)
                    kotlinx.coroutines.delay(150)
                    val postulaciones = eleccionViewModel.postulacionesPorPuesto.first()
                    totalCandidatos += postulaciones.size
                }
                
                statsMap[eleccion.id_eleccion] = EleccionStats(
                    totalPuestos = totalPuestos,
                    puestosAbiertos = puestosAbiertos,
                    puestosCerrados = puestosCerrados,
                    totalCandidatos = totalCandidatos
                )
            } catch (e: Exception) {
                // Si hay error, usar stats vacías
                statsMap[eleccion.id_eleccion] = EleccionStats()
            }
        }
        eleccionesStats = statsMap
    }
    
    // Separar elecciones por estado
    val eleccionesEnCurso = elecciones.filter { 
        it.estado == "Programada" || it.estado == "En curso" || it.estado == "Abierto"
    }
    val eleccionesFinalizadas = elecciones.filter { 
        it.estado == "Finalizado" || it.estado == "Cerrado"
    }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("En Curso", "Historial")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Elecciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEleccionClick) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nueva Elección")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // TabRow para cambiar entre "En Curso" e "Historial"
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Contenido según la pestaña seleccionada
            when (selectedTabIndex) {
                0 -> {
                    // Pestaña "En Curso"
                    if (eleccionesEnCurso.isEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "No hay elecciones en curso",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(eleccionesEnCurso) { eleccion ->
                                CardEleccion(
                                    eleccion = eleccion,
                                    stats = eleccionesStats[eleccion.id_eleccion] ?: EleccionStats(),
                                    onClick = { onEleccionClick(eleccion.id_eleccion) },
                                    onEditClick = if (onEditEleccionClick != null) {
                                        { onEditEleccionClick!!(eleccion.id_eleccion) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Pestaña "Historial"
                    if (eleccionesFinalizadas.isEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "No hay elecciones finalizadas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(eleccionesFinalizadas) { eleccion ->
                                CardEleccion(
                                    eleccion = eleccion,
                                    stats = eleccionesStats[eleccion.id_eleccion] ?: EleccionStats(),
                                    onClick = { onEleccionClick(eleccion.id_eleccion) },
                                    onEditClick = if (onEditEleccionClick != null) {
                                        { onEditEleccionClick!!(eleccion.id_eleccion) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
