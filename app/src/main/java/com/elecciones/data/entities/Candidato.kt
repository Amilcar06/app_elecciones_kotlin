package com.elecciones.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a un candidato asociado a un frente político.
 *
 * @param id_candidato Identificador único autoincremental.
 * @param id_frente Clave foránea que referencia al frente al que pertenece el candidato.
 * @param nombre Nombre del candidato.
 * @param paterno Apellido paterno.
 * @param materno Apellido materno.
 * @param genero Género del candidato.
 * @param direccion Dirección de residencia.
 * @param fecha_nacimiento Fecha de nacimiento en formato ISO 8601 ("YYYY-MM-DD").
 * @param ci Cédula de identidad, debe ser única.
 * @param correo Dirección de email.
 * @param telefono Número de contacto.
 * @param profesion Profesión del candidato.
 * @param anios_experiencia Años de experiencia profesional.
 */
@Entity(
    tableName = "Candidato",
    foreignKeys = [
        ForeignKey(
            entity = Frente::class,
            parentColumns = ["id_frente"],
            childColumns = ["id_frente"],
            onDelete = ForeignKey.RESTRICT // Evita eliminar un frente si tiene candidatos
        )
    ],
    indices = [Index(value = ["id_frente"]), Index(value = ["ci"], unique = true)]
)
data class Candidato(
    @PrimaryKey(autoGenerate = true)
    val id_candidato: Int = 0,
    val id_frente: Int,
    val nombre: String,
    val paterno: String,
    val materno: String?, // Opcional: algunos candidatos pueden no tener apellido materno
    val genero: String,
    val direccion: String?,
    val fecha_nacimiento: String,
    val ci: String,
    val correo: String?,
    val telefono: String?,
    val profesion: String?,
    val anios_experiencia: Int?,
    val foto_url: String? = null // URI de la foto del candidato guardada localmente
)
