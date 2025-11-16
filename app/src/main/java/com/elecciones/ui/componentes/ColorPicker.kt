package com.elecciones.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Colores predefinidos para seleccionar
 */
val COLORES_PREDEFINIDOS = listOf(
    "#FF0000" to "Rojo",
    "#FF6600" to "Naranja",
    "#FFCC00" to "Amarillo",
    "#00FF00" to "Verde",
    "#00CCFF" to "Azul Cielo",
    "#0066CC" to "Azul",
    "#6600CC" to "Morado",
    "#CC00CC" to "Rosa",
    "#8B4513" to "Marrón",
    "#000000" to "Negro",
    "#FFFFFF" to "Blanco",
    "#808080" to "Gris"
)

/**
 * Componente ColorPicker simple que muestra colores predefinidos en chips.
 */
@Composable
fun ColorPicker(
    colorSeleccionado: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Color del Frente",
            style = MaterialTheme.typography.labelMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostrar el color actual
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .fillMaxWidth(0.2f),
                contentAlignment = Alignment.Center
            ) {
                val colorActual = try {
                    Color(android.graphics.Color.parseColor(colorSeleccionado))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = colorActual,
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {}
            }

            // Mostrar chips de colores predefinidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                COLORES_PREDEFINIDOS.take(6).forEach { (colorHex, nombre) ->
                    ColorChip(
                        colorHex = colorHex,
                        isSelected = colorSeleccionado.equals(colorHex, ignoreCase = true),
                        onClick = { onColorSelected(colorHex) }
                    )
                }
            }
        }
        
        // Segunda fila de colores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            COLORES_PREDEFINIDOS.drop(6).forEach { (colorHex, nombre) ->
                ColorChip(
                    colorHex = colorHex,
                    isSelected = colorSeleccionado.equals(colorHex, ignoreCase = true),
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ColorChip(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = color,
        border = if (isSelected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {}
}

