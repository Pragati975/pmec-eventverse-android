package com.pmec.eventverse.ui.feedback

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    eventId: String,
    eventTitle: String,
    onBack: () -> Unit
) {
    val viewModel: FeedbackViewModel = viewModel()
    val feedbackState by viewModel.feedbackState
    val currentUser = FirebaseAuth.getInstance().currentUser

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: currentUser.email ?: ""
                }
        }
    }

    LaunchedEffect(feedbackState) {
        if (feedbackState is FeedbackState.Success) {
            showSuccess = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Give Feedback", color = TextPrimary, fontWeight = FontWeight.Bold)
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
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Feedback submitted!",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val msg = (feedbackState as? FeedbackState.Success)?.message ?: ""
                        if (msg.contains("Sentiment:")) {
                            val sentiment = msg.substringAfter("Sentiment:").trim()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "AI Analysis: ",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                val sentimentColor = when (sentiment) {
                                    "POSITIVE" -> SuccessGreen
                                    "NEGATIVE" -> ErrorRed
                                    else -> WarningYellow
                                }
                                val sentimentEmoji = when (sentiment) {
                                    "POSITIVE" -> "😊"
                                    "NEGATIVE" -> "😞"
                                    else -> "😐"
                                }
                                Text(
                                    "$sentimentEmoji $sentiment",
                                    color = sentimentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Go Back")
                }
                return@Column
            }

            // Event title
            Text(
                eventTitle,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "Share your experience",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Star Rating
            Text(
                "Rate this event *",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { rating = star },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= rating)
                                Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) WarningYellow else TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            if (rating > 0) {
                Text(
                    text = when (rating) {
                        1 -> "😞 Very Poor"
                        2 -> "😐 Poor"
                        3 -> "🙂 Average"
                        4 -> "😊 Good"
                        5 -> "🤩 Excellent!"
                        else -> ""
                    },
                    color = when (rating) {
                        1, 2 -> ErrorRed
                        3 -> WarningYellow
                        4, 5 -> SuccessGreen
                        else -> TextMuted
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Comment
            Text(
                "Your feedback *",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = {
                    Text(
                        "Tell us about your experience...",
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AccentPurple.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text("🤖", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Your feedback will be analyzed by AI to determine sentiment",
                    color = AccentPurple,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error
            if (feedbackState is FeedbackState.Error) {
                Text(
                    (feedbackState as FeedbackState.Error).message,
                    color = ErrorRed,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (rating == 0 || comment.isBlank()) return@Button
                    viewModel.submitFeedback(
                        eventId = eventId,
                        eventTitle = eventTitle,
                        userId = currentUser?.uid ?: "",
                        userName = userName,
                        rating = rating,
                        comment = comment
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = feedbackState !is FeedbackState.Loading &&
                        feedbackState !is FeedbackState.Analyzing
            ) {
                when (feedbackState) {
                    is FeedbackState.Analyzing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Analyzing...", fontWeight = FontWeight.Bold)
                    }
                    is FeedbackState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    }
                    else -> {
                        Text("Submit Feedback", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}