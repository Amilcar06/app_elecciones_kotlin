package com.elecciones.ui.candidatos

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elecciones.ui.componentes.ConfirmacionEliminarDialog
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.FrenteViewModel

/**
 * Pantalla que muestra los detalles completos de un candidato.
 *
 * @param candidatoViewModel ViewModel para la lógica de candidatos.
 * @param frenteViewModel ViewModel para obtener información del frente.
 * @param candidatoId ID del candidato a mostrar.
 * @param onEditarClick Acción para navegar a la pantalla de edición.
 * @param onEliminarClick Acción a ejecutar después de eliminar el candidato.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetalleCandidatoScreen(
    candidatoViewModel: CandidatoViewModel,
    frenteViewModel: FrenteViewModel,
    candidatoId: Int,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    val candidatos by candidatoViewModel.todosLosCandidatos.collectAsState()
    val candidato = candidatos.find { it.id_candidato == candidatoId }
    
    val frentes by frenteViewModel.todosLosFrentes.collectAsState()
    val frente = candidato?.let { frentes.find { it.id_frente == candidato.id_frente } }
    
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Candidato") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onEditarClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar candidato",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { mostrarDialogoEliminar = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar candidato",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (candidato == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Candidato no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información del frente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Frente Asociado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = frente?.nombre ?: "Desconocido",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Divider()
                
                // Información personal
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                InfoRow("Nombre completo", "${candidato.nombre} ${candidato.paterno} ${candidato.materno ?: ""}".trim())
                InfoRow("Género", candidato.genero)
                InfoRow("Fecha de Nacimiento", candidato.fecha_nacimiento)
                InfoRow("Cédula de Identidad", candidato.ci)
                
                if (candidato.direccion != null) {
                    InfoRow("Dirección", candidato.direccion)
                }
                
                Divider()
                
                // Información de contacto
                Text(
                    text = "Información de Contacto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                InfoRow("Correo Electrónico", candidato.correo ?: "No especificado")
                InfoRow("Teléfono", candidato.telefono ?: "No especificado")
                
                Divider()
                
                // Información profesional
                Text(
                    text = "Información Profesional",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                InfoRow("Profesión", candidato.profesion ?: "No especificada")
                InfoRow("Años de Experiencia", candidato.anios_experiencia?.toString() ?: "0")
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar && candidato != null) {
        ConfirmacionEliminarDialog(
            titulo = "Eliminar Candidato",
            mensaje = "¿Está seguro de que desea eliminar a ${candidato.nombre} ${candidato.paterno}?",
            onConfirmar = {
                candidatoViewModel.eliminarCandidato(candidato)
                mostrarDialogoEliminar = false
                onEliminarClick()
            },
            onCancelar = { mostrarDialogoEliminar = false }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

