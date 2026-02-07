package com.tumba.bhaga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tumba.bhaga.data.local.ThemeManager
import com.tumba.bhaga.data.local.ThemeMode
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.ui.components.BottomAppBar
import com.tumba.bhaga.ui.components.BottomBarOption
import com.tumba.bhaga.ui.components.TopAppBar
import com.tumba.bhaga.ui.components.TopBarAction
import com.tumba.bhaga.ui.navigation.BhagaNavHost
import com.tumba.bhaga.ui.theme.BhagaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var authRepository: AuthRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val scope = rememberCoroutineScope()

            // Theme
            val themeMode by themeManager.themeMode.collectAsState(
                initial = runBlocking { themeManager.themeMode.first() }
            )

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Check if user is admin
            var isAdmin by remember { mutableStateOf(false) }

            LaunchedEffect(currentRoute) {
                if (authRepository.isLoggedIn()) {
                    isAdmin = authRepository.isCurrentUserAdmin()
                }
            }

            val navOption = remember(isAdmin) {
                mutableStateListOf<BottomBarOption>().apply {
                    add(BottomBarOption(
                        label = "Favourites",
                        icon = Icons.Filled.Star,
                        onClick = { navController.navigate("favourites") }
                    ))
                    add(BottomBarOption(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        onClick = { navController.navigate("home") }
                    ))
                    if (isAdmin) {
                        add(BottomBarOption(
                            label = "Admin",
                            icon = Icons.Filled.Shield,
                            onClick = { navController.navigate("admin") }
                        ))
                    } else {
                        add(BottomBarOption(
                            label = "Profile",
                            icon = Icons.Filled.Person,
                            onClick = { navController.navigate("profile") }
                        ))
                    }
                    add(BottomBarOption(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        onClick = { navController.navigate("settings") }
                    ))
                }
            }

            val selectedOption = when(currentRoute) {
                "favourites" -> 0
                "home" -> 1
                "admin", "profile" -> 2
                "settings" -> 3
                else -> -1
            }

            val isHome = selectedOption == 1
            val screenTitle = when(currentRoute) {
                "admin" -> "Admin Panel"
                else -> currentRoute
                    ?.substringBefore("/")
                    ?.replaceFirstChar { it.uppercaseChar() }
                    ?: "Unknown"
            }

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