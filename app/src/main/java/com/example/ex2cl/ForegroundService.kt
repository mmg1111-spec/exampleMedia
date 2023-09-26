package com.example.ex2cl

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat.startActivityForResult

class ForegroundService : Service() {
    private lateinit var mediaProjection: MediaProjection

    override fun onCreate() {
        super.onCreate()
        // ForegroundService 클래스의 onCreate 메서드 내에서
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        val REQUEST_MEDIA_PROJECTION = null
        REQUEST_MEDIA_PROJECTION?.let { startActivityForResult(permissionIntent, it) }

        // MediaProjection을 초기화하고 시작하는 로직 추가
    }

    private fun startActivityForResult(permissionIntent: Intent?, requestMediaProjection: Intent) {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 포그라운드 서비스로 설정
        startForeground(1, createNotification())

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // MediaProjection 및 미디어 레코딩 리소스 해제
    }

    private fun createNotification(): Notification {
        // 포그라운드 서비스를 나타내는 알림 생성
        val channelId = "ForegroundServiceChannel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        return notification
    }
}
