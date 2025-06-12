package services.bookingapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.firebase.FirebaseApp
import services.bookingapp.UInterface.AuthPage
import services.bookingapp.UInterface.HomePage
import services.bookingapp.UInterface.LandingPage
import services.bookingapp.UInterface.RegistrationPage
import services.bookingapp.ui.pages.PhoneAuthPage
import services.bookingapp.ui.theme.BookingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this) // ✅ Init Firebase
        enableEdgeToEdge() // Optional for edge-to-edge UI

        setContent {
            BookingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = "landing",
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        }
    ) {
        composable("landing") { LandingPage(navController) }
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
