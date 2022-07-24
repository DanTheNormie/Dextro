package com.noice.dextro.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogHelper{

    fun showSimpleDialog(context: Context, msg:String){
        showSimpleDialog(context,msg,"")

    }
    fun showSimpleDialog(context: Context, msg: String, title:String){
        MaterialAlertDialogBuilder(context)
            .setMessage(msg)
            .setTitle(title)
            .setPositiveButton("ok") { _, _ ->  }
            .setCancelable(false)
            .create()
            .show()
    }


}