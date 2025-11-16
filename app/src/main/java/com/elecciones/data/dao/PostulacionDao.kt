package com.elecciones.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.elecciones.data.entities.Postulacion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Postulacion.
 * Define los métodos para gestionar las postulaciones en la base de datos.
 */
@Dao
interface PostulacionDao {

    /**
     * Inserta una nueva postulación.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postulacion: Postulacion): Long

    /**
     * Inserta múltiples postulaciones.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(postulaciones: List<Postulacion>)

    /**
     * Actualiza una postulación.
     */
    @Update
    suspend fun update(postulacion: Postulacion)

    /**
     * Actualiza múltiples postulaciones.
     */
    @Update
    suspend fun updateAll(postulaciones: List<Postulacion>)

    /**
     * Elimina una postulación.
     */
    @Query("DELETE FROM Postulacion WHERE id_postulacion = :postulacionId")
    suspend fun delete(postulacionId: Int)

    /**
     * Elimina todas las postulaciones de un puesto.
     */
    @Query("DELETE FROM Postulacion WHERE id_puesto = :puestoId")
    suspend fun deleteByPuesto(puestoId: Int)

    /**
     * Obtiene una postulación por su ID.
     */
    @Query("SELECT * FROM Postulacion WHERE id_postulacion = :postulacionId")
    fun getPostulacionById(postulacionId: Int): Flow<Postulacion?>

    /**
     * Obtiene todas las postulaciones de un puesto electoral.
     */
    @Query("SELECT * FROM Postulacion WHERE id_puesto = :puestoId ORDER BY votos DESC")
    fun getPostulacionesByPuesto(puestoId: Int): Flow<List<Postulacion>>

    /**
     * Obtiene todas las postulaciones de un puesto electoral (sincrónico).
     */
    @Query("SELECT * FROM Postulacion WHERE id_puesto = :puestoId")
    suspend fun getPostulacionesByPuestoSync(puestoId: Int): List<Postulacion>

    /**
     * Obtiene todas las postulaciones de un candidato.
     */
    @Query("SELECT * FROM Postulacion WHERE id_candidato = :candidatoId")
    fun getPostulacionesByCandidato(candidatoId: Int): Flow<List<Postulacion>>

    /**
     * Verifica si un candidato ya está postulado a un puesto.
     */
    @Query("SELECT COUNT(*) > 0 FROM Postulacion WHERE id_puesto = :puestoId AND id_candidato = :candidatoId")
    suspend fun existePostulacion(puestoId: Int, candidatoId: Int): Boolean

    /**
     * Actualiza los votos de una postulación.
     */
    @Query("UPDATE Postulacion SET votos = :votos WHERE id_postulacion = :postulacionId")
    suspend fun updateVotos(postulacionId: Int, votos: Int)
}

