package com.spendsense.domain.service

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern
import javax.inject.Inject

data class ReceiptData(
    val amount: Double?,
    val date: String?
)

class ReceiptScannerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanReceipt(imageUri: Uri): ReceiptData {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            val fullText = result.text
            
            ReceiptData(
                amount = extractAmount(fullText),
                date = extractDate(fullText)
            )
        } catch (e: Exception) {
            ReceiptData(null, null)
        }
    }

    private fun extractAmount(text: String): Double? {
        // Look for common currency indicators and amounts
        // Regex for numbers like 1,234.56 or 123.00
        val amountPattern = Pattern.compile("(?:RS|INR|TOTAL|AMOUNT|AMT)\\.?\\s*[:|\\-]?\\s*([\\d,]+\\.?\\d{0,2})", Pattern.CASE_INSENSITIVE)
        val matcher = amountPattern.matcher(text)
        
        var maxAmount = 0.0
        while (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: ""
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount > maxAmount) maxAmount = amount
        }
        
        if (maxAmount > 0) return maxAmount

        // Fallback: search for any floating point number that looks like a total
        val genericAmountPattern = Pattern.compile("(\\d{1,6}\\.\\d{2})")
        val genericMatcher = genericAmountPattern.matcher(text)
        while (genericMatcher.find()) {
            val amount = genericMatcher.group(1)?.toDoubleOrNull() ?: 0.0
            if (amount > maxAmount) maxAmount = amount
        }

        return if (maxAmount > 0) maxAmount else null
    }

    private fun extractDate(text: String): String? {
        // Regex for DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD
        val datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})")
        val matcher = datePattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }
}
