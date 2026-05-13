package com.example.raitha_vartha.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_vartha.R
import kotlinx.coroutines.delay

private val DarkGreen = Color(0xFF0B3D2E)
private val AccentGreen = Color(0xFF2E7D32)

@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(LoginStep.Details) }
    var isLoading by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, DarkGreen.copy(alpha = 0.9f), DarkGreen)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.farm),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(modifier = Modifier.fillMaxSize().background(gradient))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (fadeIn(animationSpec = tween(600)) + slideInVertically { it / 2 })
                    .togetherWith(fadeOut(animationSpec = tween(400)) + slideOutVertically { -it / 2 })
            },
            label = "LoginTransition"
        ) { currentStep ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (currentStep) {
                    LoginStep.Details -> DetailsView(
                        name = name,
                        onNameChange = { name = it },
                        phone = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        isLoading = isLoading,
                        onProceed = {
                            isLoading = true
                        }
                    )
                    LoginStep.Otp -> OtpView(
                        phone = phoneNumber,
                        otp = otp,
                        onOtpChange = { otp = it },
                        isLoading = isLoading,
                        onVerify = {
                            isLoading = true
                        },
                        onBack = { step = LoginStep.Details }
                    )
                    LoginStep.Greeting -> GreetingView(name = name) {
                        onLoginSuccess(phoneNumber, name)
                    }
                }
            }
        }
    }

    // Side effects for simulation
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(1500)
            isLoading = false
            step = when (step) {
                LoginStep.Details -> LoginStep.Otp
                LoginStep.Otp -> LoginStep.Greeting
                else -> LoginStep.Greeting
            }
        }
    }
}

enum class LoginStep { Details, Otp, Greeting }

@Composable
fun DetailsView(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Raitha-Varta",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Growing with Knowledge",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Farmer's Name", color = DarkGreen) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkGreen) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { 
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    onPhoneChange(it)
                }
            },
            label = { Text("Mobile Number", color = DarkGreen) },
            prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = DarkGreen) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DarkGreen) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank() && phone.length == 10 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkGreen)
            } else {
                Text("Get Secure OTP", color = DarkGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OtpView(
    phone: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    onVerify: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🌾", fontSize = 40.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Verification", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = "Sent to +91 $phone", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        OutlinedTextField(
            value = otp,
            onValueChange = { 
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onOtpChange(it)
                }
            },
            label = { Text("Enter 6-digit OTP", color = DarkGreen) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            enabled = otp.length >= 4 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkGreen)
            } else {
                Text("Verify & Continue", color = DarkGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        TextButton(onClick = onBack) {
            Text("Change Number", color = Color.White)
        }
    }
}

@Composable
fun GreetingView(name: String, onAnimationEnd: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500)
        onAnimationEnd()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(32.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
    ) {
        Text(text = "Namaste,", color = Color.White.copy(alpha = 0.9f), fontSize = 36.sp)
        Text(text = name, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(200.dp).height(4.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Preparing your personalized tips...", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
    }
}
