package com.example.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    return sdf.format(Date())
}

fun calculateExpiryDateString(startDateStr: String, months: Int): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date = sdf.parse(startDateStr) ?: Date()
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.add(java.util.Calendar.MONTH, months)
        sdf.format(cal.time)
    } catch (e: Exception) {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, months)
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
    }
}

fun isWarrantyActive(expiryDateStr: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val expiry = sdf.parse(expiryDateStr) ?: return false
        val today = Date()
        !today.after(expiry)
    } catch (e: Exception) {
        true
    }
}

// 1. REPAIR
data class RepairItem(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerAddress: String,
    val mobileNumber: String,
    val mobileModel: String,
    val mobileProblem: String,
    val estimatedCost: Double,
    val status: String = "Pending", // Pending, Done, Delivered
    val date: String = getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("customerName", customerName)
        put("customerAddress", customerAddress)
        put("mobileNumber", mobileNumber)
        put("mobileModel", mobileModel)
        put("mobileProblem", mobileProblem)
        put("estimatedCost", estimatedCost)
        put("status", status)
        put("date", date)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): RepairItem = RepairItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            customerName = json.optString("customerName", ""),
            customerAddress = json.optString("customerAddress", ""),
            mobileNumber = json.optString("mobileNumber", ""),
            mobileModel = json.optString("mobileModel", ""),
            mobileProblem = json.optString("mobileProblem", ""),
            estimatedCost = json.optDouble("estimatedCost", 0.0),
            status = json.optString("status", "Pending"),
            date = json.optString("date", getCurrentDateString()),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

// 2. MOBILE SOFTWARE
data class SoftwareJobItem(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val aadhaarNumber: String, // 12 digits
    val mobileNumber: String,
    val address: String,
    val mobileModel: String,
    val imeiNumber: String, // 15 digits
    val date: String = getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val maskedAadhaar: String
        get() = if (aadhaarNumber.length >= 4) {
            "XXXX XXXX " + aadhaarNumber.takeLast(4)
        } else {
            aadhaarNumber
        }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("customerName", customerName)
        put("aadhaarNumber", aadhaarNumber)
        put("mobileNumber", mobileNumber)
        put("address", address)
        put("mobileModel", mobileModel)
        put("imeiNumber", imeiNumber)
        put("date", date)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): SoftwareJobItem = SoftwareJobItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            customerName = json.optString("customerName", ""),
            aadhaarNumber = json.optString("aadhaarNumber", ""),
            mobileNumber = json.optString("mobileNumber", ""),
            address = json.optString("address", ""),
            mobileModel = json.optString("mobileModel", ""),
            imeiNumber = json.optString("imeiNumber", ""),
            date = json.optString("date", getCurrentDateString()),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

// 3. ACCESSORIES PURCHASE & PAYMENTS
data class PurchaseItem(
    val id: String = UUID.randomUUID().toString(),
    val wholesalerName: String,
    val itemName: String,
    val wholesalePrice: Double, // DP
    val quantity: Int,
    val amountPaidNow: Double,
    val date: String = getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalAmount: Double get() = wholesalePrice * quantity
    val dueAmount: Double get() = (totalAmount - amountPaidNow).coerceAtLeast(0.0)

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("wholesalerName", wholesalerName)
        put("itemName", itemName)
        put("wholesalePrice", wholesalePrice)
        put("quantity", quantity)
        put("amountPaidNow", amountPaidNow)
        put("date", date)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): PurchaseItem = PurchaseItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            wholesalerName = json.optString("wholesalerName", ""),
            itemName = json.optString("itemName", ""),
            wholesalePrice = json.optDouble("wholesalePrice", 0.0),
            quantity = json.optInt("quantity", 1),
            amountPaidNow = json.optDouble("amountPaidNow", 0.0),
            date = json.optString("date", getCurrentDateString()),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

