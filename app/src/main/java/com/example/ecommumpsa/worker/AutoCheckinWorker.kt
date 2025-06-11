package com.example.ecommumpsa.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ecommumpsa.R
import com.example.ecommumpsa.data.EcommRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine

class AutoCheckInWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val CHANNEL_ID = "AutoCheckInChannel"
        const val RADIUS_METERS = 300.0
        const val TARGET_LAT = 3.5467049889754816
        const val TARGET_LON = 103.4277851570105
    }

    override suspend fun doWork(): Result {
        // 1. Check WiFi SSID
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid?.replace("\"", "") ?: ""

        if (ssid != "UMPSA-iD" && ssid != "eduroam") {
            return Result.success()
        }

        // 2. Check location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = getCurrentLocation(fusedLocationClient) ?: return Result.success()
        val distance = floatArrayOf(0f)
        Location.distanceBetween(location.latitude, location.longitude, TARGET_LAT, TARGET_LON, distance)
        if (distance[0] > RADIUS_METERS) {
            return Result.success()
        }

        // 3. Perform check-in
        val repo = EcommRepository(context)
        val result = repo.checkIn()
        if (result.isSuccess) {
            sendNotification("Auto Check-In Success", "Checked in at ${location.latitude}, ${location.longitude}")
        } else {
            sendNotification("Auto Check-In Failed", result.exceptionOrNull()?.message ?: "Unknown error")
        }
        return Result.success()
    }

    private suspend fun getCurrentLocation(fusedLocationClient: FusedLocationProviderClient): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                val request = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setNumUpdates(1)
                fusedLocationClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedLocationClient.removeLocationUpdates(this)
                            cont.resume(result.lastLocation, null)
                        }
                    },
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                cont.resume(null, null)
            }
        }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.auto_checkin_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(101, notification)
    }
}