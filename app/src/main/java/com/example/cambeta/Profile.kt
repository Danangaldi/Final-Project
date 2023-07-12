package com.example.cambeta

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import fragment.HomeFragment


class Profile : AppCompatActivity(){

    private lateinit var editTextUsername: EditText
    private lateinit var buttonLogin: ImageButton
    private lateinit var buttonReset: ImageButton
    private lateinit var textViewUsername: TextView
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREF_NAME = "MyPrefs"
        const val KEY_NAME = "name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        editTextUsername = findViewById(R.id.username)
        buttonLogin = findViewById(R.id.imageButton2)
        buttonReset = findViewById(R.id.reset)
        textViewUsername = findViewById(R.id.userview)
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Mengambil nilai dari SharedPreferences dan mengatur kembali ke EditText
        val savedName = sharedPreferences.getString(KEY_NAME, " ")
        editTextUsername.setText(savedName)

        val name = editTextUsername.text.toString()

        buttonLogin.setOnClickListener {
            //menyimpan text ke SharePreferences
            val name = editTextUsername.text.toString()
            editor.putString(KEY_NAME, name)
            editor.apply()
            textViewUsername.text = "Holla $name!"
            SharedPreferenceHelper.saveText(this, name)
        }

        buttonReset.setOnClickListener {
            editTextUsername.text = null
            editor.remove(KEY_NAME)
            editor.apply()
            textViewUsername.text = "Holla, User!"
            SharedPreferenceHelper.saveText(this, name)
        }
        textViewUsername.text = "Holla, $name!"
    }

    override fun onStop() {
        super.onStop()
        val name = editTextUsername.text.toString()
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, name)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        val name = SharedPreferenceHelper.getText(this)
        textViewUsername.text = "Holla, $name!"
    }
}