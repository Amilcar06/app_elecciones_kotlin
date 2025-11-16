package com.elecciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elecciones.repository.EleccionesRepository

/**
 * Factory para crear instancias de ViewModels que requieren EleccionesRepository como dependencia.
 * Esto nos permite inyectar el repositorio en nuestros ViewModels de forma centralizada.
 */
class EleccionesViewModelFactory(private val repository: EleccionesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FrenteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FrenteViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CandidatoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CandidatoViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(EleccionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EleccionViewModel(repository) as T
        }
        // Añadir más ViewModels aquí a medida que se creen
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
