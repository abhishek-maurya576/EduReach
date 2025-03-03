package com.org.edureach.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

// We don't need a separate WelcomeScreen composable anymore,
// as GetStartedActivity handles the UI.
// We keep this file, but replace its content.

@Composable
fun WelcomeScreen(navController: NavController){
    GetStartedScreen(navController = navController)
}
