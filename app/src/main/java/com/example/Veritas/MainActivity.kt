package com.example.Veritas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.Veritas.core.DictionaryManager
import com.example.Veritas.ui.navigation.AppNavHost
import com.example.Veritas.ui.theme.BabylonianLibraryTheme
import com.example.Veritas.viewmodel.DictionaryViewModel
import com.example.Veritas.viewmodel.MainViewModel

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