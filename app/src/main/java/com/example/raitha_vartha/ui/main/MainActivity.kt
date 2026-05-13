package com.example.raitha_vartha.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raitha_vartha.ui.login.LoginScreen
import com.example.raitha_vartha.ui.theme.RaithavarthaTheme
import com.example.raitha_vartha.viewmodel.TipViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        enableEdgeToEdge()
        setContent {
            RaithavarthaTheme {
                val viewModel: TipViewModel = viewModel()
                val userPhone by viewModel.userPhone.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    if (userPhone == null) {
                        LoginScreen(onLoginSuccess = { phone, name ->
                            viewModel.login(phone, name)
                        })
                    } else {
                        MainScreen(viewModel)
                    }
                }
            }
        }
    }
}
