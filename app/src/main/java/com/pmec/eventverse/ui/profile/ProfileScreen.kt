package com.pmec.eventverse.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userDepartment by remember { mutableStateOf("") }
    var userYear by remember { mutableStateOf("") }
    var userRollNumber by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var userPoints by remember { mutableStateOf(0) }
    var userPhotoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var registrationCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: ""
                    userEmail = doc.getString("email") ?: currentUser.email ?: ""
                    userDepartment = doc.getString("department") ?: ""
                    userYear = doc.getString("year") ?: ""
                    userRollNumber = doc.getString("rollNumber") ?: ""
                    userRole = doc.getString("role") ?: ""
                    userPoints = doc.getLong("points")?.toInt() ?: 0
                    userPhotoUrl = doc.getString("photoUrl") ?: ""
                    isLoading = false
                }

            // Count registrations
            FirebaseFirestore.getInstance()
                .collection("registrations")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .addOnSuccessListener { snap ->
                    registrationCount = snap.size()
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    )
    {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryBlueDark, BackgroundDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar circle with photo or initials
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (userName.isNotEmpty()) userName.first().uppercase() else "?",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName.ifEmpty { "Loading..." },
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AccentBlue),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Edit Profile",
                        color = AccentBlue,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = userRole,
                        color = AccentPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Points card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    label = "Points",
                    value = "$userPoints",
                    color = WarningYellow
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = SurfaceDark
                )

                ProfileStat(
                    label = "Year",
                    value = userYear.ifEmpty { "N/A" },
                    color = AccentBlue
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = SurfaceDark
                )

                ProfileStat(
                    label = "Dept",
                    value = userDepartment.ifEmpty { "N/A" },
                    color = SuccessGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Profile Info Section
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Personal Information",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard {
                ProfileInfoRow(Icons.Default.Person, "Full Name", userName.ifEmpty { "N/A" })
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(Icons.Default.Email, "Email", userEmail.ifEmpty { "N/A" })
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(Icons.Default.Badge, "Roll Number", userRollNumber.ifEmpty { "N/A" })
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(Icons.Default.School, "Department", userDepartment.ifEmpty { "N/A" })
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(Icons.Default.CalendarToday, "Year", userYear.ifEmpty { "N/A" })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun ProfileInfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}