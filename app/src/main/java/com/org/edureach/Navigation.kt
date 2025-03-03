package com.org.edureach

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.org.edureach.ui.AuthScreen
import com.org.edureach.ui.ContentScreen
import com.org.edureach.ui.LearningPathScreen
import androidx.navigation.NavHostController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.org.edureach.ui.QuizScreen
import com.org.edureach.ui.ProgressScreen
import com.org.edureach.ui.TaskScreen
import com.org.edureach.ui.ForgetPasswordScreen
import com.org.edureach.ui.ProfileScreen

@Composable
fun EduReachNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "welcome") {
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
        
        // Replace placeholders with actual screens
        composable("quiz") { 
            QuizScreen(navController)
        }
        composable("task") { 
            TaskScreen(navController)
        }
        composable("progress") { 
            ProgressScreen(navController)
        }
        
        // Add new AI Tutor screen
        composable("ai_tutor") {
            AITutorScreen(navController)
        }
    }
}
