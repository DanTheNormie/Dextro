package com.noice.dextro.data.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.*
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RequiresApi
import java.util.*

data class DeviceInfo  constructor(

    val build_tags: String? = TAGS,

    val model: String? = MODEL,

    val id: String? = ID,

    val manufacturer: String? = MANUFACTURER,

    val version: String? = RELEASE,

    val full_product_name: String? = PRODUCT,

    val base_os: String? = VERSION.BASE_OS,

    val hardware: String? = HARDWARE,

    val bootloader: String? = BOOTLOADER,

    val user: String? = USER,

    val host: String? = HOST,

    val time: Date? = Date(TIME),

    val display: String? = DISPLAY

)