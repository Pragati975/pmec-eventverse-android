package com.pmec.eventverse.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.repository.CloudinaryRepository
import com.pmec.eventverse.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val cloudinaryRepository = remember { CloudinaryRepository() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    fun uploadPhoto(uri: Uri) {
        photoError = null
        isUploadingPhoto = true
        coroutineScope.launch {
            val result = cloudinaryRepository.uploadImage(uri)
            result
                .onSuccess { url ->
                    currentUser?.uid?.let { uid ->
                        db.collection("users").document(uid)
                            .update("photoUrl", url)
                            .addOnSuccessListener {
                                photoUrl = url
                                isUploadingPhoto = false
                            }
                            .addOnFailureListener {
                                photoError = "Couldn't save photo. Try again."
                                isUploadingPhoto = false
                            }
                    }
                }
                .onFailure {
                    photoError = "Upload failed. Try again."
                    isUploadingPhoto = false
                }
        }
    }

    fun removePhoto() {
        photoError = null
        isUploadingPhoto = true
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .update("photoUrl", "")
                .addOnSuccessListener {
                    photoUrl = ""
                    isUploadingPhoto = false
                }
                .addOnFailureListener {
                    photoError = "Couldn't remove photo. Try again."
                    isUploadingPhoto = false
                }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadPhoto(uri)
        }
    }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
                    department = doc.getString("department") ?: ""
                    year = doc.getString("year") ?: ""
                    rollNumber = doc.getString("rollNumber") ?: ""
                    photoUrl = doc.getString("photoUrl") ?: ""
                    isLoading = false
                }
        }
    }

    if (showPhotoOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptionsSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    "Profile Photo",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                PhotoOptionRow(
                    icon = Icons.Default.Photo,
                    label = "Choose from Gallery",
                    tint = AccentBlue,
                    onClick = {
                        showPhotoOptionsSheet = false
                        imagePickerLauncher.launch("image/*")
                    }
                )

                if (photoUrl.isNotEmpty()) {
                    PhotoOptionRow(
                        icon = Icons.Default.Delete,
                        label = "Remove Photo",
                        tint = ErrorRed,
                        onClick = {
                            showPhotoOptionsSheet = false
                            removePhoto()
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profile", color = TextPrimary, fontWeight = FontWeight.Bold)
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (name.isNotEmpty()) name.first().uppercase() else "?",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isUploadingPhoto) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentPurple)
                        .clickable(enabled = !isUploadingPhoto) {
                            showPhotoOptionsSheet = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Change profile photo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                currentUser?.email ?: "",
                color = TextMuted,
                fontSize = 13.sp
            )

            if (photoError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    photoError ?: "",
                    color = ErrorRed,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "✅ Profile updated successfully!",
                        color = SuccessGreen,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Form fields
            EditField(label = "Full Name", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(12.dp))
            EditField(label = "Department", value = department, onValueChange = { department = it })
            Spacer(modifier = Modifier.height(12.dp))
            EditField(label = "Year", value = year, onValueChange = { year = it })
            Spacer(modifier = Modifier.height(12.dp))
            EditField(label = "Roll Number", value = rollNumber, onValueChange = { rollNumber = it })

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isSaving = true
                    currentUser?.uid?.let { uid ->
                        db.collection("users").document(uid)
                            .update(
                                mapOf(
                                    "name" to name,
                                    "department" to department,
                                    "year" to year,
                                    "rollNumber" to rollNumber
                                )
                            )
                            .addOnSuccessListener {
                                isSaving = false
                                showSuccess = true
                            }
                            .addOnFailureListener {
                                isSaving = false
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PhotoOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(
            label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = CardDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )
    }
}