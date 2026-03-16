package com.example.babylonianlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.babylonianlibrary.core.DictionaryManager
import com.example.babylonianlibrary.ui.navigation.AppNavHost
import com.example.babylonianlibrary.ui.theme.BabylonianLibraryTheme
import com.example.babylonianlibrary.viewmodel.DictionaryViewModel
import com.example.babylonianlibrary.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var dictionaryManager: DictionaryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dictionaryManager = DictionaryManager(this)

        enableEdgeToEdge()

        setContent {
            BabylonianLibraryTheme {
                val navController = rememberNavController()

                // ViewModels
                val mainViewModel: MainViewModel = viewModel {
                    MainViewModel(dictionaryManager)
                }
                val dictionaryViewModel: DictionaryViewModel = viewModel {
                    DictionaryViewModel(dictionaryManager)
                }

                // Навигация
                AppNavHost(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    dictionaryViewModel = dictionaryViewModel
                )
            }
        }
    }
}