package com.example.ex2cl

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.media.MediaRouter
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.Display
import android.view.Surface
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var startProjectionButton: Button
    private lateinit var stopProjectionButton: Button
    private lateinit var startServerButton: Button
    private lateinit var startClientButton: Button
    private lateinit var stopServerButton: Button
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaRouter: MediaRouter? = null
    private var presentation: CustomPresentation? = null
    private lateinit var requestMediaProjectionLauncher: ActivityResultLauncher<Intent>
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var serverRunning = false
    private var clientRunning = false
    private lateinit var presentationSurface: Surface
    private var isPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startProjectionButton = findViewById(R.id.startProjectionButton)
        stopProjectionButton = findViewById(R.id.stopProjectionButton)
        startServerButton = findViewById(R.id.startServerButton)
        startClientButton = findViewById(R.id.startClientButton)
        stopServerButton = findViewById(R.id.stopServerButton)

        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)

        requestMediaProjectionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {

                        handleMediaProjectionResult(data)
                    }
                } else {

                    Toast.makeText(this, "미디어 프로젝션 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }


        startProjectionButton.setOnClickListener {
            if (!isPermissionRequested) {
                requestMediaProjection()
            }
        }

        stopProjectionButton.setOnClickListener {
            stopMediaProjection()
        }

        startServerButton.setOnClickListener {
            startServer()
        }

        startClientButton.setOnClickListener {

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Enter Server IP Address")
            val ipAddressEditText = EditText(this)
            ipAddressEditText.hint = "Server IP Address"
            alertDialogBuilder.setView(ipAddressEditText)
            alertDialogBuilder.setPositiveButton("확인") { _, _ ->
                val serverIpAddress = ipAddressEditText.text.toString()
                if (serverIpAddress.isNotEmpty()) {

                    startClient(serverIpAddress)
                } else {
                    Toast.makeText(this, "서버 IP 주소를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            alertDialogBuilder.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }

        stopServerButton.setOnClickListener {
            stopServer()
        }

        mediaRouter = getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
    }

    private fun requestMediaProjection() {
        if (mediaProjection == null) {
            val mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
            requestMediaProjectionLauncher.launch(permissionIntent)
        }
    }
    private fun startServer() {
        if (!serverRunning) {
            thread {
                try {
                    serverSocket = ServerSocket(8080)
                    serverRunning = true
                    while (serverRunning) {
                        val client = serverSocket?.accept()
                        clientSocket = client
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        serverSocket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    private fun startClient(serverIpAddress: String) {
        if (!clientRunning) {
            thread {
                var clientSocket: Socket? = null
                try {
                    val serverPort = 8080 // 서버 포트를 지정

                    clientSocket = Socket(serverIpAddress, serverPort)

                    val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    while (clientRunning) {
                        input.readLine()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        clientSocket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun stopServer() {
        serverRunning = false
        serverSocket?.close()
        clientSocket?.close()
    }

    private fun handleMediaProjectionResult(data: Intent) {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data)
        startMediaRecording()
        
    }

    private fun startMediaRecording() {
        if (mediaProjection != null) {
            mediaRecorder = MediaRecorder()
            try {
                mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Toast.makeText(this, "비디오 소스 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Toast.makeText(this, "비디오 인코더 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                return
            }

        }
    }


    private fun stopMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection?.stop()
            mediaProjection = null
        }

        if (mediaRecorder != null) {
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun showMediaOnPresentation(display: Display) {
        if (presentation == null) {
            presentation = CustomPresentation(this, display, presentationSurface)
        }
        presentation?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMediaProjection()
    }
}