data class WholesalerPayment(
    val id: String = UUID.randomUUID().toString(),
    val wholesalerName: String,
    val amount: Double,
    val note: String = "",
    val date: String = getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("wholesalerName", wholesalerName)
        put("amount", amount)
        put("note", note)
        put("date", date)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): WholesalerPayment = WholesalerPayment(
            id = json.optString("id", UUID.randomUUID().toString()),
            wholesalerName = json.optString("wholesalerName", ""),
            amount = json.optDouble("amount", 0.0),
            note = json.optString("note", ""),
            date = json.optString("date", getCurrentDateString()),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

data class WholesalerSummary(
    val wholesalerName: String,
    val totalPurchase: Double,
    val totalPaid: Double,
    val totalDue: Double,
    val purchaseCount: Int
)

// 4. WARRANTY
data class WarrantyItem(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val mobileNumber: String,
    val itemName: String,
    val warrantyPeriodMonths: Int, // 1, 3, 6, 12
    val startDate: String = getCurrentDateString(),
    val expiryDate: String = calculateExpiryDateString(startDate, warrantyPeriodMonths),
    val timestamp: Long = System.currentTimeMillis()
) {
    val isActive: Boolean get() = isWarrantyActive(expiryDate)

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("customerName", customerName)
        put("mobileNumber", mobileNumber)
        put("itemName", itemName)
        put("warrantyPeriodMonths", warrantyPeriodMonths)
        put("startDate", startDate)
        put("expiryDate", expiryDate)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): WarrantyItem {
            val start = json.optString("startDate", getCurrentDateString())
            val period = json.optInt("warrantyPeriodMonths", 3)
            val expiry = json.optString("expiryDate", calculateExpiryDateString(start, period))
            return WarrantyItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                customerName = json.optString("customerName", ""),
                mobileNumber = json.optString("mobileNumber", ""),
                itemName = json.optString("itemName", ""),
                warrantyPeriodMonths = period,
                startDate = start,
                expiryDate = expiry,
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

// 5. UNIVERSAL DISPLAY
data class UniversalDisplayItem(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String, // e.g. "Universal Display A12"
    val qualityType: String, // Original, OLED, Incell, LCD, Copy
    val color: String, // Black, White, Other
    val withFrame: Boolean,
    val stockQuantity: Int,
    val purchasePrice: Double? = null,
    val wholesalerName: String? = null,
    val compatibleModels: List<String> = emptyList(),
    val notes: String? = null,
    val date: String = getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val isOutOfStock: Boolean get() = stockQuantity <= 0
    val isLowStock: Boolean get() = stockQuantity in 1..2
    val isInStock: Boolean get() = stockQuantity > 0

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("displayName", displayName)
        put("qualityType", qualityType)
        put("color", color)
        put("withFrame", withFrame)
        put("stockQuantity", stockQuantity)
        if (purchasePrice != null) put("purchasePrice", purchasePrice)
        if (!wholesalerName.isNullOrBlank()) put("wholesalerName", wholesalerName)
        val modelsArray = JSONArray()
        compatibleModels.forEach { modelsArray.put(it) }
        put("compatibleModels", modelsArray)
        if (!notes.isNullOrBlank()) put("notes", notes)
        put("date", date)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): UniversalDisplayItem {
            val modelsList = mutableListOf<String>()
            val modelsArray = json.optJSONArray("compatibleModels")
            if (modelsArray != null) {
                for (i in 0 until modelsArray.length()) {
                    modelsList.add(modelsArray.getString(i))
                }
            }
            return UniversalDisplayItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                displayName = json.optString("displayName", ""),
                qualityType = json.optString("qualityType", "LCD"),
                color = json.optString("color", "Black"),
                withFrame = json.optBoolean("withFrame", false),
                stockQuantity = json.optInt("stockQuantity", 0),
                purchasePrice = if (json.has("purchasePrice")) json.optDouble("purchasePrice") else null,
                wholesalerName = if (json.has("wholesalerName")) json.optString("wholesalerName") else null,
                compatibleModels = modelsList,
                notes = if (json.has("notes")) json.optString("notes") else null,
                date = json.optString("date", getCurrentDateString()),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}
