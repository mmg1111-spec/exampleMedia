package com.example.ex2cl

import android.app.Presentation
import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Display
import android.view.Surface
import android.widget.VideoView

class CustomPresentation(context: Context, display: Display, private val surface: Surface) :
    Presentation(context, display) {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_presentation_layout)

        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.holder.setFormat(PixelFormat.RGBA_8888)

        mediaPlayer = MediaPlayer().apply {
            setSurface(surface)
            prepareAsync()
            setOnPreparedListener {
                start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
    }
}
