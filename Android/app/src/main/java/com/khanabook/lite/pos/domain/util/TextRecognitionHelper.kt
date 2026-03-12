package com.khanabook.lite.pos.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Expert Refactor: Helper class for Google ML Kit Text Recognition.
 * Managed as a single instance via the UI lifecycle.
 */
class TextRecognitionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Processes an InputImage. Returns the Task to allow for cancellation/tracking if needed.
     */
    fun processImage(
        image: InputImage,
        onSuccess: (String, Text) -> Unit,
        onFailure: (Exception) -> Unit
    ): Task<Text> {
        return recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text, visionText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR_HELPER", "Processing failed", e)
                onFailure(e)
            }
    }

    /**
     * Processes image from Gallery URI.
     */
    fun processUri(
        context: Context,
        uri: Uri,
        onSuccess: (String, Text) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            processImage(image, onSuccess, onFailure)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Essential for memory management: Releases the detector resources.
     */
    fun close() {
        recognizer.close()
    }
}
