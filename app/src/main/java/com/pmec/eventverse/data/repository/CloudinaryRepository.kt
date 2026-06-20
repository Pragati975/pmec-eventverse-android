package com.pmec.eventverse.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryRepository {

    suspend fun uploadImage(imageUri: Uri, uploadPreset: String = "pmec_unsigned"): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .unsigned(uploadPreset)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String
                        if (url != null) {
                            continuation.resume(Result.success(url))
                        } else {
                            continuation.resume(Result.failure(Exception("Upload succeeded but no URL returned")))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(Result.failure(Exception(error?.description ?: "Upload failed")))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(Result.failure(Exception("Upload rescheduled")))
                    }
                })
                .dispatch()
        }
    }
}