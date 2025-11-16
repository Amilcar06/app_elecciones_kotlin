package com.elecciones.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elecciones.data.dao.CandidatoDao
import com.elecciones.data.dao.EleccionDao
import com.elecciones.data.dao.FrenteDao
import com.elecciones.data.dao.PuestoElectoralDao
import com.elecciones.data.dao.PostulacionDao
import com.elecciones.data.entities.Candidato
import com.elecciones.data.entities.Eleccion
import com.elecciones.data.entities.Frente
import com.elecciones.data.entities.PuestoElectoral
import com.elecciones.data.entities.Postulacion

/**
 * Clase principal de la base de datos de la aplicación.
 * Define las entidades, la versión y los DAOs.
 * Utiliza el patrón Singleton para proporcionar una única instancia de la base de datos.
 */
@Database(
    entities = [
        Frente::class,
        Candidato::class,
        Eleccion::class,
        PuestoElectoral::class,
        Postulacion::class
    ],
    version = 5, // Nueva versión: nueva estructura con PuestoElectoral y Postulacion
    exportSchema = false // No exportar el esquema para simplificar
)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos para que Room provea la implementación de cada DAO.
    abstract fun frenteDao(): FrenteDao
    abstract fun candidatoDao(): CandidatoDao
    abstract fun eleccionDao(): EleccionDao
    abstract fun puestoElectoralDao(): PuestoElectoralDao
    abstract fun postulacionDao(): PostulacionDao

    companion object {
        // La anotación @Volatile asegura que la instancia sea siempre visible para todos los hilos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos. Si no existe, la crea.
         * Este método es seguro para ser llamado desde múltiples hilos (thread-safe).
         *
         * @param context Contexto de la aplicación.
         * @return La instancia Singleton de AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Se utiliza el operador elvis (?:) para crear la instancia si es nula.
            // El bloque synchronized asegura que solo un hilo pueda crear la base de datos.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elecciones_database" // Nombre del archivo de la base de datos
                )
                    .fallbackToDestructiveMigration() // Estrategia de migración (simple para desarrollo)
                    .build()
                INSTANCE = instance
                // Devolver la instancia creada
                instance
            }
        }
    }
}
