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

    private fun getGenerativeModel(): GenerativeModel? {
        return try {
            if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "PUT_YOUR_API_KEY_HERE") {
                GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun generateFinancialAdvice(recentExpenses: List<Expense>, totalIncome: Double): Flow<String> = flow {
        val model = getGenerativeModel()
        
        if (recentExpenses.isEmpty()) {
            emit("Not enough spending data to generate insights yet. Add some expenses first!")
            return@flow
        }

        val totalSpent = recentExpenses.sumOf { it.amount }
        val grouped = recentExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        if (model == null) {
            // Local fallback insights logic
            val topCategory = grouped.maxByOrNull { it.value }
            val insights = StringBuilder()
            insights.append("💡 **Local Smart Insights (AI Offline)**\n\n")
            
            if (totalSpent > totalIncome && totalIncome > 0) {
                insights.append("⚠️ You have spent more than your income this period. Consider reviewing your 'Others' category.\n")
            } else if (totalSpent > totalIncome * 0.8 && totalIncome > 0) {
                insights.append("ℹ️ You've used over 80% of your income. Slow down on non-essential spending.\n")
            } else {
                insights.append("✅ Great job! Your spending is well within your income limits.\n")
            }
            
            topCategory?.let {
                insights.append("🍔 Your highest spending is in **${it.key}** ($${String.format("%.2f", it.value)}).\n")
            }
            
            val smallExpenses = recentExpenses.filter { it.amount < 10.0 }
            if (smallExpenses.size > 5) {
                insights.append("🔍 Money Leak: You have many small transactions under $10. These add up quickly!\n")
            }
            
            insights.append("\n*To get personalized AI advice, add your Gemini API Key to local.properties.*")
            emit(insights.toString())
            return@flow
        }

        val expenseSummary = grouped.entries.joinToString(separator = "\n") { "- ${it.key}: $${String.format("%.2f", it.value)}" }

        val prompt = """
            You are a helpful, extremely concise financial advisor.
            Here is the user's spending data for the recent period:
            Total Income Available: $${String.format("%.2f", totalIncome)}
            Total Spent: $${String.format("%.2f", totalSpent)}
            
            Category Breakdown:
            $expenseSummary
            
            Based on this data, provide 3 very brief encouraging bullet points on how to improve or maintain their budget. 
            Mention the specific high-spending category if applicable. Keep it friendly and actionable.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            emit(response.text ?: "Could not generate insights at this time.")
        } catch (e: Exception) {
            emit("Error reaching AI service: ${e.localizedMessage}\n\nFalling back to local analysis...\n" + generateLocalSummary(recentExpenses, totalIncome))
        }
    }.flowOn(Dispatchers.IO)

    private fun generateLocalSummary(recentExpenses: List<Expense>, totalIncome: Double): String {
        val totalSpent = recentExpenses.sumOf { it.amount }
        return "Total Spent: $${String.format("%.2f", totalSpent)} against Income of $${String.format("%.2f", totalIncome)}."
    }
}
