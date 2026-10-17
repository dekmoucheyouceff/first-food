package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.AddRestaurantScreen
import com.example.ui.screens.AddReviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.theme.TabibElMaklaTheme
import com.example.ui.viewmodel.FoodViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

sealed class AppScreen {
    data object Home : AppScreen()
    data class RestaurantDetail(val restaurantId: Long) : AppScreen()
    data object AddRestaurant : AppScreen()
    data class AddReview(val restaurantId: Long?) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: FoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TabibElMaklaTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppRoot(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        TabibMaklaApp(
            viewModel = viewModel,
            onSignOut = {
                AuthManager.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun TabibMaklaApp(
    viewModel: FoodViewModel,
    onSignOut: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    Crossfade(
        targetState = currentScreen,
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is AppScreen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { id ->
                        viewModel.selectRestaurant(id)
                        currentScreen = AppScreen.RestaurantDetail(id)
                    },
                    onNavigateToAddRestaurant = {
                        currentScreen = AppScreen.AddRestaurant
                    },
                    onNavigateToAddReview = { id ->
                        currentScreen = AppScreen.AddReview(id)
                    },
                    onSignOut = onSignOut
                )
            }

            is AppScreen.RestaurantDetail -> {
                RestaurantDetailScreen(
                    restaurantId = screen.restaurantId,
                    viewModel = viewModel,
                    onBack = {
                        viewModel.selectRestaurant(null)
                        currentScreen = AppScreen.Home
                    },
                    onNavigateToAddReview = { id ->
                        currentScreen = AppScreen.AddReview(id)
                    }
                )
            }

            is AppScreen.AddRestaurant -> {
                AddRestaurantScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.Home },
                    onRestaurantAdded = { newId ->
                        viewModel.selectRestaurant(newId)
                        currentScreen = AppScreen.RestaurantDetail(newId)
                    }
                )
            }

            is AppScreen.AddReview -> {
                AddReviewScreen(
                    initialRestaurantId = screen.restaurantId,
                    viewModel = viewModel,
                    onBack = {
                        if (screen.restaurantId != null) {
                            currentScreen = AppScreen.RestaurantDetail(screen.restaurantId)
                        } else {
                            currentScreen = AppScreen.Home
                        }
                    },
                    onReviewAdded = { restId ->
                        viewModel.selectRestaurant(restId)
                        currentScreen = AppScreen.RestaurantDetail(restId)
                    }
                )
            }
        }
    }
}
