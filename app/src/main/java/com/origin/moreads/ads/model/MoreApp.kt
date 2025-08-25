package com.origin.moreads.ads.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MoreApp(

    @SerializedName("MESSAGE")
    @Expose
    val message: String? = null,

    @SerializedName("STATUS")
    @Expose
    val status: Int? = null,

    @SerializedName("DATA")
    @Expose
    val moreAppData: List<MoreAppData>? = null

)
