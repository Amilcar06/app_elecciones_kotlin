package com.elecciones.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elecciones.data.entities.PuestoElectoral
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad PuestoElectoral.
 * Define los métodos para gestionar los puestos electorales en la base de datos.
 */
@Dao
interface PuestoElectoralDao {

    /**
     * Inserta un nuevo puesto electoral.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(puesto: PuestoElectoral): Long

    /**
     * Inserta múltiples puestos electorales.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(puestos: List<PuestoElectoral>)

    /**
     * Actualiza un puesto electoral.
     */
    @Update
    suspend fun update(puesto: PuestoElectoral)

    /**
     * Elimina un puesto electoral.
     */
    @Query("DELETE FROM Puesto_Electoral WHERE id_puesto = :puestoId")
    suspend fun delete(puestoId: Int)

    /**
     * Obtiene un puesto electoral por su ID.
     */
    @Query("SELECT * FROM Puesto_Electoral WHERE id_puesto = :puestoId")
    fun getPuestoById(puestoId: Int): Flow<PuestoElectoral?>

    /**
     * Obtiene todos los puestos electorales de una elección.
     */
    @Query("SELECT * FROM Puesto_Electoral WHERE id_eleccion = :eleccionId ORDER BY nombre_puesto ASC")
    fun getPuestosByEleccion(eleccionId: Int): Flow<List<PuestoElectoral>>

    /**
     * Obtiene todos los puestos electorales de una elección (sincrónico para validaciones).
     */
    @Query("SELECT * FROM Puesto_Electoral WHERE id_eleccion = :eleccionId")
    suspend fun getPuestosByEleccionSync(eleccionId: Int): List<PuestoElectoral>

    /**
     * Actualiza el estado de un puesto electoral.
     */
    @Query("UPDATE Puesto_Electoral SET estado = :estado WHERE id_puesto = :puestoId")
    suspend fun updateEstado(puestoId: Int, estado: String)

    /**
     * Actualiza los votos nulos y blancos de un puesto.
     */
    @Query("UPDATE Puesto_Electoral SET votos_nulos = :votosNulos, votos_blancos = :votosBlancos WHERE id_puesto = :puestoId")
    suspend fun updateVotosNulosYBlancos(puestoId: Int, votosNulos: Int, votosBlancos: Int)
}

