package com.elecciones.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Datos para un segmento del gráfico circular
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * Componente que dibuja un gráfico circular (PieChart) con los datos proporcionados.
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.minDimension / 2f) * 0.75f

        var startAngle = -90f // Comenzar desde arriba

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f

            // Dibujar el arco del segmento como un sector circular
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(
                    centerX - radius,
                    centerY - radius
                ),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }
    }
}

