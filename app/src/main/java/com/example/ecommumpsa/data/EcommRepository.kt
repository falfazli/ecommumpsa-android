package com.example.ecommumpsa.data

import android.content.Context
import com.example.ecommumpsa.data.model.Attendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class EcommRepository(context: Context) {
    private val client = OkHttpClient.Builder()
        .cookieJar(PersistentCookieJar(context))
        .build()
    private val sessionManager = SessionManager(context)

    suspend fun login(username: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get initial cookies
            client.newCall(Request.Builder().url("https://ecomm.ump.edu.my").get().build()).execute().close()

            val url = "https://ecomm.ump.edu.my/Login"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000L))
            val formBody = FormBody.Builder()
                .add("userName", username)
                .add("password", password)
                .add("level", "Staff")
                .add("datebefore", yesterday)
                .add("lat", "3.5467049889754816")
                .add("lon", "103.4277851570105")
                .build()
            val req = Request.Builder().url(url).post(formBody).build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    sessionManager.saveCredentials(username, password)
                    Result.success(html)
                } else {
                    Result.failure(Exception("Login failed: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(): Result<String> = attendanceAction("checkin")
    suspend fun checkOut(): Result<String> = attendanceAction("checkout")

    private suspend fun attendanceAction(action: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val username = sessionManager.getUsername() ?: return@withContext Result.failure(Exception("No user"))
            val password = sessionManager.getPassword() ?: return@withContext Result.failure(Exception("No password"))
            val url = "https://ecomm.ump.edu.my/cms/StaffAttendance/checkatt2.jsp"
            val formBody = FormBody.Builder()
                .add("action", action)
                .add("username", username)
                .add("password", password)
                .add("lat", "3.5467049889754816")
                .add("lon", "103.4277851570105")
                .build()
            val req = Request.Builder().url(url).post(formBody).build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: ""
                if (resp.isSuccessful) Result.success(html)
                else Result.failure(Exception("Failed: HTTP ${resp.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendance(): Result<List<Attendance>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://ecomm.ump.edu.my/staffAttendance.jsp"
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    Result.success(parseAttendance(html))
                } else {
                    Result.failure(Exception("Failed to get attendance: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseAttendance(html: String): List<Attendance> {
        val result = mutableListOf<Attendance>()
        val re = Regex("""<td[^>]*>\s*<span[^>]*>(\d{2}:\d{2})</span>\s*</td>\s*<td[^>]*>\s*<span[^>]*>([\d\.]+)<br\s*/?>\(([^<]+)\)</span>""", RegexOption.IGNORE_CASE)
        re.findAll(html).forEach { m ->
            result.add(Attendance(
                m.groupValues[1],
                m.groupValues[2],
                m.groupValues[3]
            ))
        }
        return result
    }

    fun extractUsername(html: String): String {
        val re = Regex("""<h4 class="mb-0 text-white">([^<]+)</h4>""")
        return re.find(html)?.groupValues?.get(1)?.trim() ?: ""
    }

    fun logout() {
        sessionManager.clear()
    }

    fun getSavedUsername() = sessionManager.getUsername()
    fun getSavedPassword() = sessionManager.getPassword()
}