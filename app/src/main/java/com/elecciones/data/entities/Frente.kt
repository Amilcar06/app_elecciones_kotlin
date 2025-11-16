package com.elecciones.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un frente político en la base de datos.
 *
 * @param id_frente Identificador único autoincremental para el frente.
 * @param nombre Nombre oficial del frente. No puede ser nulo.
 * @param color Representación hexadecimal del color distintivo del frente.
 * @param logo_url Ruta o URL que apunta al logo del frente.
 * @param fecha_fundacion Fecha de fundación en formato ISO 8601 ("YYYY-MM-DD").
 * @param descripcion Información adicional o eslogan del frente.
 */
@Entity(tableName = "Frente")
data class Frente(
    @PrimaryKey(autoGenerate = true)
    val id_frente: Int = 0,
    val nombre: String,
    val color: String,
    val logo_url: String?,
    val fecha_fundacion: String,
    val descripcion: String?
)
