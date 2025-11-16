package com.elecciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elecciones.data.entities.Eleccion
import com.elecciones.data.entities.PuestoElectoral
import com.elecciones.data.entities.Postulacion
import com.elecciones.data.entities.PostulacionConCandidato
import com.elecciones.data.entities.PuestoConPostulaciones
import com.elecciones.repository.EleccionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Elecciones, Puestos Electorales y Postulaciones.
 */
class EleccionViewModel(private val repository: EleccionesRepository) : ViewModel() {

    // --- StateFlows Públicos ---

    /** Expone la lista de todas las elecciones. */
    val todasLasElecciones: StateFlow<List<Eleccion>> = repository.todasLasElecciones
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // StateFlow privado para gestionar la elección actualmente seleccionada.
    private val _eleccionId = MutableStateFlow<Int?>(null)

    // StateFlow privado para gestionar el puesto actualmente seleccionado.
    private val _puestoId = MutableStateFlow<Int?>(null)

    /** Expone los puestos electorales de la elección seleccionada. */
    val puestosPorEleccion: StateFlow<List<PuestoElectoral>> = _eleccionId.flatMapLatest { eleccionId ->
        if (eleccionId != null) {
            repository.getPuestosPorEleccion(eleccionId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Expone las postulaciones del puesto seleccionado. */
    val postulacionesPorPuesto: StateFlow<List<PostulacionConCandidato>> = _puestoId.flatMapLatest { puestoId ->
        if (puestoId != null) {
            repository.getPostulacionesConCandidato(puestoId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Expone el puesto con todas sus postulaciones. */
    val puestoConPostulaciones: StateFlow<PuestoConPostulaciones?> = _puestoId.flatMapLatest { puestoId ->
        if (puestoId != null) {
            flow<PuestoConPostulaciones?> {
                val puesto = repository.getPuestoConPostulacionesSync(puestoId)
                emit(puesto)
            }
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Obtiene la elección actual seleccionada.
     */
    val eleccionActual: StateFlow<Eleccion?> = _eleccionId.flatMapLatest { eleccionId ->
        if (eleccionId != null) {
            repository.getEleccion(eleccionId)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Obtiene el puesto actual seleccionado.
     */
    val puestoActual: StateFlow<PuestoElectoral?> = _puestoId.flatMapLatest { puestoId ->
        if (puestoId != null) {
            repository.getPuestoById(puestoId)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- Acciones (Funciones Públicas) ---

    /** Establece la elección activa para cargar sus datos. */
    fun setEleccionId(eleccionId: Int) {
        _eleccionId.value = eleccionId
    }

    /** Establece el puesto activo para cargar sus datos. */
    fun setPuestoId(puestoId: Int) {
        _puestoId.value = puestoId
    }

    /** Inserta una nueva elección en la base de datos. */
    fun insertarEleccion(eleccion: Eleccion) = viewModelScope.launch {
        repository.insertarEleccion(eleccion)
    }

    /** Actualiza el estado de una elección (ej: "Programada" -> "En curso"). */
    fun actualizarEleccion(eleccion: Eleccion) = viewModelScope.launch {
        repository.actualizarEleccion(eleccion)
    }

    /** Inserta un nuevo puesto electoral. */
    fun insertarPuesto(puesto: PuestoElectoral) = viewModelScope.launch {
        repository.insertarPuesto(puesto)
    }

    /** Actualiza un puesto electoral. */
    fun actualizarPuesto(puesto: PuestoElectoral) = viewModelScope.launch {
        repository.actualizarPuesto(puesto)
    }

    /** Elimina un puesto electoral. */
    fun eliminarPuesto(puestoId: Int) = viewModelScope.launch {
        repository.eliminarPuesto(puestoId)
    }

    /** Inserta una nueva postulación. */
    fun insertarPostulacion(postulacion: Postulacion) = viewModelScope.launch {
        try {
            repository.insertarPostulacion(postulacion)
        } catch (e: IllegalStateException) {
            // Manejar error de postulación duplicada
            throw e
        }
    }

    /** Elimina una postulación. */
    fun eliminarPostulacion(postulacionId: Int) = viewModelScope.launch {
        repository.eliminarPostulacion(postulacionId)
    }

    /**
     * Registra los votos de un puesto electoral:
     * - Actualiza los votos de cada postulación
     * - Actualiza votos nulos y blancos
     * - Cierra el puesto (estado = "Cerrado")
     */
    fun registrarVotosPorPuesto(
        puestoId: Int,
        postulaciones: List<Postulacion>,
        votosNulos: Int,
        votosBlancos: Int
    ) = viewModelScope.launch {
        repository.registrarVotosPorPuesto(puestoId, postulaciones, votosNulos, votosBlancos)
        
        // Intentar cerrar la elección si todos los puestos están cerrados
        val puesto = repository.getPuestoById(puestoId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null).value
        puesto?.let {
            try {
                repository.cerrarEleccionSiCompleta(it.id_eleccion)
            } catch (e: IllegalStateException) {
                // No todos los puestos están cerrados, está bien
            }
        }
    }

    /**
     * Verifica si todos los puestos de una elección están cerrados.
     */
    suspend fun todosLosPuestosCerrados(eleccionId: Int): Boolean {
        return repository.todosLosPuestosCerrados(eleccionId)
    }

    /**
     * Cierra una elección si todos sus puestos están cerrados.
     */
    fun cerrarEleccionSiCompleta(eleccionId: Int) = viewModelScope.launch {
        repository.cerrarEleccionSiCompleta(eleccionId)
    }

    /**
     * Verifica si un puesto puede ser modificado (no debe estar cerrado).
     */
    suspend fun puedeModificarPuesto(puestoId: Int): Boolean {
        return repository.puedeModificarPuesto(puestoId)
    }

    /**
     * Verifica si un frente tiene candidatos.
     */
    suspend fun frenteTieneCandidatos(frenteId: Int): Boolean {
        return repository.contarCandidatosPorFrente(frenteId) > 0
    }
}
