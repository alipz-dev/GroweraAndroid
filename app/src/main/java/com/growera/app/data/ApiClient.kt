package com.growera.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient {
    companion object {
        // Must match the Vercel deployment that owns the /api/growera rewrite.
        const val API_BASE_URL = "https://growera.vercel.app/api/growera"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun login(username: String, password: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("username", username).put("password", password)
            .toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("$API_BASE_URL/login")
            .header("Accept", "application/json")
            .post(body)
            .build()
        val payload = execute(request)
        val token = payload.optString("token").ifBlank {
            payload.optString("accessToken").ifBlank {
                payload.optJSONObject("data")?.optString("token")?.ifBlank {
                    payload.optJSONObject("data")?.optString("accessToken").orEmpty()
                }.orEmpty()
            }
        }
        if (token.isBlank() || token == "null") {
            throw IOException(payload.optString("message").ifBlank { "Server tidak mengembalikan token sesi yang dikenali." })
        }
        token
    }

    suspend fun getPlayer(token: String): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$API_BASE_URL/me/full")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        execute(request)
    }

    suspend fun logout(token: String?) = withContext(Dispatchers.IO) {
        if (token.isNullOrBlank()) return@withContext
        val request = Request.Builder()
            .url("$API_BASE_URL/logout")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .post("{}".toRequestBody(JSON))
            .build()
        runCatching { execute(request) }
        Unit
    }

    private fun execute(request: Request): JSONObject {
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = if (raw.isBlank()) JSONObject() else runCatching { JSONObject(raw) }
                .getOrElse { throw IOException("Respons server bukan JSON (HTTP ${response.code}).") }
            if (!response.isSuccessful) {
                val message = json.optString("message").ifBlank {
                    json.optString("error").ifBlank { "Permintaan gagal (HTTP ${response.code})." }
                }
                throw ApiException(message, response.code)
            }
            return json
        }
    }
}

class ApiException(message: String, val statusCode: Int) : IOException(message)
