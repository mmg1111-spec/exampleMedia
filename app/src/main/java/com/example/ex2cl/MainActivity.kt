package com.example.ex2cl
import android.app.Activity
import android.app.Presentation
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.media.MediaRouter
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Display
import android.view.Surface
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var startProjectionButton: Button
    private lateinit var stopProjectionButton: Button
    private lateinit var foregroundButton: Button
    private lateinit var startServerButton : Button
    private lateinit var startClientButton : Button
    private lateinit var stopServerButton : Button
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaRouter: MediaRouter? = null
    private var presentation: Presentation? = null
    private var isForegroundMode = false
    private lateinit var requestMediaProjectionLauncher: ActivityResultLauncher<Intent>
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var serverRunning = false
    private var clientRunning = false
    private lateinit var presentationView: SurfaceView
    private lateinit var presentationSurface: Surface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startProjectionButton = findViewById(R.id.startProjectionButton)
        stopProjectionButton = findViewById(R.id.stopProjectionButton)
        foregroundButton = findViewById(R.id.foregroundButton)
        startServerButton = findViewById(R.id.startServerButton)
        startClientButton = findViewById(R.id.startClientButton)
        stopServerButton = findViewById(R.id.stopServerButton)
       // presentationView = findViewById(R.id.presentationView)
      //  presentationSurface = presentationView.holder.surface
       // presentation = display?.let { CustomPresentation(this, it, presentationSurface) }

        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        requestMediaProjectionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        // Permission granted, handle media projection
                        handleMediaProjectionResult(data)
                    }
                } else {
                    // Permission denied, handle accordingly
                    Toast.makeText(this, "Media projection permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        startProjectionButton.setOnClickListener {
            startMediaProjection()
        }

        stopProjectionButton.setOnClickListener {
            stopMediaProjection()
        }

        foregroundButton.setOnClickListener {
            toggleForegroundMode()
        }
        startServerButton.setOnClickListener {
            startServer()
        }

        startClientButton.setOnClickListener {
            startClient()
        }

        stopServerButton.setOnClickListener {
            stopServer()
        }

        mediaRouter = getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
    }

    private fun stopServer() {
        serverRunning = false
        serverSocket?.close()
        clientSocket?.close()
    }

    private fun startClient() {
        if (!clientRunning) {
            thread {
                try {
                    val serverAddress = "192.168.0.6" // 서버의 IP 주소를 지정
                    val serverPort = 8080 // 서버 포트를 지정

                    clientSocket = Socket(serverAddress, serverPort)

                    val input = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                    while (clientRunning) {
                        val receivedData = input.readLine() // 서버로부터 데이터 수신

                        // receivedData를 Presentation에 표시하는 로직 추가
                        // Presentation에 미디어 데이터를 표시하는 방법은 CustomPresentation 클래스에 따라 달라질 수 있음
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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
                        if (client != null) {
                            // 클라이언트와 통신하는 코드 작성
                            // 예: 클라이언트로부터 미디어 데이터를 전송받고 Presentation에 표시
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleMediaProjectionResult(data: Intent) {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data)

        // Now that you have the media projection, you can start using it for screen capturing or other media tasks
        startMediaRecording()
    }

    private fun startMediaRecording() {
        if (mediaProjection != null) {
            mediaRecorder = MediaRecorder()

            // Configure MediaRecorder settings
            mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            // Set the output file path
            //   val outputFilePath = "/path/to/your/output/file.mp4"
            val outputFileName = "sample_video.mp4"
            val externalDir = Environment.getExternalStorageDirectory() // 외부 저장소의 최상위 디렉토리를 가져옵니다.
            val outputFilePath = File(externalDir, outputFileName).absolutePath
            mediaRecorder?.setOutputFile(outputFilePath)

            // Set other necessary configurations (e.g., video size, bit rate, etc.)

            try {

                mediaRecorder?.prepare()
                mediaRecorder?.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun startMediaProjection() {
        if (mediaProjection == null) {
            // 미디어 프로젝션을 시작하기 전에 미디어 프로젝션 권한을 얻는 Intent를 생성합니다.
            val mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()

            // 권한 요청을 실행하고 결과를 처리합니다.
            requestMediaProjectionLauncher.launch(permissionIntent)
            val REQUEST_MEDIA_PROJECTION = 1 // 이전에는 null로 초기화되었으므로 변경합니다.
            startActivityForResult(permissionIntent, REQUEST_MEDIA_PROJECTION)
        } else {
            // 이미 미디어 프로젝션을 실행 중인 경우 처리할 내용 추가

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

    private fun toggleForegroundMode() {
        if (!isForegroundMode) {
            // 포그라운드 모드 활성화
            presentation?.show()
            isForegroundMode = true
        } else {
            // 포그라운드 모드 비활성화
            presentation?.dismiss()
            isForegroundMode = false
        }
    }

    // Presentation을 사용하여 미디어를 프로젝션하는 방법에 대한 예제 코드
    private fun showMediaOnPresentation(display: Display) {
        presentation = CustomPresentation(this, display, presentationSurface)
        presentation?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMediaProjection()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val REQUEST_MEDIA_PROJECTION = null
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 미디어 프로젝션 권한을 얻었으므로 ForegroundService에 전달
                val serviceIntent = Intent(this, ForegroundService::class.java)
                serviceIntent.putExtra("mediaProjectionData", data)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                // 권한을 얻지 못한 경우 처리
            }
        }
    }
}
