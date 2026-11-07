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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.PhonelinkSetup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoftwareJobItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@Composable
fun SoftwareScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.filteredSoftwareJobs.collectAsState()
    val searchQuery by viewModel.softwareSearchQuery.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<SoftwareJobItem?>(null) }
    var itemToDelete by remember { mutableStateOf<SoftwareJobItem?>(null) }
    var itemDetailToView by remember { mutableStateOf<SoftwareJobItem?>(null) }

    // Intercept back button to return to Home
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
                    Icon(Icons.Default.Add, contentDescription = "Add Software Entry")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Software Job", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with Search Bar
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Mobile Software Logs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Security records with 12-digit Aadhaar & 15-digit IMEI verification",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SearchBarComponent(
                        query = searchQuery,
                        onQueryChange = { viewModel.softwareSearchQuery.value = it },
                        placeholder = "Search customer, mobile, IMEI or model..."
                    )
                }
            }

            // Software List
            if (jobs.isEmpty()) {
                EmptyStateView(
                    title = "No software jobs logged",
                    message = if (searchQuery.isNotEmpty()) "No entries match '$searchQuery'." else "Tap '+ New Software Job' to log a verified flashing or unlock job.",
                    icon = Icons.Outlined.PhonelinkSetup,
                    actionButtonText = if (searchQuery.isEmpty()) "+ Log First Software Job" else null,
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
                    items(jobs, key = { it.id }) { item ->
                        SoftwareJobCard(
                            item = item,
                            onClick = { itemDetailToView = item },
                            onEdit = {
                                itemToEdit = item
                                showFormDialog = true
                            },
                            onDelete = {
                                itemToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Screen (Shows Full Aadhaar Number as required)
    itemDetailToView?.let { detailItem ->
        SoftwareDetailDialog(
            item = detailItem,
            onDismiss = { itemDetailToView = null },
            onEdit = {
                itemDetailToView = null
                itemToEdit = detailItem
                showFormDialog = true
            }
        )
    }

    // Form Dialog
    if (showFormDialog) {
        SoftwareFormDialog(
            initialItem = itemToEdit,
            onDismiss = {
                showFormDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                if (itemToEdit == null) {
                    viewModel.addSoftwareJob(savedItem)
                } else {
                    viewModel.updateSoftwareJob(savedItem)
                }
                showFormDialog = false
                itemToEdit = null
            }
        )
    }

    // Delete Confirmation
    ConfirmDeleteDialog(
        show = itemToDelete != null,
        title = "Delete Software Record?",
        message = "Are you sure you want to delete software record for ${itemToDelete?.customerName} (${itemToDelete?.mobileModel})?",
        onConfirm = {
            itemToDelete?.let { viewModel.deleteSoftwareJob(it.id) }
            itemToDelete = null
        },
        onDismiss = { itemToDelete = null }
    )
}

@Composable
private fun SoftwareJobCard(
    item: SoftwareJobItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Customer & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Model: ${item.mobileModel}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )
                }
                Text(
                    text = item.date,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Masked Aadhaar Box (XXXX XXXX 1234)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantLight)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aadhaar: ",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = item.maskedAadhaar,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap to view",
                    fontSize = 10.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // IMEI Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IMEI: ${item.imeiNumber}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Text(
                    text = "Mobile: ${item.mobileNumber}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
private fun SoftwareDetailDialog(
    item: SoftwareJobItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Software Job Details",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailItemRow(label = "Customer Name", value = item.customerName)
                DetailItemRow(label = "Mobile Number", value = item.mobileNumber)
                DetailItemRow(label = "Mobile Model", value = item.mobileModel)

                // Full Aadhaar Number (Validated 12 digits, explicitly shown here!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlueContainer)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Full Aadhaar Number (12 Digits)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBlueContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.aadhaarNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = BluePrimary
                        )
                    }
                }

                // Full IMEI Number
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantLight)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Device IMEI (15 Digits)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.imeiNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }

                if (item.address.isNotBlank()) {
                    DetailItemRow(label = "Customer Address", value = item.address)
                }
                DetailItemRow(label = "Date Logged", value = item.date)
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Edit Record", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun SoftwareFormDialog(
    initialItem: SoftwareJobItem?,
    onDismiss: () -> Unit,
    onSave: (SoftwareJobItem) -> Unit
) {
    var customerName by remember { mutableStateOf(initialItem?.customerName ?: "") }
    var aadhaarNumber by remember { mutableStateOf(initialItem?.aadhaarNumber ?: "") }
    var mobileNumber by remember { mutableStateOf(initialItem?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(initialItem?.address ?: "") }
    var mobileModel by remember { mutableStateOf(initialItem?.mobileModel ?: "") }
    var imeiNumber by remember { mutableStateOf(initialItem?.imeiNumber ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Log Mobile Software" else "Edit Software Log",
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

                // Aadhaar Number (12 digits validated)
                OutlinedTextField(
                    value = aadhaarNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 12) aadhaarNumber = digits
                    },
                    label = { Text("Aadhaar Number (12 digits) *") },
                    supportingText = {
                        Text(
                            text = "${aadhaarNumber.length}/12 digits",
                            color = if (aadhaarNumber.length == 12) StatusGreen else TextMuted
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Mobile Model
                OutlinedTextField(
                    value = mobileModel,
                    onValueChange = { mobileModel = it },
                    label = { Text("Mobile Model *") },
                    placeholder = { Text("e.g. Vivo Y20, Oppo A53") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // IMEI Number (15 digits validated)
                OutlinedTextField(
                    value = imeiNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 15) imeiNumber = digits
                    },
                    label = { Text("IMEI Number (15 digits) *") },
                    supportingText = {
                        Text(
                            text = "${imeiNumber.length}/15 digits",
                            color = if (imeiNumber.length == 15) StatusGreen else TextMuted
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

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
                    if (aadhaarNumber.length != 12) {
                        validationError = "Aadhaar must be exactly 12 digits (currently ${aadhaarNumber.length})"
                        return@Button
                    }
                    if (mobileNumber.isBlank()) {
                        validationError = "Please enter customer mobile number"
                        return@Button
                    }
                    if (mobileModel.isBlank()) {
                        validationError = "Please enter mobile model"
                        return@Button
                    }
                    if (imeiNumber.length != 15) {
                        validationError = "IMEI must be exactly 15 digits (currently ${imeiNumber.length})"
                        return@Button
                    }

                    val item = initialItem?.copy(
                        customerName = customerName.trim(),
                        aadhaarNumber = aadhaarNumber.trim(),
                        mobileNumber = mobileNumber.trim(),
                        address = address.trim(),
                        mobileModel = mobileModel.trim(),
                        imeiNumber = imeiNumber.trim()
                    ) ?: SoftwareJobItem(
                        customerName = customerName.trim(),
                        aadhaarNumber = aadhaarNumber.trim(),
                        mobileNumber = mobileNumber.trim(),
                        address = address.trim(),
                        mobileModel = mobileModel.trim(),
                        imeiNumber = imeiNumber.trim()
                    )

                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialItem == null) "Save Log" else "Update", fontWeight = FontWeight.Bold)
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
