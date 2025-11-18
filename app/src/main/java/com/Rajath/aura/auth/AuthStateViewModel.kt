package com.Rajath.aura.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthStateViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _uid = MutableStateFlow<String?>(auth.currentUser?.uid)
    val uid = _uid.asStateFlow()

    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _uid.value = firebaseAuth.currentUser?.uid
    }

    init {
        auth.addAuthStateListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(listener)
    }
}