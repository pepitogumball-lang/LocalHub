package com.example.localhub

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("localhub_prefs", Context.MODE_PRIVATE)

    var rootFolderUri: String?
        get() = prefs.getString("root_folder_uri", null)
        set(value) = prefs.edit().putString("root_folder_uri", value).apply()

    var serverPort: Int
        get() = prefs.getInt("server_port", 8080)
        set(value) = prefs.edit().putInt("server_port", value).apply()
}
