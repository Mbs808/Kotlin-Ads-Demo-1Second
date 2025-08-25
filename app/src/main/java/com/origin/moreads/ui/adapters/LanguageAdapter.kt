package com.origin.moreads.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.origin.moreads.R
import com.origin.moreads.databinding.CellLanguageItemBinding
import com.origin.moreads.models.Language

class LanguageAdapter(
    private val context: Context,
    private val onClick: (language: Language) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(DiffCallback) {

    var languageCode = "en"

    companion object DiffCallback : DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem.languageCode == newItem.languageCode
        }

        override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding =
            CellLanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LanguageViewHolder(private val binding: CellLanguageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Language) {
            binding.root.setBackgroundResource(
                if (languageCode == data.languageCode) R.drawable.lang_selected_bg
                else R.drawable.lang_unselected_bg
            )

            binding.ivCountryFlag.setImageResource(data.countryFlag)
            binding.tvLanguageName.text = data.languageName
            binding.tvLanguageCode.text = "(${data.languageCode.uppercase()})"

            binding.root.setOnClickListener {
                languageCode = data.languageCode
                notifyDataSetChanged()
                onClick(data)
            }
        }
    }
}
