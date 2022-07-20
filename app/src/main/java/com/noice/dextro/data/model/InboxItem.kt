package com.noice.dextro.data.model

import java.util.*

class InboxItem (
    var name:String,
    var uid:String,
    var thumbnail_url:String,
    var recent_msg:String,
    var unread_msg_count:Int,
    var recent_msg_time:Date = Date()
){
    constructor():this("","","","",0,Date())
}