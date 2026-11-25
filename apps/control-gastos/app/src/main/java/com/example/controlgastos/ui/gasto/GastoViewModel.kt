package com.example.controlgastos.ui.gasto

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.controlgastos.ControlGastosApplication
import com.example.controlgastos.R
import com.example.controlgastos.data.local.Categoria
import com.example.controlgastos.data.local.Gasto
import com.example.controlgastos.data.repository.GastosRepository
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val MAXIMO_NOTA = 200

internal data class CamposGasto(
    val monto: String = "",
    val categoriaId: Long? = null,
    val fecha: LocalDate = LocalDate.now(),
    val nota: String = ""
)

internal data class EstadoFormulario(
    val editando: Boolean,
    val campos: CamposGasto = CamposGasto(),
    val camposIniciales: CamposGasto = campos,
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = true,
    val ocupado: Boolean = false,
    @param:StringRes val errorCarga: Int? = null
) {
    val hayCambios get() = campos != camposIniciales
    val disponible get() = !cargando && errorCarga == null && !ocupado
    val puedeGuardar get() =
        disponible && campos.categoriaId != null && campos.nota.length <= MAXIMO_NOTA &&
            (textoACentavos(campos.monto) ?: 0L) > 0L
}

internal sealed interface EventoFormulario {
    data class Cerrar(@param:StringRes val aviso: Int? = null) : EventoFormulario
    data class Error(@param:StringRes val respaldo: Int, val mensaje: String? = null) :
        EventoFormulario
}

internal class GastoViewModel(
    private val repositorio: GastosRepository,
    private val gastoId: Long?
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoFormulario(editando = gastoId != null))
    val estado = _estado.asStateFlow()
    private val _eventos = Channel<EventoFormulario>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()
    private var gastoOriginal: Gasto? = null

    init {
        viewModelScope.launch {
            try {
                val categorias = repositorio.obtenerCategorias().first()
                val gasto = gastoId?.let { repositorio.obtenerGastoPorId(it).first() }
                if (gastoId != null && gasto == null) {
                    _estado.update { it.copy(ocupado = true) }
                    _eventos.send(EventoFormulario.Cerrar(R.string.gasto_no_encontrado))
                    return@launch
                }
                gastoOriginal = gasto
                _estado.update { actual ->
                    val campos = gasto?.let {
                        CamposGasto(
                            monto = centavosParaEditar(it.montoCentavos),
                            categoriaId = it.categoriaId,
                            fecha = it.fecha,
                            nota = it.nota.orEmpty()
                        )
                    } ?: actual.campos
                    actual.copy(
                        campos = campos,
                        camposIniciales = campos,
                        categorias = categorias,
                        cargando = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _estado.update { it.copy(cargando = false, errorCarga = R.string.error_cargar_formulario) }
            }
        }
    }

    fun cambiarMonto(texto: String) {
        val monto = normalizarEntradaMonto(texto) ?: return
        editar { copy(monto = monto) }
    }

    fun cambiarCategoria(id: Long) = editar { copy(categoriaId = id) }

    fun cambiarFecha(fecha: LocalDate) {
        if (!fecha.isAfter(LocalDate.now())) editar { copy(fecha = fecha) }
    }

    fun cambiarNota(nota: String) {
        editar {
            if (nota.length <= MAXIMO_NOTA || nota.length < this.nota.length) copy(nota = nota)
            else this
        }
    }

    fun guardar() {
        val actual = _estado.value
        if (!actual.puedeGuardar) return
        val campos = actual.campos
        val monto = textoACentavos(campos.monto) ?: return
        val categoriaId = campos.categoriaId ?: return
        val gasto = Gasto(gastoId ?: 0L, monto, categoriaId, campos.fecha, campos.nota)
        persistir(R.string.error_guardar_gasto) {
            if (gastoId == null) repositorio.guardarGasto(gasto) > 0L
            else repositorio.actualizarGasto(gasto) > 0
        }
    }

    fun eliminar() {
        val gasto = gastoOriginal ?: return
        persistir(R.string.error_eliminar_gasto) { repositorio.eliminarGasto(gasto) > 0 }
    }

    private fun editar(cambio: CamposGasto.() -> CamposGasto) {
        _estado.update { if (it.disponible) it.copy(campos = it.campos.cambio()) else it }
    }

    private fun persistir(@StringRes errorOperacion: Int, operacion: suspend () -> Boolean) {
        if (!_estado.value.disponible) return
        _estado.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            var cerrando = false
            try {
                val existe = operacion()
                cerrando = true
                _eventos.send(EventoFormulario.Cerrar(if (existe) null else R.string.gasto_no_encontrado))
            } catch (error: CancellationException) {
                throw error
            } catch (error: IllegalArgumentException) {
                _eventos.send(EventoFormulario.Error(errorOperacion, error.message))
            } catch (_: Exception) {
                _eventos.send(EventoFormulario.Error(errorOperacion))
            } finally {
                if (!cerrando) _estado.update { it.copy(ocupado = false) }
            }
        }
    }

    companion object {
        fun factory(gastoId: Long?) = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as ControlGastosApplication
                GastoViewModel(aplicacion.gastosRepository, gastoId)
            }
        }
    }
}
