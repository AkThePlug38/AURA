package com.Rajath.aura.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val isNewUser: Boolean = false) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _uiState.value = AuthState.Loading
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser ?: false
                        _uiState.value = AuthState.Success(isNew)
                        onResult(true, if (isNew) "new" else "existing")
                    } else {
                        _uiState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign in failed")
                        onResult(false, task.exception?.localizedMessage)
                    }
                }
        }
    }

    fun signUpWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _uiState.value = AuthState.Loading
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser ?: true
                        _uiState.value = AuthState.Success(isNew)
                        onResult(true, if (isNew) "new" else "existing")
                    } else {
                        _uiState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign up failed")
                        onResult(false, task.exception?.localizedMessage)
                    }
                }
        }
    }

    fun saveFirstName(firstName: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "No authenticated user")
            return
        }
        _uiState.value = AuthState.Loading
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(firstName)
            .build()
        user.updateProfile(changeRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthState.Success(false)
                    onComplete(true, null)
                } else {
                    _uiState.value = AuthState.Error(task.exception?.localizedMessage ?: "Failed to save name")
                    onComplete(false, task.exception?.localizedMessage)
                }
            }
    }
}
