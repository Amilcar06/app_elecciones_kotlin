package com.elecciones.ui.candidatos

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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.net.Uri
import com.elecciones.data.entities.Candidato
import com.elecciones.ui.componentes.DatePickerDialog
import com.elecciones.ui.componentes.ImagePicker
import com.elecciones.ui.utilidades.*
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.FrenteViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de formulario para registrar o editar un candidato.
 *
 * @param candidatoViewModel ViewModel para manejar la lógica de negocio de los candidatos.
 * @param frenteViewModel ViewModel para obtener información del frente.
 * @param frenteId El ID del frente al que pertenecerá el candidato.
 * @param candidatoId ID del candidato a editar (null si es nuevo).
 * @param onGuardarAccion Acción a ejecutar tras guardar el candidato (navegar hacia atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarCandidatoScreen(
    candidatoViewModel: CandidatoViewModel,
    frenteViewModel: FrenteViewModel,
    frenteId: Int,
    candidatoId: Int? = null,
    onGuardarAccion: () -> Unit
) {
    val esEdicion = candidatoId != null
    val scope = rememberCoroutineScope()
    
    // Obtener información del frente
    val frentes by frenteViewModel.todosLosFrentes.collectAsState()
    val frente = frentes.find { it.id_frente == frenteId }
    
    // Obtener candidato si es edición
    val candidatoFlow = if (esEdicion) candidatoViewModel.todosLosCandidatos else null
    val candidato = if (esEdicion) {
        candidatoFlow?.collectAsState()?.value?.find { it.id_candidato == candidatoId }
    } else null
    
    // Estados para cada campo del formulario
    var nombre by remember(candidato) { mutableStateOf(candidato?.nombre ?: "") }
    var paterno by remember(candidato) { mutableStateOf(candidato?.paterno ?: "") }
    var materno by remember(candidato) { mutableStateOf(candidato?.materno ?: "") }
    var ci by remember(candidato) { mutableStateOf(candidato?.ci ?: "") }
    var fechaNacimiento by remember(candidato) { mutableStateOf(candidato?.fecha_nacimiento ?: "") }
    var genero by remember(candidato) { mutableStateOf(candidato?.genero ?: "") }
    var direccion by remember(candidato) { mutableStateOf(candidato?.direccion ?: "") }
    var correo by remember(candidato) { mutableStateOf(candidato?.correo ?: "") }
    var telefono by remember(candidato) { mutableStateOf(candidato?.telefono ?: "") }
    var profesion by remember(candidato) { mutableStateOf(candidato?.profesion ?: "") }
    var aniosExperiencia by remember(candidato) { 
        mutableStateOf(candidato?.anios_experiencia?.toString() ?: "0") 
    }
    
    // Estado para la foto del candidato
    var fotoUriState by remember(candidato) {
        mutableStateOf<android.net.Uri?>(
            candidato?.foto_url?.let { 
                try { android.net.Uri.parse(it) } catch (e: Exception) { null }
            }
        )
    }
    
    // Estados para diálogos y errores
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarErrorCI by remember { mutableStateOf(false) }
    var mostrarErrorCorreo by remember { mutableStateOf(false) }
    var mensajeErrorCI by remember { mutableStateOf("") }
    var mensajeErrorCorreo by remember { mutableStateOf("") }
    var verificarCI by remember { mutableStateOf(false) }
    var verificarCorreo by remember { mutableStateOf(false) }
    
    // Validaciones en tiempo real
    val nombreValido = nombre.isBlank() || validarNombre(nombre)
    val paternoValido = paterno.isBlank() || validarNombre(paterno)
    val maternoValido = materno.isBlank() || validarNombre(materno)
    val ciValido = ci.isBlank() || validarCI(ci)
    val fechaValida = fechaNacimiento.isBlank() || validarFormatoFecha(fechaNacimiento)
    val fechaNoFutura = fechaNacimiento.isBlank() || validarFechaNoFutura(fechaNacimiento)
    val edadValida = fechaNacimiento.isBlank() || validarEdad(fechaNacimiento)
    val generoValido = genero.isNotBlank() && genero in listOf("Masculino", "Femenino")
    val direccionValida = direccion.isBlank() || direccion.length <= 200
    val emailValido = correo.isBlank() || validarEmail(correo)
    val telefonoValido = telefono.isBlank() || validarTelefono(telefono)
    val profesionValida = profesion.isBlank() || profesion.length <= 100
    val aniosExperienciaNum = aniosExperiencia.toIntOrNull() ?: 0
    val aniosExperienciaValido = aniosExperienciaNum >= 0
    val aniosExperienciaCoherente = if (fechaNacimiento.isNotBlank() && validarFormatoFecha(fechaNacimiento)) {
        validarAniosExperiencia(fechaNacimiento, aniosExperienciaNum)
    } else true
    
    // Validación del formulario completo
    val isFormularioValido = nombre.isNotBlank() && validarNombre(nombre) &&
            paterno.isNotBlank() && validarNombre(paterno) &&
            (materno.isBlank() || validarNombre(materno)) &&
            ci.isNotBlank() && validarCI(ci) &&
            fechaNacimiento.isNotBlank() && validarFormatoFecha(fechaNacimiento) &&
            fechaNoFutura && edadValida &&
            generoValido &&
            (direccion.isBlank() || direccionValida) &&
            correo.isNotBlank() && validarEmail(correo) &&
            telefono.isNotBlank() && validarTelefono(telefono) &&
            (profesion.isBlank() || profesionValida) &&
            aniosExperienciaValido && aniosExperienciaCoherente &&
            !mostrarErrorCI && !mostrarErrorCorreo
    
    // Verificar unicidad de CI cuando el usuario termine de escribir (solo si verificarCI es true)
    LaunchedEffect(ci, verificarCI) {
        if (ci.isNotBlank() && validarCI(ci) && verificarCI) {
            scope.launch {
                val existe = if (esEdicion && candidatoId != null) {
                    candidatoViewModel.existeCIExcluyendo(ci, candidatoId)
                } else {
                    candidatoViewModel.existeCI(ci)
                }
                mostrarErrorCI = existe
                mensajeErrorCI = if (existe) "El CI ya se encuentra registrado" else ""
            }
        } else if (!verificarCI) {
            // Solo limpiar errores si no estamos verificando
            mostrarErrorCI = false
            mensajeErrorCI = ""
        }
    }
    
    // Verificar unicidad de correo cuando el usuario termine de escribir (solo si verificarCorreo es true)
    LaunchedEffect(correo, verificarCorreo) {
        if (correo.isNotBlank() && validarEmail(correo) && verificarCorreo) {
            scope.launch {
                val existe = if (esEdicion && candidatoId != null) {
                    candidatoViewModel.existeCorreoExcluyendo(correo, candidatoId)
                } else {
                    candidatoViewModel.existeCorreo(correo)
                }
                mostrarErrorCorreo = existe
                mensajeErrorCorreo = if (existe) "El correo ya se encuentra registrado" else ""
            }
        } else if (!verificarCorreo) {
            // Solo limpiar errores si no estamos verificando
            mostrarErrorCorreo = false
            mensajeErrorCorreo = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Candidato" else "Registrar Candidato") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto del candidato
                ImagePicker(
                    imageUri = fotoUriState,
                    onImageSelected = { uri -> fotoUriState = uri },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Información del frente
                Text(
                    text = "Frente Asociado: ${frente?.nombre ?: "Desconocido"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Divider()
                
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre(s) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nombre.isNotBlank() && !nombreValido,
                    supportingText = when {
                        nombre.isBlank() -> { { Text("Campo obligatorio") } }
                        !nombreValido -> { { Text("Solo letras, acentos, ñ y espacios. Entre 2 y 50 caracteres.") } }
                        else -> null
                    }
                )
                
                // Apellido Paterno
                OutlinedTextField(
                    value = paterno,
                    onValueChange = { paterno = it },
                    label = { Text("Apellido Paterno *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = paterno.isNotBlank() && !paternoValido,
                    supportingText = when {
                        paterno.isBlank() -> { { Text("Campo obligatorio") } }
                        !paternoValido -> { { Text("Solo letras, acentos, ñ y espacios. Entre 2 y 50 caracteres.") } }
                        else -> null
                    }
                )
                
                // Apellido Materno (Opcional)
                OutlinedTextField(
                    value = materno,
                    onValueChange = { materno = it },
                    label = { Text("Apellido Materno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = materno.isNotBlank() && !maternoValido,
                    supportingText = if (materno.isNotBlank() && !maternoValido) {
                        { Text("Solo letras, acentos, ñ y espacios. Entre 2 y 50 caracteres.") }
                    } else null
                )
                
                // Género (Dropdown)
                var generoExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = genero,
                        onValueChange = { },
                        label = { Text("Género *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { generoExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { generoExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar género"
                                )
                            }
                        },
                        isError = genero.isNotBlank() && !generoValido,
                        supportingText = when {
                            genero.isBlank() -> { { Text("Campo obligatorio") } }
                            !generoValido -> { { Text("Debe seleccionar una opción") } }
                            else -> null
                        }
                    )
                    DropdownMenu(
                        expanded = generoExpanded,
                        onDismissRequest = { generoExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Masculino") },
                            onClick = {
                                genero = "Masculino"
                                generoExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Femenino") },
                            onClick = {
                                genero = "Femenino"
                                generoExpanded = false
                            }
                        )
                    }
                }
                
                // Dirección (Opcional)
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = direccion.isNotBlank() && !direccionValida,
                    supportingText = if (direccion.isNotBlank() && !direccionValida) {
                        { Text("Máximo 200 caracteres") }
                    } else null
                )
                
                // Fecha de Nacimiento (DatePicker)
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = { },
                    label = { Text("Fecha de Nacimiento *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    isError = fechaNacimiento.isNotBlank() && (!fechaValida || !fechaNoFutura || !edadValida),
                    supportingText = when {
                        fechaNacimiento.isBlank() -> { { Text("Campo obligatorio") } }
                        !fechaValida -> { { Text("Formato inválido. Use YYYY-MM-DD") } }
                        !fechaNoFutura -> { { Text("La fecha no puede ser futura") } }
                        !edadValida -> { { Text("El candidato debe tener entre 18 y 90 años") } }
                        else -> null
                    }
                )
                
                // CI
                OutlinedTextField(
                    value = ci,
                    onValueChange = { 
                        ci = it
                        verificarCI = false
                        mostrarErrorCI = false
                    },
                    label = { Text("Cédula de Identidad *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = (ci.isNotBlank() && !ciValido) || mostrarErrorCI,
                    supportingText = {
                        when {
                            ci.isBlank() -> Text("Campo obligatorio")
                            !ciValido -> Text("Formato inválido. Ej: 1234567, 1234567-LP")
                            mostrarErrorCI -> Text(mensajeErrorCI)
                            else -> {}
                        }
                    }
                )
                
                // Correo
                OutlinedTextField(
                    value = correo,
                    onValueChange = { 
                        correo = it
                        verificarCorreo = false
                        mostrarErrorCorreo = false
                    },
                    label = { Text("Correo Electrónico *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = (correo.isNotBlank() && !emailValido) || mostrarErrorCorreo,
                    supportingText = {
                        when {
                            correo.isBlank() -> Text("Campo obligatorio")
                            !emailValido -> Text("Formato de email inválido")
                            mostrarErrorCorreo -> Text(mensajeErrorCorreo)
                            else -> {}
                        }
                    }
                )
                
                // Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = telefono.isNotBlank() && !telefonoValido,
                    supportingText = when {
                        telefono.isBlank() -> { { Text("Campo obligatorio") } }
                        !telefonoValido -> { { Text("Formato inválido. Mínimo 7 dígitos, máximo 12. Ej: +59171234567") } }
                        else -> null
                    }
                )
                
                // Profesión (Opcional)
                OutlinedTextField(
                    value = profesion,
                    onValueChange = { profesion = it },
                    label = { Text("Profesión") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = profesion.isNotBlank() && !profesionValida,
                    supportingText = if (profesion.isNotBlank() && !profesionValida) {
                        { Text("Máximo 100 caracteres") }
                    } else null
                )
                
                // Años de Experiencia
                val edadCalculada = remember(fechaNacimiento) {
                    calcularEdad(fechaNacimiento) ?: 0
                }
                val maxAniosExperiencia = remember(edadCalculada) {
                    maxOf(0, edadCalculada - 18)
                }
                
                OutlinedTextField(
                    value = aniosExperiencia,
                    onValueChange = { newValue ->
                        if (newValue.all { char -> char.isDigit() }) {
                            aniosExperiencia = newValue
                        }
                    },
                    label = { Text("Años de Experiencia *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !aniosExperienciaValido || !aniosExperienciaCoherente,
                    supportingText = {
                        when {
                            !aniosExperienciaValido -> Text("Debe ser 0 o mayor")
                            !aniosExperienciaCoherente -> Text("No puede ser mayor que $maxAniosExperiencia años (edad: $edadCalculada)")
                            else -> Text("Valor por defecto: 0")
                        }
                    }
                )
            }
            
            // Botones de acción - siempre visibles
            val isLoading by candidatoViewModel.isLoading.collectAsState()
            var operacionCompletada by remember { mutableStateOf(false) }
            
            // Navegar cuando la operación termine
            LaunchedEffect(isLoading) {
                if (!isLoading && operacionCompletada) {
                    operacionCompletada = false
                    onGuardarAccion()
                }
            }
            
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
                            verificarCI = true
                            verificarCorreo = true
                            
                            val existeCI = if (esEdicion && candidatoId != null) {
                                candidatoViewModel.existeCIExcluyendo(ci, candidatoId)
                            } else {
                                candidatoViewModel.existeCI(ci)
                            }
                            
                            val existeCorreo = if (esEdicion && candidatoId != null) {
                                candidatoViewModel.existeCorreoExcluyendo(correo, candidatoId)
                            } else {
                                candidatoViewModel.existeCorreo(correo)
                            }
                            
                            mostrarErrorCI = existeCI
                            mostrarErrorCorreo = existeCorreo
                            
                            if (!existeCI && !existeCorreo && isFormularioValido) {
                                val maternoTrimmed = materno.trim()
                                val profesionTrimmed = profesion.trim()
                                val direccionTrimmed = direccion.trim()
                                
                                val candidatoActualizado = Candidato(
                                    id_candidato = candidatoId ?: 0,
                                    id_frente = frenteId,
                                    nombre = nombre.trim(),
                                    paterno = paterno.trim(),
                                    materno = maternoTrimmed.takeIf { it.isNotBlank() },
                                    genero = genero,
                                    fecha_nacimiento = fechaNacimiento,
                                    ci = ci.trim(),
                                    correo = correo.trim(),
                                    telefono = telefono.trim(),
                                    profesion = profesionTrimmed.takeIf { it.isNotBlank() },
                                    direccion = direccionTrimmed.takeIf { it.isNotBlank() },
                                    anios_experiencia = aniosExperienciaNum,
                                    foto_url = fotoUriState?.toString()
                                )
                                
                                if (esEdicion) {
                                    candidatoViewModel.actualizarCandidato(candidatoActualizado)
                                } else {
                                    candidatoViewModel.insertarCandidato(candidatoActualizado)
                                }
                                operacionCompletada = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFormularioValido && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (esEdicion) "ACTUALIZAR" else "CREAR")
                    }
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
                fechaInicial = fechaNacimiento,
                onDateSelected = { fecha ->
                    fechaNacimiento = fecha
                },
                onDismiss = { mostrarDatePicker = false }
            )
        }
    }
}
