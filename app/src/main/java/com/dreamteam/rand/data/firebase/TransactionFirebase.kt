package com.dreamteam.rand.data.firebase

import android.util.Log
import com.dreamteam.rand.data.entity.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TransactionFirebase {
    private val TAG = "TransactionFirebase"
    private val db = FirebaseFirestore.getInstance()
    // get firestore transactions collection
    private val transactionsCollection = db.collection("transactions")

    // AI DECLARATION:
    // used Claude to provide some assistance in constructing the methods
    // and how to interact wih firebase

    // get all transactions for a user to use for syncing
    fun getAllTransactions(userId: String): Flow<List<Transaction>> = flow {
        Log.d(TAG, "Getting transactions for userId=$userId from Firestore")
        val snapshot = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("id", Query.Direction.ASCENDING)
            .get(Source.SERVER)
            .await()

        val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
        Log.d(TAG, "Retrieved ${transactions.size} transactions from Firestore")
        emit(transactions)
        }.catch { e ->
        Log.e(TAG, "Error getting transactions: ${e.message}", e)
        // try the local cache if firestore can't be reached
        try {
            val cacheSnapshot = transactionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("id", Query.Direction.ASCENDING)
                .get(Source.CACHE)
                .await()

            val transactions = cacheSnapshot.documents.mapNotNull {
                it.toObject(Transaction::class.java)
            }

            Log.d(TAG, "Retrieved ${transactions.size} transactions from cache")
            emit(transactions)
        } catch (e2: Exception) {
            Log.e(TAG, "Cache retrieval also failed: ${e2.message}", e2)
            emit(emptyList())
        }
    }

    // Insert new transaction with auto-incrementing ID
    suspend fun insertTransaction(transaction: Transaction): Long {
        return try {
            Log.d(TAG, "Inserting transaction in Firebase: $transaction")
            
            // Get current max ID
            val maxIdDoc = transactionsCollection
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            // use this ID to determine the next ID to use
            val nextId = if (!maxIdDoc.isEmpty) {
                val maxTransaction = maxIdDoc.documents[0].toObject(Transaction::class.java)
                (maxTransaction?.id ?: 0) + 1
            } else {
                1L // start with and ID of 1 if no categories exist
            }

            Log.d(TAG, "Generated next ID: $nextId")
            
            // create expense with the new ID
            val transactionWithId = transaction.copy(id = nextId)

            // save the expense to Firebase
            Log.d(TAG, "Saving transaction to Firebase: $transactionWithId")
            val docRef = transactionsCollection.document()
            docRef.set(transactionWithId).await()
            
            Log.d(TAG, "Successfully inserted transaction with ID: $nextId")
            nextId
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting transaction: ${e.message}", e)
            -1L
        }
    }
}