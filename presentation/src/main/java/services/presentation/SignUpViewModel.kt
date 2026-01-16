package services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.data.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            authRepository.signUpWithEmail(email, password).collect { result ->
                result.fold(
                    onSuccess = { _uiState.value = SignUpUiState.Success },
                    onFailure = { _uiState.value = SignUpUiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            authRepository.signInWithGoogle(idToken).collect { result ->
                result.fold(
                    onSuccess = { _uiState.value = SignUpUiState.Success },
                    onFailure = { _uiState.value = SignUpUiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }
}

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}