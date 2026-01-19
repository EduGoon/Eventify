package services.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object SignUp : Screen("signup")
    object Home : Screen("home")
}

@Composable
fun MainApp(
    webClientId: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    val isCheckingSession by viewModel.isCheckingSession.collectAsState()

    // Monitor auth state and redirect if user session becomes invalid
    LaunchedEffect(authState) {
        if (authState == null && !isCheckingSession) {
            navController.navigate(Screen.SignUp.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = if (authState != null) Screen.Home.route else Screen.SignUp.route
        ) {
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    webClientId = webClientId,
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen()
            }
        }
    }
}
