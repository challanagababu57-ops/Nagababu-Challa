package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUserState(
    val isSignedIn: Boolean = false,
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

class FirebaseAuthService {

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.w("FirebaseAuthService", "FirebaseAuth not available", e)
                null
            }
        } else null

    private val _currentUserState = MutableStateFlow(AuthUserState())
    val currentUserState: StateFlow<AuthUserState> = _currentUserState.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            updateUserState(user)
        }
        updateUserState(auth?.currentUser)
    }

    private fun updateUserState(user: FirebaseUser?) {
        if (user != null) {
            _currentUserState.value = AuthUserState(
                isSignedIn = true,
                uid = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "MEECHAAT User",
                email = user.email,
                photoUrl = user.photoUrl?.toString()
            )
        } else {
            _currentUserState.value = AuthUserState(isSignedIn = false)
        }
    }

    fun isConfigured(): Boolean = auth != null

    fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized. Please verify configuration.")
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        updateUserState(user)
                        onSuccess(user)
                    }
                } else {
                    onError("Failed to create user profile.")
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Sign-up failed.")
            }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized. Please verify configuration.")
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    updateUserState(user)
                    onSuccess(user)
                } else {
                    onError("Failed to sign in.")
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Sign-in failed.")
            }
    }

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized.")
            return
        }

        val credentialManager = CredentialManager.create(context)

        try {
            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)

            if (!serverClientId.isNullOrBlank()) {
                googleIdOptionBuilder.setServerClientId(serverClientId)
            } else {
                // Default dummy/web client ID fallback if none provided
                googleIdOptionBuilder.setServerClientId("dummy-client-id.apps.googleusercontent.com")
            }

            val googleIdOption = googleIdOptionBuilder.build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        updateUserState(user)
                        onSuccess(user)
                    } else {
                        onError("Google authentication returned empty user.")
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Google sign-in credential failed.")
                }
        } catch (e: GetCredentialCancellationException) {
            onError("Sign-in cancelled.")
        } catch (e: GetCredentialException) {
            onError(e.localizedMessage ?: "Google credential error.")
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized.")
            return
        }
        if (email.isBlank()) {
            onError("Please enter your email address.")
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Password reset failed.") }
    }

    fun signInAnonymously(
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized.")
            return
        }
        firebaseAuth.signInAnonymously()
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    updateUserState(user)
                    onSuccess(user)
                } else {
                    onError("Anonymous authentication returned empty user.")
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Guest login failed.")
            }
    }

    fun updateUserProfile(
        displayName: String,
        photoUri: android.net.Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth?.currentUser
        if (user == null) {
            onError("No user currently logged in.")
            return
        }

        val builder = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName.trim())
        if (photoUri != null) {
            builder.setPhotoUri(photoUri)
        }
        val profileUpdates = builder.build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                updateUserState(user)
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to update profile.")
            }
    }

    fun signOut() {
        auth?.signOut()
        _currentUserState.value = AuthUserState(isSignedIn = false)
    }
}
