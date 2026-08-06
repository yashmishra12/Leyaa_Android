package com.vijaykumawat.Leyaa.utils

import android.util.Log

object KotlinHelper {
    @JvmStatic
    fun getGreeting(): String {
        return "Hello from Kotlin!"
    }
    
    @JvmStatic
    fun logGreeting() {
        Log.d("KotlinHelper", getGreeting())
    }
}
