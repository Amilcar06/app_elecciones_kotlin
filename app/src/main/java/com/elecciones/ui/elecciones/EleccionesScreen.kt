package com.elecciones.ui.elecciones

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.elecciones.ui.componentes.CardEleccion
import com.elecciones.viewmodel.EleccionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EleccionesScreen(
    eleccionViewModel: EleccionViewModel,
    onAddEleccionClick: () -> Unit,
    onEleccionClick: (Int) -> Unit,
    onEditEleccionClick: ((Int) -> Unit)? = null
) {
    val elecciones by eleccionViewModel.todasLasElecciones.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Elecciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEleccionClick) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nueva Elección")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(elecciones) { eleccion ->
                CardEleccion(
                    eleccion = eleccion,
                    onMenuClick = { onEleccionClick(eleccion.id_eleccion) },
                    onEditClick = if (onEditEleccionClick != null) {
                        { onEditEleccionClick!!(eleccion.id_eleccion) }
                    } else {
                        null
                    }
                )
            }
        }
    }
}