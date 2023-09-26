package com.example.ex2cl

import android.app.Presentation
import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Display
import android.view.Surface
import android.view.SurfaceView
import android.widget.VideoView
import androidx.core.net.toUri
import com.example.ex2cl.R // 이 임포트 문을 코드 파일의 맨 위에 추가합니다.


class CustomPresentation(context: Context, display: Display, private val surface: Surface) :
    Presentation(context, display) {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_presentation_layout)

        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.holder.setFormat(PixelFormat.RGBA_8888) // Surface의 포맷 설정

        // 예제 동영상 파일의 경로
        val videoUri: Uri = "android.resource://${context.packageName}/${R.raw.sample_video}".toUri()

        // MediaPlayer를 사용하여 동영상 플레이
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, videoUri)
            setSurface(surface)
            prepareAsync()
            setOnPreparedListener { start() }
        }
    }
     private fun displayMediaData(mediaData: String) {
        val videoUri: Uri = Uri.parse(mediaData)
        val videoView = findViewById<VideoView>(R.id.videoView)
        val mediaDataUri = "android.resource://${context.packageName}/${R.raw.sample_video}".toUri()
        displayMediaData(mediaDataUri.toString())
        videoView.setVideoURI(videoUri)
        videoView.start()
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(context, videoUri)
        mediaPlayer?.prepareAsync()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
    }
}
