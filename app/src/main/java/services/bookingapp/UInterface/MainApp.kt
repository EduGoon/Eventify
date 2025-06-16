package services.bookingapp.UInterface

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.yourapp.booking.viewmodel.BookingViewModel
import services.bookingapp.ui.pages.PhoneAuthPage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainApp(viewModel: BookingViewModel = viewModel()) {
    val navController = rememberNavController()
    val authFlow by viewModel.authFlowState.collectAsState()

    // Check session on first launch
    LaunchedEffect(Unit) {
        viewModel.checkUserSessionAndFirstTime()
    }

    // Navigate based on session check result
    LaunchedEffect(authFlow.checked) {
        if (authFlow.checked) {
            when {
                !authFlow.isLoggedIn -> {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
                authFlow.isFirstTime -> {
                    navController.navigate("register") {
                        popUpTo(0) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = "splash", // no more landing
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it })
        }
    ) {
        composable("splash") { SplashScreen() }
        composable("auth") { AuthPage(navController) }
        composable("register") { RegistrationPage(navController) }
        composable("phoneAuth") {
            val context = LocalContext.current
            val activity = context as? Activity
            if (activity != null) {
                PhoneAuthPage(navController, activity)
            }
        }
        composable("home") { HomePage(navController) }
    }
}
