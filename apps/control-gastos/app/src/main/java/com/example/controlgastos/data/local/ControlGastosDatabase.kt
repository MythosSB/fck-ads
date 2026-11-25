package com.example.controlgastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Categoria::class, Gasto::class], version = 1, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class ControlGastosDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun gastoDao(): GastoDao

    internal class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val categorias = listOf("Comida", "Transporte", "Hogar", "Ocio", "Salud", "Otros")
            for (nombre in categorias) {
                db.execSQL("INSERT OR IGNORE INTO categorias (nombre) VALUES (?)", arrayOf(nombre))
            }
        }
    }

    companion object {
        @Volatile
        private var instancia: ControlGastosDatabase? = null

        fun obtenerInstancia(context: Context): ControlGastosDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    ControlGastosDatabase::class.java,
                    "controlgastos.db"
                ).addCallback(DatabaseCallback()).build().also { instancia = it }
            }
    }
}
