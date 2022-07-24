package com.noice.dextro.ui.auth

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.noice.dextro.ui.main.MainActivity
import com.noice.dextro.utils.DialogHelper
import com.noice.dextro.utils.NetworkCallStatus
import io.grpc.ManagedChannelProvider
import java.io.Closeable
import java.util.concurrent.TimeUnit

class VerifyOtpViewModel:ViewModel() {
    private lateinit var phoneAuthOptions: PhoneAuthOptions.Builder

    val phoneNumberVerificationStatus = MutableLiveData<NetworkCallStatus<Any>>()
    val signInStatus = MutableLiveData<NetworkCallStatus<Any>>()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var credential: PhoneAuthCredential
    private lateinit var mVerificationId: String
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken


    fun verifyPhoneNumber(phoneNumber: String, activity: VerifyOtpActivity, isFirstTime: Boolean) {


        //used for sending OTP request
        phoneAuthOptions = if (!isFirstTime && this::mResendToken.isInitialized) {
            phoneAuthOptions.setForceResendingToken(mResendToken)
        } else {
            createPhoneAuthOptions(phoneNumber, activity)
        }
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions.build())
        phoneNumberVerificationStatus.postValue(NetworkCallStatus.loading())
    }

    fun signInWithOTP(smsOTP: String) {
        //For Signing In user with OTP
        signInStatus.postValue(NetworkCallStatus.loading())
        if (isValidOTP(smsOTP)) {

            this.credential = PhoneAuthProvider.getCredential(mVerificationId, smsOTP)
            auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (isUserRegistrationComplete(it)) {
                            signInStatus.postValue(
                                NetworkCallStatus.success(
                                    null,
                                    "User Registration Completed"
                                )
                            )
                        } else {
                            signInStatus.postValue(
                                NetworkCallStatus.success(
                                    null,
                                    "User Registration Not Completed"
                                )
                            )
                        }
                    } else {
                        signInStatus.postValue(
                            NetworkCallStatus.error(
                                data = it.exception?.localizedMessage
                            )
                        )
                    }
                }

        }else{
            signInStatus.postValue(
                NetworkCallStatus.error(data = "OTP is not valid"
                )
            )
        }

    }
    private fun createPhoneAuthOptions(
        phoneNumber: String,
        activity: VerifyOtpActivity,
    ): PhoneAuthOptions.Builder {

        val verficationCallbacks = createCallbacksForNumberVerificationState()

        val phoneAuthOptions = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)                // Phone number to verify
            .setTimeout(30L, TimeUnit.SECONDS)   // Timeout and unit
            .setActivity(activity)                      // Activity (for callback binding)
            .setCallbacks(verficationCallbacks)

        return phoneAuthOptions
    }

    private fun createCallbacksForNumberVerificationState(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val verficationCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.

                    phoneNumberVerificationStatus.postValue(
                        NetworkCallStatus.success(
                            credential.smsCode,
                            "PhoneNumber Auto-Verified"
                        )
                    )
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.


                    if (e is FirebaseAuthInvalidCredentialsException) {
                        phoneNumberVerificationStatus.postValue(NetworkCallStatus.error(msg = "invalid Request. please try again later"))

                    } else if (e is FirebaseTooManyRequestsException) {
                        phoneNumberVerificationStatus.postValue(NetworkCallStatus.error(msg = "you've sent too many requests too fast \n try again after a while :)"))
                    }

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId
                    mResendToken = token

                    phoneNumberVerificationStatus.postValue(
                        NetworkCallStatus.success(
                            "",
                            msg = "OTP-Sent"
                        )
                    )
                }
            }
        return verficationCallbacks
    }

    private fun isUserRegistrationComplete(it: Task<AuthResult>) =
        !it.result.user?.displayName.isNullOrBlank()

    private fun isValidOTP(smsOTP: String): Boolean {
        return smsOTP.length == 6
    }
}
