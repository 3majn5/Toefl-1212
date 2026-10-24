package com.example.data.api

import com.example.data.model.DictionaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class DictionaryApiService {

    suspend fun fetchDefinition(word: String): Result<DictionaryResult> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanWord = word.trim().lowercase()
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            
            val definitions = mutableListOf<String>()
            val examples = mutableListOf<String>()
            val synonyms = mutableListOf<String>()
            var phonetic: String? = null

            // 1. Fetch Wiktionary definition & examples
            try {
                val url = URL("https://en.wiktionary.org/api/rest_v1/page/definition/$encoded")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("User-Agent", "TOEFL1212LearningApp/1.0")
                }

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(response)
                    val enArray = root.optJSONArray("en")
                    if (enArray != null) {
                        for (i in 0 until enArray.length()) {
                            val part = enArray.getJSONObject(i)
                            val defs = part.optJSONArray("definitions") ?: continue
                            for (j in 0 until defs.length()) {
                                val dObj = defs.getJSONObject(j)
                                val defText = stripHtml(dObj.optString("definition", ""))
                                if (defText.isNotBlank() && !definitions.contains(defText)) {
                                    definitions.add(defText)
                                }
                                val exArray = dObj.optJSONArray("examples")
                                if (exArray != null) {
                                    for (k in 0 until exArray.length()) {
                                        val exText = stripHtml(exArray.getString(k))
                                        if (exText.isNotBlank() && !examples.contains(exText)) {
                                            examples.add(exText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                // Ignore network error on secondary dictionary source
            }

            // 2. Fetch synonyms from Datamuse (free, keyless, fast)
            try {
                val synUrl = URL("https://api.datamuse.com/words?rel_syn=$encoded&max=10")
                val synConn = (synUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                if (synConn.responseCode == 200) {
                    val response = synConn.inputStream.bufferedReader().use { it.readText() }
                    val arr = JSONArray(response)
                    for (i in 0 until arr.length()) {
                        val w = arr.getJSONObject(i).getString("word")
                        if (!synonyms.contains(w) && w.lowercase() != cleanWord) {
                            synonyms.add(w)
                        }
                    }
                }
                synConn.disconnect()
            } catch (e: Exception) {
                // Ignore
            }

            DictionaryResult(
                headword = word,
                phonetic = phonetic,
                definitions = definitions.take(4),
                examples = examples.take(4),
                synonyms = synonyms.take(8)
            )
        }
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .trim()
    }

    companion object {
        fun getCambridgeUrl(word: String): String {
            val enc = URLEncoder.encode(word.trim().lowercase(), "UTF-8")
            return "https://dictionary.cambridge.org/dictionary/english/$enc"
        }

        fun getMerriamWebsterUrl(word: String): String {
            val enc = URLEncoder.encode(word.trim().lowercase(), "UTF-8")
            return "https://www.merriam-webster.com/dictionary/$enc"
        }

        fun getOxfordUrl(word: String): String {
            val enc = URLEncoder.encode(word.trim().lowercase(), "UTF-8")
            return "https://www.oxfordlearnersdictionaries.com/definition/english/$enc"
        }
    }
}
