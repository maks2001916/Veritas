package com.example.babylonianlibrary.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.babylonianlibrary.ui.screens.*
import com.example.babylonianlibrary.util.createShareIntent
import com.example.babylonianlibrary.util.showToast
import com.example.babylonianlibrary.viewmodel.DictionaryViewModel
import com.example.babylonianlibrary.viewmodel.MainViewModel
import androidx.compose.ui.res.stringResource
import com.example.Veritas.R

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Result : Screen("result/{address}") {
        fun createRoute(address: String) = "result/$address"
    }
    object Dictionaries : Screen("dictionaries")
    object CreateDictionary : Screen("create_dictionary")
    object Settings : Screen("settings")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    dictionaryViewModel: DictionaryViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = mainViewModel,
                onNavigateToResult = { address ->
                    navController.navigate(Screen.Result.createRoute(address))
                },
                onNavigateToDictionaries = {
                    navController.navigate(Screen.Dictionaries.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            val context = LocalContext.current
            val shareTitle = stringResource(R.string.share)
            ResultScreen(
                viewModel = mainViewModel,
                address = address,
                onBack = { navController.popBackStack() },
                onCopyAddress = {
                    context.showToast("Адрес скопирован")
                },
                onShare = { shareText ->
                    context.startActivity(
                        Intent.createChooser(
                            createShareIntent(shareText),
                            shareTitle
                        )
                    )
                }
            )
        }

        composable(Screen.Dictionaries.route) {
            DictionaryScreen(
                viewModel = dictionaryViewModel,
                onDictionarySelected = { dict ->
                    mainViewModel.selectDictionary(dict)
                    navController.popBackStack()
                },
                onCreateDictionary = {
                    navController.navigate(Screen.CreateDictionary.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateDictionary.route) {
            CreateDictionaryScreen(
                onSave = { dictionary ->
                    dictionaryViewModel.saveDictionary(dictionary)
                    mainViewModel.loadDictionaries()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
