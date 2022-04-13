package com.plcoding.stockmarketapp.data.remote

import com.plcoding.stockmarketapp.Keys
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {
    @GET("query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apiKey: String = Keys.apiKey
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://alphavantage.co"
    }
}