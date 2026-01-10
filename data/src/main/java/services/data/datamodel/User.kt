package services.data.datamodel

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val isFirstLogin: Boolean = true
)
