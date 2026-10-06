package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ime.KeyboardActionHandler
import com.example.ime.TermuxKeyboardSurface
import com.example.ime.TermuxKeyboardUiState
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalKeySurface
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TermuxGreen
import com.example.ui.theme.TermuxGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SandboxAndSetupScreen(
    inputLine: String,
    terminalHistory: List<TerminalLogEntry>,
    keyboardState: TermuxKeyboardUiState,
    keyboardHandler: KeyboardActionHandler,
    onSetInputLine: (String) -> Unit,
    onClearTerminal: () -> Unit,
    onCopyInputLine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isImeEnabled by remember { mutableStateOf(checkIsImeEnabled(context)) }
    var showSetupGuide by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isImeEnabled = checkIsImeEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        // Top workspace: Clean 2-Step APK Setup Card + Live Terminal Viewport
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = showSetupGuide) {
                CleanImeSetupCard(
                    isEnabled = isImeEnabled,
                    onEnableClick = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onSelectKeyboardClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    },
                    onOpenTermuxClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.termux")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        }
                    },
                    isTermuxInstalled = remember(context) {
                        context.packageManager.getLaunchIntentForPackage("com.termux") != null
                    },
                    onDismiss = { showSetupGuide = false }
                )
            }

            // Quick Smart Suggestion Demo Chips
            QuickIntentTestStrip(
                activeLine = inputLine,
                onSelectTestPhrase = onSetInputLine
            )

            // Clean Interactive Termux Terminal Sandbox
            InteractiveTermuxViewport(
                inputLine = inputLine,
                history = terminalHistory,
                ctrlLatched = keyboardState.ctrlLatched,
                altLatched = keyboardState.altLatched,
                onClear = onClearTerminal,
                onCopyLine = {
                    if (inputLine.isNotBlank()) onCopyInputLine(inputLine)
                }
            )
        }

        // Bottom Docked Termux Smart Keyboard (Exact 5-Tier Layout)
        TermuxKeyboardSurface(
            state = keyboardState,
            handler = keyboardHandler,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CleanImeSetupCard(
    isEnabled: Boolean,
    onEnableClick: () -> Unit,
    onSelectKeyboardClick: () -> Unit,
    onOpenTermuxClick: () -> Unit,
    isTermuxInstalled: Boolean,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurface)
            .border(1.dp, if (isEnabled) TermuxGreen.copy(alpha = 0.6f) else TerminalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("ime_setup_banner"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Keyboard,
                    contentDescription = "Keyboard Status",
                    tint = if (isEnabled) TermuxGreen else ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isEnabled) {
                        "Keyboard APK Ready • Select IME to Use in Termux"
                    } else {
                        "Activate Termux Smart Keyboard APK (2 Steps)"
                    },
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }

            Text(
                text = "Hide",
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) TermuxGreenContainer else TermuxGreen,
                    contentColor = if (isEnabled) TermuxGreen else Color(0xFF00210E)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("enable_ime_settings_button")
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isEnabled) "1. Enabled" else "1. Enable IME",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onSelectKeyboardClick,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("switch_active_ime_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "2. Select IME",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ElectricCyan
                )
            }

            if (isTermuxInstalled) {
                OutlinedButton(
                    onClick = onOpenTermuxClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TermuxGreen),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open Termux",
                        tint = TermuxGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickIntentTestStrip(
    activeLine: String,
    onSelectTestPhrase: (String) -> Unit
) {
    val scenarios = listOf(
        "install node" to "pkg install nodejs",
        "list files" to "ls -la",
        "open project" to "cd ~/projects",
        "git status" to "git status",
        "opencode" to "Related Clips",
        "" to "Reset"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Smart Intent Scenarios",
                tint = AmberAlert,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Quick Test Intents (tap or type below):",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            scenarios.forEachIndexed { idx, (phrase, hint) ->
                val isSelected = activeLine.trim().equals(phrase, ignoreCase = true) && phrase.isNotEmpty()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElectricCyanContainer else TerminalSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) ElectricCyan else TerminalBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectTestPhrase(phrase) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("scenario_chip_$idx")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (phrase.isEmpty()) "⌫ Clear" else phrase,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) ElectricCyan else TextPrimary
                        )
                        if (phrase.isNotEmpty()) {
                            Text(
                                text = "→ $hint",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = TermuxGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveTermuxViewport(
    inputLine: String,
    history: List<TerminalLogEntry>,
    ctrlLatched: Boolean,
    altLatched: Boolean,
    onClear: () -> Unit,
    onCopyLine: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF06090E))
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("interactive_termux_viewport"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Terminal Window Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(TermuxGreen)
                )
                Text(
                    text = "Termux Live Preview",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (inputLine.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalKeySurface)
                            .clickable(onClick = onCopyLine)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("copy_current_line_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Line",
                                tint = ElectricCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Copy",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalKeySurface)
                        .clickable(onClick = onClear)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("clear_terminal_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Terminal",
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Clear",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Previous lines committed via Enter
        history.takeLast(2).forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "~/projects $ ",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = entry.promptLine,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Input Prompt Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalSurface)
                .border(1.dp, TermuxGreen.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp)
                .testTag("sandbox_active_prompt_line"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "~/projects $ ",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TermuxGreen
            )
            if (ctrlLatched) {
                Text(
                    text = "[CTRL] ",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AmberAlert
                )
            }
            if (altLatched) {
                Text(
                    text = "[ALT] ",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = ElectricCyan
                )
            }
            Text(
                text = inputLine,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextPrimary
            )
            // Block Terminal Cursor
            Box(
                modifier = Modifier
                    .padding(start = 1.dp)
                    .width(7.dp)
                    .height(15.dp)
                    .background(TermuxGreen)
            )
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
