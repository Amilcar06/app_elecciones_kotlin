package com.elecciones.data.dao

import androidx.room.*
import com.elecciones.data.entities.Eleccion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Eleccion. Define los métodos para gestionar los
 * procesos electorales en la base de datos.
 */
@androidx.room.Dao
interface EleccionDao {

    /**
     * Inserta una nueva elección.
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(eleccion: Eleccion)

    /**
     * Actualiza los datos de una elección, como su estado o el conteo de votos
     * nulos y blancos.
     */
    @androidx.room.Update
    suspend fun update(eleccion: Eleccion)

    /**
     * Obtiene una elección por su ID.
     */
    @androidx.room.Query("SELECT * FROM Eleccion WHERE id_eleccion = :id")
    fun getEleccion(id: Int): Flow<Eleccion?>

    /**
     * Obtiene todas las elecciones, ordenadas por gestión de forma descendente.
     * Ideal para mostrar el historial.
     */
    @androidx.room.Query("SELECT * FROM Eleccion ORDER BY gestion DESC")
    fun getAllElecciones(): Flow<List<Eleccion>>
}
