package com.app.research.good_gps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun ForeGolfApp(
    navHostController: NavHostController,
    viewModel: ForeGolfVM,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navHostController,
        startDestination = "Clubs",
        modifier = modifier
    ) {

        composable(route = "Clubs") {
            ClubsScreen(navHostController = navHostController)
        }

        composable(route = "Courses") { backStackEntry ->
            CoursesScreen(
                navHostController = navHostController,
                viewModel = viewModel
            )
        }

    }
}