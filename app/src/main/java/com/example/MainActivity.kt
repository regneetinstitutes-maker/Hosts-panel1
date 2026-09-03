package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainHostScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HostViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val uiState by viewModel.uiState.collectAsState()
                    if (uiState.currentHost == null) {
                        LoginScreen(viewModel = viewModel)
                    } else {
                        MainHostScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

