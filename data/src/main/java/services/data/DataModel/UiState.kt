package services.data.DataModel

import com.google.firebase.auth.PhoneAuthProvider

data class UiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null
)
