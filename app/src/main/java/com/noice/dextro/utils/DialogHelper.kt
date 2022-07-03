package com.noice.dextro.utils

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogHelper{

    fun createSimpleDialog(context: Context, msg:String){
        createSimpleDialog(context,msg,"")

    }
    fun createSimpleDialog(context: Context,msg: String,title:String){
        MaterialAlertDialogBuilder(context)
            .setMessage(msg)
            .setTitle(title)
            .setPositiveButton("ok") { _, _ ->  }
            .setNegativeButton("cancel"){dg,_ -> dg.dismiss()}
            .setCancelable(false)
            .create()
            .show()
    }
}