package services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import services.data.AuthRepository
import services.data.User
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession = _isCheckingSession.asStateFlow()

    val authState: StateFlow<User?> = authRepository.getAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.getCurrentUser()
        )

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _isCheckingSession.value = true
            // validateSession will reload the firebase user to check if they still exist/are valid
            authRepository.validateSession().first() 
            _isCheckingSession.value = false
        }
    }

    fun getCurrentUser(): User? = authRepository.getCurrentUser()
}
