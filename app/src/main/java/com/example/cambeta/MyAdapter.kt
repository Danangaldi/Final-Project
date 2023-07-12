package com.example.cambeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

class MyAdapter(private val userList: List<EmployeeModel>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_item,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = userList[position]
        holder.bindData(user)

    }

    override fun getItemCount(): Int {
        return userList.size

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val jenisSapi: TextView = itemView.findViewById(R.id.identitas)
        val kategori1: TextView = itemView.findViewById(R.id.kategori12)
        val lambourne: TextView = itemView.findViewById(R.id.lambourne)
        val winter: TextView = itemView.findViewById(R.id.winter)
        val schoorl: TextView = itemView.findViewById(R.id.schoorl)
        val kalender1: TextView = itemView.findViewById(R.id.kalender)
        val avatar: ImageView = itemView.findViewById(R.id.gambar123)

        fun bindData(data: EmployeeModel) {
            // Set data to views
            jenisSapi.text = data.Jenis_Sapi
            lambourne.text = data.BB_Lambourne
            kategori1.text = data.kategori
            winter.text = data.BB_Winter
            schoorl.text = data.BB_Schoorl
            val kalenderTimestamp = data.kalender?.toDate()
            val kalenderFormatted = SimpleDateFormat("EEEE, dd MMMM yyyy (HH:mm)").format((kalenderTimestamp))
            kalender1.text = kalenderFormatted

            //loaf gambar menggunakan glide
            Glide.with(itemView.context)
                .load(data.imageUrl)
                .into(avatar)
        }

    }

}
