package com.noice.dextro.ui.auth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityLoginBinding
import com.noice.dextro.ui.main.MainActivity
import java.lang.StringBuilder


class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    lateinit var bind: ActivityLoginBinding
    private var isLoading = true
    private var isUserProfileSet = false
    private val auth by lazy{
        FirebaseAuth.getInstance().uid
    }
    private val currentUser by lazy{
        FirebaseAuth.getInstance().currentUser
    }
    lateinit var vm:LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(isReturningUser()) {
            if(areUserDetailsSet()){
                skipLogin()
            }else{
                goToSignUp()
            }

        }else {
            initDataBinding()

            initViewModel()

            initListeners()

            tryToGetPhoneNumberAutomatically()

        }
    }

    private fun initListeners() {
        bind.phoneNumTiet.addTextChangedListener {
            bind.verifyBtn.isEnabled = vm.verifyPhoneNumber(it.toString())
        }
        bind.verifyBtn.setOnClickListener {
            showConfirmPhoneNUmberDialog()
        }
    }

    private fun initViewModel() {
         vm = ViewModelProvider(this)[LoginViewModel::class.java]
    }

    private fun initDataBinding() {
        bind = DataBindingUtil.setContentView(this, R.layout.activity_login)
        bind.lifecycleOwner = this
    }

    private fun registerCallbackForGettingPhoneNumber(): ActivityResultLauncher<IntentSenderRequest> {
        val phoneNumberHintIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                try {
                    val phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                    bind.phoneNumTiet.setText(phoneNumber.getLast10Digits())
                } catch (e: Exception) {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            }
        return phoneNumberHintIntentResultLauncher
    }

    private fun tryToGetPhoneNumberAutomatically() {
        val phoneNumberHintCallbackLauncher = registerCallbackForGettingPhoneNumber()

        val request = GetPhoneNumberHintIntentRequest.builder().build()

        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener {
                try {
                    phoneNumberHintCallbackLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Launching the PendingIntent failed")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Phone Number Hint failed : Reason $it")
            }
    }

    private fun goToSignUp() {
        Log.i(
            TAG,
            "onCreate: name =  ${currentUser!!.displayName} photo = ${currentUser!!.photoUrl}"
        )
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun skipLogin() {
        Log.i(
            TAG,
            "onCreate: name =  ${currentUser!!.displayName} photo = ${currentUser!!.photoUrl}"
        )
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun areUserDetailsSet() = !(currentUser!!.displayName.isNullOrBlank())

    private fun showConfirmPhoneNUmberDialog(){
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Confirm Phone Number")
            setMessage("We'll be sending an OTP to ${bind.phoneNumTiet.text}. \n"+
                    "Would you like to proceed ? ")
            setPositiveButton("Yes"){_,_ ->
                gotoVerifyOTPActivity()
            }
            setNegativeButton("No, I want to edit the number"){dialog,_ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun gotoVerifyOTPActivity(){
        val countryCode = bind.ccpSpinner.selectedCountryCodeWithPlus
        val phoneNumber = countryCode + bind.phoneNumTiet.text
        val intent =  Intent(this, VerifyOtpActivity::class.java)
        intent.putExtra("phone_no",phoneNumber)
        startActivity(intent)
    }

    private fun String.getLast10Digits():String{
        val stringBuilder = StringBuilder(10)
        for ( i in length-1..0){
            if(this[i].isDigit() && stringBuilder.length <10){
                stringBuilder.append(this[i])
            }
        }
        return stringBuilder.reverse().toString()
    }

    private fun isReturningUser():Boolean{
        return currentUser != null
    }
}
