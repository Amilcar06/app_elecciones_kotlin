package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Eleccion
import com.elecciones.ui.componentes.DatePickerDialog
import com.elecciones.ui.utilidades.*
import com.elecciones.viewmodel.EleccionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Pantalla de formulario para registrar o editar una elección.
 *
 * @param eleccionViewModel ViewModel para la lógica de negocio de las elecciones.
 * @param eleccionId ID de la elección a editar (null si es nueva).
 * @param onGuardarAccion Acción a ejecutar tras guardar la elección (navegar hacia atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarEleccionScreen(
    eleccionViewModel: EleccionViewModel,
    eleccionId: Int? = null,
    onGuardarAccion: () -> Unit
) {
    val esEdicion = eleccionId != null
    val scope = rememberCoroutineScope()
    val elecciones: List<Eleccion> by eleccionViewModel.todasLasElecciones.collectAsState()
    val eleccion = if (esEdicion && eleccionId != null) {
        elecciones.find { it.id_eleccion == eleccionId }
    } else {
        null
    }

    var gestion by remember(eleccion) { mutableStateOf(eleccion?.gestion?.toString() ?: "") }
    var fecha by remember(eleccion) { mutableStateOf(eleccion?.fecha_eleccion ?: "") }
    var descripcion by remember(eleccion) { mutableStateOf(eleccion?.descripcion ?: "") }
    
    // Estados para diálogos y errores
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarErrorGestion by remember { mutableStateOf(false) }
    var mensajeErrorGestion by remember { mutableStateOf("") }
    var verificarGestion by remember { mutableStateOf(false) }
    
    // Obtener año actual
    val añoActual = LocalDate.now().year
    val añosDisponibles = remember { (añoActual..añoActual + 9).toList() }
    
    // Validaciones
    val gestionNum = gestion.toIntOrNull()
    val gestionValida = gestionNum != null && gestionNum >= añoActual && gestion.length == 4
    val fechaValida = fecha.isNotBlank() && validarFormatoFecha(fecha)
    val fechaNoPasada = fecha.isNotBlank() && validarFechaMayorIgualHoy(fecha)
    
    // Validación cruzada: gestión debe coincidir con el año de fecha_eleccion
    val gestionCoincideConFecha = if (fechaValida && gestionNum != null) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        try {
            val fechaLocal = LocalDate.parse(fecha, formatter)
            fechaLocal.year == gestionNum
        } catch (e: Exception) {
            false
        }
    } else {
        true // No validar si la fecha o gestión no son válidas aún
    }
    
    // Validación del formulario completo
    val isFormularioValido = gestionValida &&
            fechaValida &&
            fechaNoPasada &&
            gestionCoincideConFecha &&
            !mostrarErrorGestion
    
    // Verificar unicidad de gestión cuando el usuario termine de escribir
    LaunchedEffect(gestion, verificarGestion) {
        if (gestionValida && verificarGestion) {
            scope.launch {
                val existe = if (esEdicion && eleccionId != null) {
                    eleccionViewModel.existeGestionExcluyendo(gestionNum!!, eleccionId)
                } else {
                    eleccionViewModel.existeGestion(gestionNum!!)
                }
                mostrarErrorGestion = existe
                mensajeErrorGestion = if (existe) {
                    "Ya existe una elección registrada para la gestión $gestionNum"
                } else {
                    ""
                }
            }
        } else if (!verificarGestion) {
            mostrarErrorGestion = false
            mensajeErrorGestion = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Elección" else "Crear Nueva Elección") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gestión (Dropdown con años disponibles)
                var gestionExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gestion,
                        onValueChange = { newValue ->
                            // Permitir solo números, máximo 4 dígitos
                            if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                gestion = newValue
                                verificarGestion = false
                                mostrarErrorGestion = false
                            }
                        },
                        label = { Text("Gestión (Año) *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { gestionExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { gestionExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar año"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = (gestion.isNotBlank() && !gestionValida) || 
                                 !gestionCoincideConFecha || 
                                 mostrarErrorGestion,
                        supportingText = {
                            when {
                                gestion.isBlank() -> Text("Campo obligatorio")
                                !gestionValida -> {
                                    if (gestionNum == null) {
                                        Text("Debe ser un número de 4 dígitos")
                                    } else if (gestionNum < añoActual) {
                                        Text("El año debe ser mayor o igual a $añoActual")
                                    } else {
                                        Text("Debe ser un número de 4 dígitos")
                                    }
                                }
                                !gestionCoincideConFecha -> {
                                    val añoFecha = if (fechaValida) {
                                        try {
                                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                            LocalDate.parse(fecha, formatter).year
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else null
                                    Text("La gestión ($gestionNum) no coincide con el año de la fecha (${añoFecha ?: "?"})")
                                }
                                mostrarErrorGestion -> Text(mensajeErrorGestion)
                                else -> {}
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = gestionExpanded,
                        onDismissRequest = { gestionExpanded = false }
                    ) {
                        añosDisponibles.forEach { año ->
                            DropdownMenuItem(
                                text = { Text(año.toString()) },
                                onClick = {
                                    gestion = año.toString()
                                    gestionExpanded = false
                                    verificarGestion = false
                                    mostrarErrorGestion = false
                                }
                            )
                        }
                    }
                }
                
                // Fecha de Elección (DatePicker)
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { },
                    label = { Text("Fecha de Elección *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    singleLine = true,
                    isError = fecha.isNotBlank() && (!fechaValida || !fechaNoPasada || !gestionCoincideConFecha),
                    supportingText = {
                        when {
                            fecha.isBlank() -> Text("Campo obligatorio")
                            !fechaValida -> Text("Formato inválido. Use YYYY-MM-DD")
                            !fechaNoPasada -> Text("La fecha no puede ser anterior a hoy")
                            !gestionCoincideConFecha -> {
                                val añoFecha = if (fechaValida) {
                                    try {
                                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        LocalDate.parse(fecha, formatter).year
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else null
                                Text("El año de la fecha ($añoFecha) no coincide con la gestión ($gestionNum)")
                            }
                            else -> {}
                        }
                    }
                )
                
                // Descripción (Opcional)
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (Opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }
            
            // Botones de acción - siempre visibles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Verificar unicidad antes de guardar
                        scope.launch {
                            verificarGestion = true
                            
                            val existeGestion = if (esEdicion && eleccionId != null) {
                                eleccionViewModel.existeGestionExcluyendo(gestionNum!!, eleccionId)
                            } else {
                                eleccionViewModel.existeGestion(gestionNum!!)
                            }
                            
                            mostrarErrorGestion = existeGestion
                            mensajeErrorGestion = if (existeGestion) {
                                "Ya existe una elección registrada para la gestión $gestionNum"
                            } else {
                                ""
                            }
                            
                            if (!existeGestion && isFormularioValido) {
                                if (esEdicion && eleccion != null) {
                                    // Actualizar elección existente
                                    val eleccionActualizada = eleccion.copy(
                                        gestion = gestionNum!!,
                                        fecha_eleccion = fecha,
                                        descripcion = descripcion.takeIf { it.isNotBlank() }
                                    )
                                    eleccionViewModel.actualizarEleccion(eleccionActualizada)
                                } else {
                                    // Crear nueva elección
                                    val nuevaEleccion = Eleccion(
                                        gestion = gestionNum!!,
                                        fecha_eleccion = fecha,
                                        estado = "Programada", // Valor por defecto
                                        descripcion = descripcion.takeIf { it.isNotBlank() }
                                    )
                                    eleccionViewModel.insertarEleccion(nuevaEleccion)
                                }
                                onGuardarAccion()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFormularioValido
                ) {
                    Text(if (esEdicion) "ACTUALIZAR" else "CREAR")
                }
                OutlinedButton(
                    onClick = onGuardarAccion,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CANCELAR")
                }
            }
        }
        
        // DatePicker Dialog
        if (mostrarDatePicker) {
            DatePickerDialog(
                fechaInicial = fecha,
                onDateSelected = { fechaSeleccionada ->
                    fecha = fechaSeleccionada
                },
                onDismiss = { mostrarDatePicker = false }
            )
        }
    }
}
