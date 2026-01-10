package services.data.datamodel

data class AuthFlowState(
    val isLoggedIn: Boolean = false,
    val isFirstTime: Boolean = false,
    val checked: Boolean = false // ✅ Add this
)
