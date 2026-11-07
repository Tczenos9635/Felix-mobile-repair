package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.ShopTab
import com.example.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PoojaRepairApp()
            }
        }
    }
}

data class NavItem(
    val tab: ShopTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun PoojaRepairApp(
    viewModel: ShopViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()

    val navItems = listOf(
        NavItem(
            tab = ShopTab.HOME,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "nav_home"
        ),
        NavItem(
            tab = ShopTab.REPAIR,
            label = "Repair",
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Outlined.Build,
            testTag = "nav_repair"
        ),
        NavItem(
            tab = ShopTab.SOFTWARE,
            label = "Software",
            selectedIcon = Icons.Filled.PhonelinkSetup,
            unselectedIcon = Icons.Outlined.PhonelinkSetup,
            testTag = "nav_software"
        ),
        NavItem(
            tab = ShopTab.ACCESSORIES,
            label = "Accessories",
            selectedIcon = Icons.Filled.ShoppingBag,
            unselectedIcon = Icons.Outlined.ShoppingBag,
            testTag = "nav_accessories"
        ),
        NavItem(
            tab = ShopTab.WARRANTY,
            label = "Warranty",
            selectedIcon = Icons.Filled.VerifiedUser,
            unselectedIcon = Icons.Outlined.VerifiedUser,
            testTag = "nav_warranty"
        ),
        NavItem(
            tab = ShopTab.DISPLAY,
            label = "Display",
            selectedIcon = Icons.Filled.Smartphone,
            unselectedIcon = Icons.Outlined.Smartphone,
            testTag = "nav_display"
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        bottomBar = {
            if (!showSettings) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(68.dp)
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BluePrimary,
                                selectedTextColor = BluePrimary,
                                indicatorColor = BlueContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = Pair(showSettings, currentTab), label = "screen_crossfade") { (settingsOpen, tab) ->
                if (settingsOpen) {
                    SettingsScreen(viewModel = viewModel)
                } else {
                    when (tab) {
                        ShopTab.HOME -> HomeScreen(viewModel = viewModel)
                        ShopTab.REPAIR -> RepairScreen(viewModel = viewModel)
                        ShopTab.SOFTWARE -> SoftwareScreen(viewModel = viewModel)
                        ShopTab.ACCESSORIES -> AccessoriesScreen(viewModel = viewModel)
                        ShopTab.WARRANTY -> WarrantyScreen(viewModel = viewModel)
                        ShopTab.DISPLAY -> UniversalDisplayScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
