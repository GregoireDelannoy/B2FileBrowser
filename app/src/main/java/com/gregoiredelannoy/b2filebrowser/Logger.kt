package com.gregoiredelannoy.b2filebrowser

import android.util.Log
import android.widget.TextView

class Logger (val statusTextView: TextView){

    fun log(logLevel: Int, message: String){
        statusTextView.text = message
        Log.println(logLevel, null, message)
    }
}