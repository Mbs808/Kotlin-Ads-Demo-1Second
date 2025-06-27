package com.origin.moreads.ads.api.moredata

import com.origin.moreads.ads.model.moredata.MoreApp
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MoreDataApi {

    @FormUrlEncoded
    @POST("index.php")
    fun getMoreList(
        @Field("account_name") accountName: String
    ): Call<MoreApp>

}