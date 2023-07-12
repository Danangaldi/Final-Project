package com.example.cambeta

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import java.util.Locale
import java.util.UUID
import kotlin.math.sqrt

class CustomImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private lateinit var dbRef: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    private var touchPath: Path = Path()
    private var touchPaint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 15f
        style = Paint.Style.STROKE
    }

    private var startPointX = 0f
    private var startPointY = 0f
    private var endPointX = 0f
    private var endPointY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPointX = x
                startPointY = y
                touchPath.reset()
                touchPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                endPointX = x
                endPointY = y
                touchPath.reset()
                touchPath.moveTo(startPointX, startPointY)
                touchPath.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                endPointX = x
                endPointY = y
                calculateAndSetLength()
            }
        }

        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(touchPath, touchPaint)
    }


    //proses citra
//    private fun calculateAndSetLength() {
    private fun calculateAndSetLength() {

        val length = calculateLength()
        val textView = rootView.findViewById<TextView>(R.id.textView)
        val uri = selectedImageUri

        //digunakan untuk memanggil textview keluar listener
        var sumResultLD: Double = 0.0
        var sumResultPB: Double = 0.0
        var HasilBB: Double = 0.0
        var HasilSchoorl: Double = 0.0
        var HasilWinter: Double = 0.0


        //memindahkan ke LD
        val LD = rootView.findViewById<TextView>(R.id.LD)
        val buttonLD = rootView.findViewById<ImageButton>(R.id.buttonLD)
        buttonLD.setOnClickListener {
            val nilaiPixel = length.toString().toDouble()
            val ubahCM = nilaiPixel * 0.026458 //cm
            val ubahM = ubahCM / 100 //m
//            val real = 165.12 / 2
            val skala = 5.7 //8.7 //7.7 //real/ubahCM
            val rumusLD = (ubahM * skala) * 3.14 //(ubahM * skala) * 2
            LD.text = String.format(Locale.getDefault(), "LD: %.2f m", rumusLD)
            sumResultLD = rumusLD
        }

        //memindahkan ke PB
        val PB = rootView.findViewById<TextView>(R.id.PB)
        val buttonPB = rootView.findViewById<ImageButton>(R.id.buttonPB)
        buttonPB.setOnClickListener {
            val nilaiPixel = length.toString().toDouble()
            val ubahCM = nilaiPixel * 0.026458 //cm
            val ubahM = ubahCM / 100 //m
//            val real = 148.8
            val skala = 5.7 //8.7 //7.7  // real/ ubahCM // jarak 2 meter
            val rumusPB = ubahM * skala
            PB.text = String.format(Locale.getDefault(), "PB: %.2f m", rumusPB)
            sumResultPB = rumusPB

        }

        //menghitung bobot sapi
        val hasil = rootView.findViewById<TextView>(R.id.hasil)
        val button = rootView.findViewById<ImageButton>(R.id.button)
        button.setOnClickListener {
            val nilaiPB = sumResultPB * 1000
            val nilaiLD = sumResultLD * 1000

            //rumus modifikasi (lambourne)
            val nilaiBB = ((nilaiPB + (nilaiLD * nilaiLD)) / 10840 )

            //rumus schoorl
            val schoorl = (sumResultLD * 100) + 22
            val BBschoorl = (schoorl * schoorl) * 0.25 // 25% dari hasil akhir
            val BBSchoorl = ((schoorl * schoorl) - BBschoorl)  / 100

            //rumus winter
            val BBwinter = (nilaiPB + (nilaiLD * nilaiLD)) / 10815.15

            hasil.text = String.format(Locale.getDefault(), "BB: %.2f kg", nilaiBB)
            HasilBB = nilaiBB
            HasilSchoorl = BBSchoorl
            HasilWinter = BBwinter

        }

        textView.text = String.format(Locale.getDefault(), "Piksel %.2f px", length)

        //menyimpan data sapi
        dbRef = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val etName = rootView.findViewById<EditText>(R.id.jenis)
        val save = rootView.findViewById<ImageButton>(R.id.simpan)
        val time = Timestamp.now()
        save.setOnClickListener {
            val BB_Lambourne = String.format("%.2f kg", HasilBB)//.toString()
            val BB_Winter = String.format("%.2f kg", HasilWinter)//.toString() // edit ke rumus yang sebenarnya
            val BB_Schoorl = String.format("%.2f kg", HasilSchoorl)//.toString() //edit ke rumus yang sebenarnya
            val Jenis_Sapi = etName.text.toString()
            val kategori = String.format("Manual")
            if (Jenis_Sapi.isEmpty()) {
                etName.error = "Jenis sapi"
                Toast.makeText(context, "Silahkan isi semua data", Toast.LENGTH_SHORT).show()

            } else {

                val imageFileName = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("Sapi/$imageFileName")
                imageRef.putFile(uri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val Employee = Model(BB_Lambourne, kategori, BB_Winter, BB_Schoorl, Jenis_Sapi, uri.toString(), time)
                            dbRef.collection("Data Sapi")
                                .add(Employee)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(
                                        context,
                                        "Data berhasil dikirim",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }.addOnFailureListener { error ->
                                    Toast.makeText(
                                        context,
                                        "Error ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
            }
        }

    }

    fun calculateAndSetLength1(data: Intent?) {
        selectedImageUri = data?.data
        calculateAndSetLength()
    }

    private fun calculateLength(): Float {
        val bitmap = drawable?.toBitmap()
        if (bitmap != null) {
            val startX = (startPointX * bitmap.width / width).toInt()
            val startY = (startPointY * bitmap.height / height).toInt()
            val endX = (endPointX * bitmap.width / width).toInt()
            val endY = (endPointY * bitmap.height / height).toInt()

            val dx = endX - startX
            val dy = endY - startY

            return sqrt((dx * dx + dy * dy).toFloat())
        }

        return 0f
    }


}



