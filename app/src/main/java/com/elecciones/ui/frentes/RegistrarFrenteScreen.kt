package com.elecciones.ui.frentes

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.runtime.remember
import com.elecciones.data.entities.Frente
import com.elecciones.ui.componentes.ColorPicker
import com.elecciones.ui.componentes.DatePickerDialog
import com.elecciones.ui.componentes.ImagePicker
import com.elecciones.ui.theme.AppEleccionesTheme
import com.elecciones.ui.utilidades.validarColorHex
import com.elecciones.ui.utilidades.validarFormatoFecha
import com.elecciones.viewmodel.FrenteViewModel

/**
 * Pantalla para registrar o editar un frente político.
 *
 * @param frenteViewModel ViewModel para interactuar con la lógica de frentes.
 * @param frenteId ID del frente a editar (null si es nuevo).
 * @param onGuardarAccion Acción a ejecutar cuando se guarda el frente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarFrenteScreen(
    frenteViewModel: FrenteViewModel,
    frenteId: Int? = null,
    onGuardarAccion: () -> Unit
) {
    val esEdicion = frenteId != null
    val frenteActual by frenteViewModel.todosLosFrentes.collectAsState()
    val frente = if (esEdicion) frenteActual.find { it.id_frente == frenteId } else null

    // Estados para los campos del formulario
    var nombre by remember(frente) { mutableStateOf(frente?.nombre ?: "") }
    var color by remember(frente) { mutableStateOf(frente?.color ?: "#0066CC") }
    var fechaFundacion by remember(frente) { mutableStateOf(frente?.fecha_fundacion ?: "") }
    var descripcion by remember(frente) { mutableStateOf(frente?.descripcion ?: "") }
    
    // Estado para la imagen del logo
    val logoUri = remember(frente) {
        if (frente?.logo_url != null && frente.logo_url.isNotBlank()) {
            try {
                Uri.parse(frente.logo_url)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    var logoUriState by remember(frente) { mutableStateOf<Uri?>(logoUri) }

    // Estados para diálogos
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarErrorFecha by remember { mutableStateOf(false) }
    var mostrarErrorColor by remember { mutableStateOf(false) }

    // Validaciones
    val fechaValida = fechaFundacion.isBlank() || validarFormatoFecha(fechaFundacion)
    val colorValido = validarColorHex(color)
    val isFormularioValido = nombre.isNotBlank() && 
            fechaFundacion.isNotBlank() && 
            fechaValida && 
            colorValido

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Frente" else "Registrar Nuevo Frente") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Campo de texto para el nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Frente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombre.isBlank(),
                supportingText = if (nombre.isBlank()) {
                    { Text("El nombre es obligatorio") }
                } else null
            )

            // Selector de fecha con DatePicker
            OutlinedTextField(
                value = fechaFundacion,
                onValueChange = { 
                    fechaFundacion = it
                    mostrarErrorFecha = false
                },
                label = { Text("Fecha de Fundación") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                },
                isError = mostrarErrorFecha || (fechaFundacion.isNotBlank() && !fechaValida),
                supportingText = if (mostrarErrorFecha || (fechaFundacion.isNotBlank() && !fechaValida)) {
                    { Text("Formato de fecha inválido. Use YYYY-MM-DD") }
                } else null
            )

            // ColorPicker
            ColorPicker(
                colorSeleccionado = color,
                onColorSelected = { 
                    color = it
                    mostrarErrorColor = false
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Campo de texto para color hexadecimal (permite entrada manual)
            OutlinedTextField(
                value = color,
                onValueChange = { 
                    color = it
                    mostrarErrorColor = false
                },
                label = { Text("Color Hexadecimal (Ej: #0066CC)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = mostrarErrorColor || !colorValido,
                supportingText = if (mostrarErrorColor || !colorValido) {
                    { Text("Formato de color inválido. Use #RRGGBB") }
                } else null
            )

            // Selector de imagen para el logo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Logo del Frente",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ImagePicker(
                    imageUri = logoUriState,
                    onImageSelected = { uri ->
                        logoUriState = uri
                    }
                )
            }

            // Campo de texto para la descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            }
            
            // Botones de acción - siempre visibles en la parte inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Validar antes de guardar
                        mostrarErrorFecha = fechaFundacion.isNotBlank() && !fechaValida
                        mostrarErrorColor = !colorValido
                        
                        if (!mostrarErrorFecha && !mostrarErrorColor && isFormularioValido) {
                            if (esEdicion && frente != null) {
                                // Actualizar frente existente
                                val frenteActualizado = frente.copy(
                                    nombre = nombre,
                                    color = color,
                                    logo_url = logoUriState?.toString(), // Guardar URI como string
                                    fecha_fundacion = fechaFundacion,
                                    descripcion = descripcion.takeIf { it.isNotBlank() }
                                )
                                frenteViewModel.actualizarFrente(frenteActualizado)
                            } else {
                                // Crear nuevo frente
                                val nuevoFrente = Frente(
                                    nombre = nombre,
                                    color = color,
                                    logo_url = logoUriState?.toString(), // Guardar URI como string
                                    fecha_fundacion = fechaFundacion,
                                    descripcion = descripcion.takeIf { it.isNotBlank() }
                                )
                                frenteViewModel.insertarFrente(nuevoFrente)
                            }
                            onGuardarAccion() // Navegar hacia atrás
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFormularioValido
                ) {
                    Text(if (esEdicion) "ACTUALIZAR" else "CREAR")
                }
                OutlinedButton(
                    onClick = onGuardarAccion, // Usamos la misma acción para "cancelar"
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CANCELAR")
                }
            }
        }

        // DatePicker Dialog
        if (mostrarDatePicker) {
            DatePickerDialog(
                fechaInicial = fechaFundacion,
                onDateSelected = { fecha ->
                    fechaFundacion = fecha
                    mostrarErrorFecha = false
                },
                onDismiss = { mostrarDatePicker = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrarFrenteScreenPreview() {
    AppEleccionesTheme {
        // Para el preview, no podemos usar el ViewModel real.
        // Se pasa un ViewModel nulo y una acción vacía.
        val fakeViewModel: FrenteViewModel? = null
        if (fakeViewModel != null) {
            RegistrarFrenteScreen(frenteViewModel = fakeViewModel, onGuardarAccion = {})
        }
    }
}
