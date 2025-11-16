package com.elecciones.ui.utilidades

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
 * Valida formato de email
 */
fun validarEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return true // Opcional
    val regex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
    return regex.matches(email)
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
    return estado in listOf("Programada", "Abierta", "Cerrada")
}

/**
 * Valida si una fecha es mayor o igual a hoy
 */
fun validarFechaMayorIgualHoy(fecha: String): Boolean {
    if (!validarFormatoFecha(fecha)) return false
    
    val partes = fecha.split("-")
    val año = partes[0].toInt()
    val mes = partes[1].toInt()
    val dia = partes[2].toInt()
    
    val hoy = java.time.LocalDate.now()
    val fechaComparar = java.time.LocalDate.of(año, mes, dia)
    
    return !fechaComparar.isBefore(hoy)
}

