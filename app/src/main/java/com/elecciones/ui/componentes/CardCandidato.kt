package com.elecciones.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elecciones.R
import com.elecciones.data.entities.Candidato
import com.elecciones.ui.theme.AppEleccionesTheme

/**
 * Composable que muestra la información de un candidato en una tarjeta.
 *
 * @param candidato El objeto Candidato a mostrar.
 * @param onClick Acción a ejecutar al hacer clic en la tarjeta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCandidato(
    candidato: Candidato,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                contentDescription = "Foto de ${candidato.nombre}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${candidato.nombre} ${candidato.paterno}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Mostramos profesión si no es nulo
                Text(
                    text = candidato.profesion ?: "Profesión no especificada",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ver detalles del candidato",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
