package services.bookingapp.DataModel

import com.google.firebase.auth.PhoneAuthProvider


data class AuthFlowState(
    val isLoggedIn: Boolean = false,
    val isFirstTime: Boolean = false,
    val checked: Boolean = false // ✅ Add this
)

data class UiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null
)

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val bio: String = "",
    val address: String = "",
    val isFirstLogin: Boolean = true
)
