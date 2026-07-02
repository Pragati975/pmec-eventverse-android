package com.pmec.eventverse.ui.scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.pmec.eventverse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(onBack: () -> Unit) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("QR Scanner", color = TextPrimary, fontWeight = FontWeight.Bold)
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasCameraPermission) {
                // No permission state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Camera permission required",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else if (scanResult != null) {
                // Show scan result
                ScanResultView(
                    result = scanResult!!,
                    onScanAgain = {
                        scanResult = null
                        isProcessing = false
                    }
                )
            } else {
                // Camera view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { context ->
                            DecoratedBarcodeView(context).apply {
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult?) {
                                        result?.text?.let { qrContent ->
                                            if (!isProcessing && qrContent.startsWith("PMEC_")) {
                                                isProcessing = true
                                                processQRCode(qrContent) { scanRes ->
                                                    scanResult = scanRes
                                                }
                                            }
                                        }
                                    }
                                })
                                resume()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Instructions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = AccentBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Processing...", color = TextSecondary)
                        } else {
                            Text(
                                "Point camera at student's QR code",
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "QR code will scan automatically",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultView(
    result: ScanResult,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (result.success) {
            Text("✅", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Attendance Marked!",
                color = SuccessGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Student", result.studentName)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Roll No", result.rollNumber)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Event", result.eventTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Status", if (result.alreadyAttended) "Already attended" else "Marked present")
                }
            }
        } else {
            Text("❌", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Invalid QR Code",
                color = ErrorRed,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                result.errorMessage,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("Scan Another", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

data class ScanResult(
    val success: Boolean,
    val studentName: String = "",
    val rollNumber: String = "",
    val eventTitle: String = "",
    val alreadyAttended: Boolean = false,
    val errorMessage: String = ""
)

fun processQRCode(qrContent: String, onResult: (ScanResult) -> Unit) {
    // QR format: PMEC_eventId_userId_registrationId
    val parts = qrContent.split("_")
    if (parts.size < 4) {
        onResult(ScanResult(success = false, errorMessage = "Invalid QR code format"))
        return
    }

    val eventId = parts[1]
    val userId = parts[2]
    val registrationId = parts[3]

    val db = FirebaseFirestore.getInstance()

    // Find registration
    db.collection("registrations")
        .document(registrationId)
        .get()
        .addOnSuccessListener { regDoc ->
            if (!regDoc.exists()) {
                onResult(ScanResult(success = false, errorMessage = "Registration not found"))
                return@addOnSuccessListener
            }

            val status = regDoc.getString("status") ?: ""
            if (status == "CANCELLED") {
                onResult(ScanResult(success = false, errorMessage = "This registration was cancelled"))
                return@addOnSuccessListener
            }

            val alreadyAttended = regDoc.getBoolean("attended") ?: false
            val studentName = regDoc.getString("userName") ?: "Unknown"
            val rollNumber = regDoc.getString("userRollNumber") ?: "N/A"
            val eventTitle = regDoc.getString("eventTitle") ?: "Unknown Event"

            if (!alreadyAttended) {
                // Mark as attended
                db.collection("registrations")
                    .document(registrationId)
                    .update("attended", true)
                    .addOnSuccessListener {
                        onResult(
                            ScanResult(
                                success = true,
                                studentName = studentName,
                                rollNumber = rollNumber,
                                eventTitle = eventTitle,
                                alreadyAttended = false
                            )
                        )
                    }
                    .addOnFailureListener {
                        onResult(ScanResult(success = false, errorMessage = "Failed to mark attendance"))
                    }
            } else {
                onResult(
                    ScanResult(
                        success = true,
                        studentName = studentName,
                        rollNumber = rollNumber,
                        eventTitle = eventTitle,
                        alreadyAttended = true
                    )
                )
            }
        }
        .addOnFailureListener {
            onResult(ScanResult(success = false, errorMessage = "Network error. Try again."))
        }
}