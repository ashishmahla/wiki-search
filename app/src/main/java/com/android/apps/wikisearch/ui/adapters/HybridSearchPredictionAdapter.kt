package com.android.apps.wikisearch.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.apps.wikisearch.R
import com.android.apps.wikisearch.models.Page
import com.google.android.material.textview.MaterialTextView

class HybridSearchPredictionAdapter(
    private val isCompactAdapter: Boolean,
    private val itemClickListener: (position: Int) -> Unit
) :
    RecyclerView.Adapter<HybridSearchPredictionAdapter.SearchPredictionViewHolder>() {

    private val itemList: MutableList<Page> = mutableListOf()
    fun refreshDataSet(pages: List<Page>) {
        itemList.clear()
        itemList.addAll(pages)
        notifyDataSetChanged()
    }

    // to show max 7 items only if isCompatAdapter
    override fun getItemCount() = if (isCompactAdapter && itemList.size > 7) 7 else itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPredictionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (isCompactAdapter) {
            SpViewHolderCompact(
                inflater.inflate(
                    R.layout.model_search_prediction,
                    parent,
                    false
                )
            )
        } else {
            SpViewHolderFull(
                inflater.inflate(
                    R.layout.model_search_result,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: SearchPredictionViewHolder, position: Int) =
        holder.onBind(itemList[position])

    abstract inner class SearchPredictionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClickListener(adapterPosition)
            }
        }

        abstract fun onBind(searchPredictionPage: Page)
    }

    inner class SpViewHolderCompact(itemView: View) : SearchPredictionViewHolder(itemView) {
        private val tvPredictionText =
            itemView.findViewById<MaterialTextView>(R.id.mtvMspPredictionText)

        override fun onBind(searchPredictionPage: Page) {
            tvPredictionText.text = searchPredictionPage.title
        }
    }

    inner class SpViewHolderFull(itemView: View) : SearchPredictionViewHolder(itemView) {
        private val ivImage = itemView.findViewById<MaterialTextView>(R.id.ivMsrImage)
        private val mtvTitle = itemView.findViewById<MaterialTextView>(R.id.mtvMsrTitle)
        private val mtvLink = itemView.findViewById<MaterialTextView>(R.id.mtvMsrLink)
        private val mtvDescription = itemView.findViewById<MaterialTextView>(R.id.mtvMsrDescription)

        override fun onBind(searchPredictionPage: Page) {
            mtvTitle.text = searchPredictionPage.title
            mtvLink.text = searchPredictionPage.pageId.toString()
            mtvDescription.text =
                if (!searchPredictionPage.description.isNullOrBlank()) searchPredictionPage.description
                else "Page description not available"
        }
    }
}