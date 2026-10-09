package com.protocolx.inlo

import android.app.Application
import android.util.Log

class INLOApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("INLOApp", "INLO AI Application initialized - Zero Internet Permission Mode")
    }
}
