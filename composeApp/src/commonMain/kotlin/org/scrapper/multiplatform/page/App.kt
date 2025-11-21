package org.scrapper.multiplatform.page

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.route.Route
import org.scrapper.multiplatform.template.slideComposable
import org.scrapper.multiplatform.theme.AppTheme
import org.scrapper.multiplatform.viewmodel.AppViewModel

@Composable
@Preview
fun App(
    viewModel: AppViewModel = koinViewModel()
) {
    AppTheme {
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            viewModel.initData()
            viewModel.effect.collect { effect ->
                navController.navigate(effect) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Route.SplashPage
        ) {
            slideComposable<Route.SplashPage> {
                SplashPage(
                    navController = navController
                )
            }
            slideComposable<Route.LoginPage> {
                LoginPage(
                    navController = navController
                )
            }
            slideComposable<Route.HomePage> {
                HomePage(
                    navController = navController
                )
            }
            slideComposable<Route.AdminPage> {
                AdminPage(
                    navController = navController
                )
            }
            slideComposable<Route.SiipBpjsPage> {
                SiipBpjsPage(
                    navController = navController
                )
            }
            slideComposable<Route.DptPage> {
                DptPage(
                    navController = navController
                )
            }
            slideComposable<Route.LasikPage> {
                LasikPage(
                    navController = navController
                )
            }
        }
    }
}