package com.elecciones.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un puesto electoral específico dentro de una elección.
 * Ejemplos: "Director de Carrera", "Consejero Estudiantil", etc.
 *
 * @param id_puesto Identificador único autoincremental.
 * @param id_eleccion Clave foránea que referencia a la elección a la que pertenece.
 * @param nombre_puesto Nombre del cargo a elegirse (ej. "Director de Carrera").
 * @param votos_nulos Cantidad de votos nulos para este puesto.
 * @param votos_blancos Cantidad de votos blancos para este puesto.
 * @param estado Estado del puesto: "Abierto", "Votación", "Cerrado".
 */
@Entity(
    tableName = "Puesto_Electoral",
    foreignKeys = [
        ForeignKey(
            entity = Eleccion::class,
            parentColumns = ["id_eleccion"],
            childColumns = ["id_eleccion"],
            onDelete = ForeignKey.CASCADE // Si se borra la elección, se borran sus puestos
        )
    ],
    indices = [Index(value = ["id_eleccion"])]
)
data class PuestoElectoral(
    @PrimaryKey(autoGenerate = true)
    val id_puesto: Int = 0,
    val id_eleccion: Int,
    val nombre_puesto: String,
    val votos_nulos: Int = 0,
    val votos_blancos: Int = 0,
    val estado: String = "Abierto" // "Abierto", "Votación", "Cerrado"
)

