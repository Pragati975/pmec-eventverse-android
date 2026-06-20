package com.pmec.eventverse.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onEventCreated: () -> Unit
) {
    val context = LocalContext.current
    val eventViewModel: EventViewModel = viewModel()
    val eventState by eventViewModel.eventState

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TECHNICAL") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedTime by remember { mutableStateOf("09:00 AM") }
    var showSuccess by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val categories = listOf("TECHNICAL", "CULTURAL", "SPORTS", "WORKSHOP")

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDateMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
        },
        9, 0, false
    )

    LaunchedEffect(eventState) {
        if (eventState is EventState.Success) {
            showSuccess = true
        }
    }

    if (showSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            eventViewModel.resetState()
            onEventCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Event",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            if (showSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Event created! Waiting for admin approval.",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
// Poster Image Picker
            SectionLabel("Event Poster (Optional)")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to add poster image", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Title
            SectionLabel("Event Title *")
            StyledTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Annual Tech Symposium 2025"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            SectionLabel("Description *")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe your event...", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors(),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category
            SectionLabel("Category *")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Date *")
                    OutlinedButton(
                        onClick = { datePicker.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardDark)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            dateFormat.format(Date(selectedDateMillis)),
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Time *")
                    OutlinedButton(
                        onClick = { timePicker.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardDark)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedTime, color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Venue
            SectionLabel("Venue *")
            StyledTextField(
                value = venue,
                onValueChange = { venue = it },
                placeholder = "e.g. Main Auditorium"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max Participants
            SectionLabel("Max Participants *")
            StyledTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it.filter { c -> c.isDigit() } },
                placeholder = "e.g. 100"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error message
            if (eventState is EventState.Error) {
                Text(
                    text = (eventState as EventState.Error).message,
                    color = ErrorRed,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Submit Button
            val currentUser = FirebaseAuth.getInstance().currentUser
            Button(
                onClick = {
                    if (title.isBlank() || venue.isBlank() || maxParticipants.isBlank()) {
                        return@Button
                    }
                    val event = Event(
                        title = title,
                        description = description,
                        category = selectedCategory,
                        date = selectedDateMillis,
                        time = selectedTime,
                        venue = venue,
                        organizerId = currentUser?.uid ?: "",
                        organizerName = currentUser?.email ?: "Organizer",
                        maxParticipants = maxParticipants.toIntOrNull() ?: 50,
                        status = "UPCOMING",
                        approved = false
                    )
                    eventViewModel.createEventWithImage(event, selectedImageUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = eventState !is EventState.Loading
            ) {
                if (eventState is EventState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Publish Event",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = outlinedTextFieldColors(),
        singleLine = true
    )
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = CardDark,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = AccentBlue
)