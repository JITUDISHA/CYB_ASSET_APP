package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.DossierViewModel
import com.example.ui.LoginScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Enable edge-to-edge drawing so that content flows underneath the system bars correctly
    enableEdgeToEdge()
    
    setContent {
      val viewModel: DossierViewModel = viewModel()
      
      // Reactive state collection for active security session and user theme toggle overrides
      val isLoggedIn by viewModel.isLoggedIn.collectAsState()
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isDarkMode) {
        if (isLoggedIn) {
          DashboardScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          LoginScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}
