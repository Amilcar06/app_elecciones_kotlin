package com.elecciones.repository

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
import com.elecciones.data.entities.PostulacionConCandidato
import com.elecciones.data.entities.PuestoConPostulaciones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repositorio que gestiona el acceso a los datos de la aplicación.
 * Actúa como única fuente de verdad para los ViewModels, abstrayendo el
 * origen de los datos (en este caso, los DAOs de Room).
 *
 * @param frenteDao DAO para las operaciones de la entidad Frente.
 * @param candidatoDao DAO para las operaciones de la entidad Candidato.
 * @param eleccionDao DAO para las operaciones de la entidad Eleccion.
 * @param puestoElectoralDao DAO para las operaciones de la entidad PuestoElectoral.
 * @param postulacionDao DAO para las operaciones de la entidad Postulacion.
 */
class EleccionesRepository(
    private val frenteDao: FrenteDao,
    private val candidatoDao: CandidatoDao,
    private val eleccionDao: EleccionDao,
    private val puestoElectoralDao: PuestoElectoralDao,
    private val postulacionDao: PostulacionDao
) {

    // --- Operaciones para Frentes ---

    val todosLosFrentes: Flow<List<Frente>> = frenteDao.getAllFrentes()

    fun getFrente(id: Int): Flow<Frente?> = frenteDao.getFrente(id)

    suspend fun insertarFrente(frente: Frente) {
        frenteDao.insert(frente)
    }

    suspend fun actualizarFrente(frente: Frente) {
        frenteDao.update(frente)
    }

    suspend fun eliminarFrente(frente: Frente) {
        frenteDao.delete(frente)
    }

    // --- Operaciones para Candidatos ---

    fun getCandidatosPorFrente(frenteId: Int): Flow<List<Candidato>> =
        candidatoDao.getCandidatosByFrente(frenteId)

    fun getCandidato(id: Int): Flow<Candidato?> = candidatoDao.getCandidato(id)

    val todosLosCandidatos: Flow<List<Candidato>> = candidatoDao.getAllCandidatos()

    suspend fun insertarCandidato(candidato: Candidato) {
        candidatoDao.insert(candidato)
    }

    suspend fun actualizarCandidato(candidato: Candidato) {
        candidatoDao.update(candidato)
    }

    suspend fun eliminarCandidato(candidato: Candidato) {
        candidatoDao.delete(candidato)
    }

    // --- Operaciones para Elecciones ---

    val todasLasElecciones: Flow<List<Eleccion>> = eleccionDao.getAllElecciones()

    fun getEleccion(id: Int): Flow<Eleccion?> = eleccionDao.getEleccion(id)

    suspend fun insertarEleccion(eleccion: Eleccion) {
        eleccionDao.insert(eleccion)
    }

    suspend fun actualizarEleccion(eleccion: Eleccion) {
        eleccionDao.update(eleccion)
    }

    // --- Operaciones para Puestos Electorales ---

    fun getPuestosPorEleccion(eleccionId: Int): Flow<List<PuestoElectoral>> =
        puestoElectoralDao.getPuestosByEleccion(eleccionId)

    fun getPuestoById(puestoId: Int): Flow<PuestoElectoral?> =
        puestoElectoralDao.getPuestoById(puestoId)

    suspend fun insertarPuesto(puesto: PuestoElectoral): Long {
        return puestoElectoralDao.insert(puesto)
    }

    suspend fun actualizarPuesto(puesto: PuestoElectoral) {
        puestoElectoralDao.update(puesto)
    }

    suspend fun eliminarPuesto(puestoId: Int) {
        puestoElectoralDao.delete(puestoId)
    }

    suspend fun actualizarEstadoPuesto(puestoId: Int, estado: String) {
        puestoElectoralDao.updateEstado(puestoId, estado)
    }

    // --- Operaciones para Postulaciones ---

    fun getPostulacionesPorPuesto(puestoId: Int): Flow<List<Postulacion>> =
        postulacionDao.getPostulacionesByPuesto(puestoId)

    suspend fun insertarPostulacion(postulacion: Postulacion): Long {
        // Validar que no exista ya una postulación del mismo candidato al mismo puesto
        val existe = postulacionDao.existePostulacion(postulacion.id_puesto, postulacion.id_candidato)
        if (existe) {
            throw IllegalStateException("El candidato ya está postulado a este puesto")
        }
        return postulacionDao.insert(postulacion)
    }

    suspend fun eliminarPostulacion(postulacionId: Int) {
        postulacionDao.delete(postulacionId)
    }

    suspend fun actualizarPostulacion(postulacion: Postulacion) {
        postulacionDao.update(postulacion)
    }

    suspend fun actualizarPostulaciones(postulaciones: List<Postulacion>) {
        postulacionDao.updateAll(postulaciones)
    }

    // --- Operaciones con relaciones complejas ---

    /**
     * Obtiene todas las postulaciones de un puesto con la información del candidato y su frente.
     * Hace consultas separadas para obtener candidatos y frentes.
     */
    suspend fun getPostulacionesConCandidatoSync(puestoId: Int): List<PostulacionConCandidato> {
        val postulaciones = postulacionDao.getPostulacionesByPuestoSync(puestoId)
        return postulaciones.mapNotNull { postulacion ->
            val candidato = candidatoDao.getCandidato(postulacion.id_candidato).first()
            candidato?.let {
                val frente = frenteDao.getFrente(it.id_frente).first()
                frente?.let { f ->
                    PostulacionConCandidato(postulacion, it, f)
                }
            }
        }
    }

    /**
     * Obtiene todas las postulaciones de un puesto con la información del candidato y su frente (Flow).
     * Usa el método sincrónico para mejor rendimiento.
     */
    fun getPostulacionesConCandidato(puestoId: Int): Flow<List<PostulacionConCandidato>> {
        return kotlinx.coroutines.flow.flow {
            val postulaciones = getPostulacionesConCandidatoSync(puestoId)
            emit(postulaciones)
        }
    }

    /**
     * Obtiene un puesto con todas sus postulaciones y candidatos.
     */
    suspend fun getPuestoConPostulacionesSync(puestoId: Int): PuestoConPostulaciones? {
        val puesto = puestoElectoralDao.getPuestoById(puestoId).first()
        return puesto?.let {
            val postulaciones = getPostulacionesConCandidatoSync(puestoId)
            PuestoConPostulaciones(it, postulaciones)
        }
    }

    /**
     * Obtiene un puesto con todas sus postulaciones y candidatos (Flow).
     */
    fun getPuestoConPostulaciones(puestoId: Int): Flow<PuestoConPostulaciones?> {
        return kotlinx.coroutines.flow.flow {
            val puesto = puestoElectoralDao.getPuestoById(puestoId).first()
            if (puesto != null) {
                val postulaciones = getPostulacionesConCandidatoSync(puestoId)
                emit(PuestoConPostulaciones(puesto, postulaciones))
            } else {
                emit(null)
            }
        }
    }

    /**
     * Registra los votos de un puesto electoral:
     * - Actualiza los votos de cada postulación
     * - Actualiza los votos nulos y blancos del puesto
     * - Cierra el puesto (cambia estado a "Cerrado")
     */
    suspend fun registrarVotosPorPuesto(
        puestoId: Int,
        postulaciones: List<Postulacion>,
        votosNulos: Int,
        votosBlancos: Int
    ) {
        // Validar que los votos no sean negativos
        if (votosNulos < 0 || votosBlancos < 0) {
            throw IllegalArgumentException("Los votos nulos y blancos no pueden ser negativos")
        }
        postulaciones.forEach {
            if (it.votos < 0) {
                throw IllegalArgumentException("Los votos no pueden ser negativos")
            }
        }

        // Actualizar las postulaciones con sus votos
        actualizarPostulaciones(postulaciones)

        // Actualizar votos nulos y blancos del puesto
        puestoElectoralDao.updateVotosNulosYBlancos(puestoId, votosNulos, votosBlancos)

        // Cerrar el puesto
        actualizarEstadoPuesto(puestoId, "Cerrado")
    }

    /**
     * Verifica si todos los puestos de una elección están cerrados.
     */
    suspend fun todosLosPuestosCerrados(eleccionId: Int): Boolean {
        val puestos = puestoElectoralDao.getPuestosByEleccionSync(eleccionId)
        return puestos.isNotEmpty() && puestos.all { it.estado == "Cerrado" }
    }

    /**
     * Cierra una elección si todos sus puestos están cerrados.
     */
    suspend fun cerrarEleccionSiCompleta(eleccionId: Int) {
        if (todosLosPuestosCerrados(eleccionId)) {
            val eleccion = eleccionDao.getEleccion(eleccionId).first()
                ?: throw IllegalStateException("Elección no encontrada")
            val eleccionActualizada = eleccion.copy(estado = "Finalizado")
            actualizarEleccion(eleccionActualizada)
        } else {
            throw IllegalStateException("No se puede cerrar la elección: hay puestos sin cerrar")
        }
    }

    /**
     * Obtiene la cantidad de candidatos de un frente específico.
     */
    suspend fun contarCandidatosPorFrente(frenteId: Int): Int {
        return candidatoDao.getCandidatosByFrente(frenteId)
            .first().size
    }

    /**
     * Verifica si un puesto puede ser modificado (no debe estar cerrado).
     */
    suspend fun puedeModificarPuesto(puestoId: Int): Boolean {
        val puesto = puestoElectoralDao.getPuestoById(puestoId).first()
        return puesto?.estado != "Cerrado"
    }
}
