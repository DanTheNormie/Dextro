package com.noice.dextro.data.model

import java.util.*

data class InboxItem(
    val recent_msg: String,
    var uid: String,
    var name: String,
    var thumbnail_url: String,
    val recent_msg_time: Date = Date(),
    var unread_msg_count: Int = 0
) {
    constructor() : this("", "", "", "", Date(), 0)
}