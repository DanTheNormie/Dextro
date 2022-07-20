package com.noice.dextro.data.model

import android.content.Context
import com.noice.dextro.utils.formatAsHeader

import java.util.*

enum class MsgType {
    TEXT,IMAGE,VIDEO
}

interface ChatItem{
    val sentAt:Date
}

data class Message(
    val msg:String,
    val uid:String,
    val msg_id:String,
    val type:MsgType = MsgType.TEXT,
    var status:Int = 1,
    var liked:Boolean = false,
    override val sentAt: Date = Date()
):ChatItem
{
    constructor() : this("","","")
}

data class DateSectionHeader(
    override val sentAt: Date,
    val context: Context
):ChatItem{
    val date:String = sentAt.formatAsHeader(context)
}