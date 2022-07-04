package com.noice.dextro.ui.auth

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivitySignUpBinding
import java.io.IOException


class SignUpActivity : AppCompatActivity() {
    lateinit var bind: ActivitySignUpBinding
    lateinit var imgPickerActivity: ActivityResultLauncher<Intent>
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
                }
            }
        }

        bind.userIv.setOnClickListener {
            checkPermsAndPickImgFromGallery()
        }
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
}