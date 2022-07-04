package com.noice.dextro.ui.auth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this

        val vm = ViewModelProvider(this)[LoginViewModel::class.java]

        val phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                try {
                    val phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                    binding.phoneNumTiet.setText(phoneNumber)
                } catch(e: Exception) {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            }

        binding.phoneNumTiet.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && vm.getPhoneAuto){
                vm.getPhoneAuto = false
                val request: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest.builder().build()

                Identity.getSignInClient(this)
                    .getPhoneNumberHintIntent(request)
                    .addOnSuccessListener{
                        try {
                            phoneNumberHintIntentResultLauncher.launch(IntentSenderRequest.Builder(it).build())
                        } catch(e: Exception) {
                            Log.e(TAG, "Launching the PendingIntent failed")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Phone Number Hint failed : Reason $it")
                    }

            }
        }
        binding.phoneNumTiet.addTextChangedListener {
            binding.verifyBtn.isEnabled = vm.verifyPhoneNumber(it.toString())
        }
        binding.verifyBtn.setOnClickListener {
            showEditOTPDialog()
        }

    }

    private fun showEditOTPDialog(){
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Confirm Phone Number")
            setMessage("We'll be sending an OTP to ${binding.phoneNumTiet.text}. \n"+
                    "Would you like to edit your number ? ")
            setPositiveButton("No"){_,_ ->
                showVerifyOTPActivity()
            }
            setNegativeButton("edit"){dialog,_ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showVerifyOTPActivity(){
        val countryCode = binding.ccpSpinner.selectedCountryCodeWithPlus
        val phoneNumber = countryCode + binding.phoneNumTiet.text
        val intent =  Intent(this, VerifyOtpActivity::class.java)
        intent.putExtra("phone_no",phoneNumber)
        startActivity(intent)
    }
}