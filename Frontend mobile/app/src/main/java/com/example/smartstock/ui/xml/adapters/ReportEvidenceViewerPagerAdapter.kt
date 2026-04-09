package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.smartstock.R

class ReportEvidenceViewerPagerAdapter(
    private val imageUrls: List<String>,
) : RecyclerView.Adapter<ReportEvidenceViewerPagerAdapter.ViewerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewerViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_image_viewer_page, parent, false)
        return ViewerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewerViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size

    class ViewerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivViewerImage)

        fun bind(imageUrl: String) {
            imageView.load(imageUrl.toReportEvidenceMobileUrl())
        }
    }
}
