package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProMembershipSheet
import com.example.ui.components.RewardedAdDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MonetizationScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {

    private val studioViewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = studioViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: StudioViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val showRewardedAd: Boolean by viewModel.showRewardedAd.collectAsState()
    val showProSheet: Boolean by viewModel.showProSheet.collectAsState()

    // Handle back button on sub-screens to return to Studio
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0C13)),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF13111C),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 0: Studio
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Studio",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Studio",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = BrandPurple.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_tab_studio")
                )

                // Tab 1: AI Tools
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.Widgets else Icons.Outlined.Widgets,
                            contentDescription = "Tools",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Tools",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = BrandPurple.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_tab_tools")
                )

                // Tab 2: History
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.History else Icons.Outlined.History,
                            contentDescription = "History",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "History",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = BrandPurple.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                // Tab 3: Rewards & Pro
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.MonetizationOn else Icons.Outlined.MonetizationOn,
                            contentDescription = "Rewards & PRO",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Economy",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = BrandPurple.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_tab_monetization")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> StudioScreen(viewModel = viewModel)
                1 -> ToolsScreen(viewModel = viewModel, onNavigateToStudio = { selectedTab = 0 })
                2 -> HistoryScreen(viewModel = viewModel, onNavigateToStudio = { selectedTab = 0 })
                3 -> MonetizationScreen(viewModel = viewModel)
            }
        }
    }

    // Rewarded Ad Dialog overlay
    if (showRewardedAd) {
        RewardedAdDialog(
            onDismiss = { viewModel.onRewardedAdDismiss() },
            onRewardEarned = { earned -> viewModel.onRewardEarned(earned) }
        )
    }

    // Lumina PRO Bottom Sheet
    if (showProSheet) {
        ProMembershipSheet(
            onDismiss = { viewModel.onProSheetDismiss() },
            onUnlockPro = { viewModel.activatePro() }
        )
    }
}
