package com.pmec.eventverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.navigation.AppNavigation
import com.pmec.eventverse.ui.theme.PMECEventVerseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary
        val config = hashMapOf("cloud_name" to "dzhqtesya")
        com.cloudinary.android.MediaManager.init(this, config)

        // Enable Firestore offline persistence
        FirebaseFirestore.getInstance().firestoreSettings =
            com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        enableEdgeToEdge()
        setContent {
            PMECEventVerseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}