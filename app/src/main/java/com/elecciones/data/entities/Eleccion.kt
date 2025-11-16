package com.elecciones.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un proceso de elección en una fecha y gestión específicas.
 *
 * @param id_eleccion Identificador único autoincremental.
 * @param fecha_eleccion Fecha de la elección en formato ISO 8601 ("YYYY-MM-DD").
 * @param gestion Año de la gestión correspondiente a la elección (ej: 2025).
 * @param estado Estado actual de la elección ("Programada", "En curso", "Finalizado").
 * @param descripcion Descripción o información adicional de la elección.
 */
@Entity(tableName = "Eleccion")
data class Eleccion(
    @PrimaryKey(autoGenerate = true)
    val id_eleccion: Int = 0,
    val fecha_eleccion: String,
    val gestion: Int,
    val estado: String,
    val descripcion: String? = null
)
