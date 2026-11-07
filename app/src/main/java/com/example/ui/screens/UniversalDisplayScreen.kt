package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Smartphone
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
import com.example.model.UniversalDisplayItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UniversalDisplayScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.displayActiveTab.collectAsState()
    val modelQuery by viewModel.displaySearchQuery.collectAsState()
    val modelResults by viewModel.modelSearchResults.collectAsState()

    val stockQuery by viewModel.displayStockSearchQuery.collectAsState()
    val stockDisplays by viewModel.filteredDisplaysStock.collectAsState()
    val allDisplays by viewModel.displays.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<UniversalDisplayItem?>(null) }
    var prefillModelForNewDisplay by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<UniversalDisplayItem?>(null) }
    var detailItemToView by remember { mutableStateOf<UniversalDisplayItem?>(null) }
    var showAddStockDialogFor by remember { mutableStateOf<UniversalDisplayItem?>(null) }

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
                    prefillModelForNewDisplay = null
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
                    Icon(Icons.Default.Add, contentDescription = "Add Display")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Display", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Tabs: 0 = Model Search (Top Priority), 1 = Stock List
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Universal Display Compatibility",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Instant phone screen fitment & inventory lookup",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.White,
                        contentColor = BluePrimary
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { viewModel.displayActiveTab.value = 0 },
                            text = {
                                Text(
                                    text = "🔍 Model Search",
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { viewModel.displayActiveTab.value = 1 },
                            text = {
                                Text(
                                    text = "📦 Stock List (${allDisplays.size})",
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeTab == 0) {
                        // Big Search Bar: "Search phone model (e.g. Redmi Note 8)"
                        SearchBarComponent(
                            query = modelQuery,
                            onQueryChange = { viewModel.displaySearchQuery.value = it },
                            placeholder = "Search phone model (e.g. Redmi Note 8, A12)..."
                        )
                    } else {
                        // Stock List search
                        SearchBarComponent(
                            query = stockQuery,
                            onQueryChange = { viewModel.displayStockSearchQuery.value = it },
                            placeholder = "Search display name, quality, or model..."
                        )
                    }
                }
            }

            // Tab 0: Model Search View (Most Important)
            if (activeTab == 0) {
                if (modelQuery.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Find Matching Display",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Type any phone model name above (e.g. 'Note 8', 'Y20', 'A12'). The system smartly ignores spaces, dashes and case to find which universal screen will fit and shows live stock.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Quick Model Suggestions:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Redmi Note 8", "Samsung A12", "Vivo Y20", "Realme C11").forEach { sample ->
                                SuggestionChip(
                                    onClick = { viewModel.displaySearchQuery.value = sample },
                                    label = { Text(sample, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                } else if (modelResults.isEmpty()) {
                    // "No display found for this model" with "Add Display" button!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(StatusAmberBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = StatusAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No display found for \"$modelQuery\"",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "We don't have a display mapped for this model yet. You can add one now.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                itemToEdit = null
                                prefillModelForNewDisplay = modelQuery.trim()
                                showFormDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Display for $modelQuery", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Matching Displays List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Found ${modelResults.size} matching display(s) for '$modelQuery':",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BluePrimary
                            )
                        }

                        items(modelResults, key = { it.id }) { item ->
                            DisplaySearchResultCard(
                                item = item,
                                searchQuery = modelQuery,
                                onClick = { detailItemToView = item },
                                onUseOne = { viewModel.useDisplayStock(item.id) },
                                onAddStock = { showAddStockDialogFor = item }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Stock List View
                if (stockDisplays.isEmpty()) {
                    EmptyStateView(
                        title = "No display screens in stock",
                        message = if (stockQuery.isNotEmpty()) "No displays match '$stockQuery'." else "Tap '+ Add Display' to add your first screen inventory.",
                        icon = Icons.Outlined.Smartphone,
                        actionButtonText = if (stockQuery.isEmpty()) "+ Add First Display" else null,
                        onActionClick = {
                            itemToEdit = null
                            prefillModelForNewDisplay = null
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
                        items(stockDisplays, key = { it.id }) { item ->
                            DisplayStockCard(
                                item = item,
                                onClick = { detailItemToView = item },
                                onUseOne = { viewModel.useDisplayStock(item.id) },
                                onAddStock = { showAddStockDialogFor = item },
                                onEdit = {
                                    itemToEdit = item
                                    prefillModelForNewDisplay = null
                                    showFormDialog = true
                                },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Screen Dialog
    detailItemToView?.let { detailItem ->
        DisplayDetailDialog(
            item = detailItem,
            onDismiss = { detailItemToView = null },
            onEdit = {
                detailItemToView = null
                itemToEdit = detailItem
                showFormDialog = true
            },
            onUseOne = {
                viewModel.useDisplayStock(detailItem.id)
                // refresh local view
                detailItemToView = allDisplays.find { it.id == detailItem.id }
            }
        )
    }

    // Add / Edit Display Form
    if (showFormDialog) {
        DisplayFormDialog(
            initialItem = itemToEdit,
            prefillModel = prefillModelForNewDisplay,
            onDismiss = {
                showFormDialog = false
                itemToEdit = null
                prefillModelForNewDisplay = null
            },
            onSave = { savedItem ->
                if (itemToEdit == null) {
                    viewModel.addDisplay(savedItem)
                } else {
                    viewModel.updateDisplay(savedItem)
                }
                showFormDialog = false
                itemToEdit = null
                prefillModelForNewDisplay = null
            }
        )
    }

    // Quick Add Stock Dialog
    showAddStockDialogFor?.let { targetDisplay ->
        AddStockQuantityDialog(
            displayName = targetDisplay.displayName,
            currentStock = targetDisplay.stockQuantity,
            onDismiss = { showAddStockDialogFor = null },
            onConfirmAdd = { qtyToAdd ->
                viewModel.addDisplayStock(targetDisplay.id, qtyToAdd)
                showAddStockDialogFor = null
            }
        )
    }

    // Delete Confirmation
    ConfirmDeleteDialog(
        show = itemToDelete != null,
        title = "Delete Display?",
        message = "Are you sure you want to delete display '${itemToDelete?.displayName}'? This will remove its model compatibility mappings.",
        onConfirm = {
            itemToDelete?.let { viewModel.deleteDisplay(it.id) }
            itemToDelete = null
        },
        onDismiss = { itemToDelete = null }
    )
}

@Composable
private fun DisplaySearchResultCard(
    item: UniversalDisplayItem,
    searchQuery: String,
    onClick: () -> Unit,
    onUseOne: () -> Unit,
    onAddStock: () -> Unit
) {
    val isInStock = item.stockQuantity > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Display Name & In/Out Stock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Quality: ${item.qualityType} • Color: ${item.color}" +
                                if (item.withFrame) " • With Frame" else " • Without Frame",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Green "In Stock" or Red "Out of Stock" badge
                StatusBadge(
                    text = if (isInStock) "In Stock (${item.stockQuantity})" else "Out of Stock",
                    color = if (isInStock) StatusGreen else StatusRed,
                    backgroundColor = if (isInStock) StatusGreenBg else StatusRedBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Compatible models matching
            Text(
                text = "Compatible with ${item.compatibleModels.size} models including:",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Display up to 4 models chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(item.compatibleModels) { model ->
                    val isMatched = model.contains(searchQuery.trim(), ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMatched) BlueContainer else SurfaceVariantLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = model,
                            fontSize = 11.sp,
                            fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                            color = if (isMatched) BluePrimary else TextSecondary
                        )
                    }
                }
            }

            if (!item.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: ${item.notes}",
                    fontSize = 11.sp,
                    color = StatusAmber,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: "Use 1", "+ Add Stock", View Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap for full details →",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Use 1 Button (reduces stock by 1, cannot go below 0)
                    Button(
                        onClick = onUseOne,
                        enabled = item.stockQuantity > 0,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Use 1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // + Add Stock
                    OutlinedButton(
                        onClick = onAddStock,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Stock", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayStockCard(
    item: UniversalDisplayItem,
    onClick: () -> Unit,
    onUseOne: () -> Unit,
    onAddStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusLabel, statusColor, statusBg) = when {
        item.isOutOfStock -> Triple("Out of Stock", StatusRed, StatusRedBg)
        item.isLowStock -> Triple("Low Stock (${item.stockQuantity})", StatusAmber, StatusAmberBg)
        else -> Triple("In Stock (${item.stockQuantity})", StatusGreen, StatusGreenBg)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Display Name & Stock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.qualityType} • ${item.color}" +
                                if (item.withFrame) " • With Frame" else " • No Frame",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                StatusBadge(
                    text = statusLabel,
                    color = statusColor,
                    backgroundColor = statusBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Models Count & Wholesaler Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fits ${item.compatibleModels.size} phone models",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )

                if (item.purchasePrice != null && item.purchasePrice > 0) {
                    Text(
                        text = "Cost: ${formatRupees(item.purchasePrice)}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Quick models preview
            if (item.compatibleModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.compatibleModels.take(4).joinToString(", ") +
                            if (item.compatibleModels.size > 4) " + ${item.compatibleModels.size - 4} more" else "",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: "Use 1", "+ Add Stock", Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Use 1" (reduces stock by 1 when used in repair)
                Button(
                    onClick = onUseOne,
                    enabled = item.stockQuantity > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Use 1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // "+ Add Stock"
                FilledTonalButton(
                    onClick = onAddStock,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Stock", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Edit
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DisplayDetailDialog(
    item: UniversalDisplayItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onUseOne: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    text = if (item.stockQuantity > 0) "${item.stockQuantity} in Stock" else "Out of Stock",
                    color = if (item.stockQuantity > 0) StatusGreen else StatusRed,
                    backgroundColor = if (item.stockQuantity > 0) StatusGreenBg else StatusRedBg
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Specifications row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Quality", fontSize = 11.sp, color = TextMuted)
                        Text(item.qualityType, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Color", fontSize = 11.sp, color = TextMuted)
                        Text(item.color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Frame", fontSize = 11.sp, color = TextMuted)
                        Text(if (item.withFrame) "With Frame" else "No Frame", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (item.purchasePrice != null) {
                        Column {
                            Text("Price", fontSize = 11.sp, color = TextMuted)
                            Text(formatRupees(item.purchasePrice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (!item.wholesalerName.isNullOrBlank()) {
                    Text(
                        text = "Wholesaler: ${item.wholesalerName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (!item.notes.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusAmberBg)
                            .padding(8.dp)
                    ) {
                        Text("Note: ${item.notes}", fontSize = 12.sp, color = StatusAmber)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Full List of Compatible Models (Chips)
                Text(
                    text = "All Compatible Models (${item.compatibleModels.size}):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.compatibleModels.forEach { model ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BlueContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = model,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BluePrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUseOne,
                    enabled = item.stockQuantity > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Use 1", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Edit Display", fontWeight = FontWeight.Bold)
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DisplayFormDialog(
    initialItem: UniversalDisplayItem?,
    prefillModel: String?,
    onDismiss: () -> Unit,
    onSave: (UniversalDisplayItem) -> Unit
) {
    var displayName by remember { mutableStateOf(initialItem?.displayName ?: "") }
    var qualityType by remember { mutableStateOf(initialItem?.qualityType ?: "Original") }
    var color by remember { mutableStateOf(initialItem?.color ?: "Black") }
    var withFrame by remember { mutableStateOf(initialItem?.withFrame ?: false) }
    var stockQuantityStr by remember {
        mutableStateOf(if (initialItem != null) initialItem.stockQuantity.toString() else "1")
    }
    var purchasePriceStr by remember {
        mutableStateOf(if (initialItem?.purchasePrice != null) initialItem.purchasePrice.toInt().toString() else "")
    }
    var wholesalerName by remember { mutableStateOf(initialItem?.wholesalerName ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    // Compatible Models Chips State
    val initialList = remember {
        val list = mutableListOf<String>()
        if (initialItem != null) {
            list.addAll(initialItem.compatibleModels)
        } else if (!prefillModel.isNullOrBlank()) {
            list.add(prefillModel)
        }
        list
    }
    var compatibleModels by remember { mutableStateOf<List<String>>(initialList) }
    var modelInputText by remember { mutableStateOf("") }

    var qualityExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun addModelTag(rawInput: String) {
        if (rawInput.isBlank()) return
        // Split on comma or newline to support pasting multiple models at once!
        val splitModels = rawInput.split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val current = compatibleModels.toMutableList()
        splitModels.forEach { m ->
            if (!current.any { it.equals(m, ignoreCase = true) }) {
                current.add(m)
            }
        }
        compatibleModels = current
        modelInputText = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Add Universal Display" else "Edit Display",
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
                // Display Name or Code
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name or Code *") },
                    placeholder = { Text("e.g. Universal Display A12") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quality & Color Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quality Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = qualityType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Quality *") },
                            trailingIcon = {
                                IconButton(onClick = { qualityExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { qualityExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = qualityExpanded,
                            onDismissRequest = { qualityExpanded = false }
                        ) {
                            listOf("Original", "OLED", "Incell", "LCD", "Copy").forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        qualityType = q
                                        qualityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Color Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = color,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Color *") },
                            trailingIcon = {
                                IconButton(onClick = { colorExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { colorExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = colorExpanded,
                            onDismissRequest = { colorExpanded = false }
                        ) {
                            listOf("Black", "White", "Other").forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        color = c
                                        colorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // With Frame Switch & Stock Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("With Frame: ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(if (withFrame) "Yes" else "No", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = withFrame,
                            onCheckedChange = { withFrame = it }
                        )
                    }

                    OutlinedTextField(
                        value = stockQuantityStr,
                        onValueChange = { stockQuantityStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stock Qty *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Compatible Models Tag Input Box (type or paste many)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Compatible Models (Type or paste separated by comma):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = modelInputText,
                            onValueChange = { modelInputText = it },
                            placeholder = { Text("e.g. Redmi Note 8, Note 8T") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { addModelTag(modelInputText) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Add")
                        }
                    }

                    // Display Tags
                    if (compatibleModels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            compatibleModels.forEach { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(tag, fontSize = 11.sp) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    compatibleModels = compatibleModels.filterNot { it == tag }
                                                }
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                // Purchase Price (optional) & Wholesaler (optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Price (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = wholesalerName,
                        onValueChange = { wholesalerName = it },
                        label = { Text("Wholesaler (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Notes (optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Fingerprint not supported, Incell quality") },
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
                    if (displayName.isBlank()) {
                        validationError = "Please enter display name or code"
                        return@Button
                    }
                    val stockQty = stockQuantityStr.toIntOrNull() ?: 0
                    if (stockQty < 0) {
                        validationError = "Stock quantity cannot be less than 0"
                        return@Button
                    }
                    if (modelInputText.isNotBlank()) {
                        addModelTag(modelInputText)
                    }

                    val price = purchasePriceStr.toDoubleOrNull()

                    val item = initialItem?.copy(
                        displayName = displayName.trim(),
                        qualityType = qualityType,
                        color = color,
                        withFrame = withFrame,
                        stockQuantity = stockQty,
                        purchasePrice = price,
                        wholesalerName = wholesalerName.trim().ifBlank { null },
                        compatibleModels = compatibleModels,
                        notes = notes.trim().ifBlank { null }
                    ) ?: UniversalDisplayItem(
                        displayName = displayName.trim(),
                        qualityType = qualityType,
                        color = color,
                        withFrame = withFrame,
                        stockQuantity = stockQty,
                        purchasePrice = price,
                        wholesalerName = wholesalerName.trim().ifBlank { null },
                        compatibleModels = compatibleModels,
                        notes = notes.trim().ifBlank { null }
                    )

                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialItem == null) "Save Display" else "Update", fontWeight = FontWeight.Bold)
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
private fun AddStockQuantityDialog(
    displayName: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirmAdd: (Int) -> Unit
) {
    var quantityToAddStr by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stock: $displayName", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current stock: $currentStock pieces", fontSize = 13.sp, color = TextSecondary)
                OutlinedTextField(
                    value = quantityToAddStr,
                    onValueChange = { quantityToAddStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Pieces to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addQty = quantityToAddStr.toIntOrNull() ?: 1
                    if (addQty > 0) {
                        onConfirmAdd(addQty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Stock", fontWeight = FontWeight.Bold)
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
