package com.example.controlgastos.ui.gasto

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlgastos.R
import kotlinx.coroutines.flow.collectLatest

private enum class DialogoFormulario { Fecha, Eliminar, Descartar }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormularioGasto(viewModel: GastoViewModel, onVolver: (Int?) -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val recursos = LocalResources.current
    val volverActual by rememberUpdatedState(onVolver)
    val foco = LocalFocusManager.current
    var dialogo by rememberSaveable { mutableStateOf<DialogoFormulario?>(null) }

    LaunchedEffect(viewModel, snackbar, recursos) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EventoFormulario.Cerrar -> volverActual(evento.aviso)
                is EventoFormulario.Error ->
                    snackbar.showSnackbar(evento.mensaje ?: recursos.getString(evento.respaldo))
            }
        }
    }

    val solicitarSalida: () -> Unit = {
        if (!estado.ocupado) {
            if (estado.hayCambios) {
                foco.clearFocus()
                dialogo = DialogoFormulario.Descartar
            } else {
                onVolver(null)
            }
        }
    }
    BackHandler(onBack = solicitarSalida)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (estado.editando) R.string.editar_gasto else R.string.nuevo_gasto))
                },
                navigationIcon = {
                    IconButton(onClick = solicitarSalida, enabled = !estado.ocupado) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.imePadding()) }
    ) { padding ->
        val contenido = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()

        val errorCarga = estado.errorCarga
        when {
            estado.cargando -> Box(contenido, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorCarga != null -> Box(
                contenido.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(errorCarga), color = MaterialTheme.colorScheme.error)
            }
            else -> CamposFormulario(
                estado = estado,
                viewModel = viewModel,
                onFecha = {
                    foco.clearFocus()
                    dialogo = DialogoFormulario.Fecha
                },
                onEliminar = {
                    foco.clearFocus()
                    dialogo = DialogoFormulario.Eliminar
                },
                modifier = contenido
            )
        }
    }

    val cerrarDialogo = { dialogo = null }
    when (dialogo) {
        DialogoFormulario.Fecha -> SelectorFecha(
            fecha = estado.campos.fecha,
            onCerrar = cerrarDialogo,
            onElegir = {
                viewModel.cambiarFecha(it)
                cerrarDialogo()
            }
        )
        DialogoFormulario.Eliminar -> Confirmacion(
            titulo = R.string.confirmar_eliminar,
            confirmar = R.string.eliminar,
            cancelar = R.string.cancelar,
            onCancelar = cerrarDialogo,
            onConfirmar = {
                cerrarDialogo()
                viewModel.eliminar()
            }
        )
        DialogoFormulario.Descartar -> Confirmacion(
            titulo = R.string.confirmar_descartar,
            confirmar = R.string.descartar,
            cancelar = R.string.seguir_editando,
            onCancelar = cerrarDialogo,
            onConfirmar = {
                cerrarDialogo()
                onVolver(null)
            }
        )
        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CamposFormulario(
    estado: EstadoFormulario,
    viewModel: GastoViewModel,
    onFecha: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier
) {
    val campos = estado.campos
    var menuAbierto by remember { mutableStateOf(false) }
    val categoria = estado.categorias.firstOrNull { it.id == campos.categoriaId }
    val focoNota = remember { FocusRequester() }
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    val cerrarTeclado = {
        foco.clearFocus()
        teclado?.hide()
    }

    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = campos.monto,
                onValueChange = viewModel::cambiarMonto,
                modifier = Modifier.fillMaxWidth(),
                enabled = estado.disponible,
                label = { Text(stringResource(R.string.monto)) },
                prefix = { Text(stringResource(R.string.simbolo_peso)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focoNota.requestFocus() }),
                singleLine = true
            )
            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { menuAbierto = it && estado.disponible },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = categoria?.nombre.orEmpty(),
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, estado.disponible)
                        .fillMaxWidth(),
                    enabled = estado.disponible,
                    readOnly = true,
                    label = { Text(stringResource(R.string.categoria)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    estado.categorias.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion.nombre) },
                            onClick = {
                                viewModel.cambiarCategoria(opcion.id)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = onFecha,
                enabled = estado.disponible,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Column {
                        Text(stringResource(R.string.fecha), style = MaterialTheme.typography.labelMedium)
                        Text(formatearFecha(campos.fecha), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            OutlinedTextField(
                value = campos.nota,
                onValueChange = viewModel::cambiarNota,
                modifier = Modifier.fillMaxWidth().focusRequester(focoNota),
                enabled = estado.disponible,
                label = { Text(stringResource(R.string.nota_opcional)) },
                supportingText = { Text(stringResource(R.string.contador_nota, campos.nota.length, MAXIMO_NOTA)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { cerrarTeclado() }),
                minLines = 2,
                maxLines = 4
            )
            Button(
                onClick = {
                    cerrarTeclado()
                    viewModel.guardar()
                },
                enabled = estado.puedeGuardar,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.guardar))
            }
            if (estado.editando) {
                TextButton(
                    onClick = onEliminar,
                    enabled = estado.disponible,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.eliminar))
                }
            }
        }
    }
}

@Composable
private fun Confirmacion(
    @StringRes titulo: Int,
    @StringRes confirmar: Int,
    @StringRes cancelar: Int,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(titulo)) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(stringResource(confirmar)) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(cancelar)) } }
    )
}
