package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PurchaseItem
import com.example.model.WholesalerPayment
import com.example.model.WholesalerSummary
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessoriesScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.accessoriesActiveTab.collectAsState()
    val wholesalers by viewModel.wholesalerSummaries.collectAsState()
    val purchases by viewModel.filteredPurchases.collectAsState()
    val allPurchasesRaw by viewModel.purchases.collectAsState()
    val allPaymentsRaw by viewModel.payments.collectAsState()
    val searchQuery by viewModel.purchaseSearchQuery.collectAsState()
    val selectedWholesalerName by viewModel.selectedWholesaler.collectAsState()

    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToEdit by remember { mutableStateOf<PurchaseItem?>(null) }
    var purchaseToDelete by remember { mutableStateOf<PurchaseItem?>(null) }
    var showAddPaymentDialogForWholesaler by remember { mutableStateOf<String?>(null) }

    // Back handling: if in Wholesaler Detail, back to Wholesaler list; else back to Home
    BackHandler {
        if (selectedWholesalerName != null) {
            viewModel.selectedWholesaler.value = null
        } else {
            viewModel.selectTab(ShopTab.HOME)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        floatingActionButton = {
            if (selectedWholesalerName == null) {
                FloatingActionButton(
                    onClick = {
                        purchaseToEdit = null
                        showAddPurchaseDialog = true
                    },
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Purchase")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Purchase", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (selectedWholesalerName != null) {
                        // Wholesaler Detail Navigation Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { viewModel.selectedWholesaler.value = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = selectedWholesalerName ?: "",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Wholesaler Ledger & Payment History",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        // Main Tabs Header
                        Text(
                            text = "Accessories & Wholesalers",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        TabRow(
                            selectedTabIndex = activeTab,
                            containerColor = Color.White,
                            contentColor = BluePrimary
                        ) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = { viewModel.accessoriesActiveTab.value = 0 },
                                text = {
                                    Text(
                                        text = "Wholesalers (${wholesalers.size})",
                                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { viewModel.accessoriesActiveTab.value = 1 },
                                text = {
                                    Text(
                                        text = "All Purchases (${allPurchasesRaw.size})",
                                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }

                        if (activeTab == 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            SearchBarComponent(
                                query = searchQuery,
                                onQueryChange = { viewModel.purchaseSearchQuery.value = it },
                                placeholder = "Search item name or wholesaler..."
                            )
                        }
                    }
                }
            }

            // Body Content
            if (selectedWholesalerName != null) {
                // Wholesaler Detail Ledger Screen
                val currentWholesaler = selectedWholesalerName ?: ""
                val wsSummary = wholesalers.find { it.wholesalerName.equals(currentWholesaler, ignoreCase = true) }
                val wsPurchases = allPurchasesRaw.filter { it.wholesalerName.equals(currentWholesaler, ignoreCase = true) }
                val wsPayments = allPaymentsRaw.filter { it.wholesalerName.equals(currentWholesaler, ignoreCase = true) }

                WholesalerDetailLedger(
                    summary = wsSummary,
                    purchases = wsPurchases,
                    payments = wsPayments,
                    onAddPayment = { showAddPaymentDialogForWholesaler = currentWholesaler },
                    onDeletePayment = { paymentId -> viewModel.deleteWholesalerPayment(paymentId) },
                    onEditPurchase = { p ->
                        purchaseToEdit = p
                        showAddPurchaseDialog = true
                    },
                    onDeletePurchase = { p -> purchaseToDelete = p }
                )
            } else {
                // Sub-tabs: 0 = Wholesalers List, 1 = Purchases List
                if (activeTab == 0) {
                    // Wholesalers Tab
                    if (wholesalers.isEmpty()) {
                        EmptyStateView(
                            title = "No wholesalers yet",
                            message = "Add a purchase to automatically create and track wholesaler balances.",
                            icon = Icons.Outlined.Storefront,
                            actionButtonText = "+ Add First Purchase",
                            onActionClick = {
                                purchaseToEdit = null
                                showAddPurchaseDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(wholesalers, key = { it.wholesalerName }) { ws ->
                                WholesalerCard(
                                    summary = ws,
                                    onClick = { viewModel.selectedWholesaler.value = ws.wholesalerName },
                                    onAddPayment = { showAddPaymentDialogForWholesaler = ws.wholesalerName }
                                )
                            }
                        }
                    }
                } else {
                    // All Purchases Tab
                    if (purchases.isEmpty()) {
                        EmptyStateView(
                            title = "No purchase entries found",
                            message = if (searchQuery.isNotEmpty()) "No purchases match '$searchQuery'." else "Tap '+ Add Purchase' to record a wholesale buy.",
                            icon = Icons.Outlined.ShoppingBag,
                            actionButtonText = if (searchQuery.isEmpty()) "+ Add Purchase" else null,
                            onActionClick = {
                                purchaseToEdit = null
                                showAddPurchaseDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(purchases, key = { it.id }) { item ->
                                PurchaseCard(
                                    item = item,
                                    onEdit = {
                                        purchaseToEdit = item
                                        showAddPurchaseDialog = true
                                    },
                                    onDelete = { purchaseToDelete = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Purchase Dialog
    if (showAddPurchaseDialog) {
        PurchaseFormDialog(
            initialItem = purchaseToEdit,
            prefilledWholesaler = selectedWholesalerName,
            onDismiss = {
                showAddPurchaseDialog = false
                purchaseToEdit = null
            },
            onSave = { savedItem ->
                if (purchaseToEdit == null) {
                    viewModel.addPurchase(savedItem)
                } else {
                    viewModel.updatePurchase(savedItem)
                }
                showAddPurchaseDialog = false
                purchaseToEdit = null
            }
        )
    }

    // Add Payment Dialog for Wholesaler
    showAddPaymentDialogForWholesaler?.let { wsName ->
        AddPaymentDialog(
            wholesalerName = wsName,
            onDismiss = { showAddPaymentDialogForWholesaler = null },
            onSavePayment = { amount, note ->
                viewModel.addWholesalerPayment(wsName, amount, note)
                showAddPaymentDialogForWholesaler = null
            }
        )
    }

    // Delete Confirmation
    ConfirmDeleteDialog(
        show = purchaseToDelete != null,
        title = "Delete Purchase Entry?",
        message = "Are you sure you want to delete purchase '${purchaseToDelete?.itemName}' from ${purchaseToDelete?.wholesalerName}?",
        onConfirm = {
            purchaseToDelete?.let { viewModel.deletePurchase(it.id) }
            purchaseToDelete = null
        },
        onDismiss = { purchaseToDelete = null }
    )
}

@Composable
private fun WholesalerCard(
    summary: WholesalerSummary,
    onClick: () -> Unit,
    onAddPayment: () -> Unit
) {
    val isDue = summary.totalDue > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Wholesaler Name & Due Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDue) StatusRedBg else StatusGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = if (isDue) StatusRed else StatusGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = summary.wholesalerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${summary.purchaseCount} purchases",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Cleared in Green, Due in Red!
                StatusBadge(
                    text = if (isDue) "Due: ${formatRupees(summary.totalDue)}" else "Cleared",
                    color = if (isDue) StatusRed else StatusGreen,
                    backgroundColor = if (isDue) StatusRedBg else StatusGreenBg
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // 3 Values Row: Total Purchase, Total Paid, Total Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Purchase", fontSize = 11.sp, color = TextMuted)
                    Text(
                        formatRupees(summary.totalPurchase),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Paid", fontSize = 11.sp, color = TextMuted)
                    Text(
                        formatRupees(summary.totalPaid),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Due", fontSize = 11.sp, color = TextMuted)
                    Text(
                        formatRupees(summary.totalDue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDue) StatusRed else StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Add Payment button & Tap to View Ledger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to view full ledger →",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = onAddPayment,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PurchaseCard(
    item: PurchaseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDue = item.dueAmount > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Wholesaler: ${item.wholesalerName} • ${item.date}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                StatusBadge(
                    text = if (isDue) "Due: ${formatRupees(item.dueAmount)}" else "Paid",
                    color = if (isDue) StatusRed else StatusGreen,
                    backgroundColor = if (isDue) StatusRedBg else StatusGreenBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Quantity, Wholesale Price (DP), Total & Paid Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Qty & DP", fontSize = 11.sp, color = TextMuted)
                    Text(
                        "${item.quantity} pcs @ ${formatRupees(item.wholesalePrice)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Amount", fontSize = 11.sp, color = TextMuted)
                    Text(
                        formatRupees(item.totalAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Paid on Purchase", fontSize = 11.sp, color = TextMuted)
                    Text(
                        formatRupees(item.amountPaidNow),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = StatusRed
                    )
                }
            }
        }
    }
}

@Composable
private fun WholesalerDetailLedger(
    summary: WholesalerSummary?,
    purchases: List<PurchaseItem>,
    payments: List<WholesalerPayment>,
    onAddPayment: () -> Unit,
    onDeletePayment: (String) -> Unit,
    onEditPurchase: (PurchaseItem) -> Unit,
    onDeletePurchase: (PurchaseItem) -> Unit
) {
    val totalPurchase = summary?.totalPurchase ?: 0.0
    val totalPaid = summary?.totalPaid ?: 0.0
    val totalDue = summary?.totalDue ?: 0.0
    val isDue = totalDue > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card at top
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Purchase", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                formatRupees(totalPurchase),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                formatRupees(totalPaid),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Balance Due", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                formatRupees(totalDue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDue) StatusRed else StatusGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big "Add Payment" Button
                    Button(
                        onClick = onAddPayment,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Payment to Wholesaler", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Section: Payments History
        item {
            Text(
                text = "Payments Given (${payments.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (payments.isEmpty()) {
            item {
                Text(
                    text = "No additional payments logged yet.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            items(payments, key = { it.id }) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Paid: ${formatRupees(pay.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                            Text(
                                text = "Date: ${pay.date}" + if (pay.note.isNotBlank()) " • ${pay.note}" else "",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeletePayment(pay.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRed)
                        }
                    }
                }
            }
        }

        // Section: Purchase Invoices History
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Purchase Invoices (${purchases.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(purchases, key = { it.id }) { item ->
            PurchaseCard(
                item = item,
                onEdit = { onEditPurchase(item) },
                onDelete = { onDeletePurchase(item) }
            )
        }
    }
}

@Composable
private fun PurchaseFormDialog(
    initialItem: PurchaseItem?,
    prefilledWholesaler: String?,
    onDismiss: () -> Unit,
    onSave: (PurchaseItem) -> Unit
) {
    var wholesalerName by remember {
        mutableStateOf(initialItem?.wholesalerName ?: prefilledWholesaler ?: "")
    }
    var itemName by remember { mutableStateOf(initialItem?.itemName ?: "") }
    var wholesalePriceStr by remember {
        mutableStateOf(if (initialItem != null) initialItem.wholesalePrice.toInt().toString() else "")
    }
    var quantityStr by remember {
        mutableStateOf(if (initialItem != null) initialItem.quantity.toString() else "1")
    }
    var amountPaidNowStr by remember {
        mutableStateOf(if (initialItem != null) initialItem.amountPaidNow.toInt().toString() else "0")
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Auto calculate Total = DP x Quantity and Due = Total - Paid
    val dp = wholesalePriceStr.toDoubleOrNull() ?: 0.0
    val qty = quantityStr.toIntOrNull() ?: 1
    val calculatedTotal = dp * qty
    val paidNow = amountPaidNowStr.toDoubleOrNull() ?: 0.0
    val calculatedDue = (calculatedTotal - paidNow).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Add Wholesaler Purchase" else "Edit Purchase",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wholesaler Name
                OutlinedTextField(
                    value = wholesalerName,
                    onValueChange = { wholesalerName = it },
                    label = { Text("Wholesaler Name *") },
                    placeholder = { Text("e.g. Gupta Telecom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Redmi Note 8 Display / USB Cables") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // DP (Wholesale Price) & Quantity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wholesalePriceStr,
                        onValueChange = { wholesalePriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("DP / Unit (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Amount Paid Now
                OutlinedTextField(
                    value = amountPaidNowStr,
                    onValueChange = { amountPaidNowStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount Paid Now (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Real-time calculation summary box: Total = DP x Qty, Due = Total - Paid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantLight)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total (DP × Qty)", fontSize = 11.sp, color = TextMuted)
                            Text(
                                formatRupees(calculatedTotal),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                formatRupees(calculatedDue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (calculatedDue > 0) StatusRed else StatusGreen
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = StatusRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (wholesalerName.isBlank()) {
                        validationError = "Please enter wholesaler name"
                        return@Button
                    }
                    if (itemName.isBlank()) {
                        validationError = "Please enter item name"
                        return@Button
                    }
                    if (dp <= 0) {
                        validationError = "Please enter valid wholesale price (DP)"
                        return@Button
                    }
                    if (qty <= 0) {
                        validationError = "Please enter valid quantity"
                        return@Button
                    }

                    val item = initialItem?.copy(
                        wholesalerName = wholesalerName.trim(),
                        itemName = itemName.trim(),
                        wholesalePrice = dp,
                        quantity = qty,
                        amountPaidNow = paidNow
                    ) ?: PurchaseItem(
                        wholesalerName = wholesalerName.trim(),
                        itemName = itemName.trim(),
                        wholesalePrice = dp,
                        quantity = qty,
                        amountPaidNow = paidNow
                    )

                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialItem == null) "Save Purchase" else "Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun AddPaymentDialog(
    wholesalerName: String,
    onDismiss: () -> Unit,
    onSavePayment: (Double, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Payment to $wholesalerName",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "This payment will reduce the total balance due to this wholesaler.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Payment Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Mode (Optional)") },
                    placeholder = { Text("e.g. Cash, UPI, Cheque") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = StatusRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        errorMsg = "Please enter a valid payment amount"
                        return@Button
                    }
                    onSavePayment(amount, note.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
