package services.data.repository

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import services.data.datamodel.AuthFlowState
import services.data.datamodel.User
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventifyRepository @Inject constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getAuth() = auth

    suspend fun registerWithEmail(email: String, password: String, name: String, phone: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser ?: return false
            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                phone = phone,
                isFirstLogin = true
            )
            saveUserToFirestore(newUser)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): Boolean {
        return try {
            auth.signInWithCredential(credential).await()
            val firebaseUser = auth.currentUser ?: return false
            
            // Check if user exists in Firestore, if not create them
            val exists = checkIfUserExists(firebaseUser.uid)
            if (!exists) {
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    isFirstLogin = true
                )
                saveUserToFirestore(newUser)
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveUserToFirestore(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun checkIfUserExists(uid: String): Boolean {
        return try {
            firestore.collection("users").document(uid).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchUserData(uid: String): User? {
        return try {
            firestore.collection("users").document(uid).get().await().toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Boolean {
        return try {
            firestore.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAuthFlowStatus(): AuthFlowState {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userData = fetchUserData(firebaseUser.uid)
            AuthFlowState(
                isLoggedIn = true,
                isFirstTime = userData?.isFirstLogin ?: true,
                checked = true
            )
        } else {
            AuthFlowState(
                isLoggedIn = false,
                isFirstTime = false,
                checked = true
            )
        }
    }

    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
