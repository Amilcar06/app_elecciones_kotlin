package com.elecciones.ui.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Eleccion
import com.elecciones.ui.theme.AppEleccionesTheme


/**
 * Composable que muestra la información de una elección en una tarjeta.
 *
 * @param eleccion El objeto Eleccion a mostrar.
 * @param onMenuClick Acción a ejecutar cuando se pulsa el menú de opciones (click principal).
 * @param onEditClick Acción a ejecutar cuando se selecciona editar (opcional).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEleccion(
    eleccion: Eleccion,
    onMenuClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    val statusColor = when (eleccion.estado) {
        "Programada" -> MaterialTheme.colorScheme.tertiary
        "Abierta" -> Color(0xFF388E3C) // Verde
        "Cerrada" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Elecciones Gestión ${eleccion.gestion}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fecha: ${eleccion.fecha_eleccion}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!eleccion.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = eleccion.descripcion ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(
                    onClick = { /* No action */ },
                    label = { Text(eleccion.estado) },
                    colors = suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.2f),
                        labelColor = statusColor
                    )
                )
            }
            Box {
                IconButton(
                    onClick = { mostrarMenu = true }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones de la elección")
                }

                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false }
                ) {
                    if (onEditClick != null && eleccion.estado != "Cerrada") {
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
                    DropdownMenuItem(
                        text = { Text("Ver detalles") },
                        onClick = {
                            mostrarMenu = false
                            onMenuClick()
                        }
                    )
                }
            }
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
            estado = "Programada"
        )
        CardEleccion(eleccion = eleccionDePrueba, onMenuClick = {})
    }
}
