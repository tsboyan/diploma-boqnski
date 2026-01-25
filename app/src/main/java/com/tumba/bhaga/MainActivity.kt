package com.tumba.bhaga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tumba.bhaga.data.local.ThemeManager
import com.tumba.bhaga.data.local.ThemeMode
import com.tumba.bhaga.ui.components.BottomAppBar
import com.tumba.bhaga.ui.components.BottomBarOption
import com.tumba.bhaga.ui.components.TopAppBar
import com.tumba.bhaga.ui.components.TopBarAction
import com.tumba.bhaga.ui.navigation.BhagaNavHost
import com.tumba.bhaga.ui.theme.BhagaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

            // Collect theme mode
            val themeMode by themeManager.themeMode.collectAsState(
                initial = runBlocking { themeManager.themeMode.first() }
            )

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val navOption = remember {
                mutableStateListOf(
                    BottomBarOption(
                        label = "Favourites",
                        icon = Icons.Filled.Star,
                        onClick = {
                            navController.navigate("favourites")
                        }
                    ),
                    BottomBarOption(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        onClick = {
                            navController.navigate("home")
                        }
                    ),
                    BottomBarOption(
                        label = "Profile",
                        icon = Icons.Filled.Person,
                        onClick = {
                            navController.navigate("profile")
                        }
                    ),
                    BottomBarOption(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        onClick = {
                            navController.navigate("settings")
                        }
                    )
                )
            }

            val selectedOption = when(currentRoute) {
                "favourites" -> 0
                "home" -> 1
                "profile" -> 2
                "settings" -> 3
                else -> -1
            }

            val isHome = selectedOption == 1
            val screenTitle = currentRoute
                ?.substringBefore("/")
                ?.replaceFirstChar { it.uppercaseChar() }
                ?: "Unknown"

            BhagaTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = {
                        if (currentRoute != "search" && currentRoute != "login" && currentRoute != "signup") {
                            TopAppBar(
                                screenTitle = screenTitle,
                                navigationAction = if (!isHome) {
                                    TopBarAction(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        onClick = { navController.popBackStack() }
                                    )
                                } else null,
                                searchAction = TopBarAction(
                                    imageVector = Icons.Filled.Search,
                                    onClick = { navController.navigate("search") }
                                ),
                                scrollBehaviorTop = scrollBehavior
                            )
                        }
                    },
                    content = { innerPadding ->
                        BhagaNavHost(
                            navController = navController,
                            scrollBehavior = scrollBehavior,
                            modifier = Modifier.padding(innerPadding)
                        )
                    },
                    bottomBar = {
                        if (selectedOption >= 0) {
                            BottomAppBar(
                                selected = selectedOption,
                                navOptions = navOption
                            )
                        }
                    }
                )
            }
        }
    }
}