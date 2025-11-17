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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.elecciones.R
import com.elecciones.data.entities.Candidato
import com.elecciones.ui.theme.AppEleccionesTheme

/**
 * Composable que muestra la información de un candidato en una tarjeta.
 *
 * @param candidato El objeto Candidato a mostrar.
 * @param onClick Acción a ejecutar al hacer clic en la tarjeta.
 * @param onEditClick Acción a ejecutar cuando se hace clic en editar (opcional).
 * @param onDeleteClick Acción a ejecutar cuando se hace clic en eliminar (opcional).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCandidato(
    candidato: Candidato,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Obtener URI de la foto
    val fotoUri = remember(candidato.foto_url) {
        if (candidato.foto_url != null && candidato.foto_url.isNotBlank()) {
            try {
                Uri.parse(candidato.foto_url)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    // Obtener iniciales para avatar por defecto
    val iniciales = remember(candidato.nombre, candidato.paterno) {
        "${candidato.nombre.take(1)}${candidato.paterno.take(1)}".uppercase()
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con foto o iniciales
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(fotoUri)
                                .build()
                        ),
                        contentDescription = "Foto de ${candidato.nombre}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Avatar con iniciales y fondo de color
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${candidato.nombre} ${candidato.paterno}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Mostramos profesión si no es nulo
                Text(
                    text = candidato.profesion ?: "Profesión no especificada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ver detalles del candidato",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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

@Preview(showBackground = true)
@Composable
fun CardCandidatoPreview() {
    AppEleccionesTheme {
        val candidatoDePrueba = Candidato(
            id_candidato = 1,
            id_frente = 1,
            nombre = "Juan",
            paterno = "Pérez",
            materno = "López",
            genero = "Masculino",
            fecha_nacimiento = "1990-01-01",
            ci = "1234567",
            profesion = "Ingeniero de Sistemas",
            direccion = "Av. Mejillones",
            correo = "ejepmlo@gmail.com",
            telefono = "77554627",
            anios_experiencia = 5
        )
        CardCandidato(candidato = candidatoDePrueba, onClick = {})
    }
}
