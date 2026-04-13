package com.spendsense.domain.service

import com.google.ai.client.generativeai.GenerativeModel
import com.spendsense.BuildConfig
import com.spendsense.data.local.entity.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GeminiInsightService @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun generateFinancialAdvice(recentExpenses: List<Expense>, totalIncome: Double): Flow<String> = flow {
        if (BuildConfig.GEMINI_API_KEY == "PUT_YOUR_API_KEY_HERE" || BuildConfig.GEMINI_API_KEY.isBlank()) {
            emit("API Key is missing! Please configure GEMINI_API_KEY in local.properties to see AI Insights.")
            return@flow
        }

        if (recentExpenses.isEmpty()) {
            emit("Not enough spending data to generate insights yet. Add some expenses first!")
            return@flow
        }

        // Aggregate category expenses to not send individual transaction notes to AI for privacy
        val grouped = recentExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        val expenseSummary = grouped.entries.joinToString(separator = "\n") { "- ${it.key}: $${String.format("%.2f", it.value)}" }
        val totalSpent = recentExpenses.sumOf { it.amount }

        val prompt = """
            You are a helpful, extremely concise financial advisor.
            Here is the user's spending data for the recent period:
            Total Income Available: $${String.format("%.2f", totalIncome)}
            Total Spent: $${String.format("%.2f", totalSpent)}
            
            Category Breakdown:
            $expenseSummary
            
            Based on this data, provide 3 very brief encouraging bullet points on how to improve or maintain their budget. Keep it friendly and actionable.
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            emit(response.text ?: "Could not generate insights at this time.")
        } catch (e: Exception) {
            emit("Error reaching AI service: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)
}
