package com.noice.dextro.ui.auth

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Continuation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noice.dextro.R
import com.noice.dextro.data.model.User
import com.noice.dextro.databinding.ActivitySignUpBinding
import com.noice.dextro.ui.main.MainActivity
import java.io.IOException


class SignUpActivity : AppCompatActivity() {
    lateinit var bind: ActivitySignUpBinding
    lateinit var imgPickerActivity: ActivityResultLauncher<Intent>
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestoreDb by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var  downloadUrl:String
    val tag = "SignUpActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)

        imgPickerActivity = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // do your operation from here ....
                if (data != null && data.data != null) {
                    val selectedImageUri: Uri? = data.data
                    var selectedImageBitmap: Bitmap? = null
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            selectedImageUri
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    bind.userIv.apply {
                        setImageBitmap(
                            selectedImageBitmap
                        )
                    }
                    uploadImage(selectedImageUri!!)
                }
            }
        }

        bind.userIv.setOnClickListener {
            checkPermsAndPickImgFromGallery()
        }
        bind.nextBtn.setOnClickListener {
            loadingProgress(true)
            if(userDetailsAreSet()) {
                val user = User(
                    auth.uid.toString(),
                    bind.usernameTiet.text.toString(),
                    downloadUrl,
                    downloadUrl
                )
                firestoreDb.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    loadingProgress(false)
                }.addOnFailureListener {
                    loadingProgress(false)
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun userDetailsAreSet(): Boolean {
        var isUserDetailsSet = false
        var msg = ""
        if(bind.usernameTiet.text?.length!! <= 6){
            isUserDetailsSet = false
            msg += "UserName must be greater than 6 characters \n"
        }
        if(!this::downloadUrl.isInitialized){
            isUserDetailsSet = false
            msg += "please select a user profile picture \n"
        }
        if (this::downloadUrl.isInitialized && bind.usernameTiet.text?.length!! > 6){
            isUserDetailsSet = true
            msg = "You are all set, Let's get you in !!!"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return isUserDetailsSet
    }

    private fun checkPermsAndPickImgFromGallery() {
        if( (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            &&
            (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ){
            val read_perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val write_perms = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(read_perms,6969)
            requestPermissions(write_perms,9696)

        }else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        imgPickerActivity.launch(intent)
    }

    private fun uploadImage(it:Uri){
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(it)
        loadingProgress(true)
        uploadTask.continueWithTask(Continuation { task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    Log.i(tag,"${it.message}")
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if(task.isSuccessful){
                downloadUrl = task.result.toString()
            }else{
                Toast.makeText(this, "Failed to upload image, please try again !!!", Toast.LENGTH_SHORT).show()
            }
            loadingProgress(false)
        }
    }

    private fun loadingProgress(show:Boolean) {
        if(show){
            bind.progressBar.visibility = View.VISIBLE
        }else{
            bind.progressBar.visibility = View.GONE
        }

    }

}