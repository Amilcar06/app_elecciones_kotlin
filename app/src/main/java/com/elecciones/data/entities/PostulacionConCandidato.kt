package com.elecciones.data.entities

/**
 * Clase de datos que representa una postulación junto con la información del candidato.
 * Se utiliza para mostrar las postulaciones con los datos del candidato en las pantallas.
 */
data class PostulacionConCandidato(
    val postulacion: Postulacion,
    val candidato: Candidato,
    val frente: Frente
) {
    /**
     * Obtiene el nombre completo del candidato.
     */
    fun getNombreCompleto(): String {
        return "${candidato.nombre} ${candidato.paterno} ${candidato.materno}".trim()
    }
}

