package com.origin.moreads.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.origin.moreads.R
import com.origin.moreads.models.Language

object Utils {

    fun getLanguageList(): List<Language> {
        val languageList: MutableList<Language> = mutableListOf()
        languageList.add(Language(R.drawable.ic_english, "English", "en", false))
        languageList.add(Language(R.drawable.ic_hindi, "हिन्दी", "hi", false))
        languageList.add(Language(R.drawable.ic_chinese, "普通话", "zh", false))
        languageList.add(Language(R.drawable.ic_spanish, "Española", "es", false))
        languageList.add(Language(R.drawable.ic_french, "Français", "fr", false))
        languageList.add(Language(R.drawable.ic_arabic, "عربي", "ar", false))
        languageList.add(Language(R.drawable.ic_bengali, "বাংলা", "bn", false))
        languageList.add(Language(R.drawable.ic_russian, "Русский", "ru", false))
        languageList.add(Language(R.drawable.germany, "Deutsch", "de", false))
        languageList.add(Language(R.drawable.japan, "日本", "ja", false))
        languageList.add(Language(R.drawable.ic_portuges, "Português", "pt", false))
        languageList.add(Language(R.drawable.pakistan, "اردو", "ur", false))
        return languageList
    }

    fun openAppStore(context: Context) {
        val sendView = Intent.ACTION_VIEW
        val playStoreLink1 = "market://details?id="
        val playStoreLink2 = "http://play.google.com/store/apps/details?id="
        try {
            context.startActivity(Intent(sendView, Uri.parse(playStoreLink1 + context.packageName)))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(sendView, Uri.parse(playStoreLink2 + context.packageName)))
        }
    }

    fun shareApp(context: Context) {
        val intShare = Intent(Intent.ACTION_SEND)
        intShare.type = "text/plain"
        intShare.putExtra(
            Intent.EXTRA_SUBJECT, context.getString(R.string.app_name)
        )
        val message: String = """
            ${context.resources.getString(R.string.share_app_description)}
            https://play.google.com/store/apps/details?id=${context.packageName}
            """.trimIndent()
        intShare.putExtra(Intent.EXTRA_TEXT, message)
        context.startActivity(Intent.createChooser(intShare, context.getString(R.string.app_name)))
    }

}