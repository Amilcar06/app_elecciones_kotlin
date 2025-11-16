package com.elecciones.data.dao

import androidx.room.*
import com.elecciones.data.entities.Frente
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Frente. Define los métodos de acceso a la base de datos
 * para la tabla de frentes políticos.
 */
@androidx.room.Dao
interface FrenteDao {

    /**
     * Inserta un nuevo frente en la base de datos.
     * Si el frente ya existe, la operación se aborta.
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insert(frente: Frente)

    /**
     * Actualiza los datos de un frente existente.
     */
    @androidx.room.Update
    suspend fun update(frente: Frente)

    /**
     * Elimina un frente de la base de datos.
     * La eliminación está restringida si el frente tiene candidatos asociados.
     */
    @androidx.room.Delete
    suspend fun delete(frente: Frente)

    /**
     * Obtiene un frente específico por su ID.
     * Retorna un Flow para que la UI se actualice automáticamente ante cambios.
     */
    @androidx.room.Query("SELECT * FROM Frente WHERE id_frente = :id")
    fun getFrente(id: Int): Flow<Frente?>

    /**
     * Obtiene todos los frentes registrados, ordenados por nombre.
     * Retorna un Flow para observar cambios en la lista de frentes en tiempo real.
     */
    @androidx.room.Query("SELECT * FROM Frente ORDER BY nombre ASC")
    fun getAllFrentes(): Flow<List<Frente>>
}
