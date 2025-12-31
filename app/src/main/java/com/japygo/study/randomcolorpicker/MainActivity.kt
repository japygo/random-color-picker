package com.japygo.study.randomcolorpicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.japygo.study.randomcolorpicker.data.ColorRepository
import com.japygo.study.randomcolorpicker.ui.CameraScreen
import com.japygo.study.randomcolorpicker.ui.ColorScreen
import com.japygo.study.randomcolorpicker.ui.theme.RandomColorPickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob
        com.japygo.study.randomcolorpicker.ads.AdMobManager.initialize(this)
        
        val repository = ColorRepository(applicationContext)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }
        
        enableEdgeToEdge()
        setContent {
            RandomColorPickerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel(factory = factory)

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            ColorScreen(
                                viewModel = viewModel,
                                onCameraClick = {
                                    navController.navigate("camera")
                                }
                            )
                        }
                        
                        composable("camera") {
                            CameraScreen(
                                onColorCaptured = { color ->
                                    viewModel.setCapturedColor(color)
                                    // 1. Navigate Back to Home first
                                    navController.popBackStack()
                                    // 2. Then Check/Show Ad
                                    com.japygo.study.randomcolorpicker.ads.AdMobManager.handleCameraExit(this@MainActivity)
                                },
                                onBack = {
                                    // 1. Navigate Back to Home first
                                    navController.popBackStack()
                                    // 2. Then Check/Show Ad
                                    com.japygo.study.randomcolorpicker.ads.AdMobManager.handleCameraExit(this@MainActivity)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}