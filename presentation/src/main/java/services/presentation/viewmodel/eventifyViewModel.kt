package services.presentation.viewmodel

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import services.data.datamodel.AuthFlowState
import services.data.datamodel.UiState
import services.data.datamodel.User
import services.data.repository.EventifyRepository
import javax.inject.Inject

@HiltViewModel
class EventifyViewModel @Inject constructor(
    private val repository: EventifyRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    private val _authState = MutableStateFlow(false)

    private val _isFirstTime = MutableStateFlow(false)
    val isFirstTime: StateFlow<Boolean> = _isFirstTime

    private val _authFlowState = MutableStateFlow(AuthFlowState())
    val authFlowState: StateFlow<AuthFlowState> = _authFlowState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun setLoading() {
        _uiState.value = UiState(loading = true)
    }

    fun setSuccess() {
        _uiState.value = UiState(success = true)
    }

    fun resetState() {
        _uiState.value = UiState()
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message,
            loading = false
        )
    }

    fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        phone: String,
        onResult: (Boolean) -> Unit
    ) {
        setLoading()
        viewModelScope.launch {
            val success = repository.registerWithEmail(email, password, name, phone)
            _authState.value = success
            onResult(success)
            if (success) setSuccess() else setError("Registration failed")
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        setLoading()
        viewModelScope.launch {
            val success = repository.loginWithEmail(email, password)
            _authState.value = success
            onResult(success)
            if (success) setSuccess() else setError("Login failed")
        }
    }

    fun setFirstTime(value: Boolean) {
        val uid = repository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            repository.updateUserProfile(uid, mapOf("isFirstLogin" to value))
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        setLoading()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            val success = repository.signInWithCredential(credential)
            _authState.value = success
            onResult(success)
            if (success) setSuccess() else setError("Google sign-in failed")
        }
    }

    fun checkUserSessionAndFirstTime() {
        viewModelScope.launch {
            _authFlowState.value = repository.getAuthFlowStatus()
        }
    }

    fun updateUserProfile(uid: String, area: String, budget: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val updates = mapOf(
                "area" to area,
                "budget" to budget,
                "isFirstLogin" to false
            )
            val success = repository.updateUserProfile(uid, updates)
            if (success) {
                _isFirstTime.value = false
                _currentUser.value = _currentUser.value?.copy(isFirstLogin = false)
            }
            onComplete(success)
        }
    }

    fun fetchUserData(uid: String, onComplete: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.fetchUserData(uid)
            _currentUser.value = user
            onComplete(user)
        }
    }

    fun fetchCurrentUserName(onResult: (String?) -> Unit) {
        val uid = repository.getCurrentUser()?.uid
        if (uid != null) {
            viewModelScope.launch {
                val user = repository.fetchUserData(uid)
                onResult(user?.name)
            }
        } else {
            onResult(null)
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        repository.sendVerificationCode(phoneNumber, activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    val success = repository.signInWithCredential(credential)
                    _authState.value = success
                    if (success) setSuccess()
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                setError(e.localizedMessage ?: "Verification failed")
                _authState.value = false
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.value = _uiState.value.copy(verificationId = verificationId, resendToken = token)
            }
        })
    }

    fun verifyCode(code: String) {
        val verificationId = _uiState.value.verificationId
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneCredential(credential)
        } else {
            setError("Verification ID is missing")
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val success = repository.signInWithCredential(credential)
            _authState.value = success
            if (success) setSuccess() else setError("Phone sign-in failed")
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = false
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = repository.getCurrentUser() != null
}
