package com.example.cambeta

import android.content.Context

object SharedPreferenceHelper {

    private const val PREF_NAME = "MyPrefs"
    private const val KEY_TEXT = "TextValue"

    fun saveText(context: Context, text: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_TEXT, text)
        editor.apply()
    }

    fun getText(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_TEXT, "") ?: ""
    }

}