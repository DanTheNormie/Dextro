package com.noice.dextro.ui.auth

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.utils.NetworkCallStatus

class SignUpViewModel:ViewModel() {
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestoreDb by lazy {
        FirebaseFirestore.getInstance()
    }

    val uploadImageStatus = MutableLiveData<NetworkCallStatus<Any>>()
    val uploadUserDetailsStatus = MutableLiveData<NetworkCallStatus<Any>>()

    fun uploadImage(imageUri: Uri){
        uploadImageStatus.postValue(NetworkCallStatus.loading())
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(imageUri)
        uploadTask.continueWithTask(Continuation { task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result.toString()
                uploadImageStatus.postValue(NetworkCallStatus.success(downloadUrl))
            }else{
               uploadImageStatus.postValue(NetworkCallStatus.error())
            }
        }
    }
    fun uploadUserDetails(user: UserItem, profile: UserProfileChangeRequest){
        uploadUserDetailsStatus.postValue(NetworkCallStatus.loading())
        updateProfile(profile)
        firestoreDb.collection("users").document(auth.uid!!).set(user)
            .addOnSuccessListener {

                uploadUserDetailsStatus.postValue(NetworkCallStatus.success(null))

            }.addOnFailureListener {
                uploadUserDetailsStatus.postValue(NetworkCallStatus.error(it.localizedMessage))
            }
    }

    private fun updateProfile(profile: UserProfileChangeRequest) {
        FirebaseAuth.getInstance().currentUser?.updateProfile(profile)
    }
}