package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Phone
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
import com.example.model.RepairItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repairs by viewModel.filteredRepairs.collectAsState()
    val searchQuery by viewModel.repairSearchQuery.collectAsState()
    val statusFilter by viewModel.repairStatusFilter.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<RepairItem?>(null) }
    var itemToDelete by remember { mutableStateOf<RepairItem?>(null) }

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
                    Icon(Icons.Default.Add, contentDescription = "Add Repair")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Repair", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                        text = "Mobile Repairs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SearchBarComponent(
                        query = searchQuery,
                        onQueryChange = { viewModel.repairSearchQuery.value = it },
                        placeholder = "Search customer name, mobile or model..."
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filter Tabs
                    val filters = listOf("All", "Pending", "Done", "Delivered")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSelected = statusFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.repairStatusFilter.value = filter },
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

            // Repairs List
            if (repairs.isEmpty()) {
                EmptyStateView(
                    title = "No repair jobs found",
                    message = if (searchQuery.isNotEmpty()) "Try searching for a different keyword." else "Tap '+ New Repair' to add a customer job sheet.",
                    icon = Icons.Outlined.Build,
                    actionButtonText = if (searchQuery.isEmpty()) "+ Add First Repair" else null,
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
                    items(repairs, key = { it.id }) { item ->
                        RepairCard(
                            item = item,
                            onEdit = {
                                itemToEdit = item
                                showFormDialog = true
                            },
                            onDelete = {
                                itemToDelete = item
                            },
                            onCall = {
                                dialPhoneNumber(context, item.mobileNumber)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showFormDialog) {
        RepairFormDialog(
            initialItem = itemToEdit,
            onDismiss = {
                showFormDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                if (itemToEdit == null) {
                    viewModel.addRepair(savedItem)
                } else {
                    viewModel.updateRepair(savedItem)
                }
                showFormDialog = false
                itemToEdit = null
            },
            onCheckDisplay = { modelName ->
                showFormDialog = false
                itemToEdit = null
                viewModel.checkDisplayForModel(modelName)
            }
        )
    }

    // Delete Confirmation Dialog
    ConfirmDeleteDialog(
        show = itemToDelete != null,
        title = "Delete Repair Job?",
        message = "Are you sure you want to delete repair entry for ${itemToDelete?.customerName} (${itemToDelete?.mobileModel})?",
        onConfirm = {
            itemToDelete?.let { viewModel.deleteRepair(it.id) }
            itemToDelete = null
        },
        onDismiss = { itemToDelete = null }
    )
}

@Composable
private fun RepairCard(
    item: RepairItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    val (statusColor, statusBg) = when (item.status) {
        "Done" -> Pair(BluePrimary, BlueContainer)
        "Delivered" -> Pair(StatusGreen, StatusGreenBg)
        else -> Pair(StatusAmber, StatusAmberBg)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Model & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.mobileModel,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Job Date: ${item.date}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                StatusBadge(
                    text = item.status,
                    color = statusColor,
                    backgroundColor = statusBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Mobile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.mobileNumber,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                // Estimated Cost
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Estimated Cost",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = formatRupees(item.estimatedCost),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimary
                    )
                }
            }

            // Problem Description
            if (item.mobileProblem.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Issue: ${item.mobileProblem}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Customer Address
            if (item.customerAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${item.customerAddress}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Call, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Button
                FilledTonalButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StatusGreenBg,
                        contentColor = StatusGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepairFormDialog(
    initialItem: RepairItem?,
    onDismiss: () -> Unit,
    onSave: (RepairItem) -> Unit,
    onCheckDisplay: (String) -> Unit
) {
    var customerName by remember { mutableStateOf(initialItem?.customerName ?: "") }
    var address by remember { mutableStateOf(initialItem?.customerAddress ?: "") }
    var mobileNumber by remember { mutableStateOf(initialItem?.mobileNumber ?: "") }
    var mobileModel by remember { mutableStateOf(initialItem?.mobileModel ?: "") }
    var mobileProblem by remember { mutableStateOf(initialItem?.mobileProblem ?: "") }
    var estimatedCostStr by remember {
        mutableStateOf(if (initialItem != null) initialItem.estimatedCost.toInt().toString() else "")
    }
    var status by remember { mutableStateOf(initialItem?.status ?: "Pending") }

    var statusExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "New Repair Job" else "Edit Repair Job",
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

                // Mobile Model + "Check Display" Button!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mobileModel,
                        onValueChange = { mobileModel = it },
                        label = { Text("Mobile Model *") },
                        placeholder = { Text("e.g. Redmi Note 8") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (mobileModel.isNotBlank()) {
                                onCheckDisplay(mobileModel)
                            }
                        },
                        enabled = mobileModel.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Smartphone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Check\nDisplay",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }

                // Problem
                OutlinedTextField(
                    value = mobileProblem,
                    onValueChange = { mobileProblem = it },
                    label = { Text("Mobile Problem *") },
                    placeholder = { Text("e.g. Broken display, No sound") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Estimated Cost & Status in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = estimatedCostStr,
                        onValueChange = { estimatedCostStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Status Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = {
                                IconButton(onClick = { statusExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf("Pending", "Done", "Delivered").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        status = s
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

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
                    if (mobileModel.isBlank()) {
                        validationError = "Please enter mobile model"
                        return@Button
                    }
                    val cost = estimatedCostStr.toDoubleOrNull() ?: 0.0

                    val item = initialItem?.copy(
                        customerName = customerName.trim(),
                        customerAddress = address.trim(),
                        mobileNumber = mobileNumber.trim(),
                        mobileModel = mobileModel.trim(),
                        mobileProblem = mobileProblem.trim(),
                        estimatedCost = cost,
                        status = status
                    ) ?: RepairItem(
                        customerName = customerName.trim(),
                        customerAddress = address.trim(),
                        mobileNumber = mobileNumber.trim(),
                        mobileModel = mobileModel.trim(),
                        mobileProblem = mobileProblem.trim(),
                        estimatedCost = cost,
                        status = status
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialItem == null) "Save Repair" else "Update", fontWeight = FontWeight.Bold)
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

fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.trim()}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer for $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}
