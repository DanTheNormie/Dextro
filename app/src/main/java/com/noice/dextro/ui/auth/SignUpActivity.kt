package com.noice.dextro.ui.auth

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.noice.dextro.R
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.databinding.ActivitySignUpBinding
import com.noice.dextro.ui.main.MainActivity
import com.noice.dextro.utils.NetworkCallStatus.Status.*


class SignUpActivity : AppCompatActivity() {
    lateinit var bind: ActivitySignUpBinding
    lateinit var vm:SignUpViewModel
    lateinit var  downloadUrl:String
    lateinit var selectedImageUri: Uri
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    lateinit var imagePickerActivityResultLauncher: ActivityResultLauncher<CropImageContractOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModelAndViewBinding()

        initViewModelObservers()

        registerActivityResultCallbacks()

        initOnClickListeners()
    }

    private fun initViewModelAndViewBinding() {
        bind = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        vm = ViewModelProvider(this)[SignUpViewModel::class.java]
    }

    private fun initViewModelObservers() {
        vm.uploadImageStatus.observe(this){
            when(it.status){
                LOADING ->{
                    showNextButton(false)
                    loadingProgress(true,"please wait while we upload your profile picture")
                }
                SUCCESS->{
                    showNextButton(true)
                    downloadUrl = it.data.toString()
                    bind.uploadImgBtn.visibility = View.GONE
                    loadingProgress(false,"Image has been uploaded successfully")
                }
                ERROR->{
                    showNextButton(false)
                    bind.uploadImgBtn.visibility = View.VISIBLE
                    loadingProgress(false,"Failed to upload image, please try again !!!")
                }
                else -> {}
            }
        }

        vm.uploadUserDetailsStatus.observe(this){
            when(it.status){
                LOADING->{
                    loadingProgress(true,"")
                }
                SUCCESS->{
                    loadingProgress(false,"")
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    finish()
                }
                ERROR->{
                    loadingProgress(false,(it.data as Exception).localizedMessage!!)
                }
                else -> {}
            }
        }
    }

    private fun registerActivityResultCallbacks() {
        registerImagePickerActivityResultLauncher()
    }

    private fun registerImagePickerActivityResultLauncher() {
        imagePickerActivityResultLauncher =  registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                // use the returned uri
                val uriContent = result.uriContent
                if (uriContent != null) {
                    bind.userIv.setImageURI(result.uriContent)
                    vm.uploadImage(uriContent)
                }

            } else {
                // an error occurred
                val exception = result.error
                Log.i(TAG, "onCreate: ${exception?.localizedMessage}")
            }
        }
    }

    private fun initOnClickListeners() {
        bind.userIv.setOnClickListener {
            checkPermsAndPickImgFromPhone()
        }

        bind.nextBtn.setOnClickListener {
            if(userDetailsAreSet()) {
                uploadUserDetails()
            }else{
                bind.helperTxtTv.setTextColor(Color.RED)
            }
        }

        bind.uploadImgBtn.setOnClickListener {
            vm.uploadImage(selectedImageUri)
        }

    }

    private fun uploadUserDetails() {
        val user = UserItem(
            auth.uid.toString(),
            bind.usernameTiet.text.toString(),
            downloadUrl,
            downloadUrl
        )
        val profile = UserProfileChangeRequest.Builder().apply {
            displayName = bind.usernameTiet.text.toString()
        }.build()
        vm.uploadUserDetails(user, profile)
    }

    private fun getImageFromPhone() {
        // start picker to get image for cropping and then use the image in cropping activity
        imagePickerActivityResultLauncher.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setActivityTitle("Adjust image")
                setAspectRatio(1,1)
                setAllowRotation(true)
                setCropShape(CropImageView.CropShape.OVAL)
                setCropMenuCropButtonTitle("crop")
                setMultiTouchEnabled(true)
            }
        )
    }

    private fun checkPermsAndPickImgFromPhone() {
        if( (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            &&
            (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ){
            val read_perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val write_perms = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(read_perms,6969)
            requestPermissions(write_perms,9696)

        }else{
            getImageFromPhone()
        }
    }

    private fun userDetailsAreSet(): Boolean {
        var isUserDetailsSet = false
        var msg = ""
        if(bind.usernameTiet.text?.length!! <= 2){
            isUserDetailsSet = false
            msg += "UserName must be greater than 2 characters \n"
        }
        if(!this::downloadUrl.isInitialized){
            isUserDetailsSet = false
            msg += "please select and upload a user profile picture \n"
        }
        if (this::downloadUrl.isInitialized && bind.usernameTiet.text?.length!! > 2){
            isUserDetailsSet = true
            msg = "You are all set, Let's get you in !!!"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return isUserDetailsSet
    }
    private fun showNextButton(show: Boolean) {
        bind.nextBtn.isEnabled = show
    }
    private fun loadingProgress(show:Boolean,reason:String) {
        if(show){
            bind.progressBar.visibility = View.VISIBLE

        }else{
            bind.progressBar.visibility = View.GONE

        }
        if(reason.isNotBlank()){
            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
        }

    }

}