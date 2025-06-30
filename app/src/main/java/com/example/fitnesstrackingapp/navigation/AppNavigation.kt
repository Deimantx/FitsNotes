package com.example.fitnesstrackingapp.navigation // Adjust package name as needed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnesstrackingapp.ui.auth.AuthViewModel
import com.example.fitnesstrackingapp.ui.auth.SignInScreen
import com.example.fitnesstrackingapp.ui.auth.SignUpScreen
import com.example.fitnesstrackingapp.ui.lifts.AddLiftScreen
import com.example.fitnesstrackingapp.ui.lifts.LiftHistoryScreen
import com.example.fitnesstrackingapp.ui.main.HomeScreen
import com.example.fitnesstrackingapp.ui.main.ProfileScreen
import com.example.fitnesstrackingapp.ui.templates.CreateTemplateScreen
import com.example.fitnesstrackingapp.ui.templates.TemplateDetailScreen
import com.example.fitnesstrackingapp.ui.templates.ViewTemplatesScreen

// Define navigation routes
object AppRoutes {
    const val AUTH_GRAPH_ROUTE = "auth_graph"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    // const val FORGOT_PASSWORD = "forgot_password"

    const val MAIN_APP_GRAPH_ROUTE = "main_app_graph"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ADD_LIFT = "add_lift"
    const val LIFT_HISTORY = "lift_history"
    const val VIEW_TEMPLATES = "view_templates"
    const val CREATE_TEMPLATE = "create_template"
    const val TEMPLATE_DETAIL = "template_detail/{templateId}" // Route with argument
    fun templateDetailRoute(templateId: String) = "template_detail/$templateId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val startDestination = if (authState != null) AppRoutes.MAIN_APP_GRAPH_ROUTE else AppRoutes.AUTH_GRAPH_ROUTE

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoutes.AUTH_GRAPH_ROUTE) {
            AuthNavHost(
                onAuthSuccess = {
                    navController.navigate(AppRoutes.MAIN_APP_GRAPH_ROUTE) {
                        popUpTo(AppRoutes.AUTH_GRAPH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(AppRoutes.MAIN_APP_GRAPH_ROUTE) {
            MainAppNavHost(
                onSignOutComplete = {
                    navController.navigate(AppRoutes.AUTH_GRAPH_ROUTE) {
                        popUpTo(AppRoutes.MAIN_APP_GRAPH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun AuthNavHost(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = AppRoutes.SIGN_IN) {
        composable(AppRoutes.SIGN_IN) {
            SignInScreen(
                authViewModel = authViewModel,
                onSignInSuccess = onAuthSuccess,
                onNavigateToSignUp = { navController.navigate(AppRoutes.SIGN_UP) },
                onNavigateToForgotPassword = { /* TODO */ }
            )
        }
        composable(AppRoutes.SIGN_UP) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = onAuthSuccess,
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainAppNavHost(
    onSignOutComplete: () -> Unit,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = AppRoutes.HOME) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToProfile = { navController.navigate(AppRoutes.PROFILE) },
                onNavigateToAddLift = { navController.navigate(AppRoutes.ADD_LIFT) },
                onNavigateToLiftHistory = { navController.navigate(AppRoutes.LIFT_HISTORY) },
                onNavigateToViewTemplates = { navController.navigate(AppRoutes.VIEW_TEMPLATES) }
            )
        }
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                onSignedOut = onSignOutComplete
            )
        }
        composable(AppRoutes.ADD_LIFT) {
            AddLiftScreen(
                onLiftAdded = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.LIFT_HISTORY) {
            LiftHistoryScreen(
                onNavigateToAddLift = { navController.navigate(AppRoutes.ADD_LIFT) }
            )
        }
        composable(AppRoutes.VIEW_TEMPLATES) {
            ViewTemplatesScreen(
                onNavigateToCreateTemplate = { navController.navigate(AppRoutes.CREATE_TEMPLATE) },
                onNavigateToTemplateDetail = { templateId ->
                    navController.navigate(AppRoutes.templateDetailRoute(templateId))
                }
            )
        }
        composable(AppRoutes.CREATE_TEMPLATE) {
            CreateTemplateScreen(
                onTemplateCreated = { navController.popBackStack() }
            )
        }
        composable(
            route = AppRoutes.TEMPLATE_DETAIL,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            if (templateId != null) {
                TemplateDetailScreen(
                    templateId = templateId,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Handle error or navigate back if templateId is null
                navController.popBackStack()
            }
        }
    }
}
