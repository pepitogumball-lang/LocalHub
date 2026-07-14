package com.example.localhub

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.localhub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: AppSettings
    private lateinit var folderPicker: FolderPickerManager
    private lateinit var internetMonitor: InternetMonitor
    private lateinit var webViewController: WebViewController

    private var isServerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        settings = AppSettings(this)
        folderPicker = FolderPickerManager(this)
        webViewController = WebViewController(binding.webView)

        setupUI()
        setupPermissions()
        webViewController.init()

        internetMonitor = InternetMonitor(this) { isOnline ->
            runOnUiThread {
                binding.chipInternet.text = if (isOnline) "Connected" else "Offline"
                binding.chipInternet.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    resources.getColor(
                        if (isOnline) R.color.status_running else R.color.status_stopped,
                        theme
                    )
                )
                binding.chipInternet.setTextColor(
                    resources.getColor(
                        if (isOnline) R.color.status_running else R.color.status_stopped,
                        theme
                    )
                )
            }
        }
        internetMonitor.start()

        binding.btnSelectFolder.setOnClickListener {
            folderPicker.launch()
        }

        binding.btnStartStop.setOnClickListener {
            if (isServerRunning) stopServer() else startServer()
        }

        binding.btnOpenBrowser.setOnClickListener {
            if (isServerRunning) {
                binding.webView.visibility = View.VISIBLE
                binding.cardLogs.visibility = View.GONE
                webViewController.loadUrl("http://localhost:${settings.serverPort}")
            } else {
                Toast.makeText(this, "Start the server first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        updateFolderUI()
        updateServerStatus()

        folderPicker.setupPicker { uri ->
            settings.rootFolderUri = uri.toString()
            updateFolderUI()
            log("Folder selected")
        }

        // Restore server state if folder was previously selected
        if (settings.rootFolderUri != null) {
            binding.tvStatusDetail.text = "Ready to start"
        }
    }

    private fun startServer() {
        val uriString = settings.rootFolderUri
        if (uriString == null) {
            Toast.makeText(this, "Select a folder first", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, LocalWebServerService::class.java).apply {
            putExtra("root_uri", uriString)
            putExtra("port", settings.serverPort)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        isServerRunning = true
        updateServerStatus()
        log("Server started on port ${settings.serverPort}")
    }

    private fun stopServer() {
        val intent = Intent(this, LocalWebServerService::class.java)
        stopService(intent)
        isServerRunning = false
        updateServerStatus()

        // Hide WebView, show logs
        binding.webView.visibility = View.GONE
        binding.cardLogs.visibility = View.VISIBLE
        log("Server stopped")
    }

    private fun updateServerStatus() {
        if (isServerRunning) {
            binding.tvStatus.text = "Server running"
            binding.tvStatusDetail.text = "http://localhost:${settings.serverPort}"
            binding.tvStatusDetail.setTextColor(resources.getColor(R.color.accent, theme))
            binding.statusDot.setBackgroundResource(R.drawable.status_dot_running)
            binding.btnStartStop.text = "Stop"
        } else {
            binding.tvStatus.text = "Server stopped"
            binding.tvStatusDetail.text = "Select a folder and start the server"
            binding.tvStatusDetail.setTextColor(resources.getColor(R.color.text_secondary, theme))
            binding.statusDot.setBackgroundResource(R.drawable.status_dot_stopped)
            binding.btnStartStop.text = "Start"
        }
    }

    private fun updateFolderUI() {
        val uri = settings.rootFolderUri
        binding.tvFolder.text = if (uri != null) {
            // Try to show a readable folder name
            try {
                val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(this, Uri.parse(uri))
                docFile?.name ?: uri
            } catch (e: Exception) {
                uri
            }
        } else {
            "No folder selected"
        }
    }

    private fun setupPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ -> }

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        binding.tvLogs.append("[$timestamp] $message\n")

        // Auto-scroll to bottom
        binding.scrollLogs.post {
            binding.scrollLogs.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        internetMonitor.stop()
        super.onDestroy()
    }
}
