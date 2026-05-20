package com.example.localhub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class LocalWebServerService : Service() {

    private var server: LocalWebServer? = null
    private val CHANNEL_ID = "LocalHubServerChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rootUriString = intent?.getStringExtra("root_uri")
        val port = intent?.getIntExtra("port", 8080) ?: 8080

        if (rootUriString != null) {
            val rootUri = Uri.parse(rootUriString)
            startServer(rootUri, port)
        }

        val notification = createNotification()
        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun startServer(rootUri: Uri, port: Int) {
        try {
            server?.stop()
            server = LocalWebServer(this, rootUri, port)
            server?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "LocalHub Server Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LocalHub Server is Running")
            .setContentText("Serving local files on localhost")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
