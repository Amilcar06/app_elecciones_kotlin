package com.elecciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elecciones.data.entities.Candidato
import com.elecciones.repository.EleccionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Candidatos.
 */
class CandidatoViewModel(private val repository: EleccionesRepository) : ViewModel() {

    // MutableStateFlow para mantener el ID del frente actualmente seleccionado.
    private val _frenteId = MutableStateFlow<Int?>(null)

    /**
     * Expone la lista de candidatos para el frente seleccionado.
     * Utiliza flatMapLatest para cambiar automáticamente la fuente del Flow
     * cada vez que el _frenteId cambia.
     */
    val candidatos: StateFlow<List<Candidato>> = _frenteId.flatMapLatest { frenteId ->
        if (frenteId != null) {
            repository.getCandidatosPorFrente(frenteId)
        } else {
            flowOf(emptyList()) // Si no hay ID, emite una lista vacía
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Expone la lista de todos los candidatos.
     */
    val todosLosCandidatos: StateFlow<List<Candidato>> = repository.todosLosCandidatos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado de carga para operaciones asíncronas
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Establece el ID del frente para el cual se deben cargar los candidatos.
     */
    fun setFrenteId(frenteId: Int) {
        _frenteId.value = frenteId
    }

    /**
     * Inserta un nuevo candidato.
     */
    fun insertarCandidato(candidato: Candidato) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.insertarCandidato(candidato)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Actualiza un candidato existente.
     */
    fun actualizarCandidato(candidato: Candidato) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.actualizarCandidato(candidato)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Elimina un candidato.
     */
    fun eliminarCandidato(candidato: Candidato) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.eliminarCandidato(candidato)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Verifica si existe un candidato con el CI dado.
     */
    suspend fun existeCI(ci: String): Boolean {
        return repository.existeCI(ci)
    }
    
    /**
     * Verifica si existe un candidato con el CI dado, excluyendo un ID específico.
     */
    suspend fun existeCIExcluyendo(ci: String, excluirId: Int): Boolean {
        return repository.existeCIExcluyendo(ci, excluirId)
    }
    
    /**
     * Verifica si existe un candidato con el correo dado.
     */
    suspend fun existeCorreo(correo: String): Boolean {
        return repository.existeCorreo(correo)
    }
    
    /**
     * Verifica si existe un candidato con el correo dado, excluyendo un ID específico.
     */
    suspend fun existeCorreoExcluyendo(correo: String, excluirId: Int): Boolean {
        return repository.existeCorreoExcluyendo(correo, excluirId)
    }
}
