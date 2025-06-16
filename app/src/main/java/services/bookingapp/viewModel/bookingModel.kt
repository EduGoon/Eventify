package com.yourapp.booking.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.yourapp.booking.viewmodel.BookingViewModel.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import services.bookingapp.DataModel.AuthFlowState
import services.bookingapp.DataModel.UiState
import services.bookingapp.DataModel.User
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor() : ViewModel() {
    annotation class HiltViewModel

    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(false)

    private val _isFirstTime = MutableStateFlow(false)
    val isFirstTime: StateFlow<Boolean> = _isFirstTime

    private val _authFlowState = MutableStateFlow(AuthFlowState())
    val authFlowState: StateFlow<AuthFlowState> = _authFlowState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun setLoading(){
        _uiState.value = UiState(loading = true)
    }

    fun setSuccess() {
        _uiState.value = UiState(success = true)
    }

    fun resetState() {
        _uiState.value = UiState()
    }

    fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        phone: String,
        onResult: (Boolean) -> Unit
    ) {
        setLoading()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    val newUser = User(
                        uid = it.uid,
                        email = email,
                        name = name,
                        phone = phone,
                        isFirstLogin = true
                    )
                    saveUserToFirestore(newUser) {
                        onResult(it)
                        _authState.value = it
                        if (it) setSuccess()
                    }
                } ?: onResult(false)
            } else {
                onResult(false)
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        setLoading()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = true
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun setFirstTime(value: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .update("isFirstLogin", value)
            .addOnSuccessListener {
                Log.d("ViewModel", "First login flag updated")
            }
            .addOnFailureListener {
                Log.e("ViewModel", "Failed to update flag", it)
            }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        setLoading()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                firebaseUser?.let {
                    checkAndCreateUserIfNeeded(it.uid, it.email ?: "", it.displayName ?: "", it.phoneNumber ?: "") {
                        onResult(it)
                        _authState.value = it
                        if (it) setSuccess()
                    }
                } ?: onResult(false)
            } else {
                onResult(false)
            }
        }
    }

    private fun checkAndCreateUserIfNeeded(uid: String, email: String, name: String, phone: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val user = User(uid, email, name, phone, "", "", isFirstLogin = true)
                saveUserToFirestore(user, onComplete)
            } else {
                onComplete(true)
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message,
            loading = false
        )
    }

    private fun saveUserToFirestore(user: User, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(user.uid).set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun checkUserSessionAndFirstTime() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            checkIfFirstTime(user.uid) { isFirst ->
                _authFlowState.value = AuthFlowState(
                    isLoggedIn = true,
                    isFirstTime = isFirst,
                    checked = true
                )
            }
        } else {
            _authFlowState.value = AuthFlowState(
                isLoggedIn = false,
                isFirstTime = false,
                checked = true
            )
        }
    }

    fun checkIfFirstTime(uid: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userDoc = Firebase.firestore
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                val isFirst = userDoc.getBoolean("isFirstLogin") ?: true
                onResult(isFirst)
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error checking first time", e)
                onResult(true) // Assume true if something went wrong
            }
        }
    }

    fun updateUserProfile(uid: String, area: String, budget: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid).update(
            mapOf(
                "area" to area,
                "budget" to budget,
                "isFirstLogin" to false
            )
        ).addOnSuccessListener {
            _isFirstTime.value = false
            _currentUser.value = _currentUser.value?.copy(isFirstLogin = false) // ✅ updates collected state
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun fetchUserData(uid: String, onComplete: (User?) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject<User>()
                _currentUser.value = user
                onComplete(user)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun fetchCurrentUserName(onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject<User>()
                    onResult(user?.name)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto verification (instant or auto-retrieval)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            _authState.value = task.isSuccessful
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _uiState.value = _uiState.value.copy(error = e.localizedMessage)
                    _authState.value = false
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _uiState.value = _uiState.value.copy(verificationId = verificationId, resendToken = token)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    fun verifyCode(code: String) {
        val verificationId = _uiState.value.verificationId
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneCredential(credential)
        } else {
            _uiState.value = _uiState.value.copy(error = "Verification ID is missing")
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val newUser = User(
                            uid = it.uid,
                            email = it.email ?: "",
                            name = it.displayName ?: "",
                            phone = it.phoneNumber ?: "",
                            isFirstLogin = true
                        )
                        saveUserToFirestore(newUser) {
                            _authState.value = it
                            _uiState.value = _uiState.value.copy(success = it)
                        }
                    } ?: run {
                        _uiState.value = _uiState.value.copy(error = "Failed to retrieve user after sign in")
                        _authState.value = false
                    }
                } else {
                    _uiState.value = _uiState.value.copy(error = task.exception?.localizedMessage)
                    _authState.value = false
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = false
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
