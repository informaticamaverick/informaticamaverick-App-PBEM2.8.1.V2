package com.example.myapplication.prestador

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.prestador.data.migration.FirestoreMigration
import com.example.myapplication.prestador.ui.navigation.PrestadorNavGraph
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val editProfileViewModel: EditProfileViewModel by viewModels()


    private fun presenceRef() = com.google.firebase.auth.FirebaseAuth.getInstance()
        .currentUser?.uid?.let {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .reference.child("users").child(it).child("online")
        }

    override fun onResume() {
        super.onResume()
        com.google.firebase.database.FirebaseDatabase.getInstance().goOnline()
        val ref = presenceRef()
        ref?.apply {
            setValue(true)
            onDisconnect().setValue(false)
        }
    }

    override fun onStop() {
        super.onStop()
        presenceRef()?.setValue(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Migración Firestore: limpia campos viejos del prestador logueado
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            lifecycleScope.launch {
                FirestoreMigration.runIfNeeded(
                    context = applicationContext,
                    firestore = FirebaseFirestore.getInstance(),
                    uid = uid
                )
            }
        }

        //      println("MainActivity: ChatSimulationViewModel creado (${chatSimulationViewModel.hashCode()})")

        // 🔥 [NUEVO] Sincronización proactiva de Topics al iniciar la App
        lifecycleScope.launch {
            editProfileViewModel.profileState.collectLatest { state ->
                if (state is ProfileState.Success) {
                    val provider = state.provider
                    // Recopilamos todas las combinaciones posibles para suscribir el dispositivo
                    val allCp = (listOfNotNull(provider.address?.codigoPostal) + provider.addresses.map { it.codigoPostal }).filter { it.isNotBlank() }.distinct()
                    val allCats = (provider.categories + provider.companies.flatMap { it.categories }).filter { it.isNotBlank() }.distinct()
                    
                    if (allCp.isNotEmpty() && allCats.isNotEmpty()) {
                        allCp.forEach { cp ->
                            editProfileViewModel.syncTopics(
                                cp = cp,
                                categories = allCats,
                                isSubscribed = provider.isSubscribed
                            )
                        }
                    }
                }
            }
        }

        setContent {
            PrestadorTheme {
                val navController = rememberNavController()
                PrestadorNavGraph(
                    navController = navController
                )
            }
        }

        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val senderId = it.getStringExtra("senderId")
            if (!senderId.isNullOrBlank()) {
//                chatSimulationViewModel.setPendingOpenUsersId(senderId)
            }
        }
    }
}
