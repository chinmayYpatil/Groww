package com.example.groww.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.model.network.StockInfo
import com.example.groww.domain.usecase.GetTopGainersUseCase
import com.example.groww.domain.usecase.GetTopLosersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getTopGainersUseCase: GetTopGainersUseCase,
    private val getTopLosersUseCase: GetTopLosersUseCase
) : ViewModel() {

    private val _topGainers = MutableLiveData<List<StockInfo>>()
    val topGainers: LiveData<List<StockInfo>> = _topGainers

    private val _topLosers = MutableLiveData<List<StockInfo>>()
    val topLosers: LiveData<List<StockInfo>> = _topLosers

    fun fetchTopStocks(apiKey: String) {
        viewModelScope.launch {
            try {
                _topGainers.value = getTopGainersUseCase.execute(apiKey)
                _topLosers.value = getTopLosersUseCase.execute(apiKey)
            } catch (e: Exception) {
                // TODO: Handle errors gracefully, e.g., by updating a LiveData for error state
            }
        }
    }
}