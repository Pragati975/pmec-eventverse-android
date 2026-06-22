package com.pmec.eventverse.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.data.model.Registration
import com.pmec.eventverse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onBack: () -> Unit
) {
    val registrationViewModel: RegistrationViewModel = viewModel()
    val registrationState by registrationViewModel.registrationState
    val isRegistered by registrationViewModel.isRegistered
    val currentRegistrationId by registrationViewModel.currentRegistrationId

    val currentUser = FirebaseAuth.getInstance().currentUser
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val seatsLeft = event.maxParticipants - event.currentRegistrations
    val isFull = seatsLeft <= 0

    var showSuccessDialog by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            registrationViewModel.checkRegistration(event.eventId, uid)
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    userProfile = doc.data
                }
        }
    }

    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationState.Success) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                registrationViewModel.resetState()
            },
            title = { Text("🎉 Registration Successful!", color = TextPrimary) },
            text = {
                Column {
                    Text("You're registered for ${event.title}!", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A QR code has been generated for your entry.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        registrationViewModel.resetState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Done")
                }
            },
            containerColor = SurfaceDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BackgroundDark,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp)
            ) {
                if (isRegistered) {
                    OutlinedButton(
                        onClick = {
                            registrationViewModel.cancelRegistration(
                                currentRegistrationId,
                                event.eventId
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Cancel Registration",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            currentUser?.let { user ->
                                val registration = Registration(
                                    eventId = event.eventId,
                                    eventTitle = event.title,
                                    eventDate = event.date,
                                    eventVenue = event.venue,
                                    eventCategory = event.category,
                                    userId = user.uid,
                                    userName = userProfile?.get("name") as? String ?: user.email ?: "",
                                    userEmail = user.email ?: "",
                                    userRollNumber = userProfile?.get("rollNumber") as? String ?: ""
                                )
                                registrationViewModel.registerForEvent(registration)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFull) TextMuted else AccentBlue
                        ),
                        enabled = !isFull && registrationState !is RegistrationState.Loading
                    ) {
                        if (registrationState is RegistrationState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (isFull) Icons.Default.Block
                                else Icons.Default.HowToReg,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFull) "Event Full" else "Register Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                if (registrationState is RegistrationState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (registrationState as RegistrationState.Error).message,
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Event Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (event.posterUrl.isNotEmpty()) {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val categoryColor = when (event.category) {
                        "TECHNICAL" -> TechnicalColor
                        "CULTURAL" -> CulturalColor
                        "SPORTS" -> SportsColor
                        "WORKSHOP" -> WorkshopColor
                        else -> AccentBlue
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryBlueDark, categoryColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (event.category) {
                                "TECHNICAL" -> "💻"
                                "CULTURAL" -> "🎭"
                                "SPORTS" -> "⚽"
                                "WORKSHOP" -> "🔧"
                                else -> "🎪"
                            },
                            fontSize = 64.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, BackgroundDark),
                                startY = 150f
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            when (event.category) {
                                "TECHNICAL" -> TechnicalColor
                                "CULTURAL" -> CulturalColor
                                "SPORTS" -> SportsColor
                                "WORKSHOP" -> WorkshopColor
                                else -> AccentBlue
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        event.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        iconColor = SuccessGreen,
                        label = "Seats Left",
                        value = if (isFull) "Full" else "$seatsLeft"
                    )
                    InfoMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.HowToReg,
                        iconColor = AccentBlue,
                        label = "Registered",
                        value = "${event.currentRegistrations}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isRegistered) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "You are registered for this event!",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    "Event Details",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                EventInfoRow(
                    icon = Icons.Default.CalendarToday,
                    iconColor = AccentBlue,
                    label = "Date",
                    value = dateFormat.format(Date(event.date))
                )
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                EventInfoRow(
                    icon = Icons.Default.Schedule,
                    iconColor = AccentPurple,
                    label = "Time",
                    value = event.time
                )
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                EventInfoRow(
                    icon = Icons.Default.LocationOn,
                    iconColor = ErrorRed,
                    label = "Venue",
                    value = event.venue
                )
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                EventInfoRow(
                    icon = Icons.Default.Person,
                    iconColor = WarningYellow,
                    label = "Organizer",
                    value = event.organizerName
                )
                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))
                EventInfoRow(
                    icon = Icons.Default.Groups,
                    iconColor = SuccessGreen,
                    label = "Total Seats",
                    value = "${event.maxParticipants}"
                )

                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "About this Event",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun InfoMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun EventInfoRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(
                value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}