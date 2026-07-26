package com.pmec.eventverse.ui.auth

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val BRANCH_OPTIONS = listOf("CSE", "ECE", "EEE", "ME", "CE", "IT","CHE")
private val YEAR_OPTIONS = listOf("1st", "2nd", "3rd", "4th")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var branchMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

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
            onSignUpSuccess((authState as AuthState.Success).role)
            viewModel.resetState()
        }
    }

    SpaceBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "Join EventVerse",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(listOf(AccentBlue, AccentPurple))
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Create your account to get started",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            GoogleSignInButton(
                text = "Continue with Google",
                onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))
            OrDivider("or register with email")
            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "John Doe"
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                label = "College Email",
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
                placeholder = "Min 6 characters",
                isPassword = true,
                keyboardType = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Branch dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "BRANCH",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = branchMenuExpanded,
                        onExpandedChange = { branchMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = branch,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select", color = TextMuted, fontSize = 14.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = CardDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = branchMenuExpanded,
                            onDismissRequest = { branchMenuExpanded = false },
                            containerColor = SurfaceDark
                        ) {
                            BRANCH_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = TextPrimary) },
                                    onClick = {
                                        branch = option
                                        branchMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Year dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "YEAR",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = yearMenuExpanded,
                        onExpandedChange = { yearMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select", color = TextMuted, fontSize = 14.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = CardDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false },
                            containerColor = SurfaceDark
                        ) {
                            YEAR_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = TextPrimary) },
                                    onClick = {
                                        year = option
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                label = "Roll Number",
                value = rollNumber,
                onValueChange = { rollNumber = it },
                placeholder = "e.g. 213"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign-up always creates a Student account. Organizer/Admin accounts
            // are created manually via the Firebase Console — see conversation notes.
            GradientAuthButton(
                text = "Create Account",
                onClick = {
                    viewModel.signUp(name, email, password, "STUDENT", branch, year, rollNumber)
                },
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
                Text("Already have an account? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    "Sign In",
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}