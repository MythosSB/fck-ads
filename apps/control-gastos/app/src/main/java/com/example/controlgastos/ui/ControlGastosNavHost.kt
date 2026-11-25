package com.example.controlgastos.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.controlgastos.R
import com.example.controlgastos.ui.gasto.FormularioGasto
import com.example.controlgastos.ui.gasto.GastoViewModel
import com.example.controlgastos.ui.inicio.Inicio
import com.example.controlgastos.ui.inicio.InicioViewModel
import com.example.controlgastos.ui.mes.MesViewModel
import com.example.controlgastos.ui.resumen.Resumen
import com.example.controlgastos.ui.resumen.ResumenViewModel
import kotlinx.coroutines.launch

private enum class Destino(@param:StringRes val titulo: Int, val icono: ImageVector) {
    Inicio(R.string.inicio, Icons.Filled.Home),
    Resumen(R.string.resumen, Icons.AutoMirrored.Filled.List)
}

private const val ARG_GASTO_ID = "gastoId"
private const val RUTA_GASTO = "gasto"

internal fun rutaFormulario(gastoId: Long? = null): String =
    if (gastoId == null) RUTA_GASTO else "$RUTA_GASTO?$ARG_GASTO_ID=$gastoId"

@Composable
internal fun ControlGastosNavHost() {
    val mesViewModel: MesViewModel = viewModel()
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val recursos = LocalResources.current
    NavHost(navController = navController, startDestination = Destino.Inicio.name) {
        Destino.entries.forEach { destino ->
            composable(destino.name) { entrada ->
                PantallaPrincipal(
                    destino = destino,
                    snackbar = snackbar,
                    onSeleccionar = { seleccionado ->
                        if (seleccionado != destino) {
                            navController.navigate(seleccionado.name) {
                                popUpTo(Destino.Inicio.name)
                                launchSingleTop = true
                            }
                        }
                    },
                    onAgregar = {
                        navController.navigate(rutaFormulario()) { launchSingleTop = true }
                    }
                ) { modifier ->
                    when (destino) {
                        Destino.Inicio -> {
                            val factory = remember(mesViewModel) {
                                InicioViewModel.factory(mesViewModel.mesSeleccionado)
                            }
                            Inicio(
                                mesViewModel = mesViewModel,
                                inicioViewModel = viewModel(viewModelStoreOwner = entrada, factory = factory),
                                onEditar = { gastoId ->
                                    navController.navigate(rutaFormulario(gastoId)) { launchSingleTop = true }
                                },
                                modifier = modifier
                            )
                        }
                        Destino.Resumen -> {
                            val factory = remember(mesViewModel) {
                                ResumenViewModel.factory(mesViewModel.mesSeleccionado)
                            }
                            Resumen(
                                mesViewModel = mesViewModel,
                                resumenViewModel = viewModel(viewModelStoreOwner = entrada, factory = factory),
                                modifier = modifier
                            )
                        }
                    }
                }
            }
        }
        composable(
            route = "$RUTA_GASTO?$ARG_GASTO_ID={$ARG_GASTO_ID}",
            arguments = listOf(navArgument(ARG_GASTO_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entrada ->
            val gastoId = entrada.arguments?.getString(ARG_GASTO_ID)?.toLong()
            val factory = remember(gastoId) { GastoViewModel.factory(gastoId) }
            FormularioGasto(
                viewModel = viewModel(viewModelStoreOwner = entrada, factory = factory),
                onVolver = { aviso ->
                    if (navController.popBackStack() && aviso != null) {
                        scope.launch { snackbar.showSnackbar(recursos.getString(aviso)) }
                    }
                }
            )
        }
    }
}

@Composable
private fun PantallaPrincipal(
    destino: Destino,
    snackbar: SnackbarHostState,
    onSeleccionar: (Destino) -> Unit,
    onAgregar: () -> Unit,
    contenido: @Composable (Modifier) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                Destino.entries.forEach { opcion ->
                    NavigationBarItem(
                        selected = opcion == destino,
                        onClick = { onSeleccionar(opcion) },
                        icon = { Icon(opcion.icono, contentDescription = null) },
                        label = { Text(stringResource(opcion.titulo)) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (destino == Destino.Inicio) {
                FloatingActionButton(onClick = onAgregar) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.agregar_gasto))
                }
            }
        }
    ) { padding ->
        contenido(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding))
    }
}
