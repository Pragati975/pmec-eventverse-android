package com.pmec.eventverse.ui.events

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.pmec.eventverse.data.model.Registration
import com.pmec.eventverse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegistrationsScreen(onBack: () -> Unit) {
    val registrationViewModel: RegistrationViewModel = viewModel()
    val registrations by registrationViewModel.registrations
    val registrationState by registrationViewModel.registrationState
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedQRRegistration by remember { mutableStateOf<Registration?>(null) }

    LaunchedEffect(Unit) {
        registrationViewModel.loadUserRegistrations(currentUserId)
    }

    if (selectedQRRegistration != null) {
        QRCodeDialog(
            registration = selectedQRRegistration!!,
            onDismiss = { selectedQRRegistration = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Registrations", color = TextPrimary, fontWeight = FontWeight.Bold)
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
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (registrationState is RegistrationState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = AccentBlue) }
                }
            }

            if (registrations.isEmpty() && registrationState !is RegistrationState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎫", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No registrations yet", color = TextSecondary, fontSize = 16.sp)
                            Text("Register for events to see them here", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }

            items(registrations) { registration ->
                RegistrationCard(
                    registration = registration,
                    onViewQR = { selectedQRRegistration = registration },
                    onCancel = {
                        registrationViewModel.cancelRegistration(
                            registration.registrationId,
                            registration.eventId
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun RegistrationCard(
    registration: Registration,
    onViewQR: () -> Unit,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isCancelled = registration.status == "CANCELLED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) CardDark.copy(alpha = 0.5f) else CardDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        registration.eventTitle,
                        color = if (isCancelled) TextMuted else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        registration.eventVenue,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (registration.status) {
                                "CONFIRMED" -> SuccessGreen.copy(alpha = 0.2f)
                                "CANCELLED" -> ErrorRed.copy(alpha = 0.2f)
                                else -> WarningYellow.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        registration.status,
                        color = when (registration.status) {
                            "CONFIRMED" -> SuccessGreen
                            "CANCELLED" -> ErrorRed
                            else -> WarningYellow
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    dateFormat.format(Date(registration.eventDate)),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .background(SurfaceDark, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(registration.eventCategory, color = AccentBlue, fontSize = 10.sp)
                }

                if (registration.attended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Attended",
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (!isCancelled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewQR,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View QR", color = AccentBlue, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", color = ErrorRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QRCodeDialog(registration: Registration, onDismiss: () -> Unit) {
    val qrBitmap = remember { generateQRCode(registration.qrCode) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Your Entry QR Code",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    registration.eventTitle,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                androidx.compose.ui.graphics.Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Show this at the event entrance",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}