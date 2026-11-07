package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WarrantyItem
import com.example.model.calculateExpiryDateString
import com.example.model.getCurrentDateString
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val warranties by viewModel.filteredWarranties.collectAsState()
    val searchQuery by viewModel.warrantySearchQuery.collectAsState()
    val filterStatus by viewModel.warrantyFilter.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<WarrantyItem?>(null) }
    var itemToDelete by remember { mutableStateOf<WarrantyItem?>(null) }

    // Intercept back to home
    BackHandler {
        viewModel.selectTab(ShopTab.HOME)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showFormDialog = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Warranty")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Issue Warranty", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                    Text(
                        text = "Warranty Records",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SearchBarComponent(
                        query = searchQuery,
                        onQueryChange = { viewModel.warrantySearchQuery.value = it },
                        placeholder = "Search customer, mobile, or item..."
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips (All, Active, Expired)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Active", "Expired").forEach { filter ->
                            val isSelected = filterStatus == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.warrantyFilter.value = filter },
                                label = {
                                    Text(
                                        text = filter,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceVariantLight,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // List
            if (warranties.isEmpty()) {
                EmptyStateView(
                    title = "No warranty cards found",
                    message = if (searchQuery.isNotEmpty()) "No entries match '$searchQuery'." else "Tap '+ Issue Warranty' to create a customer guarantee card.",
                    icon = Icons.Outlined.VerifiedUser,
                    actionButtonText = if (searchQuery.isEmpty()) "+ Issue First Warranty" else null,
                    onActionClick = {
                        itemToEdit = null
                        showFormDialog = true
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
                    items(warranties, key = { it.id }) { item ->
                        WarrantyCard(
                            item = item,
                            onEdit = {
                                itemToEdit = item
                                showFormDialog = true
                            },
                            onDelete = { itemToDelete = item },
                            onCall = { dialPhoneNumber(context, item.mobileNumber) }
                        )
                    }
                }
            }
        }
    }

    // Form Dialog
    if (showFormDialog) {
        WarrantyFormDialog(
            initialItem = itemToEdit,
            onDismiss = {
                showFormDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                if (itemToEdit == null) {
                    viewModel.addWarranty(savedItem)
                } else {
                    viewModel.updateWarranty(savedItem)
                }
                showFormDialog = false
                itemToEdit = null
            }
        )
    }

    // Delete Confirmation
    ConfirmDeleteDialog(
        show = itemToDelete != null,
        title = "Delete Warranty Card?",
        message = "Are you sure you want to delete warranty for ${itemToDelete?.customerName} (${itemToDelete?.itemName})?",
        onConfirm = {
            itemToDelete?.let { viewModel.deleteWarranty(it.id) }
            itemToDelete = null
        },
        onDismiss = { itemToDelete = null }
    )
}

@Composable
private fun WarrantyCard(
    item: WarrantyItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    val isActive = item.isActive

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Item Name & Active/Expired Badge
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
                        text = "Customer: ${item.customerName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )
                }

                // Active (green) or Expired (red) badge!
                StatusBadge(
                    text = if (isActive) "Active" else "Expired",
                    color = if (isActive) StatusGreen else StatusRed,
                    backgroundColor = if (isActive) StatusGreenBg else StatusRedBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Warranty Dates & Period
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Period", fontSize = 11.sp, color = TextMuted)
                    Text(
                        "${item.warrantyPeriodMonths} Months",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Start Date", fontSize = 11.sp, color = TextMuted)
                    Text(
                        item.startDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Expiry Date", fontSize = 11.sp, color = TextMuted)
                    Text(
                        item.expiryDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) TextPrimary else StatusRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mobile: ${item.mobileNumber}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Call Button
                    FilledTonalButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StatusGreenBg,
                            contentColor = StatusGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Phone, contentDescription = "Call", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

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
}

@Composable
private fun WarrantyFormDialog(
    initialItem: WarrantyItem?,
    onDismiss: () -> Unit,
    onSave: (WarrantyItem) -> Unit
) {
    var customerName by remember { mutableStateOf(initialItem?.customerName ?: "") }
    var mobileNumber by remember { mutableStateOf(initialItem?.mobileNumber ?: "") }
    var itemName by remember { mutableStateOf(initialItem?.itemName ?: "") }
    var warrantyPeriodMonths by remember { mutableStateOf(initialItem?.warrantyPeriodMonths ?: 3) }
    var startDate by remember { mutableStateOf(initialItem?.startDate ?: getCurrentDateString()) }

    var periodExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Auto calculate Expiry Date!
    val calculatedExpiryDate = remember(startDate, warrantyPeriodMonths) {
        calculateExpiryDateString(startDate, warrantyPeriodMonths)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Issue Customer Warranty" else "Edit Warranty Card",
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
                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Mobile Number
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 15) mobileNumber = it },
                    label = { Text("Mobile Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Original Display, Battery, Fast Charger") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Warranty Period Dropdown (1 / 3 / 6 / 12 months)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "$warrantyPeriodMonths Months",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Warranty Period *") },
                        trailingIcon = {
                            IconButton(onClick = { periodExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { periodExpanded = true },
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        listOf(1, 3, 6, 12).forEach { m ->
                            DropdownMenuItem(
                                text = { Text("$m Months") },
                                onClick = {
                                    warrantyPeriodMonths = m
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start Date & Auto-calculated Expiry Box
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
                            Text("Start Date (Today)", fontSize = 11.sp, color = TextMuted)
                            Text(startDate, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Auto Expiry Date", fontSize = 11.sp, color = TextMuted)
                            Text(
                                calculatedExpiryDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
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
                    if (customerName.isBlank()) {
                        validationError = "Please enter customer name"
                        return@Button
                    }
                    if (mobileNumber.isBlank()) {
                        validationError = "Please enter mobile number"
                        return@Button
                    }
                    if (itemName.isBlank()) {
                        validationError = "Please enter item name"
                        return@Button
                    }

                    val item = initialItem?.copy(
                        customerName = customerName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        itemName = itemName.trim(),
                        warrantyPeriodMonths = warrantyPeriodMonths,
                        startDate = startDate,
                        expiryDate = calculatedExpiryDate
                    ) ?: WarrantyItem(
                        customerName = customerName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        itemName = itemName.trim(),
                        warrantyPeriodMonths = warrantyPeriodMonths,
                        startDate = startDate,
                        expiryDate = calculatedExpiryDate
                    )

                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialItem == null) "Issue Warranty" else "Update", fontWeight = FontWeight.Bold)
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
