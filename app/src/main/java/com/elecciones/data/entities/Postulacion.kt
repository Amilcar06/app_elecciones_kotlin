package com.elecciones.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa la postulación de un candidato a un puesto electoral específico.
 * Un candidato puede postularse a múltiples puestos (en diferentes elecciones),
 * pero solo puede tener una postulación por puesto.
 *
 * @param id_postulacion Identificador único autoincremental.
 * @param id_puesto Clave foránea que referencia al puesto al que postula.
 * @param id_candidato Clave foránea que referencia al candidato que postula.
 * @param votos Cantidad de votos obtenidos por esta postulación (default 0).
 */
@Entity(
    tableName = "Postulacion",
    foreignKeys = [
        ForeignKey(
            entity = PuestoElectoral::class,
            parentColumns = ["id_puesto"],
            childColumns = ["id_puesto"],
            onDelete = ForeignKey.CASCADE // Si se borra el puesto, se borran sus postulaciones
        ),
        ForeignKey(
            entity = Candidato::class,
            parentColumns = ["id_candidato"],
            childColumns = ["id_candidato"],
            onDelete = ForeignKey.CASCADE // Si se borra el candidato, se borran sus postulaciones
        )
    ],
    // Constraint: El par (id_puesto, id_candidato) debe ser único
    indices = [
        Index(value = ["id_puesto", "id_candidato"], unique = true),
        Index(value = ["id_puesto"]),
        Index(value = ["id_candidato"])
    ]
)
data class Postulacion(
    @PrimaryKey(autoGenerate = true)
    val id_postulacion: Int = 0,
    val id_puesto: Int,
    val id_candidato: Int,
    val votos: Int = 0
)

