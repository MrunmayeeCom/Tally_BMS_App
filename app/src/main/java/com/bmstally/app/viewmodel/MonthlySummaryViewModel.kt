package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.MonthlySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject

data class MonthlySummaryState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val data: List<MonthlySummary> = emptyList(),
    val previousYearTotals: Triple<Double, Double, Double>? = null
) {
    val totalTurnover: Double get() = data.sumOf { it.turnover }
    val totalExpense: Double get() = data.sumOf { it.expense }
    val totalProfit: Double get() = data.sumOf { it.profit }
    val profitMargin: Double get() = if (totalTurnover > 0) (totalProfit / totalTurnover) * 100 else 0.0
}

@HiltViewModel
class MonthlySummaryViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MonthlySummaryState())
    val state: StateFlow<MonthlySummaryState> = _state.asStateFlow()

    val years: List<Int> = (Calendar.getInstance().get(Calendar.YEAR) downTo (Calendar.getInstance().get(Calendar.YEAR) - 49)).toList()

    init { loadYear(_state.value.selectedYear) }

    fun setYear(year: Int) {
        _state.value = _state.value.copy(selectedYear = year)
        loadYear(year)
    }

    private fun loadYear(year: Int) {
        val data = repository.getMonthlySummary(year)
        val prevData = repository.getMonthlySummary(year - 1)
        val prevTotals = if (prevData.isNotEmpty()) {
            Triple(prevData.sumOf { it.turnover }, prevData.sumOf { it.expense }, prevData.sumOf { it.profit })
        } else null
        _state.value = _state.value.copy(data = data, previousYearTotals = prevTotals)
    }
}
