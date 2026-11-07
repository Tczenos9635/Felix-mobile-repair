package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ShopRepository private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dbFile = File(context.filesDir, "pooja_mobile_repair_db.json")

    private val _repairs = MutableStateFlow<List<RepairItem>>(emptyList())
    val repairs: StateFlow<List<RepairItem>> = _repairs.asStateFlow()

    private val _softwareJobs = MutableStateFlow<List<SoftwareJobItem>>(emptyList())
    val softwareJobs: StateFlow<List<SoftwareJobItem>> = _softwareJobs.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val purchases: StateFlow<List<PurchaseItem>> = _purchases.asStateFlow()

    private val _payments = MutableStateFlow<List<WholesalerPayment>>(emptyList())
    val payments: StateFlow<List<WholesalerPayment>> = _payments.asStateFlow()

    private val _warranties = MutableStateFlow<List<WarrantyItem>>(emptyList())
    val warranties: StateFlow<List<WarrantyItem>> = _warranties.asStateFlow()

    private val _displays = MutableStateFlow<List<UniversalDisplayItem>>(emptyList())
    val displays: StateFlow<List<UniversalDisplayItem>> = _displays.asStateFlow()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        if (!dbFile.exists()) {
            loadInitialDemoData()
            saveToDisk()
            return
        }

        try {
            val content = dbFile.readText()
            parseJsonAndPopulate(content)
        } catch (e: Exception) {
            e.printStackTrace()
            loadInitialDemoData()
            saveToDisk()
        }
    }

    private fun parseJsonAndPopulate(jsonString: String) {
        val root = JSONObject(jsonString)

        val repList = mutableListOf<RepairItem>()
        val repArray = root.optJSONArray("repairs") ?: JSONArray()
        for (i in 0 until repArray.length()) {
            repList.add(RepairItem.fromJsonObject(repArray.getJSONObject(i)))
        }
        _repairs.value = repList.sortedByDescending { it.timestamp }

        val softList = mutableListOf<SoftwareJobItem>()
        val softArray = root.optJSONArray("softwareJobs") ?: JSONArray()
        for (i in 0 until softArray.length()) {
            softList.add(SoftwareJobItem.fromJsonObject(softArray.getJSONObject(i)))
        }
        _softwareJobs.value = softList.sortedByDescending { it.timestamp }

        val purList = mutableListOf<PurchaseItem>()
        val purArray = root.optJSONArray("purchases") ?: JSONArray()
        for (i in 0 until purArray.length()) {
            purList.add(PurchaseItem.fromJsonObject(purArray.getJSONObject(i)))
        }
        _purchases.value = purList.sortedByDescending { it.timestamp }

        val payList = mutableListOf<WholesalerPayment>()
        val payArray = root.optJSONArray("payments") ?: JSONArray()
        for (i in 0 until payArray.length()) {
            payList.add(WholesalerPayment.fromJsonObject(payArray.getJSONObject(i)))
        }
        _payments.value = payList.sortedByDescending { it.timestamp }

        val warList = mutableListOf<WarrantyItem>()
        val warArray = root.optJSONArray("warranties") ?: JSONArray()
        for (i in 0 until warArray.length()) {
            warList.add(WarrantyItem.fromJsonObject(warArray.getJSONObject(i)))
        }
        _warranties.value = warList.sortedByDescending { it.timestamp }

        val disList = mutableListOf<UniversalDisplayItem>()
        val disArray = root.optJSONArray("displays") ?: JSONArray()
        for (i in 0 until disArray.length()) {
            disList.add(UniversalDisplayItem.fromJsonObject(disArray.getJSONObject(i)))
        }
        _displays.value = disList.sortedByDescending { it.timestamp }
    }

    private fun saveToDisk() {
        scope.launch {
            try {
                val json = exportBackupJson()
                dbFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadInitialDemoData() {
        _repairs.value = listOf(
            RepairItem(
                customerName = "Rahul Sharma",
                customerAddress = "Shop 12, Main Bazaar, Delhi",
                mobileNumber = "9876543210",
                mobileModel = "Redmi Note 8",
                mobileProblem = "Broken Display & Touch not working",
                estimatedCost = 1450.0,
                status = "Pending"
            ),
            RepairItem(
                customerName = "Pooja Verma",
                customerAddress = "Sector 15, Rohini",
                mobileNumber = "9811223344",
                mobileModel = "Samsung M31",
                mobileProblem = "Charging Port & Mic issue",
                estimatedCost = 750.0,
                status = "Done"
            ),
            RepairItem(
                customerName = "Amit Kumar",
                customerAddress = "Krishna Nagar",
                mobileNumber = "9899887766",
                mobileModel = "Realme C11",
                mobileProblem = "Battery replacement",
                estimatedCost = 900.0,
                status = "Delivered"
            )
        )

        _softwareJobs.value = listOf(
            SoftwareJobItem(
                customerName = "Vikas Patel",
                aadhaarNumber = "123456789012",
                mobileNumber = "9712345678",
                address = "Civil Lines, Delhi",
                mobileModel = "Vivo Y20",
                imeiNumber = "863920041234567"
            ),
            SoftwareJobItem(
                customerName = "Suresh Gupta",
                aadhaarNumber = "987654321098",
                mobileNumber = "9822334455",
                address = "Karol Bagh",
                mobileModel = "Oppo A53",
                imeiNumber = "869402058372619"
            )
        )

        _purchases.value = listOf(
            PurchaseItem(
                wholesalerName = "Gupta Telecom",
                itemName = "Redmi Note 8 OG Display (Black)",
                wholesalePrice = 850.0,
                quantity = 5,
                amountPaidNow = 3000.0
            ),
            PurchaseItem(
                wholesalerName = "Gupta Telecom",
                itemName = "Samsung Type-C Fast Data Cables",
                wholesalePrice = 45.0,
                quantity = 20,
                amountPaidNow = 900.0
            ),
            PurchaseItem(
                wholesalerName = "Sharma Accessories",
                itemName = "Realme C11/C12 Incell Display",
                wholesalePrice = 650.0,
                quantity = 4,
                amountPaidNow = 1500.0
            )
        )

        _payments.value = listOf(
            WholesalerPayment(
                wholesalerName = "Gupta Telecom",
                amount = 500.0,
                note = "Cash payment for previous balance"
            )
        )

        _warranties.value = listOf(
            WarrantyItem(
                customerName = "Deepak Singh",
                mobileNumber = "9845123456",
                itemName = "Original Display Replacement",
                warrantyPeriodMonths = 3,
                startDate = getCurrentDateString()
            ),
            WarrantyItem(
                customerName = "Sunil Yadav",
                mobileNumber = "9871122445",
                itemName = "Fast Charging Adapter 25W",
                warrantyPeriodMonths = 6,
                startDate = "01 Jan 2026",
                expiryDate = "01 Jul 2026"
            )
        )

        _displays.value = listOf(
            UniversalDisplayItem(
                displayName = "Universal Display Redmi Note 8",
                qualityType = "Original",
                color = "Black",
                withFrame = true,
                stockQuantity = 4,
                purchasePrice = 850.0,
                wholesalerName = "Gupta Telecom",
                compatibleModels = listOf("Redmi Note 8", "Redmi Note 8T", "Redmi Note 8 (2021)"),
                notes = "Supports high brightness, original touch response"
            ),
            UniversalDisplayItem(
                displayName = "Universal Display A12 / M12 Combo",
                qualityType = "Incell",
                color = "Black",
                withFrame = false,
                stockQuantity = 2, // Low stock <= 2
                purchasePrice = 720.0,
                wholesalerName = "Sharma Accessories",
                compatibleModels = listOf("Samsung Galaxy A12", "Samsung A12 Nacho", "Samsung Galaxy M12", "Samsung Galaxy F12"),
                notes = "Tested 90Hz refresh rate compatible"
            ),
            UniversalDisplayItem(
                displayName = "Realme C-Series Display Combo",
                qualityType = "LCD",
                color = "Black",
                withFrame = false,
                stockQuantity = 0, // Out of stock
                purchasePrice = 650.0,
                wholesalerName = "Sharma Accessories",
                compatibleModels = listOf("Realme C11", "Realme C12", "Realme C15", "Realme Narzo 20A"),
                notes = "Backlight paper included"
            ),
            UniversalDisplayItem(
                displayName = "Vivo Y20 OLED Display",
                qualityType = "OLED",
                color = "Black",
                withFrame = true,
                stockQuantity = 5,
                purchasePrice = 1150.0,
                wholesalerName = "Gupta Telecom",
                compatibleModels = listOf("Vivo Y20", "Vivo Y20i", "Vivo Y20G", "Vivo Y12s", "Vivo Y21"),
                notes = "Vibrant colors, fingerprint cutout aligned"
            )
        )
    }

    // 1. REPAIR CRUD
    fun addRepair(item: RepairItem) {
        _repairs.value = listOf(item) + _repairs.value
        saveToDisk()
    }

    fun updateRepair(item: RepairItem) {
        _repairs.value = _repairs.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    fun deleteRepair(id: String) {
        _repairs.value = _repairs.value.filterNot { it.id == id }
        saveToDisk()
    }

    // 2. SOFTWARE CRUD
    fun addSoftwareJob(item: SoftwareJobItem) {
        _softwareJobs.value = listOf(item) + _softwareJobs.value
        saveToDisk()
    }

    fun updateSoftwareJob(item: SoftwareJobItem) {
        _softwareJobs.value = _softwareJobs.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    fun deleteSoftwareJob(id: String) {
        _softwareJobs.value = _softwareJobs.value.filterNot { it.id == id }
        saveToDisk()
    }

    // 3. PURCHASES & PAYMENTS
    fun addPurchase(item: PurchaseItem) {
        _purchases.value = listOf(item) + _purchases.value
        saveToDisk()
    }

    fun updatePurchase(item: PurchaseItem) {
        _purchases.value = _purchases.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    fun deletePurchase(id: String) {
        _purchases.value = _purchases.value.filterNot { it.id == id }
        saveToDisk()
    }

    fun addWholesalerPayment(payment: WholesalerPayment) {
        _payments.value = listOf(payment) + _payments.value
        saveToDisk()
    }

    fun deleteWholesalerPayment(id: String) {
        _payments.value = _payments.value.filterNot { it.id == id }
        saveToDisk()
    }

    fun getWholesalerSummaries(): List<WholesalerSummary> {
        val allPurchases = _purchases.value
        val allPayments = _payments.value

        val wholesalerNames = (allPurchases.map { it.wholesalerName.trim() } +
                allPayments.map { it.wholesalerName.trim() })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        return wholesalerNames.map { name ->
            val wsPurchases = allPurchases.filter { it.wholesalerName.equals(name, ignoreCase = true) }
            val wsPayments = allPayments.filter { it.wholesalerName.equals(name, ignoreCase = true) }

            val totalPurchase = wsPurchases.sumOf { it.totalAmount }
            val initialPaid = wsPurchases.sumOf { it.amountPaidNow }
            val laterPaid = wsPayments.sumOf { it.amount }
            val totalPaid = initialPaid + laterPaid
            val totalDue = (totalPurchase - totalPaid).coerceAtLeast(0.0)

            WholesalerSummary(
                wholesalerName = name,
                totalPurchase = totalPurchase,
                totalPaid = totalPaid,
                totalDue = totalDue,
                purchaseCount = wsPurchases.size
            )
        }
    }

    fun getTotalDueToAllWholesalers(): Double {
        return getWholesalerSummaries().sumOf { it.totalDue }
    }

    // 4. WARRANTY CRUD
    fun addWarranty(item: WarrantyItem) {
        _warranties.value = listOf(item) + _warranties.value
        saveToDisk()
    }

    fun updateWarranty(item: WarrantyItem) {
        _warranties.value = _warranties.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    fun deleteWarranty(id: String) {
        _warranties.value = _warranties.value.filterNot { it.id == id }
        saveToDisk()
    }

    // 5. UNIVERSAL DISPLAY CRUD & STOCK
    fun addDisplay(item: UniversalDisplayItem) {
        _displays.value = listOf(item) + _displays.value
        saveToDisk()
    }

    fun updateDisplay(item: UniversalDisplayItem) {
        _displays.value = _displays.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    fun deleteDisplay(id: String) {
        _displays.value = _displays.value.filterNot { it.id == id }
        saveToDisk()
    }

    fun useDisplayStock(id: String) {
        _displays.value = _displays.value.map {
            if (it.id == id) {
                val newStock = (it.stockQuantity - 1).coerceAtLeast(0)
                it.copy(stockQuantity = newStock)
            } else it
        }
        saveToDisk()
    }

    fun addDisplayStock(id: String, amount: Int) {
        _displays.value = _displays.value.map {
            if (it.id == id) {
                it.copy(stockQuantity = it.stockQuantity + amount)
            } else it
        }
        saveToDisk()
    }

    // BACKUP & RESTORE
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("appName", "Pooja Mobile Repair")
        root.put("version", 1)
        root.put("exportDate", getCurrentDateString())
        root.put("exportTimestamp", System.currentTimeMillis())

        val repArray = JSONArray()
        _repairs.value.forEach { repArray.put(it.toJsonObject()) }
        root.put("repairs", repArray)

        val softArray = JSONArray()
        _softwareJobs.value.forEach { softArray.put(it.toJsonObject()) }
        root.put("softwareJobs", softArray)

        val purArray = JSONArray()
        _purchases.value.forEach { purArray.put(it.toJsonObject()) }
        root.put("purchases", purArray)

        val payArray = JSONArray()
        _payments.value.forEach { payArray.put(it.toJsonObject()) }
        root.put("payments", payArray)

        val warArray = JSONArray()
        _warranties.value.forEach { warArray.put(it.toJsonObject()) }
        root.put("warranties", warArray)

        val disArray = JSONArray()
        _displays.value.forEach { disArray.put(it.toJsonObject()) }
        root.put("displays", disArray)

        return root.toString(2)
    }

    fun restoreBackupJson(jsonString: String): Result<Unit> {
        return try {
            parseJsonAndPopulate(jsonString)
            saveToDisk()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resetAllData() {
        _repairs.value = emptyList()
        _softwareJobs.value = emptyList()
        _purchases.value = emptyList()
        _payments.value = emptyList()
        _warranties.value = emptyList()
        _displays.value = emptyList()
        saveToDisk()
    }

    companion object {
        @Volatile
        private var instance: ShopRepository? = null

        fun getInstance(context: Context): ShopRepository {
            return instance ?: synchronized(this) {
                instance ?: ShopRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
