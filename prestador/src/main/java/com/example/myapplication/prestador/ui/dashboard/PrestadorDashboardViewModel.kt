package com.example.myapplication.prestador.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel class PrestadorDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository) : ViewModel() {
    fun signOut() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                try {
                    FirebaseDatabase.getInstance()
                        .reference.child("users").child(uid).child("online")
                        .setValue(false).await()
                } catch (e: Exception) { }
            }
            authRepository.signOut()
        }
    }
}
