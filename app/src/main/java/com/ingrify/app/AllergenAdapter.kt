package com.ingrify.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllergenAdapter(
    private val allergenList: MutableList<Allergen>,
    private val onDeleteClick: (Allergen) -> Unit
) : RecyclerView.Adapter<AllergenAdapter.AllergenViewHolder>() {

    class AllergenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val allergenName: TextView = itemView.findViewById(R.id.tv_allergen_name)
        val deleteButton: ImageView = itemView.findViewById(R.id.allergen_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allergen, parent, false)
        return AllergenViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllergenViewHolder, position: Int) {
        val allergen = allergenList[position]
        holder.allergenName.text = allergen.name

        holder.deleteButton.setOnClickListener {
            onDeleteClick(allergen)
        }
    }

    override fun getItemCount(): Int = allergenList.size

    fun removeItem(allergen: Allergen) {
        val index = allergenList.indexOf(allergen)
        if (index != -1) {
            allergenList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
