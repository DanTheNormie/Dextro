package com.noice.dextro.ui.auth

import androidx.lifecycle.ViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {
    fun verifyPhoneNumber(number:String):Boolean {
        val pattern = Pattern.compile("^[\\d]{10}$")
        val matcher = pattern.matcher(number)
        return matcher.find()
    }
}