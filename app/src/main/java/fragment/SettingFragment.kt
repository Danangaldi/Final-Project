package fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.cambeta.About
import com.example.cambeta.Profile
import com.example.cambeta.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingFragment : Fragment() {

    var context: Activity? = null
    private lateinit var dateTimeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = activity
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        //menampilkan kalender
        dateTimeTextView = view.findViewById(R.id.kalender)
        val dateFormat = SimpleDateFormat("EEEE\ndd MMMM yyyy", Locale.getDefault())
        val currentDate = Date()
        val dateString = dateFormat.format(currentDate)

        val dateTimeText = "$dateString"
        dateTimeTextView.text = dateTimeText
        return view

    }

    override fun onStart() {
        super.onStart()
        val btn = context!!.findViewById<View>(R.id.profil) as ImageButton
        btn.setOnClickListener {
            val intent = Intent(context, Profile::class.java)
            startActivity(intent)
        }
        super.onStart()
        val aboutbtn = context!!.findViewById<View>(R.id.about) as ImageButton
        aboutbtn.setOnClickListener {
            val intent1 = Intent(context, About::class.java)
            startActivity(intent1)
        }
    }
}






