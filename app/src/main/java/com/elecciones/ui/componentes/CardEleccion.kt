package com.elecciones.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Eleccion
import com.elecciones.ui.theme.AppEleccionesTheme

/**
 * Datos de estadísticas de una elección para mostrar en la tarjeta.
 */
data class EleccionStats(
    val totalPuestos: Int = 0,
    val puestosAbiertos: Int = 0,
    val puestosCerrados: Int = 0,
    val totalCandidatos: Int = 0
)

/**
 * Composable que muestra la información de una elección en una tarjeta mejorada.
 *
 * @param eleccion El objeto Eleccion a mostrar.
 * @param stats Estadísticas de la elección (puestos, candidatos, etc.)
 * @param onClick Acción a ejecutar cuando se hace click en la tarjeta (ir a puestos electorales).
 * @param onEditClick Acción a ejecutar cuando se selecciona editar (opcional).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEleccion(
    eleccion: Eleccion,
    stats: EleccionStats = EleccionStats(),
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    
    val statusColor = when (eleccion.estado) {
        "Programada" -> MaterialTheme.colorScheme.tertiary
        "En curso", "Abierto" -> Color(0xFF388E3C) // Verde
        "Finalizado", "Cerrado" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fila superior: Título y menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Elecciones Gestión ${eleccion.gestion}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fecha: ${eleccion.fecha_eleccion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Menú de 3 puntitos (solo para acciones secundarias)
                Box {
                    IconButton(
                        onClick = { mostrarMenu = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opciones de la elección",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = mostrarMenu,
                        onDismissRequest = { mostrarMenu = false }
                    ) {
                        if (onEditClick != null && eleccion.estado != "Finalizado" && eleccion.estado != "Cerrado") {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    mostrarMenu = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar"
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Descripción si existe
            if (!eleccion.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = eleccion.descripcion ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estadísticas de la elección
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Puestos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Puestos",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${stats.totalPuestos} puesto${if (stats.totalPuestos != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Candidatos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Candidatos",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${stats.totalCandidatos} candidato${if (stats.totalCandidatos != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Progreso de puestos (si hay puestos)
            if (stats.totalPuestos > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progreso: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.puestosCerrados}/${stats.totalPuestos} puestos cerrados",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (stats.puestosCerrados == stats.totalPuestos) {
                            Color(0xFF388E3C) // Verde cuando está completo
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estado de la elección
            SuggestionChip(
                onClick = { /* No action */ },
                label = { Text(eleccion.estado) },
                colors = suggestionChipColors(
                    containerColor = statusColor.copy(alpha = 0.2f),
                    labelColor = statusColor
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardEleccionPreview() {
    AppEleccionesTheme {
        val eleccionDePrueba = Eleccion(
            id_eleccion = 1,
            gestion = 2025,
            fecha_eleccion = "2025-10-26",
            estado = "Programada",
            descripcion = "Elecciones para la carrera de Informática"
        )
        CardEleccion(
            eleccion = eleccionDePrueba,
            stats = EleccionStats(
                totalPuestos = 5,
                puestosAbiertos = 3,
                puestosCerrados = 2,
                totalCandidatos = 12
            ),
            onClick = {}
        )
    }
}
