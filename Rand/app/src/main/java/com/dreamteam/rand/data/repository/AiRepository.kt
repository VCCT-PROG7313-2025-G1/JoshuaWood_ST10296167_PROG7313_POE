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
        // Using Cloudflare Worker URL instead of localhost
        private const val BASE_URL = "https://rand-api.abdulbaaridavids04.workers.dev"
    }

    /**
     * Get AI insights based on the most recent expenses
     */
    suspend fun getAiInsights(expenses: List<Transaction>): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AiRepository", "Making request to $BASE_URL with ${expenses.size} expenses")
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Origin", "https://dreamteam.rand.app") // Add origin header
            connection.doOutput = true
            connection.connectTimeout = 15000 // 15 seconds
            connection.readTimeout = 15000 // 15 seconds

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
            
            val requestBodyString = requestBody.toString()
            android.util.Log.d("AiRepository", "Request body: $requestBodyString")

            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBodyString)
                writer.flush()
            }

            // Read response
            val responseCode = connection.responseCode
            android.util.Log.d("AiRepository", "Response code: $responseCode")
            
            // Read response headers for debugging
            connection.headerFields.forEach { (key, values) ->
                android.util.Log.d("AiRepository", "Header: $key = $values")
            }
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("AiRepository", "Response body: $response")
                
                if (response.isEmpty()) {
                    val emptyResponseMsg = "Received empty response from server"
                    android.util.Log.e("AiRepository", emptyResponseMsg)
                    return@withContext Result.failure(Exception(emptyResponseMsg))
                }
                
                try {
                    val jsonResponse = JSONObject(response)
                    
                    if (jsonResponse.getBoolean("success")) {
                        return@withContext Result.success(jsonResponse.getString("insight"))
                    } else {
                        val errorMsg = "API error: ${jsonResponse.optString("error", "Unknown error")}"
                        android.util.Log.e("AiRepository", errorMsg)
                        return@withContext Result.failure(Exception(errorMsg))
                    }
                } catch (e: Exception) {
                    val jsonParseMsg = "Failed to parse JSON response: ${e.message}"
                    android.util.Log.e("AiRepository", jsonParseMsg)
                    return@withContext Result.failure(Exception(jsonParseMsg))
                }
            } else {
                // Try to read error stream for more details
                val errorMessage = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
                } catch (e: Exception) {
                    "Could not read error details: ${e.message}"
                }
                
                val fullErrorMsg = "HTTP error: $responseCode - $errorMessage"
                android.util.Log.e("AiRepository", fullErrorMsg)
                return@withContext Result.failure(Exception(fullErrorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("AiRepository", "Exception in API call", e)
            return@withContext Result.failure(e)
        }
    }
} 