package com.elecciones.ui.candidatos

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.elecciones.ui.componentes.CardCandidato
import com.elecciones.ui.componentes.ConfirmacionEliminarDialog
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.FrenteViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Pantalla que muestra la lista de candidatos de un frente específico.
 *
 * @param candidatoViewModel ViewModel para la lógica de candidatos.
 * @param frenteViewModel ViewModel para obtener información del frente.
 * @param frenteId ID del frente cuyos candidatos se mostrarán.
 * @param onAddCandidatoClick Acción para navegar a la pantalla de registro de candidato.
 * @param onCandidatoClick Acción al hacer clic en un candidato.
 * @param onEditCandidatoClick Acción para navegar a la pantalla de edición de candidato.
 * @param onDeleteCandidatoClick Acción a ejecutar después de eliminar un candidato.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CandidatosScreen(
    candidatoViewModel: CandidatoViewModel,
    frenteViewModel: FrenteViewModel,
    frenteId: Int,
    onAddCandidatoClick: () -> Unit,
    onCandidatoClick: (Int) -> Unit,
    onEditCandidatoClick: (Int) -> Unit = {},
    onDeleteCandidatoClick: () -> Unit = {}
) {
    // Usamos LaunchedEffect para indicar al ViewModel qué frente cargar.
    // Se ejecutará solo cuando frenteId cambie.
    LaunchedEffect(frenteId) {
        candidatoViewModel.setFrenteId(frenteId)
    }

    // Observamos la lista de candidatos del StateFlow del ViewModel.
    val candidatos by candidatoViewModel.candidatos.collectAsState()
    
    // Obtener información del frente
    val frentes by frenteViewModel.todosLosFrentes.collectAsState()
    val frente = frentes.find { it.id_frente == frenteId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = frente?.nombre ?: "Candidatos del Frente",
                            fontWeight = FontWeight.Bold
                        )
                        if (frente != null) {
                            Text(
                                text = "Año ${frente.fecha_fundacion.take(4)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCandidatoClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Candidato")
            }
        }
    ) { paddingValues ->
        var candidatoAEliminar by remember { mutableStateOf<com.elecciones.data.entities.Candidato?>(null) }
        
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(candidatos) { candidato ->
                CardCandidato(
                    candidato = candidato,
                    onClick = { onCandidatoClick(candidato.id_candidato) },
                    onEditClick = { onEditCandidatoClick(candidato.id_candidato) },
                    onDeleteClick = { candidatoAEliminar = candidato }
                )
            }
        }
        
        // Diálogo de confirmación para eliminar
        candidatoAEliminar?.let { candidato ->
            ConfirmacionEliminarDialog(
                titulo = "Eliminar Candidato",
                mensaje = "¿Está seguro de que desea eliminar a ${candidato.nombre} ${candidato.paterno}?",
                onConfirmar = {
                    candidatoViewModel.eliminarCandidato(candidato)
                    candidatoAEliminar = null
                    onDeleteCandidatoClick()
                },
                onCancelar = { candidatoAEliminar = null }
            )
        }
    }
}
