package com.pmec.eventverse.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.theme.*
import com.pmec.eventverse.util.EventTimeUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    userName: String = "Student",
    onEventClick: (Event) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.filteredEvents
    val eventState by eventViewModel.eventState
    val selectedCategory by eventViewModel.selectedCategory
    val recommendedEvents by eventViewModel.recommendedEvents

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

    LaunchedEffect(Unit) {
        eventViewModel.loadApprovedEvents()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            eventViewModel.loadRecommendedEvents(uid)
        }
    }

    val categories = listOf(
        "ALL" to "🎯",
        "TECHNICAL" to "💻",
        "CULTURAL" to "🎭",
        "SPORTS" to "⚽",
        "WORKSHOP" to "🔧"
    )

    // Only ever show events that haven't started yet.
    val upcomingEvents: List<Event> = events.filterNot { EventTimeUtils.isEventPast(it) }
    val upcomingRecommendedEvents: List<Event> = recommendedEvents.filterNot { EventTimeUtils.isEventPast(it) }

    val filteredBySearch: List<Event> =
        if (debouncedQuery.isEmpty()) upcomingEvents
        else upcomingEvents.filter { event ->
            event.title.contains(debouncedQuery, ignoreCase = true) ||
                    event.venue.contains(debouncedQuery, ignoreCase = true) ||
                    event.category.contains(debouncedQuery, ignoreCase = true)
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryBlueDark, BackgroundDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Good day,",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = userName,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Add this line
                            Text(
                                text = "${filteredBySearch.size} events available",
                                color = AccentBlue,
                                fontSize = 12.sp
                            )
                        }


                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search events...", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Categories",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (category, icon) ->
                        CategoryChip(
                            label = category,
                            icon = icon,
                            isSelected = selectedCategory == category,
                            onClick = { eventViewModel.filterByCategory(category) }
                        )
                    }
                }
            }
        }

        item {
            RecommendedEventsRow(
                events = upcomingRecommendedEvents,
                onEventClick = onEventClick
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Events",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${filteredBySearch.size} events",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        if (eventState is EventState.Loading) {
            items(3) {
                ShimmerEventCard()
            }
        }

        if (filteredBySearch.isEmpty() && eventState !is EventState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎪", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No events found",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Check back later for upcoming events",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(filteredBySearch) { event ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onEdit = {}
                )
            }
        }
    }
}