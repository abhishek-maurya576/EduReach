package com.org.edureach.ui

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.org.edureach.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kotlin.math.min

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            SignUpScreen(navController)
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    var fullName by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Field error states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Validation functions
    fun validateFullName(): Boolean {
        return if (fullName.text.trim().length < 3) {
            fullNameError = "Name must be at least 3 characters"
            false
        } else {
            fullNameError = null
            true
        }
    }

    fun validateEmail(): Boolean {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            emailError = "Please enter a valid email address"
            false
        } else {
            emailError = null
            true
        }
    }

    fun validatePassword(): Boolean {
        return if (password.text.length < 6) {
            passwordError = "Password must be at least 6 characters"
            false
        } else {
            passwordError = null
            true
        }
    }

    fun validateConfirmPassword(): Boolean {
        return if (password.text != confirmPassword.text) {
            confirmPasswordError = "Passwords do not match"
            false
        } else {
            confirmPasswordError = null
            true
        }
    }

    fun validateAll(): Boolean {
        val nameValid = validateFullName()
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        val confirmValid = validateConfirmPassword()
        return nameValid && emailValid && passwordValid && confirmValid
    }

    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    // Calculate dynamic sizes
    val logoSize = (screenWidth * 0.4f).coerceAtMost(200.dp)
    val buttonHeight = (screenHeight * 0.07f).coerceAtMost(56.dp)
    val horizontalPadding = screenWidth * 0.06f
    val verticalSpacing = screenHeight * 0.02f
    
    // Calculate text sizes
    val density = LocalDensity.current
    val titleSize = with(density) { (screenWidth * 0.05f).coerceAtMost(24.dp).toSp() }
    val bodySize = with(density) { (screenWidth * 0.035f).coerceAtMost(16.dp).toSp() }
    val smallTextSize = with(density) { (screenWidth * 0.03f).coerceAtMost(14.dp).toSp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
                .padding(top = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

            // App Logo with dynamic sizing
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.edureach_logo),
                    contentDescription = "EduReach Logo",
                    modifier = Modifier
                        .size(logoSize * 0.8f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

            // Input Fields with dynamic sizing
            OutlinedTextField(
                value = fullName,
                onValueChange = { 
                    fullName = it
                    if (fullNameError != null) validateFullName()
                },
                label = { Text("Full Name", color = Color.Black, fontSize = bodySize) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                isError = fullNameError != null,
                supportingText = { 
                    fullNameError?.let { 
                        Text(it, 
                            color = MaterialTheme.colorScheme.error,
                            fontSize = smallTextSize
                        ) 
                    } 
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = bodySize),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE6E6E6),
                    focusedContainerColor = Color(0xFFE6E6E6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorContainerColor = Color(0xFFFFEBEE)
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (emailError != null) validateEmail()
                },
                label = { Text("Email Address", color = Color.Black, fontSize = bodySize) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                isError = emailError != null,
                supportingText = { 
                    emailError?.let { 
                        Text(it, 
                            color = MaterialTheme.colorScheme.error,
                            fontSize = smallTextSize
                        ) 
                    } 
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = bodySize),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE6E6E6),
                    focusedContainerColor = Color(0xFFE6E6E6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorContainerColor = Color(0xFFFFEBEE)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    if (passwordError != null) validatePassword()
                    if (confirmPasswordError != null) validateConfirmPassword()
                },
                label = { Text("Password", color = Color.Black, fontSize = bodySize) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                isError = passwordError != null,
                supportingText = { 
                    passwordError?.let { 
                        Text(it, 
                            color = MaterialTheme.colorScheme.error,
                            fontSize = smallTextSize
                        ) 
                    } 
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = bodySize),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE6E6E6),
                    focusedContainerColor = Color(0xFFE6E6E6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorContainerColor = Color(0xFFFFEBEE)
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    if (confirmPasswordError != null) validateConfirmPassword()
                },
                label = { Text("Confirm Password", color = Color.Black, fontSize = bodySize) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                isError = confirmPasswordError != null,
                supportingText = { 
                    confirmPasswordError?.let { 
                        Text(it, 
                            color = MaterialTheme.colorScheme.error,
                            fontSize = smallTextSize
                        ) 
                    } 
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = bodySize),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE6E6E6),
                    focusedContainerColor = Color(0xFFE6E6E6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorContainerColor = Color(0xFFFFEBEE)
                )
            )

            Spacer(modifier = Modifier.height(verticalSpacing))

            // Sign Up Button with dynamic sizing
            Button(
                onClick = {
                    error = null
                    if (validateAll()) {
                        isLoading = true
                        // Create user with Firebase Auth
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text, password.text)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // User registered successfully, now store user data in Firestore
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    userId?.let {
                                        val userData = hashMapOf(
                                            "userId" to it,
                                            "email" to email.text,
                                            "displayName" to fullName.text.trim(),
                                            "interests" to emptyList<String>()
                                        )
                                        
                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(it)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                // Navigate to interest screen instead of home
                                                navController.navigate("interest") {
                                                    popUpTo("welcome") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                error = "Error creating user profile: ${e.message}"
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    error = when {
                                        task.exception?.message?.contains("email address is already in use") == true ->
                                            "This email is already registered"
                                        task.exception?.message?.contains("badly formatted") == true ->
                                            "Please enter a valid email address"
                                        else -> "Registration failed: ${task.exception?.message}"
                                    }
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDBA84F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Sign Up",
                        fontSize = bodySize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

            // Error message with dynamic sizing
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = smallTextSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

            // Already have an account link with dynamic sizing
            Text(
                text = "Already have an Account? Login",
                fontSize = smallTextSize,
                color = Color(0xFFDBA84F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { 
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
