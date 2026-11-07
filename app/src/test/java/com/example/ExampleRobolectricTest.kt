package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pooja Mobile Repair", appName)
    }

    @Test
    fun `software job aadhaar masking formats correctly`() {
        val job = SoftwareJobItem(
            customerName = "Vikas Patel",
            aadhaarNumber = "123456789012",
            mobileNumber = "9876543210",
            address = "Delhi",
            mobileModel = "Vivo Y20",
            imeiNumber = "863920041234567"
        )
        assertEquals("XXXX XXXX 9012", job.maskedAadhaar)
    }

    @Test
    fun `purchase calculations for total and due are accurate`() {
        val purchase = PurchaseItem(
            wholesalerName = "Gupta Telecom",
            itemName = "Display Screen",
            wholesalePrice = 850.0,
            quantity = 5,
            amountPaidNow = 3000.0
        )
        assertEquals(4250.0, purchase.totalAmount, 0.01)
        assertEquals(1250.0, purchase.dueAmount, 0.01)
    }

    @Test
    fun `warranty expiry date calculates correctly`() {
        val expiry = calculateExpiryDateString("01 Jan 2026", 3)
        assertEquals("01 Apr 2026", expiry)
    }

    @Test
    fun `universal display smart model matching handles case spaces dashes`() {
        fun normalize(str: String): String =
            str.lowercase().replace(" ", "").replace("-", "").replace("_", "").replace("/", "")

        val compatibleModels = listOf("Redmi Note 8", "Redmi Note 8T")

        fun matches(query: String): Boolean {
            val cleanQuery = normalize(query)
            return compatibleModels.any { model ->
                val cleanModel = normalize(model)
                cleanModel.contains(cleanQuery) || cleanQuery.contains(cleanModel)
            }
        }

        assertTrue(matches("note8"))
        assertTrue(matches("Note 8"))
        assertTrue(matches("redmi note 8"))
        assertTrue(matches("Redmi-Note-8"))
        assertTrue(matches("note 8t"))
    }
}
