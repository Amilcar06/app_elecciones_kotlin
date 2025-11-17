package com.elecciones.ui.componentes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Diálogo de confirmación para eliminar un frente.
 * Muestra advertencias si el frente tiene candidatos o está participando en elecciones.
 */
@Composable
fun ConfirmacionEliminarDialog(
    titulo: String,
    mensaje: String,
    tieneCandidatos: Boolean = false,
    tieneElecciones: Boolean = false,
    textoConfirmar: String = "ELIMINAR",
    textoCancelar: String = "CANCELAR",
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val mensajeCompleto = buildString {
        append(mensaje)
        if (tieneCandidatos) {
            append("\n\n⚠️ Este frente tiene candidatos asociados. Se eliminarán también.")
        }
        if (tieneElecciones) {
            append("\n\n⚠️ Este frente está participando en elecciones activas.")
        }
        if (tieneCandidatos || tieneElecciones) {
            append("\n\n¿Desea continuar?")
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensajeCompleto) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(textoCancelar)
            }
        }
    )
}

