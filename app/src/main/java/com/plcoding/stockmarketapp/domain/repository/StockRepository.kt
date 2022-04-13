package com.plcoding.stockmarketapp.domain.repository

import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.util.Result
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Result<List<CompanyListing>>>

    suspend fun getIntradayInfo(symbol: String): Result<List<IntradayInfo>>

    suspend fun getCompanyInfo(symbol: String): Result<CompanyInfo>
}