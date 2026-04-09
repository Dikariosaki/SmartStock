package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.smartstock.R

class ReportEvidenceGalleryAdapter(
    private val onImageClick: (Int) -> Unit,
) : ListAdapter<String, ReportEvidenceGalleryAdapter.GalleryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_gallery_image, parent, false)
        return GalleryViewHolder(view, onImageClick)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class GalleryViewHolder(
        itemView: View,
        private val onImageClick: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val preview: ImageView = itemView.findViewById(R.id.ivReportGalleryImage)

        fun bind(imageUrl: String, position: Int) {
            preview.load(imageUrl.toReportEvidenceMobileUrl())
            itemView.setOnClickListener { onImageClick(position) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
