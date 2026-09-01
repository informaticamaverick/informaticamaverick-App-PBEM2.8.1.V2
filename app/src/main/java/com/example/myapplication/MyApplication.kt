package com.example.myapplication

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.myapplication.BuildConfig
import com.google.firebase.FirebaseApp
// import com.google.firebase.appcheck.FirebaseAppCheck
// import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
// import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // [ELITE EMERGENCY]: Desactivamos App Check temporalmente para evitar error 403 
        // mientras se configura correctamente en la consola de Google.
        /*
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            FirebaseApp.initializeApp(this@MyApplication)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        }
        */
        FirebaseApp.initializeApp(this) // Inicialización básica sin App Check
    }
}

































