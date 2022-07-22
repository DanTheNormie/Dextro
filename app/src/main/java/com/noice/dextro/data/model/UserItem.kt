package com.noice.dextro.data.model

data class UserItem(
    val uid:String,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val name:String,
    val imageUrl:String,
    val thumbnailUrl:String,
    val status:String,
    val isOnline:Boolean
){
    constructor():this("", DeviceInfo(),"","","","",false)
    constructor(uid: String,name: String, imageUrl: String, thumbImage: String) :
            this(uid,DeviceInfo(),name,imageUrl,thumbImage,"",false)
}