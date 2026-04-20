package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.User
import com.spendsense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            repository.getUser().collect { user ->
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            val guestUser = User(
                name = "Guest User",
                email = "guest@spendsense.local",
                profilePic = "",
                isGuest = true
            )
            repository.saveUser(guestUser)
        }
    }

    fun signInWithGoogle(name: String, email: String, photoUrl: String) {
        viewModelScope.launch {
            val user = User(
                name = name,
                email = email,
                profilePic = photoUrl,
                isGuest = false
            )
            repository.saveUser(user)
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            // In a real app, you would validate with a backend here.
            // For this project, we'll create a local user based on the email.
            val userName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val user = User(
                name = userName,
                email = email,
                profilePic = "",
                isGuest = false
            )
            repository.saveUser(user)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.deleteUser()
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
}
