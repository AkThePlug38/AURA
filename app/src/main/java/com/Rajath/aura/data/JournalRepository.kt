package com.Rajath.aura.data

import android.util.Log
import com.Rajath.aura.network.HFRequest
import com.Rajath.aura.network.HuggingFaceService
import com.Rajath.aura.network.RetrofitClient
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import java.util.UUID
import java.util.Locale
import kotlin.collections.set

class JournalRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val hf: HuggingFaceService = RetrofitClient.hfService

    // Router-friendly model — confirmed working
    private val modelId = "cardiffnlp/twitter-roberta-base-sentiment"

    suspend fun analyzeText(text: String): Pair<String, Float> = withContext(Dispatchers.IO) {
        try {
            val resp = hf.analyze(modelId, HFRequest(text))

            // Debug lines — copy these from Logcat if something fails
            Log.d("AURA-HF", "HF CODE = ${resp.code()}")
            val err = resp.errorBody()?.string()
            if (!err.isNullOrEmpty()) Log.d("AURA-HF", "HF ERROR BODY = $err")
            Log.d("AURA-HF", "HF BODY = ${resp.body()}")

            if (!resp.isSuccessful) {
                val message = err ?: "HTTP ${resp.code()}"
                return@withContext Pair("Error: $message", 0f)
            }

            val body = resp.body() ?: return@withContext Pair("Error: empty body", 0f)

            // CASE: nested list -> [[ {label,score},... ]]
            if (body.isNotEmpty() && body[0] is List<*>) {
                val inner = body[0] as List<*>
                val candidates = inner.filterIsInstance<Map<*, *>>()
                val best = candidates.maxByOrNull { (it["score"] as? Number)?.toFloat() ?: 0f }
                if (best != null) {
                    val rawLabel = best["label"]?.toString() ?: "Unknown"
                    val score = (best["score"] as? Number)?.toFloat() ?: 0f
                    val sentiment = when (rawLabel) {
                        "LABEL_2" -> "Positive"
                        "LABEL_1" -> "Neutral"
                        "LABEL_0" -> "Negative"
                        else -> rawLabel
                    }
                    return@withContext Pair(sentiment, score)
                }
            }

            // CASE: flat list -> [ {label,score}, ... ]
            if (body.isNotEmpty() && body[0] is Map<*, *>) {
                val first = body[0] as Map<*, *>
                val label = first["label"]?.toString() ?: "Unknown"
                val score = (first["score"] as? Number)?.toFloat() ?: 0f
                val sentiment = when (label) {
                    "LABEL_2" -> "Positive"
                    "LABEL_1" -> "Neutral"
                    "LABEL_0" -> "Negative"
                    else -> label
                }
                return@withContext Pair(sentiment, score)
            }

            // fallback: string-scan for common words (keeps app usable)
            val raw = body.toString().lowercase(Locale.getDefault())
            return@withContext when {
                listOf("positive","pos","label_2").any { it in raw } -> Pair("Positive", 0f)
                listOf("negative","neg","label_0").any { it in raw } -> Pair("Negative", 0f)
                listOf("neutral","neu","label_1").any { it in raw } -> Pair("Neutral", 0f)
                else -> quickLexiconSentiment(text)
            }
        } catch (e: Exception) {
            Log.e("AURA-HF", "Exception in analyzeText: ${e.localizedMessage}", e)
            // fallback to local lexicon so UI still gets a result
            return@withContext quickLexiconSentiment(text)
        }
    }

    private fun quickLexiconSentiment(text: String): Pair<String, Float> {
        val positiveWords = listOf("happy","joy","good","great","love","relaxed","calm","excited","motivated","peace")
        val negativeWords = listOf("sad","angry","depressed","anxious","stressed","tired","hate","upset","lonely")
        val t = text.lowercase(Locale.getDefault())
        val pos = positiveWords.count { it in t }
        val neg = negativeWords.count { it in t }
        return when {
            pos > neg -> Pair("Positive", (pos - neg).toFloat())
            neg > pos -> Pair("Negative", (neg - pos).toFloat())
            else -> Pair("Neutral", 0f)
        }
    }

    // ---------- GET RECENT JOURNALS (REAL-TIME) ----------
    fun getRecentJournals(uid: String): Flow<List<JournalEntry>> = callbackFlow {
        val ref = firestore
            .collection("users")
            .document(uid)
            .collection("journals")
            .orderBy("timestamp", Query.Direction.DESCENDING) // newest first

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.map { doc ->
                // prefer numeric 'timestamp' field, fallback to server 'createdAt' timestamp, then fallback to 0
                val tsFromLong = doc.getLong("timestamp")
                val tsFromCreatedAt = doc.getTimestamp("createdAt")?.toDate()?.time
                val ts = tsFromLong ?: tsFromCreatedAt ?: 0L

                JournalEntry(
                    id = doc.id,
                    text = doc.getString("text") ?: "",
                    sentiment = doc.getString("sentiment") ?: "",
                    score = doc.getDouble("score") ?: 0.0,
                    timestamp = ts
                )
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    // ---------- DELETE JOURNAL ----------
    suspend fun deleteJournal(uid: String, id: String) {
        firestore.collection("users")
            .document(uid)
            .collection("journals")
            .document(id)
            .delete()
            .await()
    }

    /**
     * Save a journal entry.
     * We write both:
     *  - "timestamp" (numeric client millis) for immediate client use (charts, sorting)
     *  - "createdAt" (FieldValue.serverTimestamp()) so server authoritative time is available later
     */
    suspend fun saveJournal(userId: String, text: String, sentiment: String, score: Float): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val clientTs = System.currentTimeMillis()

            // Compose data map explicitly so we can include serverTimestamp
            val data = mutableMapOf<String, Any?>()
            data["id"] = id
            data["text"] = text
            data["sentiment"] = sentiment
            data["score"] = score.toDouble()
            data["timestamp"] = clientTs
            data["createdAt"] = FieldValue.serverTimestamp()

            firestore.collection("users")
                .document(userId)
                .collection("journals")
                .document(id)
                .set(data)
                .await()

            Log.d("AURA-REPO", "Saved journal id=$id ts=$clientTs")
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



