package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.LedgerEntry
import com.bmstally.app.model.Voucher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VoucherListState(
    val vouchers: List<Voucher> = emptyList(),
    val searchQuery: String = "",
    val selectedType: String = "All",
    val selectedStatus: String = "All Status",
    val fromDate: String = "",
    val toDate: String = "",
    val currentPage: Int = 1,
    val pageSize: Int = 10
) {
    val filteredVouchers: List<Voucher>
        get() = vouchers.filter { v ->
            val matchesSearch = searchQuery.isBlank() ||
                (v.party?.contains(searchQuery, ignoreCase = true) == true) ||
                (v.reference_no?.contains(searchQuery, ignoreCase = true) == true) ||
                (v.narration?.contains(searchQuery, ignoreCase = true) == true)
            val matchesType = selectedType == "All" || v.voucher_type == selectedType
            val matchesStatus = selectedStatus == "All Status" || v.status == selectedStatus
            val matchesFrom = fromDate.isBlank() || (v.voucher_date ?: "") >= fromDate
            val matchesTo = toDate.isBlank() || (v.voucher_date ?: "") <= toDate
            matchesSearch && matchesType && matchesStatus && matchesFrom && matchesTo
        }

    val totalRecords: Int get() = filteredVouchers.size
    val totalPages: Int get() = maxOf(1, (totalRecords + pageSize - 1) / pageSize)

    val paginatedVouchers: List<Voucher>
        get() {
            val start = (currentPage - 1) * pageSize
            return filteredVouchers.drop(start).take(pageSize)
        }
}

data class VoucherFormState(
    val voucherType: String = "Journal",
    val voucherDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
    val referenceNo: String = "",
    val narration: String = "",
    val entries: List<LedgerEntry> = listOf(LedgerEntry("", is_debit = true, amount = 0.0)),
    val party: String = "",
    val saving: Boolean = false
) {
    fun isValid(): Boolean = voucherDate.isNotBlank() && entries.any { it.ledger_name.isNotBlank() && it.amount > 0 }
}

@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(VoucherListState())
    val listState: StateFlow<VoucherListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(VoucherFormState())
    val formState: StateFlow<VoucherFormState> = _formState.asStateFlow()

    private val _deleteConfirmId = MutableStateFlow<String?>(null)
    val deleteConfirmId: StateFlow<String?> = _deleteConfirmId.asStateFlow()

    fun loadVouchers() {
        _listState.value = _listState.value.copy(vouchers = repository.getVouchers())
    }

    fun setSearchQuery(query: String) { _listState.value = _listState.value.copy(searchQuery = query, currentPage = 1) }
    fun setSelectedType(type: String) { _listState.value = _listState.value.copy(selectedType = type, currentPage = 1) }
    fun setSelectedStatus(status: String) { _listState.value = _listState.value.copy(selectedStatus = status, currentPage = 1) }
    fun setFromDate(date: String) { _listState.value = _listState.value.copy(fromDate = date, currentPage = 1) }
    fun setToDate(date: String) { _listState.value = _listState.value.copy(toDate = date, currentPage = 1) }
    fun setPage(page: Int) { _listState.value = _listState.value.copy(currentPage = page.coerceIn(1, _listState.value.totalPages)) }
    fun setPageSize(size: Int) { _listState.value = _listState.value.copy(pageSize = size, currentPage = 1) }

    fun requestDelete(id: String) { _deleteConfirmId.value = id }
    fun cancelDelete() { _deleteConfirmId.value = null }

    fun confirmDelete() {
        _deleteConfirmId.value?.let { id ->
            repository.deleteVoucher(id)
            _listState.value = _listState.value.copy(
                vouchers = _listState.value.vouchers.filter { it.id != id }
            )
        }
        _deleteConfirmId.value = null
    }

    // Form methods
    fun initForm(voucherType: String = "Journal") {
        val entries = when (voucherType) {
            "Sales" -> listOf(LedgerEntry("", is_debit = true, amount = 0.0), LedgerEntry("Sales", is_debit = false, amount = 0.0))
            "Purchase" -> listOf(LedgerEntry("Purchase", is_debit = true, amount = 0.0), LedgerEntry("", is_debit = false, amount = 0.0))
            else -> listOf(LedgerEntry("", is_debit = true, amount = 0.0))
        }
        _formState.value = VoucherFormState(voucherType = voucherType, entries = entries)
    }

    fun setFormVoucherType(type: String) {
        val entries = when (type) {
            "Sales" -> listOf(LedgerEntry("", is_debit = true, amount = 0.0), LedgerEntry("Sales", is_debit = false, amount = 0.0))
            "Purchase" -> listOf(LedgerEntry("Purchase", is_debit = true, amount = 0.0), LedgerEntry("", is_debit = false, amount = 0.0))
            else -> listOf(LedgerEntry("", is_debit = true, amount = 0.0))
        }
        _formState.value = _formState.value.copy(voucherType = type, entries = entries)
    }

    fun setFormDate(date: String) { _formState.value = _formState.value.copy(voucherDate = date) }
    fun setFormRefNo(ref: String) { _formState.value = _formState.value.copy(referenceNo = ref) }
    fun setFormNarration(nar: String) { _formState.value = _formState.value.copy(narration = nar) }
    fun setFormParty(party: String) { _formState.value = _formState.value.copy(party = party) }

    fun updateEntry(index: Int, entry: LedgerEntry) {
        val entries = _formState.value.entries.toMutableList()
        if (index in entries.indices) {
            entries[index] = entry
            _formState.value = _formState.value.copy(entries = entries)
        }
    }

    fun addEntry() {
        _formState.value = _formState.value.copy(
            entries = _formState.value.entries + LedgerEntry("", is_debit = true, amount = 0.0)
        )
    }

    fun removeEntry(index: Int) {
        if (_formState.value.entries.size > 1) {
            _formState.value = _formState.value.copy(
                entries = _formState.value.entries.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun saveVoucher(): Boolean {
        val form = _formState.value
        if (!form.isValid()) return false
        val voucher = Voucher(
            id = "",
            voucher_date = form.voucherDate,
            voucher_type = form.voucherType,
            reference_no = form.referenceNo,
            status = "Approved",
            party = form.party,
            narration = form.narration,
            entries = form.entries
        )
        val totalDebit = form.entries.filter { it.is_debit }.sumOf { it.amount }
        val totalCredit = form.entries.filter { !it.is_debit }.sumOf { it.amount }
        val saved = repository.createVoucher(
            voucher.copy(debit = totalDebit, credit = totalCredit)
        )
        _listState.value = _listState.value.copy(
            vouchers = _listState.value.vouchers + saved
        )
        return true
    }
}
