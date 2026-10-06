package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ClipboardVaultScreen
import com.example.ui.CommandsScreen
import com.example.ui.SandboxAndSetupScreen
import com.example.ui.TermuxHubViewModel
import com.example.ui.UsageRankingScreen
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TermuxGreen
import com.example.ui.theme.TermuxGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class AppDestination(val title: String, val icon: ImageVector, val tag: String) {
    KEYBOARD_SANDBOX("Keyboard", Icons.Default.Keyboard, "nav_keyboard_sandbox"),
    COMMAND_DATABASE("Commands", Icons.Default.Code, "nav_command_database"),
    CLIPBOARD_VAULT("Clipboard", Icons.Default.ContentPaste, "nav_clipboard_vault"),
    MOST_USED_RANKING("Most Used", Icons.Default.LocalFireDepartment, "nav_most_used_ranking")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TermuxSmartKeyboardApp()
            }
        }
    }
}

@Composable
fun TermuxSmartKeyboardApp(
    viewModel: TermuxHubViewModel = viewModel()
) {
    var currentTab by rememberSaveable { mutableStateOf(AppDestination.KEYBOARD_SANDBOX) }

    val inputLine by viewModel.sandboxInputLine.collectAsStateWithLifecycle()
    val terminalHistory by viewModel.terminalHistory.collectAsStateWithLifecycle()
    val keyboardState by viewModel.sandboxKeyboardState.collectAsStateWithLifecycle()
    val filteredCommands by viewModel.filteredCommands.collectAsStateWithLifecycle()
    val allCommands by viewModel.allCommands.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val allClipboardItems by viewModel.allClipboardItems.collectAsStateWithLifecycle()
    val recentThreeClipboard by viewModel.recentThreeClipboard.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    if (currentTab != AppDestination.KEYBOARD_SANDBOX) {
        BackHandler {
            currentTab = AppDestination.KEYBOARD_SANDBOX
        }
    }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(2800)
            viewModel.dismissStatusBanner()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TerminalBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AppHeaderBar(
                totalCommands = allCommands.size,
                totalClips = allClipboardItems.size
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TerminalSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                AppDestination.entries.forEach { dest ->
                    val selected = currentTab == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = dest },
                        modifier = Modifier.testTag(dest.tag),
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00210E),
                            selectedTextColor = TermuxGreen,
                            indicatorColor = TermuxGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppDestination.KEYBOARD_SANDBOX -> {
                    SandboxAndSetupScreen(
                        inputLine = inputLine,
                        terminalHistory = terminalHistory,
                        keyboardState = keyboardState,
                        keyboardHandler = viewModel,
                        onSetInputLine = viewModel::setSandboxInputLine,
                        onClearTerminal = viewModel::clearSandboxTerminal,
                        onCopyInputLine = { text ->
                            viewModel.copyTextToClipboard(text, "Terminal Sandbox Copy")
                        }
                    )
                }

                AppDestination.COMMAND_DATABASE -> {
                    CommandsScreen(
                        commands = filteredCommands,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSelectCategory = viewModel::selectCategory,
                        onInsertIntoTerminal = { cmd ->
                            viewModel.insertIntoSandbox(cmd.command, cmd)
                            currentTab = AppDestination.KEYBOARD_SANDBOX
                        },
                        onCopyCommand = { text ->
                            viewModel.copyTextToClipboard(text, "Command Database")
                        },
                        onSaveCustomCommand = viewModel::saveCustomCommand,
                        onDeleteCommand = viewModel::deleteCommand
                    )
                }

                AppDestination.CLIPBOARD_VAULT -> {
                    ClipboardVaultScreen(
                        recentThree = recentThreeClipboard,
                        allClipboardItems = allClipboardItems,
                        onInsertClip = { clip ->
                            viewModel.onSelectClipboardItem(clip)
                            currentTab = AppDestination.KEYBOARD_SANDBOX
                        },
                        onCopyClip = { text, label ->
                            viewModel.copyTextToClipboard(text, label)
                        },
                        onTogglePin = viewModel::toggleClipboardPin,
                        onDeleteClip = viewModel::deleteClipboardItem,
                        onClearUnpinned = viewModel::clearUnpinnedClipboard,
                        onAddManualClip = viewModel::addManualClipboardItem
                    )
                }

                AppDestination.MOST_USED_RANKING -> {
                    UsageRankingScreen(
                        allCommands = allCommands,
                        allClipboardItems = allClipboardItems,
                        onInsertCommand = { cmd ->
                            viewModel.insertIntoSandbox(cmd.command, cmd)
                            currentTab = AppDestination.KEYBOARD_SANDBOX
                        },
                        onInsertClipboard = { clip ->
                            viewModel.onSelectClipboardItem(clip)
                            currentTab = AppDestination.KEYBOARD_SANDBOX
                        },
                        onResetStats = viewModel::resetAllUsageStats
                    )
                }
            }

            // Floating Status Toast Banner
            AnimatedVisibility(
                visible = statusBanner != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TermuxGreenContainer)
                        .border(1.dp, TermuxGreen, RoundedCornerShape(8.dp))
                        .clickable { viewModel.dismissStatusBanner() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = statusBanner.orEmpty(),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TermuxGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeaderBar(
    totalCommands: Int,
    totalClips: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TermuxGreenContainer)
                    .border(1.dp, TermuxGreen, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Termux Smart Keyboard",
                    tint = TermuxGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = "Termux Smart Keyboard",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Android IME • Smart Suggestions • Shortcut Bar",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalBg)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$totalCommands CMDs • $totalClips CLIPs",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = ElectricCyan
                )
            }
        }
    }
}
