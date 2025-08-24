package com.ingrify.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllergenAdapter(private val allergenList: List<Allergen>) :
    RecyclerView.Adapter<AllergenAdapter.AllergenViewHolder>() {

    class AllergenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val allergenName: TextView = itemView.findViewById(R.id.tv_allergen_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allergen, parent, false)
        return AllergenViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllergenViewHolder, position: Int) {
        val allergen = allergenList[position]
        holder.allergenName.text = allergen.name
    }

    override fun getItemCount(): Int = allergenList.size
}
