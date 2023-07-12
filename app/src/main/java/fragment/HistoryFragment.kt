package fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cambeta.EmployeeModel
import com.example.cambeta.MyAdapter
import com.example.cambeta.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var dateTimeTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private val dataList: MutableList<EmployeeModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyAdapter(dataList)
        recyclerView.adapter = adapter


        // Mengambil data dari Firebase Firestore
        val db = FirebaseFirestore.getInstance()
        val titlesCollection = db.collection("Data Sapi")

        // Ambil koleksi data dari Firestore
        titlesCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Penanganan kesalahan
                return@addSnapshotListener
            }

            val userList = mutableListOf<EmployeeModel>()
            for (document in snapshot!!.documents) {
                val bb = document.getString("bb_Lambourne")
                val bb1 = document.getString("bb_Winter")
                val bb2 = document.getString("bb_Schoorl")
                val jenisSapi = document.getString("jenis_Sapi")
                val time = document.getTimestamp("time")
                val kategori = document.getString("kategori")
                val imageUrl = document.getString("imageUrl")
                jenisSapi?.let { imageUrl?.let { time?.let{ bb?.let{ bb1?.let { bb2?.let { kategori?.let{ EmployeeModel(it, bb, bb1, bb2, jenisSapi, time, imageUrl) } } } } } }}
                    ?.let { userList.add(it) }
            }

            adapter = MyAdapter(userList)
            adapter.notifyDataSetChanged()
            recyclerView.adapter = adapter
        }


        //menampilkan kalender
        dateTimeTextView = view.findViewById(R.id.kalender)
        val dateFormat = SimpleDateFormat("EEEE\ndd MMMM yyyy", Locale.getDefault())
        val currentDate = Date()
        val dateString = dateFormat.format(currentDate)

        val dateTimeText = "$dateString"
        dateTimeTextView.text = dateTimeText
        return view

    }

}