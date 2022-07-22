package com.noice.dextro.ui.auth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityLoginBinding


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(currentUser != null) {
            if( !(currentUser!!.displayName.isNullOrBlank())){
                Log.i(TAG, "onCreate: name =  ${currentUser!!.displayName} photo = ${currentUser!!.photoUrl}")
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                finish()
            }else{
                Log.i(TAG, "onCreate: name =  ${currentUser!!.displayName} photo = ${currentUser!!.photoUrl}")
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }

        }else {
            bind = DataBindingUtil.setContentView(this, R.layout.activity_login)
            bind.lifecycleOwner = this

            val vm = ViewModelProvider(this)[LoginViewModel::class.java]

            val phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
                registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    try {
                        val phoneNumber =
                            Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                        bind.phoneNumTiet.setText(phoneNumber.getLast10Digits())
                    } catch (e: Exception) {
                        Log.e(TAG, "Phone Number Hint failed")
                    }
                }

            bind.phoneNumTiet.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus && vm.getPhoneAuto) {
                    vm.getPhoneAuto = false
                    val request: GetPhoneNumberHintIntentRequest =
                        GetPhoneNumberHintIntentRequest.builder().build()

                    Identity.getSignInClient(this)
                        .getPhoneNumberHintIntent(request)
                        .addOnSuccessListener {
                            try {
                                phoneNumberHintIntentResultLauncher.launch(
                                    IntentSenderRequest.Builder(
                                        it
                                    ).build()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Launching the PendingIntent failed")
                            }
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Phone Number Hint failed : Reason $it")
                        }
                }
            }
            bind.phoneNumTiet.addTextChangedListener {
                bind.verifyBtn.isEnabled = vm.verifyPhoneNumber(it.toString())
            }
            bind.verifyBtn.setOnClickListener {
                showShouldEditPhoneNUmberDialog()
            }
        }
    }

    private fun showShouldEditPhoneNUmberDialog(){
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
}