package com.pmec.eventverse.ui.auth

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pmec.eventverse.R
import com.pmec.eventverse.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                viewModel.setGoogleSignInError("Google sign-in was cancelled or failed")
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).role)
            viewModel.resetState()
        }
    }

    SpaceBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                "Welcome Back",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(listOf(AccentBlue, AccentPurple))
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Sign in to your EventVerse account",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            GoogleSignInButton(
                text = "Continue with Google",
                onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))
            OrDivider("or sign in with email")
            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "you@pmec.ac.in",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true,
                keyboardType = KeyboardType.Password
            )

            TextButton(
                onClick = { viewModel.forgotPassword(email) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot password?", color = AccentBlue, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            GradientAuthButton(
                text = "Sign In",
                onClick = { viewModel.login(email, password) },
                enabled = authState !is AuthState.Loading,
                loading = authState is AuthState.Loading
            )

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    "Register",
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}