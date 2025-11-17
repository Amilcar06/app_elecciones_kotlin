package com.elecciones.ui.componentes

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Componente para seleccionar y mostrar una imagen.
 * Guarda la imagen en el almacenamiento interno de la app.
 */
@Composable
fun ImagePicker(
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Guardar la imagen en el almacenamiento interno
            val savedUri = saveImageToInternalStorage(context, it)
            savedUri?.let { saved ->
                onImageSelected(saved)
            }
        }
    }

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable { imagePickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            // Mostrar la imagen seleccionada
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imageUri)
                        .build()
                ),
                contentDescription = "Imagen seleccionada",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Mostrar icono de agregar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Seleccionar imagen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agregar Foto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Guarda una imagen desde un URI externo al almacenamiento interno de la app.
 * Retorna el URI de la imagen guardada o null si falla.
 * Guarda el path absoluto como string para poder reconstruir el URI después.
 */
private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            // Crear directorio de imágenes si no existe
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // Crear archivo único
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(imagesDir, fileName)
            
            // Copiar el archivo
            val outputStream = FileOutputStream(file)
            it.copyTo(outputStream)
            outputStream.close()
            it.close()
            
            // Retornar URI usando el path absoluto
            // Guardamos el path completo para poder reconstruir el URI después
            Uri.parse("file://${file.absolutePath}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

