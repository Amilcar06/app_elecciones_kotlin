package com.elecciones.data.entities

/**
 * Clase de datos que representa un puesto electoral junto con todas sus postulaciones.
 * Se utiliza para mostrar los detalles completos de un puesto.
 */
data class PuestoConPostulaciones(
    val puesto: PuestoElectoral,
    val postulaciones: List<PostulacionConCandidato>
) {
    /**
     * Calcula el total de votos válidos (suma de votos de todas las postulaciones).
     */
    fun getTotalVotosValidos(): Int {
        return postulaciones.sumOf { it.postulacion.votos }
    }

    /**
     * Calcula el total de votos (válidos + nulos + blancos).
     */
    fun getTotalVotos(): Int {
        return getTotalVotosValidos() + puesto.votos_nulos + puesto.votos_blancos
    }

    /**
     * Obtiene el candidato ganador (el que tiene más votos).
     * Retorna null si no hay postulaciones o hay empate.
     */
    fun getGanador(): PostulacionConCandidato? {
        if (postulaciones.isEmpty()) return null
        
        val maxVotos = postulaciones.maxOfOrNull { it.postulacion.votos } ?: return null
        val ganadores = postulaciones.filter { it.postulacion.votos == maxVotos }
        
        // Si hay empate, retorna null (se debe manejar manualmente)
        return if (ganadores.size == 1) ganadores.first() else null
    }
}

