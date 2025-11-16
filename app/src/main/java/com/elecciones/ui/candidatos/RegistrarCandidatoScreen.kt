package com.elecciones.ui.candidatos

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elecciones.data.entities.Candidato
import com.elecciones.ui.utilidades.validarEmail
import com.elecciones.ui.utilidades.validarFormatoFecha
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.FrenteViewModel

/**
 * Pantalla de formulario para registrar un nuevo candidato.
 *
 * @param candidatoViewModel ViewModel para manejar la lógica de negocio de los candidatos.
 * @param frenteId El ID del frente al que pertenecerá el candidato.
 * @param onGuardarAccion Acción a ejecutar tras guardar el candidato (navegar hacia atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarCandidatoScreen(
    candidatoViewModel: CandidatoViewModel,
    frenteViewModel: FrenteViewModel,
    frenteId: Int,
    onGuardarAccion: () -> Unit
) {
    // Obtener información del frente
    val frentes by frenteViewModel.todosLosFrentes.collectAsState()
    val frente = frentes.find { it.id_frente == frenteId }
    
    // Estados para cada campo del formulario
    var nombre by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var ci by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var profesion by remember { mutableStateOf("") }
    var aniosExperiencia by remember { mutableStateOf("") }

    // Estados para validación
    var mostrarErrorFecha by remember { mutableStateOf(false) }
    var mostrarErrorEmail by remember { mutableStateOf(false) }

    // Validaciones
    val fechaValida = fechaNacimiento.isBlank() || validarFormatoFecha(fechaNacimiento)
    val emailValido = correo.isBlank() || validarEmail(correo)
    val isFormularioValido = nombre.isNotBlank() && 
            paterno.isNotBlank() && 
            ci.isNotBlank() && 
            fechaValida && 
            emailValido

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Candidato") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                Text(
                    text = "Frente Asociado: ${frente?.nombre ?: "Desconocido"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item { OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre(s)") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = paterno, onValueChange = { paterno = it }, label = { Text("Apellido Paterno") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = materno, onValueChange = { materno = it }, label = { Text("Apellido Materno") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = ci, onValueChange = { ci = it }, label = { Text("Cédula de Identidad") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
            item { 
                OutlinedTextField(
                    value = fechaNacimiento, 
                    onValueChange = { 
                        fechaNacimiento = it
                        mostrarErrorFecha = false
                    }, 
                    label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = mostrarErrorFecha || (fechaNacimiento.isNotBlank() && !fechaValida),
                    supportingText = if (mostrarErrorFecha || (fechaNacimiento.isNotBlank() && !fechaValida)) {
                        { Text("Formato de fecha inválido. Use YYYY-MM-DD") }
                    } else null
                ) 
            }
            item { OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth()) }
            item { 
                OutlinedTextField(
                    value = correo, 
                    onValueChange = { 
                        correo = it
                        mostrarErrorEmail = false
                    }, 
                    label = { Text("Correo Electrónico") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = mostrarErrorEmail || (correo.isNotBlank() && !emailValido),
                    supportingText = if (mostrarErrorEmail || (correo.isNotBlank() && !emailValido)) {
                        { Text("Formato de email inválido") }
                    } else null
                ) 
            }
            item { OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
            item { OutlinedTextField(value = profesion, onValueChange = { profesion = it }, label = { Text("Profesión") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = aniosExperiencia, onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    aniosExperiencia = newValue
                }
            }, label = { Text("Años de Experiencia") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            // Validar antes de guardar
                            mostrarErrorFecha = fechaNacimiento.isNotBlank() && !fechaValida
                            mostrarErrorEmail = correo.isNotBlank() && !emailValido
                            
                            if (!mostrarErrorFecha && !mostrarErrorEmail && isFormularioValido) {
                                val nuevoCandidato = Candidato(
                                    id_frente = frenteId,
                                    nombre = nombre,
                                    paterno = paterno,
                                    materno = materno,
                                    genero = genero,
                                    fecha_nacimiento = fechaNacimiento,
                                    ci = ci,
                                    correo = correo,
                                    telefono = telefono,
                                    profesion = profesion,
                                    direccion = null, // No solicitado en el formulario 7.4
                                    anios_experiencia = aniosExperiencia.toIntOrNull()
                                )
                                candidatoViewModel.insertarCandidato(nuevoCandidato)
                                onGuardarAccion()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormularioValido
                    ) {
                        Text("GUARDAR")
                    }
                    OutlinedButton(
                        onClick = onGuardarAccion,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCELAR")
                    }
                }
            }
        }
    }
}
