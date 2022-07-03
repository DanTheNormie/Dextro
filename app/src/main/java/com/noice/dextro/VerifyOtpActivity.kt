package com.noice.dextro

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.View


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.noice.dextro.databinding.ActivityVerifyOtpBinding

class VerifyOtpActivity : AppCompatActivity() {
    val TAG = "VerifyOTPActivity"
    lateinit var phoneNumber: String
    lateinit var bind:ActivityVerifyOtpBinding
    lateinit var mVerificationId:String
    lateinit var mResendToken:PhoneAuthProvider.ForceResendingToken
    lateinit var verficationCallbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this,R.layout.activity_verify_otp)
        bind.lifecycleOwner = this


        initViews()


    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra("phone_no").toString()
        bind.verifyNoTitleTv.text = getString(R.string.verify_custom_number,phoneNumber)
        setSpannableString()
        showTimer(60000)

        //setting callbacks to handle verification state
        verficationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun showTimer(ms: Long) {
        bind.resendOtpBtn.isEnabled = false
        val countDownTimer = object:CountDownTimer(ms,1000){
            override fun onTick(millisUntilFinished: Long) {
                bind.helperTxt2Tv.visibility = View.VISIBLE
                bind.helperTxt2Tv.text = getString(R.string.resend_otp,millisUntilFinished/1000)
            }

            override fun onFinish() {
                bind.resendOtpBtn.isEnabled = true
                bind.helperTxt2Tv.visibility = View.GONE
            }
        }.start()
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_info_txt,phoneNumber))

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan,span.length - 13,span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        bind.helperTxtTv.text = span
        bind.helperTxtTv.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showLoginActivity() {
        startActivity(Intent(this,LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}