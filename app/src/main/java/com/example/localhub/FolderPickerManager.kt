package com.example.localhub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class FolderPickerManager(private val activity: AppCompatActivity) {

    private lateinit var pickerLauncher: ActivityResultLauncher<Uri?>

    fun setupPicker(onFolderSelected: (Uri) -> Unit) {
        pickerLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                // Persist permissions
                val contentResolver = activity.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                onFolderSelected(uri)
            }
        }
    }

    fun launch() {
        pickerLauncher.launch(null)
    }

    fun isUriValid(uriString: String?): Boolean {
        if (uriString == null) return false
        return try {
            val uri = Uri.parse(uriString)
            val persistedPermissions = activity.contentResolver.persistedUriPermissions
            persistedPermissions.any { it.uri == uri && it.isReadPermission }
        } catch (e: Exception) {
            false
        }
    }
}
