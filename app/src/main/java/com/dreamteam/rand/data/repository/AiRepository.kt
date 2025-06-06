package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.entity.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class AiRepository {

    companion object {
        // Using Cloudflare Worker URL
        private const val BASE_URL = "https://rand-api.abdulbaaridavids04.workers.dev"
    }

    /**
     * Get AI insights based on the most recent expenses
     */
    suspend fun getAiInsights(expenses: List<Transaction>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)  // Endpoint is the root for Cloudflare Worker
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Format expenses for the API
            val expensesArray = JSONArray()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            
            for (expense in expenses) {
                val expenseJson = JSONObject().apply {
                    put("category", expense.categoryId?.toString() ?: "Uncategorized")
                    put("amount", expense.amount)
                    put("date", dateFormat.format(java.util.Date(expense.date)))
                    put("description", expense.description)
                }
                expensesArray.put(expenseJson)
            }

            val requestBody = JSONObject().apply {
                put("expenses", expensesArray)
            }

            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    return@withContext Result.success(jsonResponse.getString("insight"))
                } else {
                    return@withContext Result.failure(Exception("API error: ${jsonResponse.optString("error", "Unknown error")}"))
                }
            } else {
                return@withContext Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
} 