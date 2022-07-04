package com.noice.dextro.ui.auth

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.View


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityVerifyOtpBinding
import com.noice.dextro.utils.DialogHelper
import java.util.concurrent.TimeUnit

/*
* What we are doing?
* step 1:- Send request to server for OTP
* step 2:- Sending the OTP to verify
*
* misc:-
* (1) you can resend a request for otp once every 60 seconds
* (2) every network request must
*       while starting :
*           (a) show a loading progress
*           (b) notify user with a toast about what the network request is about
*       Upon response :
*           (a) stop the loading progress
*           (b) notify user with a toast/dialog about what the response is
*
* */



class VerifyOtpActivity : AppCompatActivity() {
    val TAG = "VerifyOTPActivity"
    lateinit var phoneNumber: String
    lateinit var bind:ActivityVerifyOtpBinding
    lateinit var mVerificationId:String
    private var mcountDownTimer:CountDownTimer? = null
    lateinit var mResendToken:PhoneAuthProvider.ForceResendingToken
    lateinit var verficationCallbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var phoneAtuhOptions: PhoneAuthOptions.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_verify_otp)
        bind.lifecycleOwner = this

        initViews()
        initOnClickListeners()
        sendRequestForOTP(true)

    }

    private fun initOnClickListeners() {
        bind.verifyOtpBtn.setOnClickListener {
            val code = bind.otpTiet.text.toString()

            if(code.length==6 && mVerificationId.isNotBlank()){
                val credential = PhoneAuthProvider.getCredential(mVerificationId,code)
                signInWithPhoneAuthCredential(credential)

                bind.refreshLayout.isRefreshing = true
                Toast.makeText(
                    this,
                    "Please wait while we verify your OTP code.",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                DialogHelper.createSimpleDialog(this,"Please input proper OTP code")
            }
        }

        bind.resendOtpBtn.setOnClickListener {
            sendRequestForOTP(true)
            Toast.makeText(this, "Sendind OTP to $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendRequestForOTP(isFirstTime:Boolean){
        if (isFirstTime){
            PhoneAuthProvider.verifyPhoneNumber(phoneAtuhOptions.build())
        }else{
            // using a hack here, to be removed after adding network detection
            if(this::mResendToken.isInitialized){
                PhoneAuthProvider.verifyPhoneNumber(phoneAtuhOptions.setForceResendingToken(mResendToken).build())
            }else{
                PhoneAuthProvider.verifyPhoneNumber(phoneAtuhOptions.build())
            }
        }
        notifyUser()

    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra("phone_no").toString()

        bind.verifyNoTitleTv.text = getString(R.string.verify_custom_number,phoneNumber)

        initHelperTxtSpannableString()

        //<editor-fold desc="Init PhoneAuthOptions var for sending a request for OTP">
        //should always be above PhoneAuthOptions variable
        verficationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.


                Toast.makeText(this@VerifyOtpActivity, "OTP Auto-Detected.", Toast.LENGTH_SHORT).show()
                bind.otpTiet.setText(credential.smsCode)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                bind.refreshLayout.isRefreshing = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@VerifyOtpActivity, "Invalid Request. please try again later", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@VerifyOtpActivity, "Sms Quota has exceeded. No money no honey :)", Toast.LENGTH_SHORT).show()
                }

                // Show a message and update the UI
                DialogHelper.createSimpleDialog(applicationContext,"Phone Number Verification failed. Please try again")
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
                bind.refreshLayout.isRefreshing = false
                bind.verifyOtpBtn.isEnabled = true

                Toast.makeText(
                    this@VerifyOtpActivity,
                    "An otp has been sent to $phoneNumber please enter it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //used for sending OTP request
        phoneAtuhOptions = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)                // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS)   // Timeout and unit
            .setActivity(this)                          // Activity (for callback binding)
            .setCallbacks(verficationCallbacks)         //setting callbacks to handle verification state
        //</editor-fold>
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        //For Signing In user with OTP

        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    startActivity(Intent(this,SignUpActivity::class.java))
                    Toast.makeText(this, "Successful biro , You made it !!!", Toast.LENGTH_SHORT).show()
                }else{
                    DialogHelper.createSimpleDialog(this,"your phone number verification failed. Please try again !!")
                }
                bind.refreshLayout.isRefreshing = false
            }
    }

    private fun notifyUser() {

        /*
        * (1) Show a loading progress
        * (2) Disable ResendOTPBtn and enable after 60 seconds
        * (3) Disable VerifyOTPBtn until otp is sent to mobile number (btn will be enabled when OnCodeSent() is called in callback)
        * (4) Start 60s CountDown Timer
        * (5) show a toast to notify user that a request for otp has been sent
        * */

        bind.refreshLayout.isRefreshing = true
        bind.resendOtpBtn.isEnabled = false
        bind.verifyOtpBtn.isEnabled = false
        mcountDownTimer = object:CountDownTimer(60000,1000){
            override fun onTick(millisUntilFinished: Long) {
                bind.helperTxt2Tv.visibility = View.VISIBLE
                bind.helperTxt2Tv.text = getString(R.string.resend_otp,millisUntilFinished/1000)
            }

            override fun onFinish() {
                bind.resendOtpBtn.isEnabled = true
                bind.helperTxt2Tv.visibility = View.GONE
            }
        }.start()
        Toast.makeText(this, "Sending a request for OTP to server", Toast.LENGTH_SHORT).show()
    }

    private fun initHelperTxtSpannableString() {
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
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun onBackPressed() {
        showLoginActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        mcountDownTimer?.cancel()
    }
}