package com.nttung.oufood.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.domain.model.User
import com.nttung.oufood.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Vui lòng nhập đầy đủ email và mật khẩu")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signIn(email, pass)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, phone: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || phone.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Vui lòng điền đầy đủ tất cả các trường")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState(errorMessage = "Mật khẩu tối thiểu 6 ký tự")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUp(email, pass, name, phone)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Vui lòng nhập email")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState()
    }
}
