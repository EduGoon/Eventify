package services.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(task.result!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Sign up failed")))
                }
                close()
            }
        awaitClose()
    }

    override fun signInWithGoogle(idToken: String): Flow<Result<AuthResult>> = callbackFlow {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(task.result!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Google sign in failed")))
                }
                close()
            }
        awaitClose()
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let {
            User(
                id = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: "",
                photoUrl = it.photoUrl?.toString() ?: ""
            )
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}