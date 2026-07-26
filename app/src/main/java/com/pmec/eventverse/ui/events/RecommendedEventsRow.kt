package com.pmec.eventverse.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecommendedEventsRow(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    if (events.isEmpty()) return

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Recommended for You",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                "AI Powered",
                color = AccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                RecommendedEventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

@Composable
fun RecommendedEventCard(
    event: Event,
    onClick: () -> Unit
) {
    val categoryColor = when (event.category) {
        "TECHNICAL" -> TechnicalColor
        "CULTURAL" -> CulturalColor
        "SPORTS" -> SportsColor
        "WORKSHOP" -> WorkshopColor
        else -> AccentBlue
    }

    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Image / Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (event.posterUrl.isNotEmpty()) {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
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
                            fontSize = 32.sp
                        )
                    }
                }

                // Category badge
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .background(categoryColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        event.category,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = event.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(event.date)),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.maxParticipants - event.currentRegistrations} seats left",
                    color = if ((event.maxParticipants - event.currentRegistrations) < 10)
                        ErrorRed else SuccessGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}