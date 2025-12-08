package com.app.research.utils

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.app.research.utils.AppUtils.showErrorMessage
import com.app.research.utils.AppUtils.showInfoMessage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import timber.log.Timber

class GoogleSignInClient(
    private val activityContext: Context,
) {
    private val credentialManager = CredentialManager.create(activityContext)

    fun isSignedIn(): Boolean {
//        return App.instance?.userData?.token?.accessToken != null
        return false
    }

    suspend fun signIn(
        isGButtonMethod: Boolean = false,
        onSuccess: (token: String) -> Unit
    ): Boolean {
        if (isSignedIn()) {
            Timber.d("User already signed in.")
            showInfoMessage(activityContext, "User already signed in. Try again.")
            signOut()
            return true
        }

        return try {

            handleSignIn(buildCredentialRequest(isGButtonMethod = isGButtonMethod), onSuccess)

        } catch (e: GoogleIdTokenParsingException) {
            Timber.e(e)
            showErrorMessage(activityContext, e.message ?: "Invalid Google ID token")
            false
        } catch (e: NoCredentialException) {
            if (!isGButtonMethod) {
                handleSignIn(
                    buildCredentialRequest(hasFilter = false), onSuccess
                )
            }
            Timber.e(e.message ?: "No Credential Available")
            showErrorMessage(activityContext, e.message ?: "No Credential Available")
            false
        } catch (e: GetCredentialCancellationException) {
            Timber.e(e)
            return false
        } catch (e: GetCredentialException) {
            Timber.e(e)
            showErrorMessage(activityContext, e.message ?: "Credential error occurred")
            false
        } catch (e: Exception) {
            Timber.e(e)
            showErrorMessage(activityContext, e.message ?: "Unknown error occurred")
            false
        }
    }

    private suspend fun handleSignIn(
        credentialRequest: GetCredentialRequest,
        onSuccess: (String) -> Unit
    ): Boolean {

        val result = credentialManager.getCredential(
            context = activityContext,
            request = credentialRequest
        )

        val credential = result.credential

        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            Timber.d(
                """"Google Sign-In Success"
                            "Name: ${tokenCredential.displayName}"
                            "Email: ${tokenCredential.id}
                            "Token: ${tokenCredential.idToken}"""
            )

            // This is your Google ID Token to send to backend
            onSuccess(tokenCredential.idToken)
            return true

        } else {
            Timber.e("Unexpected credential type: ${credential.type}")
            showErrorMessage(activityContext, "Unexpected credential type: ${credential.type}")
            return false
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        Timber.d("Signed out from Google Credential Manager only.")
    }

    private fun buildCredentialRequest(
        isGButtonMethod: Boolean = false,
        hasFilter: Boolean = true
    ): GetCredentialRequest {

        //"R.string.oauth_web_client_id"
        val oauthWebClientId = activityContext.getString(0)

        val googleSignInOption =
            GetSignInWithGoogleOption.Builder(oauthWebClientId)
                .build()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(hasFilter)
            .setAutoSelectEnabled(hasFilter)
            .setServerClientId(oauthWebClientId)
            .build()

        val credentialOption = if (isGButtonMethod) googleSignInOption else googleIdOption

        return GetCredentialRequest.Builder()
            .addCredentialOption(credentialOption)
            .build()
    }
}