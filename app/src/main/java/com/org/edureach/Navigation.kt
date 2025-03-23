package com.org.edureach

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.org.edureach.ui.AuthScreen
import com.org.edureach.ui.ContentScreen
import com.org.edureach.ui.LearningPathScreen
import androidx.navigation.NavHostController
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.org.edureach.ui.AssessmentScreen
import com.org.edureach.ui.WelcomeScreen
import com.org.edureach.ui.LoginActivity
import com.org.edureach.ui.SignUpActivity
import com.org.edureach.ui.SignUpScreen
import com.org.edureach.ui.GetStartedActivity
import com.org.edureach.ui.GetStartedScreen
import com.org.edureach.ui.InterestScreen
import com.org.edureach.ui.SettingsScreen
import com.org.edureach.ui.settings.LanguageSettingsScreen
import com.org.edureach.ui.settings.DataUsageSettingsScreen
import com.org.edureach.ui.LoginScreen
import com.org.edureach.ui.HomeScreen
import com.org.edureach.ui.AITutorScreen
import com.org.edureach.ui.ForgetPasswordScreen
import com.org.edureach.ui.ProfileScreen
import com.org.edureach.ui.TaskScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EduReachNavigation(
    navController: NavHostController = rememberNavController(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Check if user is already signed in
    val currentUser = auth.currentUser
    val startDestination = remember { mutableStateOf(if (currentUser != null) "home" else "welcome") }
    
    // Listen for auth changes - important for when user logs out
    LaunchedEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            startDestination.value = if (firebaseAuth.currentUser != null) "home" else "welcome"
        }
        auth.addAuthStateListener(authListener)
    }
    
    NavHost(navController = navController, startDestination = startDestination.value) {
        composable(
            route = "auth",
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth }
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> -fullWidth }
                )
            }
        ) { AuthScreen(navController) }
        composable("learningPath") { LearningPathScreen(navController) }
        composable("content/{lessonId}") { backStackEntry ->
            ContentScreen(
                lessonId = backStackEntry.arguments?.getString("lessonId") ?: "",
                navController = navController
            )
        }
        composable("assessment/{lessonId}") { backStackEntry ->
            AssessmentScreen(
                navController = navController,
                lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            )
        }
        composable("welcome") { GetStartedScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("forget_password") { ForgetPasswordScreen(navController) }
        composable("interest") { InterestScreen(navController) }
        composable("settings") { 
            SettingsScreen(navController) 
        }
        composable("settings/language") { 
            LanguageSettingsScreen(navController) 
        }
        composable("settings/data_usage") { 
            DataUsageSettingsScreen(navController) 
        }
        composable("home") { HomeScreen(navController) }
        
        composable("profile") { ProfileScreen(navController) }
        
        // Restore screen routes with placeholder implementations
        composable("quiz") { 
            // Temporary placeholder until QuizScreen is implemented
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Quiz Screen Coming Soon")
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            }
        }
        composable("task") { 
            TaskScreen(navController)
        }
        composable("progress") { 
            // Temporary placeholder until ProgressScreen is implemented
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Progress Screen Coming Soon")
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            }
        }
        
        // Add new AI Tutor screen
        composable("ai_tutor") {
            AITutorScreen(navController)
        }
    }
}
