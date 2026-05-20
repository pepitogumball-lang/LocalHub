package com.example.localhub

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
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

        settings = AppSettings(this)
        folderPicker = FolderPickerManager(this)
        webViewController = WebViewController(binding.webView)
        
        setupUI()
        setupPermissions()
        webViewController.init()

        internetMonitor = InternetMonitor(this) { isOnline ->
            runOnUiThread {
                binding.tvInternet.text = "Internet: ${if (isOnline) "Connected" else "Offline"}"
            }
        }
        internetMonitor.start()

        folderPicker.setupPicker { uri ->
            settings.rootFolderUri = uri.toString()
            updateFolderUI()
            log("Folder selected: $uri")
        }

        updateFolderUI()
    }

    private fun setupUI() {
        binding.btnSelectFolder.setOnClickListener {
            folderPicker.launch()
        }

        binding.btnStartStop.setOnClickListener {
            if (isServerRunning) {
                stopServer()
            } else {
                startServer()
            }
        }

        binding.btnOpenBrowser.setOnClickListener {
            if (!isServerRunning) {
                Toast.makeText(this, "Start server first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            toggleView()
        }
    }

    private fun toggleView() {
        if (binding.webView.visibility == View.VISIBLE) {
            binding.webView.visibility = View.GONE
            binding.scrollLogs.visibility = View.VISIBLE
            binding.btnOpenBrowser.text = "Browse"
        } else {
            binding.webView.visibility = View.VISIBLE
            binding.scrollLogs.visibility = View.GONE
            binding.btnOpenBrowser.text = "Logs"
            webViewController.loadUrl("http://localhost:${settings.serverPort}/")
        }
    }

    private fun startServer() {
        val uriStr = settings.rootFolderUri
        if (uriStr == null) {
            Toast.makeText(this, "Please select a folder first", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, LocalWebServerService::class.java).apply {
            putExtra("root_uri", uriStr)
            putExtra("port", settings.serverPort)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        isServerRunning = true
        binding.btnStartStop.text = "Stop"
        binding.tvStatus.text = "Status: Running on port ${settings.serverPort}"
        log("Server started on http://localhost:${settings.serverPort}")
    }

    private fun stopServer() {
        val intent = Intent(this, LocalWebServerService::class.java)
        stopService(intent)
        isServerRunning = false
        binding.btnStartStop.text = "Start"
        binding.tvStatus.text = "Status: Stopped"
        log("Server stopped")
    }

    private fun updateFolderUI() {
        val uri = settings.rootFolderUri
        binding.tvFolder.text = "Folder: ${uri ?: "None"}"
    }

    private fun setupPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle permissions results
        }

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun log(message: String) {
        binding.tvLogs.append("$message\n")
    }

    override fun onDestroy() {
        internetMonitor.stop()
        super.onDestroy()
    }
}

