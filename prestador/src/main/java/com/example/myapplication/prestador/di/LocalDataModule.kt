package com.example.myapplication.prestador.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.prestador.datos.local.PrestadorDatabase
import com.example.myapplication.prestador.datos.local.dao.PresupuestoDao
import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import com.example.myapplication.prestador.datos.local.dao.MovimientoStockDao
import com.example.myapplication.core.datos.local.dao.ExcepcionHorariaDao
import com.example.myapplication.prestador.datos.local.dao.PrestadorExcepcionHorariaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun providePrestadorDatabase(@ApplicationContext context: Context): PrestadorDatabase {
        return Room.databaseBuilder(
            context,
            PrestadorDatabase::class.java,
            "prestador_private_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePresupuestoDao(db: PrestadorDatabase): PresupuestoDao = db.presupuestoDao()

    @Provides
    fun provideProductoDao(db: PrestadorDatabase): ProductoDao = db.ProductoDao()

    @Provides
    fun provideMovimientoStockDao(db: PrestadorDatabase): MovimientoStockDao = db.movimientoStockDao()

    @Provides
    fun providePrestadorExcepcionHorariaDao(db: PrestadorDatabase): PrestadorExcepcionHorariaDao = db.excepcionHorariaDao()
}
