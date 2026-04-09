package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.smartstock.R
import java.io.File
import java.util.UUID

data class ReportEvidenceDraftItem(
    val id: String,
    val previewModel: Any,
    val remoteUrl: String? = null,
    val localFile: File? = null,
    val badgeLabel: String,
) {
    companion object {
        fun remote(url: String): ReportEvidenceDraftItem =
            ReportEvidenceDraftItem(
                id = "remote-$url",
                previewModel = url.toReportEvidenceMobileUrl(),
                remoteUrl = url,
                badgeLabel = "CDN",
            )

        fun local(file: File): ReportEvidenceDraftItem =
            ReportEvidenceDraftItem(
                id = UUID.randomUUID().toString(),
                previewModel = file,
                localFile = file,
                badgeLabel = "Nueva",
            )
    }
}

class ReportEvidenceDraftAdapter(
    private val onRemove: (ReportEvidenceDraftItem) -> Unit,
) : ListAdapter<ReportEvidenceDraftItem, ReportEvidenceDraftAdapter.EvidenceViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_evidence_image, parent, false)
        return EvidenceViewHolder(view, onRemove)
    }

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EvidenceViewHolder(
        itemView: View,
        private val onRemove: (ReportEvidenceDraftItem) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val preview: ImageView = itemView.findViewById(R.id.ivEvidencePreview)
        private val badge: TextView = itemView.findViewById(R.id.tvEvidenceBadge)
        private val removeButton: ImageButton = itemView.findViewById(R.id.btnRemoveEvidenceImage)

        fun bind(item: ReportEvidenceDraftItem) {
            badge.text = item.badgeLabel
            preview.load(item.previewModel)
            removeButton.setOnClickListener { onRemove(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ReportEvidenceDraftItem>() {
        override fun areItemsTheSame(
            oldItem: ReportEvidenceDraftItem,
            newItem: ReportEvidenceDraftItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ReportEvidenceDraftItem,
            newItem: ReportEvidenceDraftItem,
        ): Boolean = oldItem == newItem
    }
}
