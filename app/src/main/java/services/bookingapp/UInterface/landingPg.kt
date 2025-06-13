package services.bookingapp.UInterface

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.yourapp.booking.viewmodel.BookingViewModel

@Composable
fun LandingPage(navController: NavHostController) {

    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    // Floating animation for elements
    val floatingAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFF6B73FF)
                    ),
                    startY = gradientOffset,
                    endY = gradientOffset + 800f
                )
            )
    ) {
        // Animated background particles
        repeat(20) { index ->
            val animatedOffsetX by infiniteTransition.animateFloat(
                initialValue = (index * 50).toFloat(),
                targetValue = (index * 50 + 100).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween((3000 + index * 200), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_$index"
            )

            val animatedOffsetY by infiniteTransition.animateFloat(
                initialValue = (index * 80).toFloat(),
                targetValue = (index * 80 + 150).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween((4000 + index * 150), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_y_$index"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
                    .size((4 + index % 3).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f + (index % 3) * 0.05f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Hero Section with floating animation
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo/Icon with glow effect
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                radius = 100f
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Main Title with gradient text effect
                Text(
                    text = "Eventify",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Blue,
                                Color.Cyan,
                                Color.Magenta
                            )
                        )
                    )
                )


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Discover • Book • Experience",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your gateway to unforgettable events",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Action Section with glassmorphism cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Sign Up Card
                GlassmorphismCard(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "New to Eventify?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Card
                GlassmorphismCard(
                    onClick = { navController.navigate("auth") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Login,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "  Or continue with  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SocialAuthButtons(navController, viewModel())

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun GlassmorphismCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f)
        )
    ) {
        content()
    }
}

@Composable
fun SocialAuthButtons(
    navController: NavHostController,
    viewModel: BookingViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate("home") {
                popUpTo("landing") { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode != Activity.RESULT_OK) {
                Log.d("GoogleSignIn", "Cancelled or resultCode=${result.resultCode}")
                return@rememberLauncherForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken) {}
            } else {
                Log.e("GoogleSignIn", "Missing idToken: ${account?.email ?: "No email"}")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google API error (${e.statusCode})", e)
        } catch (t: Throwable) {
            Log.e("GoogleSignIn", "Unhandled launcher error", t)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp), // Optional: add some side padding
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Signing in...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        } else {
            AndroidView(
                factory = { context ->
                    SignInButton(context).apply {
                        setSize(SignInButton.SIZE_WIDE)
                        setColorScheme(SignInButton.COLOR_LIGHT)
                        setOnClickListener {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("1002676602075-uc8iv3mq30fm0cjr6edv6s7emb0e0ulr.apps.googleusercontent.com")
                                .requestEmail()
                                .build()

                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                launcher.launch(signInIntent)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }

        //Other social buttons can be added here

    }
}
