package com.example.data.api

import android.net.Uri
import com.example.data.model.DictionaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class DictionaryApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val htmlTagPattern = Pattern.compile("<[^>]*>")

    private fun stripHtml(html: String): String {
        return htmlTagPattern.matcher(html).replaceAll("").trim()
    }

    suspend fun fetchDefinition(rawHeadword: String): Result<DictionaryResult> = withContext(Dispatchers.IO) {
        try {
            // Clean headword (e.g., remove parentheses, lower case)
            val cleanWord = rawHeadword.replace(Regex("\\(.*?\\)"), "").trim()
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = "https://en.wiktionary.org/api/rest_v1/page/definition/$encoded"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TOEFL1212LearningApp/1.0 (academic-toefl@example.com)")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Dictionary server returned code ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty dictionary response"))
            val json = JSONObject(body)

            val enArray = json.optJSONArray("en")
            if (enArray == null || enArray.length() == 0) {
                return@withContext Result.failure(Exception("No English definitions found in dictionary"))
            }

            val definitions = mutableListOf<String>()
            val examples = mutableListOf<String>()
            val synonyms = mutableListOf<String>()

            // Also query Datamuse for rich academic synonyms
            try {
                val synUrl = "https://api.datamuse.com/words?rel_syn=$encoded"
                val synReq = Request.Builder()
                    .url(synUrl)
                    .header("User-Agent", "TOEFL1212LearningApp/1.0")
                    .build()
                val synResp = client.newCall(synReq).execute()
                if (synResp.isSuccessful) {
                    val synBody = synResp.body?.string()
                    if (!synBody.isNullOrBlank()) {
                        val synArray = org.json.JSONArray(synBody)
                        for (s in 0 until synArray.length()) {
                            val synObj = synArray.getJSONObject(s)
                            val wordStr = synObj.optString("word", "").trim()
                            if (wordStr.isNotBlank() && wordStr.lowercase() != cleanWord.lowercase()) {
                                synonyms.add(wordStr)
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {}

            for (i in 0 until enArray.length()) {
                val item = enArray.getJSONObject(i)
                val pos = item.optString("partOfSpeech", "")
                val defsArray = item.optJSONArray("definitions") ?: continue

                for (j in 0 until defsArray.length()) {
                    val defObj = defsArray.getJSONObject(j)
                    val rawDef = defObj.optString("definition", "")
                    val cleanDef = stripHtml(rawDef)
                    if (cleanDef.isNotBlank() && cleanDef.length > 3) {
                        val posPrefix = if (pos.isNotBlank()) "[$pos] " else ""
                        definitions.add("$posPrefix$cleanDef")
                    }

                    val exArray = defObj.optJSONArray("examples")
                    if (exArray != null) {
                        for (k in 0 until exArray.length()) {
                            val rawEx = exArray.getString(k)
                            val cleanEx = stripHtml(rawEx)
                            if (cleanEx.isNotBlank() && cleanEx.length > 5) {
                                examples.add(cleanEx)
                            }
                        }
                    }
                }
            }

            Result.success(
                DictionaryResult(
                    term = cleanWord,
                    definitions = definitions.take(5),
                    examples = examples.take(6),
                    synonyms = synonyms.distinct().take(8),
                    source = "Wiktionary & Datamuse Academic API"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun getCambridgeUrl(word: String): String {
            val clean = word.replace(Regex("\\(.*?\\)"), "").trim().lowercase()
            return "https://dictionary.cambridge.org/dictionary/english/${Uri.encode(clean)}"
        }

        fun getMerriamWebsterUrl(word: String): String {
            val clean = word.replace(Regex("\\(.*?\\)"), "").trim().lowercase()
            return "https://www.merriam-webster.com/dictionary/${Uri.encode(clean)}"
        }

        fun getOxfordLearnersUrl(word: String): String {
            val clean = word.replace(Regex("\\(.*?\\)"), "").trim().lowercase()
            return "https://www.oxfordlearnersdictionaries.com/definition/english/${Uri.encode(clean)}"
        }
    }
}
