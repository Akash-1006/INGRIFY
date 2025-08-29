package com.ingrify.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.ingrify.app.RetrofitClient.BASE_URL

class ScanAdapter(
    private var items: List<ScanItem>,
    private val onItemClick: (ScanItem) -> Unit   // ✅ callback
) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    inner class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgScan: ImageView = itemView.findViewById(R.id.imgScan)
        val scanName: TextView = itemView.findViewById(R.id.Scan_name)
        val colorBar: MaterialButton = itemView.findViewById(R.id.colorBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val item = items[position]

        holder.scanName.text = item.scan_name.ifEmpty { "Scan #${item.id}" }

        if (!item.image_filename.isNullOrEmpty()) {
            val url = "$BASE_URL/static/uploads/${item.image_filename}"
            Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.placeholder_bg)
                .into(holder.imgScan)
        } else {
            holder.imgScan.setImageResource(R.drawable.placeholder_bg)
        }

        val score = item.overall_safety?.score ?: 0
        val barColor = getColorFromScore(score)
        holder.colorBar.backgroundTintList = ColorStateList.valueOf(barColor)

        // ✅ Click sends item back
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ScanItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // getColorFromScore() stays the same...

    // ✅ Map score (0 = green, 100 = red)
    private fun getColorFromScore(score: Int): Int {
        // Clamp score to [0, 100]
        val clampedScore = score.coerceIn(0, 100)

        return when (clampedScore) {
            100 -> Color.RED // Final point

            in 0..20 -> interpolateColor(Color.parseColor("#006400"), Color.parseColor("#00FF00"), clampedScore / 20f)
            // Dark Green (#006400) → Light Green (#00FF00)

            in 21..40 -> interpolateColor(Color.parseColor("#00FF00"), Color.YELLOW, (clampedScore - 20) / 20f)
            // Light Green → Yellow

            in 41..60 -> interpolateColor(Color.YELLOW, Color.parseColor("#FFA500"), (clampedScore - 40) / 20f)
            // Yellow → Orange

            in 61..80 -> interpolateColor(Color.parseColor("#FFA500"), Color.parseColor("#FF8C00"), (clampedScore - 60) / 20f)
            // Orange → Dark Orange

            in 81..99 -> interpolateColor(Color.parseColor("#FF8C00"), Color.RED, (clampedScore - 80) / 19f)
            // Dark Orange → Red (almost red, but full red at 100)

            else -> Color.RED
        }
    }

    // Helper: Linearly interpolate between two colors
    private fun interpolateColor(colorStart: Int, colorEnd: Int, fraction: Float): Int {
        val r = Color.red(colorStart) + ((Color.red(colorEnd) - Color.red(colorStart)) * fraction).toInt()
        val g = Color.green(colorStart) + ((Color.green(colorEnd) - Color.green(colorStart)) * fraction).toInt()
        val b = Color.blue(colorStart) + ((Color.blue(colorEnd) - Color.blue(colorStart)) * fraction).toInt()
        return Color.rgb(r, g, b)
    }

}
