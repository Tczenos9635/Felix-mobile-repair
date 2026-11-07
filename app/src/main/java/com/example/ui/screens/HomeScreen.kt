package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatRupees
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val pendingRepairs by viewModel.pendingRepairsCount.collectAsState()
    val totalDue by viewModel.totalDueToWholesalers.collectAsState()
    val lowStockDisplays by viewModel.lowStockDisplaysCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pooja Mobile Repair",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimary
                    )
                    Text(
                        text = "Shop Management & Inventory",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.openSettings() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Status Summary Section Header
            Text(
                text = "Shop Overview",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // 3 Small Summary Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Pending Repairs
                SummaryBox(
                    title = "Pending Repairs",
                    value = pendingRepairs.toString(),
                    valueColor = if (pendingRepairs > 0) StatusAmber else StatusGreen,
                    icon = Icons.Outlined.Build,
                    iconBg = StatusAmberBg,
                    modifier = Modifier.weight(1f)
                )

                // 2. Total Due to Wholesalers (IN RED)
                SummaryBox(
                    title = "Wholesaler Due",
                    value = formatRupees(totalDue),
                    valueColor = if (totalDue > 0) StatusRed else StatusGreen,
                    icon = Icons.Outlined.AccountBalanceWallet,
                    iconBg = if (totalDue > 0) StatusRedBg else StatusGreenBg,
                    modifier = Modifier.weight(1.1f)
                )

                // 3. Low Stock Displays
                SummaryBox(
                    title = "Low Stock Screens",
                    value = lowStockDisplays.toString(),
                    valueColor = if (lowStockDisplays > 0) StatusAmber else StatusGreen,
                    icon = Icons.Outlined.Smartphone,
                    iconBg = StatusAmberBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Services & Inventory",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // 5 Big Cards
            // 1. Repair
            BigMenuCard(
                title = "Repair Jobs",
                subtitle = "Manage job sheets, mobile problems, estimated costs & customer calls",
                badge = "$pendingRepairs Pending",
                badgeColor = if (pendingRepairs > 0) StatusAmber else StatusGreen,
                badgeBg = if (pendingRepairs > 0) StatusAmberBg else StatusGreenBg,
                icon = Icons.Default.Build,
                iconColor = BluePrimary,
                onClick = { viewModel.selectTab(ShopTab.REPAIR) }
            )

            // 2. Mobile Software
            val softwareJobs by viewModel.softwareJobs.collectAsState()
            BigMenuCard(
                title = "Mobile Software",
                subtitle = "Software flashing & unlocking with 12-digit Aadhaar & 15-digit IMEI security logs",
                badge = "${softwareJobs.size} Entries",
                badgeColor = BluePrimary,
                badgeBg = BlueContainer,
                icon = Icons.Default.PhonelinkSetup,
                iconColor = Color(0xFF7C3AED),
                onClick = { viewModel.selectTab(ShopTab.SOFTWARE) }
            )

            // 3. Accessories Purchase (Wholesaler)
            val wholesalerList by viewModel.wholesalerSummaries.collectAsState()
            BigMenuCard(
                title = "Accessories Purchase",
                subtitle = "Track wholesaler wholesale rates (DP), purchase history, payments & dues",
                badge = if (totalDue > 0) "Due: ${formatRupees(totalDue)}" else "All Cleared",
                badgeColor = if (totalDue > 0) StatusRed else StatusGreen,
                badgeBg = if (totalDue > 0) StatusRedBg else StatusGreenBg,
                icon = Icons.Default.ShoppingBag,
                iconColor = Color(0xFF0D9488),
                onClick = { viewModel.selectTab(ShopTab.ACCESSORIES) }
            )

            // 4. Warranty
            val warranties by viewModel.warranties.collectAsState()
            val activeWarranties = warranties.count { it.isActive }
            BigMenuCard(
                title = "Warranty Tracker",
                subtitle = "Customer warranty cards with 1/3/6/12 month auto-calculated expiry tracking",
                badge = "$activeWarranties Active",
                badgeColor = StatusGreen,
                badgeBg = StatusGreenBg,
                icon = Icons.Default.VerifiedUser,
                iconColor = Color(0xFFEA580C),
                onClick = { viewModel.selectTab(ShopTab.WARRANTY) }
            )

            // 5. Universal Display
            val displayList by viewModel.displays.collectAsState()
            val totalScreenStock = displayList.sumOf { it.stockQuantity }
            BigMenuCard(
                title = "Universal Display",
                subtitle = "Instant phone model compatibility matching, quality types & live stock",
                badge = "$totalScreenStock In Stock",
                badgeColor = BluePrimary,
                badgeBg = BlueContainer,
                icon = Icons.Default.Smartphone,
                iconColor = BluePrimary,
                onClick = { viewModel.selectTab(ShopTab.DISPLAY) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryBox(
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BigMenuCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    badgeBg: Color,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = TextMuted
            )
        }
    }
}
