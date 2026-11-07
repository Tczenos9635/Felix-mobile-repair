package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ShopRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ShopTab(val label: String) {
    HOME("Home"),
    REPAIR("Repair"),
    SOFTWARE("Software"),
    ACCESSORIES("Accessories"),
    WARRANTY("Warranty"),
    DISPLAY("Display")
}

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShopRepository.getInstance(application)

    // Navigation state
    private val _currentTab = MutableStateFlow(ShopTab.HOME)
    val currentTab: StateFlow<ShopTab> = _currentTab.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Tab navigation
    fun selectTab(tab: ShopTab) {
        _currentTab.value = tab
        _showSettings.value = false
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    // 1. REPAIR STATE
    val repairs = repository.repairs
    val repairSearchQuery = MutableStateFlow("")
    val repairStatusFilter = MutableStateFlow("All") // "All", "Pending", "Done", "Delivered"

    val filteredRepairs: StateFlow<List<RepairItem>> = combine(
        repairs,
        repairSearchQuery,
        repairStatusFilter
    ) { list, query, filter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.mobileNumber.contains(query, ignoreCase = true) ||
                    item.mobileModel.contains(query, ignoreCase = true) ||
                    item.mobileProblem.contains(query, ignoreCase = true)
            val matchesFilter = filter == "All" || item.status.equals(filter, ignoreCase = true)
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. SOFTWARE STATE
    val softwareJobs = repository.softwareJobs
    val softwareSearchQuery = MutableStateFlow("")

    val filteredSoftwareJobs: StateFlow<List<SoftwareJobItem>> = combine(
        softwareJobs,
        softwareSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { item ->
            item.customerName.contains(query, ignoreCase = true) ||
                    item.mobileNumber.contains(query, ignoreCase = true) ||
                    item.mobileModel.contains(query, ignoreCase = true) ||
                    item.imeiNumber.contains(query, ignoreCase = true) ||
                    item.aadhaarNumber.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. ACCESSORIES PURCHASE & WHOLESALER
    val purchases = repository.purchases
    val payments = repository.payments
    val purchaseSearchQuery = MutableStateFlow("")
    val accessoriesActiveTab = MutableStateFlow(0) // 0: Wholesalers, 1: Purchases
    val selectedWholesaler = MutableStateFlow<String?>(null) // For Wholesaler Detail Screen

    val wholesalerSummaries: StateFlow<List<WholesalerSummary>> = combine(
        purchases,
        payments
    ) { _, _ ->
        repository.getWholesalerSummaries()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPurchases: StateFlow<List<PurchaseItem>> = combine(
        purchases,
        purchaseSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { item ->
            item.wholesalerName.contains(query, ignoreCase = true) ||
                    item.itemName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. WARRANTY STATE
    val warranties = repository.warranties
    val warrantySearchQuery = MutableStateFlow("")
    val warrantyFilter = MutableStateFlow("All") // "All", "Active", "Expired"

    val filteredWarranties: StateFlow<List<WarrantyItem>> = combine(
        warranties,
        warrantySearchQuery,
        warrantyFilter
    ) { list, query, filter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.mobileNumber.contains(query, ignoreCase = true) ||
                    item.itemName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Active" -> item.isActive
                "Expired" -> !item.isActive
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. UNIVERSAL DISPLAY STATE
    val displays = repository.displays
    val displaySearchQuery = MutableStateFlow("")
    val displayStockSearchQuery = MutableStateFlow("")
    val displayActiveTab = MutableStateFlow(0) // 0: Model Search, 1: Stock List

    private fun normalize(str: String): String {
        return str.lowercase().replace(" ", "").replace("-", "").replace("_", "").replace("/", "")
    }

    val modelSearchResults: StateFlow<List<UniversalDisplayItem>> = combine(
        displays,
        displaySearchQuery
    ) { list, query ->
        val cleanQuery = normalize(query.trim())
        if (cleanQuery.isBlank()) {
            emptyList()
        } else {
            list.filter { item ->
                val nameMatch = normalize(item.displayName).contains(cleanQuery)
                val modelMatch = item.compatibleModels.any { model ->
                    val cleanModel = normalize(model)
                    cleanModel.contains(cleanQuery) || cleanQuery.contains(cleanModel)
                }
                nameMatch || modelMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDisplaysStock: StateFlow<List<UniversalDisplayItem>> = combine(
        displays,
        displayStockSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else {
            val cleanQuery = normalize(query.trim())
            list.filter { item ->
                normalize(item.displayName).contains(cleanQuery) ||
                        item.compatibleModels.any { normalize(it).contains(cleanQuery) } ||
                        item.qualityType.contains(query, ignoreCase = true) ||
                        (item.wholesalerName?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // HOME SUMMARY METRICS
    val pendingRepairsCount: StateFlow<Int> = repairs.map { list ->
        list.count { it.status.equals("Pending", ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDueToWholesalers: StateFlow<Double> = combine(purchases, payments) { _, _ ->
        repository.getTotalDueToAllWholesalers()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lowStockDisplaysCount: StateFlow<Int> = displays.map { list ->
        list.count { it.stockQuantity in 0..2 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // CHECK DISPLAY INTERACTION
    fun checkDisplayForModel(modelName: String) {
        _showSettings.value = false
        _currentTab.value = ShopTab.DISPLAY
        displayActiveTab.value = 0
        displaySearchQuery.value = modelName.trim()
    }

    // ACTIONS: REPAIRS
    fun addRepair(item: RepairItem) = repository.addRepair(item)
    fun updateRepair(item: RepairItem) = repository.updateRepair(item)
    fun deleteRepair(id: String) = repository.deleteRepair(id)

    // ACTIONS: SOFTWARE
    fun addSoftwareJob(item: SoftwareJobItem) = repository.addSoftwareJob(item)
    fun updateSoftwareJob(item: SoftwareJobItem) = repository.updateSoftwareJob(item)
    fun deleteSoftwareJob(id: String) = repository.deleteSoftwareJob(id)

    // ACTIONS: PURCHASES & PAYMENTS
    fun addPurchase(item: PurchaseItem) = repository.addPurchase(item)
    fun updatePurchase(item: PurchaseItem) = repository.updatePurchase(item)
    fun deletePurchase(id: String) = repository.deletePurchase(id)
    fun addWholesalerPayment(wholesalerName: String, amount: Double, note: String) {
        repository.addWholesalerPayment(
            WholesalerPayment(
                wholesalerName = wholesalerName,
                amount = amount,
                note = note
            )
        )
    }
    fun deleteWholesalerPayment(id: String) = repository.deleteWholesalerPayment(id)

    // ACTIONS: WARRANTIES
    fun addWarranty(item: WarrantyItem) = repository.addWarranty(item)
    fun updateWarranty(item: WarrantyItem) = repository.updateWarranty(item)
    fun deleteWarranty(id: String) = repository.deleteWarranty(id)

    // ACTIONS: DISPLAYS
    fun addDisplay(item: UniversalDisplayItem) = repository.addDisplay(item)
    fun updateDisplay(item: UniversalDisplayItem) = repository.updateDisplay(item)
    fun deleteDisplay(id: String) = repository.deleteDisplay(id)
    fun useDisplayStock(id: String) = repository.useDisplayStock(id)
    fun addDisplayStock(id: String, amount: Int) = repository.addDisplayStock(id, amount)

    // ACTIONS: BACKUP & RESTORE
    fun exportBackupJson(): String = repository.exportBackupJson()
    fun restoreBackupJson(jsonString: String): Result<Unit> = repository.restoreBackupJson(jsonString)
    fun resetAllData() = repository.resetAllData()
    fun loadDemoData() = repository.loadInitialDemoData()
}
