package com.spendsense.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.spendsense.domain.repository.TransactionRepository
import com.spendsense.data.local.entity.Expense
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.displayMessageBody
                parseExpenseFromSms(body)?.let { expense ->
                    scope.launch {
                        repository.insertExpense(expense)
                    }
                }
            }
        }
    }

    private fun parseExpenseFromSms(body: String): Expense? {
        // Simple regex for Indian Bank SMS patterns: "Spent INR 500.00", "Debited Rs. 100", etc.
        val amountPattern = Pattern.compile("(?i)(?:Rs|INR|Debited|Spent)\\.?\\s*([\\d,]+\\.?\\d{0,2})")
        val matcher = amountPattern.matcher(body)
        
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: return null
            val amount = amountStr.toDoubleOrNull() ?: return null
            
            // Basic logic to determine category from SMS keywords
            val category = when {
                body.contains("Zomato", true) || body.contains("Swiggy", true) -> "Food"
                body.contains("Amazon", true) || body.contains("Flipkart", true) -> "Shopping"
                body.contains("Uber", true) || body.contains("Ola", true) -> "Transport"
                else -> "Bank Transfer"
            }

            return Expense(
                amount = amount,
                category = category,
                date = System.currentTimeMillis(),
                paymentMethod = "Bank/UPI",
                note = "Auto-parsed from SMS"
            )
        }
        return null
    }
}
