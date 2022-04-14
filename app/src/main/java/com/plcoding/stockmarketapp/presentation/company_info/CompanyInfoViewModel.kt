package com.plcoding.stockmarketapp.presentation.company_info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {
    var state by mutableStateOf(CompanyInfoState())

    init {
        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoading = true) //todo: questionable
            val companyInfoResult = async { repository.getCompanyInfo(symbol) }
            val intradayInfoResult = async { repository.getIntradayInfo(symbol) }
            state = when (val result = companyInfoResult.await()) {
                is Result.Success -> {
                    state.copy(company = result.data, isLoading = false, error = null)
                }
                is Result.Error -> {
                    state.copy(isLoading = false, error = result.throwable?.message, company = null)
                }
            }

            state = when (val result = intradayInfoResult.await()) {
                is Result.Success -> {
                    state.copy(
                        stockInfos = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    state.copy(
                        isLoading = false,
                        error = result.throwable?.message ?: "Error",
                        stockInfos = emptyList()
                    )
                }
            }
        }
    }
}