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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onBack: () -> Unit,
    onRegister: () -> Unit = {},
    isRegistered: Boolean = false
) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val seatsLeft = event.maxParticipants - event.currentRegistrations
    val isFull = seatsLeft <= 0

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = BackgroundDark,
        bottomBar = {
            // Register Button at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isRegistered -> SuccessGreen
                            isFull -> TextMuted
                            else -> AccentBlue
                        }
                    ),
                    enabled = !isFull && !isRegistered
                ) {
                    Icon(
                        imageVector = when {
                            isRegistered -> Icons.Default.CheckCircle
                            isFull -> Icons.Default.Block
                            else -> Icons.Default.HowToReg
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isRegistered -> "Already Registered ✓"
                            isFull -> "Event Full"
                            else -> "Register Now"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
            // Event Banner / Poster
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
                    // Gradient placeholder
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

                // Gradient overlay
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

                // Category badge
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

            // Content
            Column(modifier = Modifier.padding(20.dp)) {

                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Seats card
                    InfoMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        iconColor = SuccessGreen,
                        label = "Seats Left",
                        value = if (isFull) "Full" else "$seatsLeft"
                    )

                    // Registrations card
                    InfoMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.HowToReg,
                        iconColor = AccentBlue,
                        label = "Registered",
                        value = "${event.currentRegistrations}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details Section
                Text(
                    "Event Details",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                EventInfoRow(
                    icon = Icons.Default.CalendarToday,
                    iconColor = AccentBlue,
                    label = "Date",
                    value = dateFormat.format(Date(event.date))
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                // Time
                EventInfoRow(
                    icon = Icons.Default.Schedule,
                    iconColor = AccentPurple,
                    label = "Time",
                    value = event.time
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                // Venue
                EventInfoRow(
                    icon = Icons.Default.LocationOn,
                    iconColor = ErrorRed,
                    label = "Venue",
                    value = event.venue
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                // Organizer
                EventInfoRow(
                    icon = Icons.Default.Person,
                    iconColor = WarningYellow,
                    label = "Organizer",
                    value = event.organizerName
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                // Max Participants
                EventInfoRow(
                    icon = Icons.Default.Groups,
                    iconColor = SuccessGreen,
                    label = "Total Seats",
                    value = "${event.maxParticipants}"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                if (event.description.isNotEmpty()) {
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

                Spacer(modifier = Modifier.height(16.dp))

                // Tags
                if (event.tags.isNotEmpty()) {
                    Text(
                        "Tags",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        event.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(SurfaceDark, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("#$tag", color = AccentBlue, fontSize = 12.sp)
                            }
                        }
                    }
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
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}