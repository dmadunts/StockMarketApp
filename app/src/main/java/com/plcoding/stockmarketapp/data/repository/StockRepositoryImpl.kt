package com.plcoding.stockmarketapp.data.repository

import com.plcoding.stockmarketapp.data.csv.CSVParser
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.mapper.toCompanyInfo
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>
) :
    StockRepository {
    private val dao = db.dao
    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Result<List<CompanyListing>>> {
        return flow {
            try {
                val localListings = dao.searchCompanyListing(query)
                val isDbEmpty = localListings.isEmpty() && query.isBlank()
                val shouldLoadFromCache = !isDbEmpty && !fetchFromRemote
                if (shouldLoadFromCache) {
                    return@flow emit(Result.Success(localListings.map { it.toCompanyListing() }))
                } else {
                    val response = api.getListings()
                    val listings = companyListingsParser.parse(response.byteStream())
                    dao.clearCompanyListings()
                    dao.insertCompanyListing(listings.map { it.toCompanyListingEntity() })
                    emit(Result.Success(
                        data = dao.searchCompanyListing("")
                            .map { it.toCompanyListing() }
                    ))
                }
            } catch (e: Throwable) {
                emit(Result.Error(e))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Result<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol = symbol)
            val intradayInfo = intradayInfoParser.parse(response.byteStream())
            Result.Success(intradayInfo)
        } catch (e: Throwable) {
            return Result.Error(e)
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Result<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol = symbol)
            Result.Success(result.toCompanyInfo())
        } catch (e: Throwable) {
            return Result.Error(e)
        }
    }
}