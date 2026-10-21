package com.growera.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.growera.app.data.ApiClient
import com.growera.app.data.SessionStore
import com.growera.app.ui.GroweraApp
import com.growera.app.ui.theme.GroweraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionStore = SessionStore(applicationContext)
        val apiClient = ApiClient()
        setContent {
            GroweraTheme {
                GroweraApp(sessionStore = sessionStore, apiClient = apiClient)
            }
        }
    }
}
