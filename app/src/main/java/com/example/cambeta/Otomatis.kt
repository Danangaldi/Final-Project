package com.example.cambeta

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

class Otomatis : AppCompatActivity() {

    private lateinit var dateTimeTextView: TextView
    private lateinit var jenis: EditText
    private lateinit var nama: TextView
    private lateinit var PB: TextView
    private lateinit var LD: TextView
    private lateinit var lambourne: TextView
    private lateinit var winter: TextView
    private lateinit var schoorl: TextView
    private lateinit var pixel: TextView
    private lateinit var pixelwarna: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageSave: ImageButton
    private lateinit var uploadImageBtn: Button
    private lateinit var selectImageBtn: ImageButton

    private val API_KEY = "Wp78uG29Cgk5HPtov7UeQHV2"
    private val API_URL = "https://api.remove.bg/v1.0/removebg"
    private val GALLERY_REQUEST_CODE = 123
    private var selectedImageUri: Uri? = null

    private lateinit var dbRef: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otomatis)
        supportActionBar?.hide()

        selectImageBtn = findViewById(R.id.imageButton)
        imageSave = findViewById(R.id.ImageSave)
        uploadImageBtn = findViewById(R.id.kalkulasi)
        imageView1 = findViewById(R.id.gambarsapi1)
        imageView2 = findViewById(R.id.gambarsapi2)
        imageView3 = findViewById(R.id.gambarsapi3)
        PB = findViewById(R.id.pbaja)
        LD = findViewById(R.id.bbaja)
        lambourne = findViewById(R.id.bb1)
        winter = findViewById(R.id.bb2)
        schoorl = findViewById(R.id.bb3)
        pixel = findViewById(R.id.pixel)
        pixelwarna = findViewById(R.id.pixelwarna)
        jenis = findViewById(R.id.jenissapi)

        dbRef = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        dateTimeTextView = findViewById(R.id.kalender)
        nama = findViewById(R.id.nama)

        val name = SharedPreferenceHelper.getText(this)
        nama.text = "Holla, $name!"

        //menampilkan kalender
        val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = Date()
        val dateString = dateFormat.format(currentDate)

        val dateTimeText = "$dateString"
        dateTimeTextView.text = dateTimeText


        selectImageBtn.setOnClickListener {
            openGallery()

        }

        OpenCVLoader.initDebug()
        uploadImageBtn.setOnClickListener {
            if (imageView2.drawable != null) {
                val drawable: Drawable? = imageView2.drawable
                val bitmap: Bitmap? = drawable?.toBitmap()

                if (bitmap != null) {
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(mutableBitmap)
                    val paint = Paint()

                    val imageMat = Mat()
                    Utils.bitmapToMat(bitmap, imageMat)
                    //proses graysclae
                    val gray = Mat()
                    Imgproc.cvtColor(imageMat, gray, Imgproc.COLOR_BGR2GRAY)
                    //proses deteksi tepi canny
                    val edges = Mat()
                    Imgproc.Canny(gray, edges, 50.0, 100.0)
                    //proses mencari kontur
                    val contours = mutableListOf<MatOfPoint>()
                    val hierarchy = Mat()
                    Imgproc.findContours(
                        edges,
                        contours,
                        hierarchy,
                        Imgproc.RETR_EXTERNAL,
                        Imgproc.CHAIN_APPROX_SIMPLE
                    )

                    var lowestPoint: Point? = null
                    var highestPoint: Point? = null
                    var leftmostPoint: Point? = null
                    var rightmostPoint: Point? = null

                    for (contour in contours) {
                        val extLeft = contour.toList().minByOrNull { it.x }
                        val extRight = contour.toList().maxByOrNull { it.x }
                        val extTop = contour.toList().minByOrNull { it.y }
                        val extBottom = contour.toList().maxByOrNull { it.y }

                        if (lowestPoint == null || extBottom?.y ?: 0.0 > lowestPoint.y) {
                            lowestPoint = extBottom
                        }
                        if (highestPoint == null || extTop?.y ?: 0.0 < highestPoint.y) {
                            highestPoint = extTop
                        }
                        if (leftmostPoint == null || extLeft?.x ?: 0.0 < leftmostPoint.x) {
                            leftmostPoint = extLeft
                        }
                        if (rightmostPoint == null || extRight?.x ?: 0.0 > rightmostPoint.x) {
                            rightmostPoint = extRight
                        }
                    }

                    Imgproc.circle(
                        imageMat,
                        lowestPoint,
                        5,
                        Scalar(255.0, 0.0, 0.0),
                        -5
                    ) // Titik terendah dengan lingkaran biru
                    Imgproc.circle(
                        imageMat,
                        highestPoint,
                        5,
                        Scalar(0.0, 255.0, 0.0),
                        -1
                    ) // Titik tertinggi dengan lingkaran hijau
                    Imgproc.circle(
                        imageMat,
                        leftmostPoint,
                        5,
                        Scalar(0.0, 0.0, 255.0),
                        -1
                    ) // Titik terkiri dengan lingkaran merah
                    Imgproc.circle(
                        imageMat,
                        rightmostPoint,
                        5,
                        Scalar(0.0, 0.0, 0.0),
                        -1
                    ) // Titik terkanan dengan lingkaran hit

                    // Gambar lingkaran berwarna merah pada titik terendah
                    paint.color = Color.RED
                    lowestPoint?.x?.let {
                        canvas.drawCircle(
                            it.toFloat(),
                            lowestPoint.y.toFloat(),
                            10f,
                            paint
                        )
                    }

                    // Gambar lingkaran berwarna hijau pada titik tertinggi
                    paint.color = Color.GREEN
                    highestPoint?.x?.let {
                        canvas.drawCircle(
                            it.toFloat(),
                            highestPoint.y.toFloat(),
                            10f,
                            paint
                        )
                    }

                    // Gambar lingkaran berwarna biru pada titik terkiri
                    paint.color = Color.BLUE
                    leftmostPoint?.x?.let {
                        canvas.drawCircle(
                            it.toFloat(),
                            leftmostPoint.y.toFloat(),
                            10f,
                            paint
                        )
                    }

                    // Gambar lingkaran berwarna hitam pada titik terkanan
                    paint.color = Color.BLACK
                    rightmostPoint?.x?.let {
                        canvas.drawCircle(
                            it.toFloat(),
                            rightmostPoint.y.toFloat(),
                            10f,
                            paint
                        )
                    }

                    // Setel gambar yang telah dimodifikasi ke ImageView
                    imageView2.setImageBitmap(mutableBitmap)

                    val greenPointText = "Titik Hijau: (${highestPoint?.x}, ${highestPoint?.y})\n" +
                            "Titik Merah: (${lowestPoint?.x}, ${lowestPoint?.y})\n" +
                            "Titik Biru: (${leftmostPoint?.x}, ${leftmostPoint?.y})\n" +
                            "Titik Hitam: (${rightmostPoint?.x}, ${rightmostPoint?.y})"

                    //rumus eucladian
                    val panjangPixel_atas_bawah = sqrt(
                        (lowestPoint!!.x - highestPoint!!.x).toDouble()
                            .pow(2) + (lowestPoint!!.y - highestPoint!!.y).toDouble().pow(2)
                    )

                    val panjangPixel_kiri_kanan = sqrt(
                        (leftmostPoint!!.x - rightmostPoint!!.x).toDouble()
                            .pow(2) + (leftmostPoint!!.y - rightmostPoint!!.y).toDouble().pow(2)
                    )
                    //LD
                    val ubahCMLD = panjangPixel_atas_bawah * 0.026458 //cm
                    val ubahMLD = ubahCMLD / 100 //m
                    val skalaLD = 6.7 //7.7 //real/ubahCM
                    val rumusLD = (ubahMLD * skalaLD) * 3.14
                    val rumusLDD = (rumusLD * 100) + 22// ubah lagi ke cm
                    val rumusLDDD = rumusLD * 1000 //khusus winter dan lambourne
                    LD.text = String.format(Locale.getDefault(), "LD: %.2f m", rumusLD)

                    //PB
                    val ubahCMPB = panjangPixel_kiri_kanan * 0.026458 //cm
                    val ubahMPB = ubahCMPB / 100 //m
                    val skalaPB = 6.7 //7.7 //real/ubahCM
                    val rumusPB = (ubahMPB * skalaPB)
                    val rumusPBB = rumusPB * 100 // ubah lagi ke cm
                    val rumusPBBB = rumusPB * 1000 //khusus winter dan lambourne
                    PB.text = String.format(Locale.getDefault(), "PB: %.2f m", rumusPB)

                    val Hasil_Lambourne = (rumusPBBB + (rumusLDDD * rumusLDDD)) /10840
                    val Hasil_Winter = (rumusPBBB + (rumusLDDD * rumusLDDD)) /10815.15
                    val Hasil_Schoorl = (rumusLDD * rumusLDD) / 100
                    lambourne.text = String.format(Locale.getDefault(), "Lambourne: %.2f kg", Hasil_Lambourne)
                    schoorl.text = String.format(Locale.getDefault(), "Schoorl: %.2f kg", Hasil_Schoorl)
                    winter.text = String.format(Locale.getDefault(), "Winter: %.2f kg", Hasil_Winter)

                    val time = Timestamp.now()

                    imageSave.setOnClickListener {
                        val BB_Lambourne = String.format("%.2f kg", Hasil_Lambourne)//.toString()
                        val BB_Winter = String.format("%.2f kg", Hasil_Winter)//.toString() // edit ke rumus yang sebenarnya
                        val BB_Schoorl = String.format("%.2f kg", Hasil_Schoorl)//.toString() //edit ke rumus yang sebenarnya
                        val Jenis_Sapi = jenis.text.toString()
                        val kategori = String.format("Otomatis")

                        if (Jenis_Sapi.isEmpty()) {
                            jenis.error = "Jenis sapi"
                            Toast.makeText(this, "Silahkan isi semua data", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            val imageFileName = UUID.randomUUID().toString()
                            val imageRef = storage.reference.child("Sapi/$imageFileName")
                            imageRef.putFile(selectedImageUri!!)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                                        val Employee = Model(BB_Lambourne, kategori, BB_Winter, BB_Schoorl, Jenis_Sapi, uri.toString(), time)
                                        dbRef.collection("Data Sapi")
                                            .add(Employee)
                                            .addOnSuccessListener { documentReference ->
                                                Toast.makeText(
                                                    this,
                                                    "Data berhasil dikirim",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }.addOnFailureListener { error ->
                                                Toast.makeText(
                                                    this,
                                                    "Error ${error.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                        }

                    }

                    pixel.text = greenPointText
                    pixelwarna.text = String.format("pixel hijau-merah: %.2f\npixel biru-hitam: %.2f", panjangPixel_atas_bawah, panjangPixel_kiri_kanan)

                }
            } else {
                Toast.makeText(this@Otomatis, "tidak ada gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        selectedImageUri = data!!.data
        imageView3.setImageURI(selectedImageUri)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val imagePath = cursor?.getString(columnIndex ?: 0)
                cursor?.close()

                if (!imagePath.isNullOrEmpty()) {
                    val inputImageBitmap = BitmapFactory.decodeFile(imagePath)
                    val resizedBitmap =
                        Bitmap.createScaledBitmap(inputImageBitmap, 1920, 1080, false)

                    // Lakukan pemrosesan gambar di sini menggunakan objek resizedBitmap
                    removeBackground(resizedBitmap) { bitmap ->
                        val result : ImageView = findViewById(R.id.gambarsapi1)
                        val resultImageView: ImageView = findViewById(R.id.gambarsapi2)
                        resultImageView.setImageBitmap(bitmap)
                        result.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun removeBackground(inputImageBitmap: Bitmap, callback: (Bitmap) -> Unit) {
        val client = OkHttpClient()

        val imageFile = File.createTempFile("input_image", ".png", cacheDir)
        val outputStream = FileOutputStream(imageFile)
        inputImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image_file",
                imageFile.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            )
            .addFormDataPart("size", "auto")
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("X-Api-Key", API_KEY)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    runOnUiThread {
                        callback(bitmap)
                        Toast.makeText(
                            this@Otomatis,
                            "Background berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@Otomatis, "Error ${response.code}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

}





