package com.plcoding.stockmarketapp.data.repository

import com.plcoding.stockmarketapp.data.csv.CSVParser
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db: StockDatabase,
    val companyListingsParser: CSVParser<CompanyListing>
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
}