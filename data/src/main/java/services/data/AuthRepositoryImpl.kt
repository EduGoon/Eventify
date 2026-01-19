package services.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = "",
                            photoUrl = ""
                        )
                        saveUserToFirestore(user)
                    }
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
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "",
                            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                        )
                        saveUserToFirestore(user)
                    }
                    trySend(Result.success(task.result!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Google sign in failed")))
                }
                close()
            }
        awaitClose()
    }

    private fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.id).set(user)
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

    override fun getAuthState(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: "",
                    photoUrl = it.photoUrl?.toString() ?: ""
                )
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override fun validateSession(): Flow<User?> = callbackFlow {
        val user = firebaseAuth.currentUser
        if (user != null) {
            try {
                user.reload().await()
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    trySend(User(
                        id = currentUser.uid,
                        email = currentUser.email ?: "",
                        displayName = currentUser.displayName ?: "",
                        photoUrl = currentUser.photoUrl?.toString() ?: ""
                    ))
                } else {
                    trySend(null)
                }
            } catch (e: Exception) {
                trySend(null)
            }
        } else {
            trySend(null)
        }
        close()
        awaitClose()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}