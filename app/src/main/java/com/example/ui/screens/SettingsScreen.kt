package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPasteRestoreDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showDemoConfirmDialog by remember { mutableStateOf(false) }

    // Launcher for file picker to restore JSON
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                if (!jsonString.isNullOrBlank()) {
                    val result = viewModel.restoreBackupJson(jsonString)
                    if (result.isSuccess) {
                        Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Invalid backup file: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler {
        viewModel.closeSettings()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.closeSettings() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings & Data Backup",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup & Restore Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.CloudDownload,
                                contentDescription = null,
                                tint = BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Full Shop Data Backup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Includes Repairs, Software, Purchases, Dues, Warranties & Screens",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. "Backup (Download JSON)" Button
                    Button(
                        onClick = {
                            val json = viewModel.exportBackupJson()
                            // Copy to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Pooja Mobile Repair Backup", json)
                            clipboard.setPrimaryClip(clip)

                            // Open Share Sheet so user can save file or send to WhatsApp/Drive
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "application/json"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Save or Share Pooja Repair Backup (JSON)")
                            context.startActivity(shareIntent)

                            Toast.makeText(context, "Backup JSON copied to clipboard & opened save prompt!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup (Download JSON)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. "Restore from file" Button
                    OutlinedButton(
                        onClick = {
                            try {
                                filePickerLauncher.launch("*/*")
                            } catch (e: Exception) {
                                showPasteRestoreDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from file (JSON)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct Paste Restore Option
                    TextButton(
                        onClick = { showPasteRestoreDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Or paste backup JSON directly", fontSize = 13.sp, color = BluePrimary)
                    }
                }
            }

            // Quick Data Tools Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Sample Data & Reset",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Load Demo Data Button
                    FilledTonalButton(
                        onClick = { showDemoConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Sample Demo Data", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reset All Data Button
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Shop Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                    }
                }
            }

            // App About & Offline Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "About Pooja Mobile Repair",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0 • 100% Offline Device Storage\nDesigned specifically for mobile repair and spare parts shop owners in India. No cloud login required. All your job sheets, customer Aadhaar/IMEI records, wholesaler ledgers, warranties, and universal screen inventories stay safely stored on this device.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Paste JSON Restore Dialog
    if (showPasteRestoreDialog) {
        var pastedJson by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPasteRestoreDialog = false },
            title = { Text("Paste JSON to Restore", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste the JSON backup string exported previously:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = pastedJson,
                        onValueChange = { pastedJson = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("{\"appName\": \"Pooja Mobile Repair\", ...}") }
                    )
                    if (errorMessage != null) {
                        Text(text = errorMessage ?: "", color = StatusRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.restoreBackupJson(pastedJson.trim())
                        if (result.isSuccess) {
                            Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                            showPasteRestoreDialog = false
                        } else {
                            errorMessage = "Invalid JSON: ${result.exceptionOrNull()?.message}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPasteRestoreDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Reset Confirm Dialog
    ConfirmDeleteDialog(
        show = showResetConfirmDialog,
        title = "Clear All Shop Data?",
        message = "Are you sure you want to delete all repairs, software logs, purchases, warranties, and displays? Be sure to take a JSON backup first.",
        onConfirm = {
            viewModel.resetAllData()
            Toast.makeText(context, "All data cleared.", Toast.LENGTH_SHORT).show()
            showResetConfirmDialog = false
        },
        onDismiss = { showResetConfirmDialog = false }
    )

    // Demo Data Confirm Dialog
    ConfirmDeleteDialog(
        show = showDemoConfirmDialog,
        title = "Load Demo Sample Data?",
        message = "This will populate sample repairs, software jobs, wholesaler dues, warranties, and displays to test the app.",
        onConfirm = {
            viewModel.loadDemoData()
            Toast.makeText(context, "Demo data loaded!", Toast.LENGTH_SHORT).show()
            showDemoConfirmDialog = false
        },
        onDismiss = { showDemoConfirmDialog = false }
    )
}
