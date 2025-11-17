package com.elecciones

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elecciones.data.AppDatabase
import com.elecciones.repository.EleccionesRepository
import com.elecciones.ui.candidatos.CandidatosScreen
import com.elecciones.ui.candidatos.DetalleCandidatoScreen
import com.elecciones.ui.candidatos.RegistrarCandidatoScreen
import com.elecciones.ui.elecciones.DetallePuestoScreen
import com.elecciones.ui.elecciones.EleccionesScreen
import com.elecciones.ui.elecciones.PuestosElectoralesScreen
import com.elecciones.ui.elecciones.RegistrarEleccionScreen
import com.elecciones.ui.elecciones.RegistrarPuestoScreen
import com.elecciones.ui.elecciones.RegistrarVotosPorPuestoScreen
import com.elecciones.ui.elecciones.ResultadosPorPuestoScreen
import com.elecciones.ui.elecciones.ResultadosEleccionScreen
import com.elecciones.ui.elecciones.SeleccionarCandidatoScreen
import com.elecciones.ui.frentes.FrentesScreen
import com.elecciones.ui.frentes.RegistrarFrenteScreen
import com.elecciones.ui.componentes.ConfirmacionEliminarDialog
import com.elecciones.ui.theme.AppEleccionesTheme
import com.elecciones.viewmodel.CandidatoViewModel
import com.elecciones.viewmodel.EleccionViewModel
import com.elecciones.viewmodel.EleccionesViewModelFactory
import com.elecciones.viewmodel.FrenteViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Data class para los items de la barra de navegación
data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy {
        EleccionesRepository(
            database.frenteDao(),
            database.candidatoDao(),
            database.eleccionDao(),
            database.puestoElectoralDao(),
            database.postulacionDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppEleccionesTheme {
                val viewModelFactory = EleccionesViewModelFactory(repository)
                MainScreen(viewModelFactory = viewModelFactory)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModelFactory: EleccionesViewModelFactory) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Frentes", Icons.Default.Group, "frentes"),
        BottomNavItem("Elecciones", Icons.Default.HowToVote, "elecciones")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            viewModelFactory = viewModelFactory,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModelFactory: EleccionesViewModelFactory,
    modifier: Modifier = Modifier
) {
    val frenteViewModel: FrenteViewModel = viewModel(factory = viewModelFactory)
    val candidatoViewModel: CandidatoViewModel = viewModel(factory = viewModelFactory)
    val eleccionViewModel: EleccionViewModel = viewModel(factory = viewModelFactory)
    val elecciones by eleccionViewModel.todasLasElecciones.collectAsState()
    
    NavHost(navController, startDestination = "frentes", modifier = modifier) {
        // --- Pantalla Principal de Frentes ---
        composable("frentes") {
            val frentes by frenteViewModel.todosLosFrentes.collectAsState()
            val candidatos by candidatoViewModel.todosLosCandidatos.collectAsState()
            
            // Estados para el diálogo de confirmación de eliminación
            var mostrarDialogoEliminar by remember { mutableStateOf(false) }
            var frenteAEliminar by remember { mutableStateOf<com.elecciones.data.entities.Frente?>(null) }
            var tieneCand by remember { mutableStateOf(false) }
            var tieneElec by remember { mutableStateOf(false) }
            
            FrentesScreen(
                frenteViewModel = frenteViewModel,
                onFrenteClick = { frenteId -> navController.navigate("candidatos/$frenteId") },
                onAddFrenteClick = { navController.navigate("registrar_frente") },
                onEditFrenteClick = { frenteId -> navController.navigate("editar_frente/$frenteId") },
                onDeleteFrenteClick = { frenteId ->
                    val frente = frentes.find { it.id_frente == frenteId }
                    frente?.let {
                        // Verificar si tiene candidatos
                        val candidatosFrente = candidatos.filter { it.id_frente == frenteId }
                        tieneCand = candidatosFrente.isNotEmpty()
                        
                        // Verificar si está en elecciones (buscar en resultados)
                        tieneElec = elecciones.any { eleccion ->
                            // Verificar si hay resultados para este frente en alguna elección
                            // Esto requiere una verificación más compleja, por ahora solo verificamos si hay elecciones abiertas/programadas
                            eleccion.estado in listOf("Abierta", "Programada")
                        }
                        
                        frenteAEliminar = it
                        mostrarDialogoEliminar = true
                    }
                }
            )
            
            // Diálogo de confirmación
            if (mostrarDialogoEliminar && frenteAEliminar != null) {
                ConfirmacionEliminarDialog(
                    titulo = "Eliminar Frente",
                    mensaje = "¿Está seguro de que desea eliminar el frente \"${frenteAEliminar!!.nombre}\"?",
                    tieneCandidatos = tieneCand,
                    tieneElecciones = tieneElec,
                    onConfirmar = {
                        frenteAEliminar?.let { frenteViewModel.eliminarFrente(it) }
                        mostrarDialogoEliminar = false
                        frenteAEliminar = null
                    },
                    onCancelar = {
                        mostrarDialogoEliminar = false
                        frenteAEliminar = null
                    }
                )
            }
        }

        // --- Pantalla Principal de Elecciones ---
        composable("elecciones") {
            EleccionesScreen(
                eleccionViewModel = eleccionViewModel,
                onAddEleccionClick = { navController.navigate("registrar_eleccion") },
                onEditEleccionClick = { eleccionId ->
                    navController.navigate("editar_eleccion/$eleccionId")
                },
                // Lógica de navegación: 
                // - Si está finalizada, ir a resultados
                // - Si no, ir a puestos electorales
                onEleccionClick = { eleccionId ->
                    // Verificar el estado de la elección desde el StateFlow
                    val elecciones = eleccionViewModel.todasLasElecciones.value
                    val eleccion = elecciones.find { it.id_eleccion == eleccionId }
                    if (eleccion?.estado == "Finalizado" || eleccion?.estado == "Cerrado") {
                        navController.navigate("resultados_eleccion/$eleccionId")
                    } else {
                        navController.navigate("puestos/$eleccionId")
                    }
                }
            )
        }

        // --- NUEVA RUTA: Registrar Elección ---
        composable("registrar_eleccion") {
            RegistrarEleccionScreen(
                eleccionViewModel = eleccionViewModel,
                eleccionId = null,
                onGuardarAccion = { navController.popBackStack()
                }
            )
        }

        // --- NUEVA RUTA: Editar Elección ---
        composable(
            "editar_eleccion/{eleccionId}",
            arguments = listOf(navArgument("eleccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eleccionId = backStackEntry.arguments?.getInt("eleccionId") ?: 0
            RegistrarEleccionScreen(
                eleccionViewModel = eleccionViewModel,
                eleccionId = eleccionId,
                onGuardarAccion = { navController.popBackStack() }
            )
        }

        // --- RUTA: Puestos Electorales de la Elección ---
        composable(
            route = "puestos/{eleccionId}",
            arguments = listOf(navArgument("eleccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eleccionId = backStackEntry.arguments?.getInt("eleccionId") ?: 0
            PuestosElectoralesScreen(
                eleccionViewModel = eleccionViewModel,
                eleccionId = eleccionId,
                onAddPuestoClick = {
                    navController.navigate("registrar_puesto/$eleccionId")
                },
                onPuestoClick = { puestoId ->
                    navController.navigate("detalle_puesto/$puestoId")
                }
            )
        }

        // --- RUTA: Registrar Puesto Electoral ---
        composable(
            route = "registrar_puesto/{eleccionId}",
            arguments = listOf(navArgument("eleccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eleccionId = backStackEntry.arguments?.getInt("eleccionId") ?: 0
            RegistrarPuestoScreen(
                eleccionViewModel = eleccionViewModel,
                eleccionId = eleccionId,
                puestoId = null,
                onGuardarClick = {
                    navController.popBackStack()
                }
            )
        }

        // --- RUTA: Detalle del Puesto (Postulaciones) ---
        composable(
            route = "detalle_puesto/{puestoId}",
            arguments = listOf(navArgument("puestoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val puestoId = backStackEntry.arguments?.getInt("puestoId") ?: 0
            DetallePuestoScreen(
                eleccionViewModel = eleccionViewModel,
                puestoId = puestoId,
                onAddPostulacionClick = {
                    navController.navigate("seleccionar_candidato/$puestoId")
                },
                onRegistrarVotosClick = {
                    navController.navigate("registrar_votos/$puestoId")
                },
                onVerResultadosClick = {
                    navController.navigate("resultados_puesto/$puestoId")
                },
                onEliminarPostulacion = { postulacionId ->
                    eleccionViewModel.eliminarPostulacion(postulacionId)
                }
            )
        }

        // --- RUTA: Seleccionar Candidato para Postulación ---
        composable(
            route = "seleccionar_candidato/{puestoId}",
            arguments = listOf(navArgument("puestoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val puestoId = backStackEntry.arguments?.getInt("puestoId") ?: 0
            SeleccionarCandidatoScreen(
                candidatoViewModel = candidatoViewModel,
                frenteViewModel = frenteViewModel,
                eleccionViewModel = eleccionViewModel,
                puestoId = puestoId,
                onCandidatoSeleccionado = {
                    navController.popBackStack()
                }
            )
        }

        // --- RUTA: Registrar Votos por Puesto ---
        composable(
            route = "registrar_votos/{puestoId}",
            arguments = listOf(navArgument("puestoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val puestoId = backStackEntry.arguments?.getInt("puestoId") ?: 0
            RegistrarVotosPorPuestoScreen(
                eleccionViewModel = eleccionViewModel,
                puestoId = puestoId,
                onVotosRegistrados = {
                    navController.popBackStack()
                }
            )
        }

        // --- RUTA: Resultados de Elección (para elecciones finalizadas) ---
        composable(
            route = "resultados_eleccion/{eleccionId}",
            arguments = listOf(navArgument("eleccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eleccionId = backStackEntry.arguments?.getInt("eleccionId") ?: 0
            ResultadosEleccionScreen(
                eleccionViewModel = eleccionViewModel,
                eleccionId = eleccionId,
                onVerDetallePuesto = { puestoId ->
                    navController.navigate("resultados_puesto/$puestoId")
                }
            )
        }

        // --- RUTA: Resultados por Puesto (detalle completo con gráfico) ---
        composable(
            route = "resultados_puesto/{puestoId}",
            arguments = listOf(navArgument("puestoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val puestoId = backStackEntry.arguments?.getInt("puestoId") ?: 0
            ResultadosPorPuestoScreen(
                eleccionViewModel = eleccionViewModel,
                puestoId = puestoId
            )
        }

        // --- Otras pantallas (no están en la barra de navegación) ---
        composable("registrar_frente") {
            RegistrarFrenteScreen(
                frenteViewModel = frenteViewModel,
                frenteId = null,
                onGuardarAccion = { navController.popBackStack() }
            )
        }

        composable(
            "editar_frente/{frenteId}",
            arguments = listOf(navArgument("frenteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val frenteId = backStackEntry.arguments?.getInt("frenteId") ?: 0
            RegistrarFrenteScreen(
                frenteViewModel = frenteViewModel,
                frenteId = frenteId,
                onGuardarAccion = { navController.popBackStack() }
            )
        }

        composable(
            "candidatos/{frenteId}",
            arguments = listOf(navArgument("frenteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val frenteId = backStackEntry.arguments?.getInt("frenteId") ?: 0
            CandidatosScreen(
                candidatoViewModel = candidatoViewModel,
                frenteViewModel = frenteViewModel,
                frenteId = frenteId,
                onAddCandidatoClick = { navController.navigate("registrar_candidato/$frenteId") },
                onCandidatoClick = { candidatoId -> 
                    navController.navigate("detalle_candidato/$candidatoId")
                },
                onEditCandidatoClick = { candidatoId ->
                    navController.navigate("editar_candidato/$candidatoId")
                },
                onDeleteCandidatoClick = { 
                    navController.popBackStack()
                }
            )
        }

        composable(
            "registrar_candidato/{frenteId}",
            arguments = listOf(navArgument("frenteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val frenteId = backStackEntry.arguments?.getInt("frenteId") ?: 0
            RegistrarCandidatoScreen(
                candidatoViewModel = candidatoViewModel,
                frenteViewModel = frenteViewModel,
                frenteId = frenteId,
                onGuardarAccion = { navController.popBackStack() }
            )
        }
        
        composable(
            "detalle_candidato/{candidatoId}",
            arguments = listOf(navArgument("candidatoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val candidatoId = backStackEntry.arguments?.getInt("candidatoId") ?: 0
            val candidatos by candidatoViewModel.todosLosCandidatos.collectAsState()
            val candidato = candidatos.find { it.id_candidato == candidatoId }
            val frenteId = candidato?.id_frente ?: 0
            
            DetalleCandidatoScreen(
                candidatoViewModel = candidatoViewModel,
                frenteViewModel = frenteViewModel,
                candidatoId = candidatoId,
                onEditarClick = { 
                    navController.navigate("editar_candidato/$candidatoId")
                },
                onEliminarClick = { 
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            "editar_candidato/{candidatoId}",
            arguments = listOf(navArgument("candidatoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val candidatoId = backStackEntry.arguments?.getInt("candidatoId") ?: 0
            val candidatos by candidatoViewModel.todosLosCandidatos.collectAsState()
            val candidato = candidatos.find { it.id_candidato == candidatoId }
            val frenteId = candidato?.id_frente ?: 0
            
            RegistrarCandidatoScreen(
                candidatoViewModel = candidatoViewModel,
                frenteViewModel = frenteViewModel,
                frenteId = frenteId,
                candidatoId = candidatoId,
                onGuardarAccion = { navController.popBackStack() }
            )
        }
    }
}
