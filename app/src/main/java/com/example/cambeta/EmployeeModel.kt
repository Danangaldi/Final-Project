package com.example.cambeta

import com.google.firebase.Timestamp


data class EmployeeModel(
    val kategori: String,
    val BB_Lambourne: String,
    val BB_Winter: String,
    val BB_Schoorl: String,
    val Jenis_Sapi: String,
    val kalender: Timestamp,
    val imageUrl: String
)

