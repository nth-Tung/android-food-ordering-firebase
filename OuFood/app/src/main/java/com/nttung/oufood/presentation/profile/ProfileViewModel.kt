package com.nttung.oufood.presentation.profile

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

data class ProfileUiState(
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfile(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) {
            _uiState.value = ProfileUiState(errorMessage = "Tên và số điện thoại không được để trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState(isUpdating = true)
            when (val result = authRepository.updateProfile(name, phone)) {
                is Resource.Success -> {
                    _uiState.value = ProfileUiState(updateSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = ProfileUiState(errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState()
    }
}
