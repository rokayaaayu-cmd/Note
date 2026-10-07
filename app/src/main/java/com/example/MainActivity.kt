package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ClipboardVaultScreen
import com.example.ui.CommandsScreen
import com.example.ui.TermuxHubViewModel
import com.example.ui.UsageRankingScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TermuxGreen
import com.example.ui.theme.TermuxGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class AppDestination(val title: String, val icon: ImageVector, val tag: String) {
    COMMAND_DATABASE("Commands", Icons.Default.Code, "nav_command_database"),
    CLIPBOARD_VAULT("Clipboard (15)", Icons.Default.ContentPaste, "nav_clipboard_vault"),
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
    var currentTab by rememberSaveable { mutableStateOf(AppDestination.COMMAND_DATABASE) }

    val filteredCommands by viewModel.filteredCommands.collectAsStateWithLifecycle()
    val allCommands by viewModel.allCommands.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val allClipboardItems by viewModel.allClipboardItems.collectAsStateWithLifecycle()
    val recentFifteenClipboard by viewModel.recentFifteenClipboard.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    if (currentTab != AppDestination.COMMAND_DATABASE) {
        BackHandler {
            currentTab = AppDestination.COMMAND_DATABASE
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
            AppHeaderAndImeSwitcherBar(
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
                AppDestination.COMMAND_DATABASE -> {
                    CommandsScreen(
                        commands = filteredCommands,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSelectCategory = viewModel::selectCategory,
                        onInsertIntoTerminal = { cmd ->
                            viewModel.recordCommandUsageAndCopy(cmd)
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
                        recentFifteen = recentFifteenClipboard,
                        allClipboardItems = allClipboardItems,
                        onInsertClip = { clip ->
                            viewModel.recordClipboardUsageAndCopy(clip)
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
                            viewModel.recordCommandUsageAndCopy(cmd)
                        },
                        onInsertClipboard = { clip ->
                            viewModel.recordClipboardUsageAndCopy(clip)
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
private fun AppHeaderAndImeSwitcherBar(
    totalCommands: Int,
    totalClips: Int
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isImeEnabled by remember { mutableStateOf(checkIsImeEnabled(context)) }
    var isImeSelected by remember { mutableStateOf(checkIsImeSelected(context)) }

    DisposableEffect(context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isImeEnabled = checkIsImeEnabled(context)
                isImeSelected = checkIsImeSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val imeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                isImeEnabled = checkIsImeEnabled(context)
                isImeSelected = checkIsImeSelected(context)
            }
        }
        ContextCompat.registerReceiver(
            context,
            imeReceiver,
            IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                context.unregisterReceiver(imeReceiver)
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isImeSelected) {
                            "Active System Keyboard • Swipe up on IME for 3 Related & 15 Recent"
                        } else {
                            "Manage Commands & Clipboard • Enable & Switch Keyboard below"
                        },
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = if (isImeSelected) TermuxGreen else TextMuted
                    )
                }
            }

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

        // System Keyboard Enable & Switch Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isImeEnabled) TerminalSurfaceElevated else TermuxGreen,
                    contentColor = if (isImeEnabled) TermuxGreen else Color(0xFF00210E)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("enable_ime_settings_button")
            ) {
                Icon(
                    imageVector = if (isImeEnabled) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isImeEnabled) "1. IME Enabled" else "1. Enable in Settings",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isImeEnabled && !isImeSelected) TermuxGreen else ElectricCyanContainer,
                    contentColor = if (isImeEnabled && !isImeSelected) Color(0xFF00210E) else ElectricCyan
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("switch_active_ime_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isImeSelected) "Switch Keyboard" else "2. Select Keyboard",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun checkIsImeEnabled(context: Context): Boolean {
    return try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledList = imm?.enabledInputMethodList.orEmpty()
        enabledList.any { it.packageName == context.packageName }
    } catch (_: Exception) {
        false
    }
}

private fun checkIsImeSelected(context: Context): Boolean {
    return try {
        val defaultIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ).orEmpty()
        defaultIme.startsWith("${context.packageName}/")
    } catch (_: Exception) {
        false
    }
}
