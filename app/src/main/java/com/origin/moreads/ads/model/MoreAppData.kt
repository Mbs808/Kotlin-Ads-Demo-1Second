package com.origin.moreads.ads.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MoreAppData(

    @SerializedName("id")
    @Expose
    val id: Int?= null,

    @SerializedName("app_name")
    @Expose
    val appName: String? = null,

    @SerializedName("app_description")
    @Expose
    val appDescription: String? = null,

    @SerializedName("app_link")
    @Expose
    val appLink: String? = null,

    @SerializedName("app_icon")
    @Expose
    val appIcon: String? = null,

    @SerializedName("app_banner")
    @Expose
    val appBanner: String? = null,

    @SerializedName("app_screenshot")
    @Expose
    val appScreenshot: String? = null

) {
    override fun toString(): String {
        return """
            ---id---$id
            ---appName----$appName
            ---appDescription----$appDescription
            ---appLink----$appLink
            ------appIcon---$appIcon
            -----appBanner----$appBanner
            ----appScreenshot-----$appScreenshot
            """.trimIndent();
    }
}
