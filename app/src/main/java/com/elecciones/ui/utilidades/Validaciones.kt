package com.elecciones.ui.utilidades

import android.util.Patterns
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Utilidades para validaciones de formularios
 */

/**
 * Valida si una cadena tiene el formato de fecha ISO (YYYY-MM-DD)
 */
fun validarFormatoFecha(fecha: String): Boolean {
    if (fecha.isBlank()) return false
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    if (!regex.matches(fecha)) return false
    
    // Validar que sea una fecha válida
    val partes = fecha.split("-")
    if (partes.size != 3) return false
    
    val año = partes[0].toIntOrNull() ?: return false
    val mes = partes[1].toIntOrNull() ?: return false
    val dia = partes[2].toIntOrNull() ?: return false
    
    // Validaciones básicas
    if (año < 1900 || año > 2100) return false
    if (mes < 1 || mes > 12) return false
    if (dia < 1 || dia > 31) return false
    
    // Validar días por mes (simplificado)
    val diasPorMes = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val maxDias = if (mes == 2 && esAñoBisiesto(año)) 29 else diasPorMes[mes - 1]
    if (dia > maxDias) return false
    
    return true
}

/**
 * Verifica si un año es bisiesto
 */
private fun esAñoBisiesto(año: Int): Boolean {
    return (año % 4 == 0 && año % 100 != 0) || (año % 400 == 0)
}

/**
 * Valida formato de email usando Patterns de Android
 */
fun validarEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Valida formato de color hexadecimal (#RRGGBB o #AARRGGBB)
 */
fun validarColorHex(color: String): Boolean {
    if (color.isBlank()) return false
    val regex = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")
    return regex.matches(color)
}

/**
 * Valida si un estado de elección es válido
 */
fun validarEstadoEleccion(estado: String): Boolean {
    return estado in listOf("Programada", "En curso", "Finalizado")
}

/**
 * Valida si una fecha es mayor o igual a hoy
 */
fun validarFechaMayorIgualHoy(fecha: String): Boolean {
    if (!validarFormatoFecha(fecha)) return false
    
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return try {
        val fechaComparar = LocalDate.parse(fecha, formatter)
        val hoy = LocalDate.now()
        !fechaComparar.isBefore(hoy)
    } catch (e: DateTimeParseException) {
        false
    }
}

/**
 * Valida si una fecha es anterior o igual a hoy (no puede ser futura)
 */
fun validarFechaNoFutura(fecha: String): Boolean {
    if (!validarFormatoFecha(fecha)) return false
    
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return try {
        val fechaComparar = LocalDate.parse(fecha, formatter)
        val hoy = LocalDate.now()
        !fechaComparar.isAfter(hoy)
    } catch (e: DateTimeParseException) {
        false
    }
}

/**
 * Calcula la edad a partir de una fecha de nacimiento
 */
fun calcularEdad(fechaNacimiento: String): Int? {
    if (!validarFormatoFecha(fechaNacimiento)) return null
    
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return try {
        val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
        val hoy = LocalDate.now()
        val edad = hoy.year - fechaNac.year
        val ajuste = if (hoy.dayOfYear < fechaNac.dayOfYear) -1 else 0
        edad + ajuste
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * Valida que la edad esté entre 18 y 90 años
 */
fun validarEdad(fechaNacimiento: String): Boolean {
    val edad = calcularEdad(fechaNacimiento) ?: return false
    return edad >= 18 && edad <= 90
}

/**
 * Valida formato de nombre (solo letras, acentos, ñ y espacios)
 */
fun validarNombre(nombre: String): Boolean {
    if (nombre.isBlank()) return false
    // Permite letras, acentos, ñ, espacios
    val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{2,50}$")
    return regex.matches(nombre.trim())
}

/**
 * Valida formato de CI boliviano (números + opcional extensión)
 * Ejemplos válidos: 1234567, 1234567-LP, 1234567 1E, 1234567-1E
 */
fun validarCI(ci: String): Boolean {
    if (ci.isBlank()) return false
    // Permite números seguidos opcionalmente de guion/espacio y extensión alfanumérica
    val regex = Regex("^\\d{4,10}([-\\s]?[A-Za-z0-9]{1,3})?$")
    return regex.matches(ci.trim())
}

/**
 * Valida formato de teléfono (solo números, opcional + al inicio)
 */
fun validarTelefono(telefono: String): Boolean {
    if (telefono.isBlank()) return false
    val sinEspacios = telefono.replace(" ", "").replace("-", "")
    // Permite + opcional al inicio, seguido de 7-12 dígitos
    val regex = Regex("^(\\+?)\\d{7,12}$")
    return regex.matches(sinEspacios)
}

/**
 * Valida que los años de experiencia sean coherentes con la edad
 */
fun validarAniosExperiencia(fechaNacimiento: String, aniosExperiencia: Int): Boolean {
    val edad = calcularEdad(fechaNacimiento) ?: return false
    val edadMinimaTrabajo = 18
    val maxAniosExperiencia = maxOf(0, edad - edadMinimaTrabajo)
    return aniosExperiencia >= 0 && aniosExperiencia <= maxAniosExperiencia
}
