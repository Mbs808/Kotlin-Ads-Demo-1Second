package com.origin.moreads.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.origin.moreads.R
import com.origin.moreads.models.Language

class LanguageAdapter(
    private val context: Context,
    private val onClick: (language: Language) -> Unit
): ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(Companion) {

    var languageCode = "en"

    private companion object: DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem.languageCode == newItem.languageCode
        }

        override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cell_language_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val items = getItem(position)
        holder.bind(items)
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class LanguageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView.rootView) {

        private val ivCountryFlag = itemView.findViewById<ImageView>(R.id.ivCountryFlag)
        private val tvLanguageName = itemView.findViewById<TextView>(R.id.tvLanguageName)
        private val tvLanguageCode = itemView.findViewById<TextView>(R.id.tvLanguageCode)

        fun bind(data: Language) {

            itemView.rootView.setBackgroundResource(
                if (languageCode == data.languageCode) R.drawable.lang_selected_bg
                else R.drawable.lang_unselected_bg
            )


            ivCountryFlag.setImageResource(data.countryFlag)
            tvLanguageName.text = data.languageName
            val code = "(${data.languageCode.uppercase()})"
            tvLanguageCode.text = code

            itemView.setOnClickListener {
                languageCode = data.languageCode
                notifyDataSetChanged()
                onClick(data)
            }
        }
    }

}