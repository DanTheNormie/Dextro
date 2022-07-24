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
import android.util.TypedValue
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityVerifyOtpBinding
import com.noice.dextro.ui.main.MainActivity
import com.noice.dextro.utils.DialogHelper
import com.noice.dextro.utils.NetworkCallStatus.Status.SUCCESS
import com.noice.dextro.utils.NetworkCallStatus.Status.ERROR
import com.noice.dextro.utils.NetworkCallStatus.Status.LOADING
import java.lang.Exception

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
*           (b) notify user with a toast/dialog about what the response result says
*
* */

class VerifyOtpActivity : AppCompatActivity() {
    val TAG = "VerifyOTPActivity"
    lateinit var phoneNumber: String
    lateinit var bind:ActivityVerifyOtpBinding
    lateinit var mVerificationId:String
    private var mcountDownTimer:CountDownTimer? = null

    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser by lazy {
        FirebaseAuth.getInstance().currentUser
    }
    lateinit var vm :VerifyOtpViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewBinding()

        initViewModel()

        initViews()

        vm.verifyPhoneNumber(phoneNumber,this,true)
    }

    override fun onResume() {
        super.onResume()

        initViewModelObservers()

        initOnClickListeners()
    }

    private fun initViewBinding() {
        bind = DataBindingUtil.setContentView(this, R.layout.activity_verify_otp)
        bind.lifecycleOwner = this
    }

    private fun initViewModel() {
        vm = ViewModelProvider(this)[VerifyOtpViewModel::class.java]
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra("phone_no").toString()
        bind.verifyNoTitleTv.text = getString(R.string.verify_custom_number,phoneNumber)

        bind.helperTxtTv.text = initHelperTxtSpannableString()
    }

    private fun initViewModelObservers(){
        vm.phoneNumberVerificationStatus.observe(this){
            when (it.status){
                SUCCESS ->{
                    when(it.msg){
                        "PhoneNumber Auto-Verified"->{
                            Toast.makeText(this@VerifyOtpActivity, "OTP Auto-Detected.", Toast.LENGTH_SHORT).show()
                            bind.otpTiet.setText(it.data as String)
                            vm.signInWithOTP(bind.otpTiet.text.toString())
                        }
                        "OTP-Sent" ->{
                            bind.verifyOtpBtn.isEnabled = true
                            Toast.makeText(this, "An OTP has been sent to $phoneNumber", Toast.LENGTH_SHORT).show()
                        }
                    }
                    stopLoadingIndications()
                }
                ERROR -> {
                    Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                    DialogHelper.showSimpleDialog(this,"Phone Number Verification failed. Please try again")
                    stopLoadingIndications()
                }
                LOADING ->{
                    showLoadingIndications()
                    notifyUser()
                }

                else -> {/*unreachable*/}
            }
        }

        vm.signInStatus.observe(this){
            when(it.status){
                SUCCESS -> {
                    when(it.msg){
                        "User Registration Completed" ->{
                            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                            finish()
                        }
                        "User Registration Not Completed" ->{
                            startActivity(Intent(this, SignUpActivity::class.java))
                            finish()
                        }
                    }
                    stopLoadingIndications()
                }
                ERROR -> {
                    DialogHelper.showSimpleDialog(
                        this,
                        "your phone number verification failed. Please try again !! \n\n Reason : ${it.data} "
                    )
                    stopLoadingIndications()
                }
                LOADING -> {
                    showLoadingIndications()
                }

                else -> {/*unreachable*/}
            }
        }
    }

    private fun initOnClickListeners() {
        bind.verifyOtpBtn.setOnClickListener {
            val code = bind.otpTiet.text.toString()
            vm.signInWithOTP(code)
        }

        bind.resendOtpBtn.setOnClickListener {
            vm.verifyPhoneNumber(phoneNumber,this,false)
            Toast.makeText(this, "Sending OTP to $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLoadingIndications() {
        bind.refreshLayout.visibility = View.GONE
    }

    private fun showLoadingIndications() {
        bind.refreshLayout.visibility = View.VISIBLE
    }

    private fun notifyUser() {

        /*
        * (1) Show a loading progress
        * (2) Disable ResendOTPBtn and enable after 60 seconds
        * (3) Disable VerifyOTPBtn until otp is sent to mobile number (btn will be enabled when OnCodeSent() is called in callback)
        * (4) Start 60s CountDown Timer
        * (5) show a toast to notify user that a request for otp has been sent
        * */
        bind.resendOtpBtn.isEnabled = false
        bind.verifyOtpBtn.isEnabled = false
        mcountDownTimer = object:CountDownTimer(30000,1000){
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

    private fun initHelperTxtSpannableString():SpannableString {
        val span = SpannableString(getString(R.string.waiting_info_txt,phoneNumber))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showLoginActivity()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                val value = TypedValue()
                this@VerifyOtpActivity.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary,value,true)
                ds.color = value.data
            }
        }
        span.setSpan(clickableSpan,span.length - 13,span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        bind.helperTxtTv.movementMethod = LinkMovementMethod.getInstance()
        return span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    override fun onBackPressed() {
        showLoginActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        mcountDownTimer?.cancel()
    }
}