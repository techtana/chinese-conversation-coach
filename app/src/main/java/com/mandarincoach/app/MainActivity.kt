package com.mandarincoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mandarincoach.app.ui.navigation.AppNavigation
import com.mandarincoach.app.ui.theme.MandarinCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MandarinCoachTheme {
                AppNavigation()
            }
        }
    }
}
