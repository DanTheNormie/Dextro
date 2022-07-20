package com.noice.dextro.data.model

import com.google.firebase.firestore.FieldValue

data class User(
    val uid:String,
    val deviceToken:String,
    val name:String,
    val imageUrl:String,
    val thumbnailUrl:String,
    val status:String,
    val isOnline:Boolean
){
    constructor():this("","","","","","",false)
    constructor(uid: String,name: String, imageUrl: String, thumbImage: String) :
            this(uid,"",name,imageUrl,thumbImage,"",false)
}