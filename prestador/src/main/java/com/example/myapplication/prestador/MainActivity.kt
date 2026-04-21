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
//// import com.example.myapplication.prestador.viewmodel.ChatSimulationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

 //   private val chatSimulationViewModel: ChatSimulationViewModel by viewModels()

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
