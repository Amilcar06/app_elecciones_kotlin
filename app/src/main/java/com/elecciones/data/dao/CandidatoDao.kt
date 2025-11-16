package com.elecciones.data.dao

import androidx.room.*
import com.elecciones.data.entities.Candidato
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Candidato. Proporciona los métodos para interactuar
 * con la tabla de candidatos en la base de datos.
 */
@androidx.room.Dao
interface CandidatoDao {

    /**
     * Inserta un nuevo candidato. La Cédula de Identidad (ci) debe ser única.
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insert(candidato: Candidato)

    /**
     * Actualiza la información de un candidato existente.
     */
    @androidx.room.Update
    suspend fun update(candidato: Candidato)

    /**
     * Elimina un candidato de la base de datos.
     */
    @androidx.room.Delete
    suspend fun delete(candidato: Candidato)

    /**
     * Obtiene todos los candidatos pertenecientes a un frente específico.
     * Retorna un Flow para que la lista se actualice si hay cambios.
     */
    @androidx.room.Query("SELECT * FROM Candidato WHERE id_frente = :frenteId ORDER BY paterno, materno, nombre ASC")
    fun getCandidatosByFrente(frenteId: Int): Flow<List<Candidato>>

    /**
     * Obtiene un candidato por su ID.
     */
    @androidx.room.Query("SELECT * FROM Candidato WHERE id_candidato = :id")
    fun getCandidato(id: Int): Flow<Candidato?>

    /**
     * Obtiene todos los candidatos.
     */
    @androidx.room.Query("SELECT * FROM Candidato ORDER BY paterno, materno, nombre ASC")
    fun getAllCandidatos(): Flow<List<Candidato>>
}
