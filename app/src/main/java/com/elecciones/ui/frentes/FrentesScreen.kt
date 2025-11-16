package com.elecciones.ui.frentes

// Necesitarás una forma de obtener el repositorio, esto es un placeholder
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
import androidx.compose.ui.tooling.preview.Preview
import com.elecciones.data.entities.Frente
import com.elecciones.ui.componentes.CardFrente
import com.elecciones.viewmodel.FrenteViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrentesScreen(
    frenteViewModel: FrenteViewModel,
    onFrenteClick: (Int) -> Unit,
    onAddFrenteClick: () -> Unit,
    onEditFrenteClick: (Int) -> Unit = {},
    onDeleteFrenteClick: (Int) -> Unit = {}
) {
    // Recolectamos el estado del ViewModel. Cada vez que la lista de frentes
    // cambie en la DB, la UI se recompondrá automáticamente.
    val frentes by frenteViewModel.todosLosFrentes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frentes Registrados") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFrenteClick) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Nuevo Frente")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(frentes) { frente ->
                CardFrente(
                    frente = frente,
                    onClick = { onFrenteClick(frente.id_frente) },
                    onEditClick = { onEditFrenteClick(frente.id_frente) },
                    onDeleteClick = { onDeleteFrenteClick(frente.id_frente) }
                )
            }
        }
    }
}

// Preview con datos falsos para desarrollo
@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun FrentesScreenPreview() {
    // Para el Preview, no podemos instanciar el ViewModel real fácilmente.
    // Creamos una lista falsa de frentes.
    val frentesDePrueba = listOf(
        Frente(1, "Frente Tech", "#FF0000", "", "2021-01-01", ""),
        Frente(2, "Frente Data", "#0000FF", "", "2022-05-10", "")
    )

    // Aquí simulamos el comportamiento del ViewModel
    val fakeViewModel: Unit = /* Necesitarías una instancia falsa del repo */
    // Por ahora, mostraremos el Scaffold con una lista vacía en el preview real
    // o podemos crear un ViewModel falso.
    // La implementación del Composable es lo más importante aquí.

        // Mostraremos un estado simple para el preview:
        Scaffold(
            topBar = { TopAppBar(title = { Text("Frentes Registrados") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, "") } }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(frentesDePrueba) { frente ->
                    CardFrente(frente = frente, onClick = {})
                }
            }
        }
}
