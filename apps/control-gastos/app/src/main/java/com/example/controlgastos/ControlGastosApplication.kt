package com.example.controlgastos

import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.controlgastos.data.local.ControlGastosDatabase
import com.example.controlgastos.data.repository.GastosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ControlGastosApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var gastosRepository: GastosRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = ControlGastosDatabase.obtenerInstancia(this)
        gastosRepository = GastosRepository(database.categoriaDao(), database.gastoDao(), resources)
        applicationScope.launch {
            try {
                gastosRepository.obtenerCategorias().first()
            } catch (error: SQLiteException) {
                Log.e("ControlGastos", "No se pudo abrir la base de datos al arrancar.", error)
            }
        }
    }
}
