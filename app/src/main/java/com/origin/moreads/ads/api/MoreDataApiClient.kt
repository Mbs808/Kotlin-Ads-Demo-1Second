package com.origin.moreads.ads.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.charset.Charset

object MoreDataApiClient {

    private fun getResponse(): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            val mainResponse = chain.proceed(request)
            getResponseString(mainResponse)
            mainResponse
        }

    private fun getResponseString(response: Response): String {
        val responseBody = response.body
        val source = responseBody.source()
        try {
            source.request(Long.MAX_VALUE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val buffer = source.buffer
        var charset = Charset.forName("UTF-8")
        val mediaType = responseBody.contentType()
        if (mediaType != null) {
            charset = mediaType.charset(Charset.forName("UTF-8"))
        }
        return if (charset == null) " " else
            buffer.clone().readString(charset)
    }

    private fun getOkHttpClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder().addInterceptor(getResponse())

    private fun getOkHttpClient(): OkHttpClient =
        getOkHttpClientBuilder().build()

    private fun getRetrofitBuilder(url: String): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())

    private fun getRetrofit(url: String): Retrofit =
        getRetrofitBuilder(url).build()

    fun getMoreApiInterface(url: String): MoreDataApi =
        getRetrofit(url).create(MoreDataApi::class.java)

}