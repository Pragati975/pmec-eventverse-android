package com.pmec.eventverse.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmec.eventverse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentDashboardScreen(
    eventId: String,
    eventTitle: String,
    onBack: () -> Unit
) {
    val viewModel: FeedbackViewModel = viewModel()
    val feedbacks by viewModel.feedbacks
    val summary by viewModel.sentimentSummary
    val feedbackState by viewModel.feedbackState

    LaunchedEffect(Unit) {
        viewModel.loadEventFeedback(eventId)
        viewModel.loadSentimentSummary(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Feedback Analysis", color = TextPrimary, fontWeight = FontWeight.Bold)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event title
            item {
                Text(eventTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("AI Sentiment Analysis", color = AccentPurple, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Summary cards
            summary?.let { s ->
                item {
                    // Overall sentiment bar
                    if (s.total > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Overall Sentiment",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val positivePercent = (s.positive.toFloat() / s.total * 100).toInt()
                                val negativePercent = (s.negative.toFloat() / s.total * 100).toInt()
                                val neutralPercent = 100 - positivePercent - negativePercent

                                // Sentiment breakdown
                                SentimentBar("😊 Positive", positivePercent, SuccessGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                SentimentBar("😐 Neutral", neutralPercent, WarningYellow)
                                Spacer(modifier = Modifier.height(8.dp))
                                SentimentBar("😞 Negative", negativePercent, ErrorRed)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stats row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem("Total", "${s.total}", TextPrimary)
                                    StatItem(
                                        "Avg Rating",
                                        "%.1f⭐".format(s.averageRating),
                                        WarningYellow
                                    )
                                    StatItem("Positive", "$positivePercent%", SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }

            // Empty state
            if (feedbacks.isEmpty() && feedbackState !is FeedbackState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No feedback yet", color = TextSecondary, fontSize = 16.sp)
                            Text(
                                "Feedback will appear here after the event",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Individual feedback cards
            if (feedbacks.isNotEmpty()) {
                item {
                    Text(
                        "Individual Feedback",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(feedbacks) { feedback ->
                    FeedbackCard(feedback = feedback)
                }
            }
        }
    }
}

@Composable
fun SentimentBar(label: String, percent: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = color,
            trackColor = SurfaceDark
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("$percent%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
fun FeedbackCard(feedback: com.pmec.eventverse.data.model.Feedback) {
    val sentimentColor = when (feedback.sentiment) {
        "POSITIVE" -> SuccessGreen
        "NEGATIVE" -> ErrorRed
        else -> WarningYellow
    }
    val sentimentEmoji = when (feedback.sentiment) {
        "POSITIVE" -> "😊"
        "NEGATIVE" -> "😞"
        else -> "😐"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        feedback.userName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Row {
                        repeat(feedback.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = WarningYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            sentimentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$sentimentEmoji ${feedback.sentiment}",
                        color = sentimentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (feedback.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    feedback.comment,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}