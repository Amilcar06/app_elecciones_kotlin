package com.elecciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elecciones.data.entities.Frente
import com.elecciones.repository.EleccionesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Frentes Políticos.
 * Se encarga de la lógica de negocio y expone el estado a la UI.
 */
class FrenteViewModel(private val repository: EleccionesRepository) : ViewModel() {

    /**
     * Expone un Flow con la lista de todos los frentes como un StateFlow.
     * La UI observará este estado para mostrar la lista de frentes.
     * stateIn convierte el Flow "frío" del repositorio en un Flow "caliente" (StateFlow),
     * compartiendo el último valor emitido con todos los colectores.
     */
    val todosLosFrentes: StateFlow<List<Frente>> = repository.todosLosFrentes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // El Flow se mantiene activo 5 seg después de que el último observador desaparece
            initialValue = emptyList() // Valor inicial mientras se carga la data
        )

    /**
     * Inserta un nuevo frente en la base de datos.
     * Se ejecuta en una corrutina lanzada desde viewModelScope.
     */
    fun insertarFrente(frente: Frente) = viewModelScope.launch {
        repository.insertarFrente(frente)
    }

    /**
     * Actualiza un frente existente.
     */
    fun actualizarFrente(frente: Frente) = viewModelScope.launch {
        repository.actualizarFrente(frente)
    }

    /**
     * Elimina un frente.
     */
    fun eliminarFrente(frente: Frente) = viewModelScope.launch {
        repository.eliminarFrente(frente)
    }
}
