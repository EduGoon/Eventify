package services.data

import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>>
    fun signInWithGoogle(idToken: String): Flow<Result<AuthResult>>
    fun getCurrentUser(): User?
    fun signOut()
}