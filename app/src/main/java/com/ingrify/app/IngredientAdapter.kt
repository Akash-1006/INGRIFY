package com.ingrify.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.card.MaterialCardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ingrify.app.R
import com.ingrify.app.Ingredient

class IngredientAdapter(
    private var ingredients: List<Ingredient> = emptyList()
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_ingredient_name)
        val expandToggleIcon: ImageView = itemView.findViewById(R.id.iv_expand_toggle)
        val collapsibleDetailsLayout: LinearLayout = itemView.findViewById(R.id.cl_collapsible_details)
        val useTextView: TextView = itemView.findViewById(R.id.tv_ingredient_use)
        val madeFromTextView: TextView = itemView.findViewById(R.id.tv_ingredient_made_from)
        val sideEffectsTextView: TextView = itemView.findViewById(R.id.tv_ingredient_side_effects)
        val allergenTextView: TextView = itemView.findViewById(R.id.tv_ingredient_allergen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.nameTextView.text = ingredient.name
        holder.useTextView.text = "${ingredient.use}"
        holder.madeFromTextView.text = "${ingredient.madeFrom}"
        holder.sideEffectsTextView.text = "${ingredient.sideEffects}"
        holder.allergenTextView.text = " ${if (ingredient.allergen) "Yes" else "No"}"

        // Set the visibility of the collapsible details based on the isExpanded state
        holder.collapsibleDetailsLayout.visibility = if (ingredient.isExpanded) View.VISIBLE else View.GONE

        // Change the icon to reflect the state
        holder.expandToggleIcon.setImageResource(
            if (ingredient.isExpanded) R.drawable.ic_drop_down else R.drawable.ic_right
        )

        // Set a click listener on the entire item view
        holder.itemView.setOnClickListener {
            // Toggle the isExpanded state for the clicked item
            ingredient.isExpanded = !ingredient.isExpanded
            // Notify the adapter that this specific item has changed to re-bind it
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = ingredients.size

    fun updateData(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }
}