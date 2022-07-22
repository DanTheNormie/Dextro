package com.noice.dextro.data.model

import android.content.Context
import android.util.AttributeSet
import com.noice.dextro.utils.formatAsHeader
import java.util.*


interface ChatItem{
    val sentAt:Date
}

enum class MsgType{
    TEXT,IMAGE,AUDIO,REMINDER,EVENT_PLAN
}

enum class MsgStatus{
    SENT,DELIVERED,READ
}

data class Message(
    val msg: String,
    val uid: String,
    val msgId: String,
    val type: MsgType = MsgType.TEXT,
    val status: MsgStatus = MsgStatus.SENT,
    val liked: Boolean = false,
    override val sentAt: Date = Date()
):ChatItem{
    constructor() : this("", "", "", MsgType.TEXT, MsgStatus.SENT, false, Date(0L))
}

data class DateSectionHeader (
    override val sentAt:Date,
    val context:Context
): ChatItem{
    val date:String = sentAt.formatAsHeader(context)
}
