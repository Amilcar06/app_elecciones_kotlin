package com.elecciones.ui.componentes

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.elecciones.R
import com.elecciones.data.entities.Frente
import com.elecciones.ui.theme.AppEleccionesTheme

/**
 * Composable reutilizable que muestra la información de un frente en una tarjeta.
 *
 * @param frente El objeto Frente a mostrar.
 * @param onClick Acción a ejecutar cuando se hace clic en la tarjeta.
 * @param onEditClick Acción a ejecutar cuando se hace clic en editar.
 * @param onDeleteClick Acción a ejecutar cuando se hace clic en eliminar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFrente(
    frente: Frente,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    // Convertir el color hexadecimal del frente a un objeto Color de Compose
    val chipColor = try {
        Color(android.graphics.Color.parseColor(frente.color))
    } catch (e: IllegalArgumentException) {
        MaterialTheme.colorScheme.secondary
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = chipColor.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo del frente con borde de color
            val context = LocalContext.current
            val logoUri = remember(frente.logo_url) {
                if (frente.logo_url != null && frente.logo_url.isNotBlank()) {
                    try {
                        Uri.parse(frente.logo_url)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
            
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        spotColor = chipColor.copy(alpha = 0.3f)
                    )
            ) {
                if (logoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(logoUri)
                                .build()
                        ),
                        contentDescription = "Logo de ${frente.nombre}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder con fondo de color del frente
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(chipColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = frente.nombre.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = chipColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Información del frente
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = frente.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Chip con el color y año de fundación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = { /* No action */ },
                        label = { 
                            Text(
                                "Fundado ${frente.fecha_fundacion.take(4)}",
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = chipColor.copy(alpha = 0.15f),
                            labelColor = chipColor
                        )
                    )
                }
            }

            // Icono de flecha con mejor estilo
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ver candidatos",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            // Menú contextual si hay acciones disponibles
            if (onEditClick != null || onDeleteClick != null) {
                Box {
                    IconButton(
                        onClick = { mostrarMenu = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = mostrarMenu,
                        onDismissRequest = { mostrarMenu = false }
                    ) {
                        if (onEditClick != null) {
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
                        if (onDeleteClick != null) {
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = {
                                    mostrarMenu = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview para visualizar el componente en Android Studio
@Preview(showBackground = true)
@Composable
fun CardFrentePreview() {
    AppEleccionesTheme {
        val frenteDePrueba = Frente(
            id_frente = 1,
            nombre = "Frente Nueva Ola",
            color = "#0066CC",
            logo_url = "",
            fecha_fundacion = "1998-10-22",
            descripcion = "Un frente de prueba"
        )
        CardFrente(frente = frenteDePrueba, onClick = {})
    }
}
