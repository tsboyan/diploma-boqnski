package com.tumba.bhaga.ui.navigation

import androidx.compose.animation.core.animate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tumba.bhaga.ui.screens.favourites.FavouritesScreen
import com.tumba.bhaga.ui.screens.home.HomeScreen
import com.tumba.bhaga.ui.screens.login.LoginScreen
import com.tumba.bhaga.ui.screens.profile.ProfileScreen
import com.tumba.bhaga.ui.screens.search.SearchScreen
import com.tumba.bhaga.ui.screens.settings.SettingsScreen
import com.tumba.bhaga.ui.screens.signup.SignUpScreen
import com.tumba.bhaga.ui.screens.stockdetail.StockDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhagaNavHost(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    startDestination: String = "login"
) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect {
            val currentHeightOffset = scrollBehavior.state.heightOffset
            val currentContentOffset = scrollBehavior.state.contentOffset

            if (currentHeightOffset != 0f) {
                animate(
                    initialValue = currentHeightOffset,
                    targetValue = 0f
                ) { value, _ ->
                    scrollBehavior.state.heightOffset = value
                }
            } else {
                scrollBehavior.state.heightOffset = 0f
            }

            if (currentContentOffset != 0f) {
                animate(
                    initialValue = currentContentOffset,
                    targetValue = 0f
                ) { value, _ ->
                    scrollBehavior.state.contentOffset = value
                }
            } else {
                scrollBehavior.state.contentOffset = 0f
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                scrollBehavior = scrollBehavior,
                onStockClick = { ticker: String ->
                    println(ticker)
                    navController.navigate("details/$ticker")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen()
        }

        composable("favourites") {
            FavouritesScreen(
                scrollBehavior = scrollBehavior,
                onStockClick = { ticker: String ->
                    println(ticker)
                    navController.navigate("details/$ticker")
                }
            )
        }

        composable("search") {
            SearchScreen(
                onStockClick = {
                    navController.navigate("details/$it")
                }
            )
        }

        composable(
            route = "details/{ticker}",
            arguments = listOf(navArgument("ticker") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker")!!
            StockDetailScreen(ticker = ticker)
        }
    }
}