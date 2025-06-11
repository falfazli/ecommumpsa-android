package com.example.ecommumpsa.data

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecomm_cookies", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookieString = cookies.joinToString(";") { it.toString() }
        prefs.edit().putString(url.host, cookieString).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieString = prefs.getString(url.host, null) ?: return ArrayList()
        return cookieString.split(";").mapNotNull {
            Cookie.parse(url, it)
        }
    }
}