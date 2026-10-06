package com.example.ime

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntity
import com.example.data.CommandEntity
import com.example.data.RankedSuggestion
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalKeyModifier
import com.example.ui.theme.TerminalKeySurface
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TermuxGreen
import com.example.ui.theme.TermuxGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val qwertyRows = listOf(
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
    listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
    listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
    listOf("z", "x", "c", "v", "b", "n", "m")
)

private val symbolRows = listOf(
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
    listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
    listOf("-", "_", "=", "+", "[", "]", "{", "}", "\\", "|"),
    listOf(";", ":", "'", "\"", ",", ".", "<", ">", "/", "?")
)

@Composable
fun TermuxKeyboardSurface(
    state: TermuxKeyboardUiState,
    handler: KeyboardActionHandler,
    modifier: Modifier = Modifier,
    onOpenFullAppClick: (() -> Unit)? = null
) {
    val view = LocalView.current

    fun performTick() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("termux_keyboard_surface"),
        color = TerminalBg,
        tonalElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            if (state.oneHandedMode == OneHandedMode.RIGHT) {
                OneHandedSidebar(
                    isLeft = true,
                    onExpandFull = {
                        performTick()
                        handler.onCycleOneHandedMode()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalBg)
            ) {
                // =========================================================================
                // 1. RELATED COMMANDS BAR
                // ┌──────────────────────────────────┐
                // │ Related Commands                 │
                // │ ls -la | cd | pwd | git status   │
                // └──────────────────────────────────┘
                // =========================================================================
                RelatedCommandsBar(
                    activeQuery = state.activeQuery,
                    suggestions = state.relatedCommands,
                    oneHandedMode = state.oneHandedMode,
                    isBottomVaultExpanded = state.isBottomVaultExpanded,
                    onCommandClick = { cmd ->
                        performTick()
                        handler.onSelectCommandSuggestion(cmd)
                    },
                    onCycleOneHanded = {
                        performTick()
                        handler.onCycleOneHandedMode()
                    },
                    onToggleVault = {
                        performTick()
                        handler.onToggleBottomVault()
                    },
                    onOpenApp = onOpenFullAppClick
                )

                SubtleDivider()

                // =========================================================================
                // 2. TERMUX SHORTCUT KEYS BAR
                // ├──────────────────────────────────┤
                // │ CTRL ALT ESC TAB ↑ ↓ ← → ~ | &&  │
                // └──────────────────────────────────┘
                // =========================================================================
                TermuxShortcutRow(
                    ctrlLatched = state.ctrlLatched,
                    altLatched = state.altLatched,
                    onShortcutClick = { key ->
                        performTick()
                        when (key) {
                            TermuxShortcutKey.CTRL -> handler.onToggleCtrl()
                            TermuxShortcutKey.ALT -> handler.onToggleAlt()
                            else -> handler.onShortcutKey(key)
                        }
                    }
                )

                SubtleDivider()

                // =========================================================================
                // 3. NORMAL KEYBOARD (QWERTY / SYMBOLS)
                // ├──────────────────────────────────┤
                // │         Normal Keyboard          │
                // └──────────────────────────────────┘
                // =========================================================================
                NormalQwertyKeyboard(
                    shiftState = state.shiftState,
                    isSymbolLayer = state.isSymbolLayer,
                    ctrlLatched = state.ctrlLatched,
                    altLatched = state.altLatched,
                    onKeyPress = { ch ->
                        performTick()
                        handler.onCharacterKey(ch)
                    },
                    onShiftClick = {
                        performTick()
                        handler.onToggleShift()
                    },
                    onBackspace = {
                        performTick()
                        handler.onBackspace()
                    },
                    onSymbolToggle = {
                        performTick()
                        handler.onToggleSymbolLayer()
                    },
                    onSwitchIme = {
                        performTick()
                        handler.onSwitchInputMethod()
                    },
                    onSpace = {
                        performTick()
                        handler.onSpace()
                    },
                    onEnter = {
                        performTick()
                        handler.onEnter()
                    }
                )

                // =========================================================================
                // 4 & 5. RECENT (Top 3 Clipboard) + MOST USED / RELATED
                // ├──────────────────────────────────┤
                // │ RECENT                           │
                // │ 1. latest copied item            │
                // │ 2. second copied item            │
                // │ 3. third copied item             │
                // ├──────────────────────────────────┤
                // │ MOST USED / RELATED              │
                // │ pkg update                       │
                // │ git status                       │
                // │ cd ~/projects                    │
                // └──────────────────────────────────┘
                // =========================================================================
                AnimatedVisibility(
                    visible = state.isBottomVaultExpanded,
                    enter = expandVertically(animationSpec = tween(180)),
                    exit = shrinkVertically(animationSpec = tween(180))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SubtleDivider()
                        RecentAndMostUsedDeck(
                            activeQuery = state.activeQuery,
                            recentThree = state.recentClipboardTop3,
                            mostUsedAndRelated = state.mostUsedAndRelated,
                            onClipboardItemClick = { item ->
                                performTick()
                                handler.onSelectClipboardItem(item)
                            },
                            onRankedSuggestionClick = { item ->
                                performTick()
                                handler.onSelectRankedSuggestion(item)
                            }
                        )
                    }
                }
            }

            if (state.oneHandedMode == OneHandedMode.LEFT) {
                OneHandedSidebar(
                    isLeft = false,
                    onExpandFull = {
                        performTick()
                        handler.onCycleOneHandedMode()
                    }
                )
            }
        }
    }
}

@Composable
private fun RelatedCommandsBar(
    activeQuery: String,
    suggestions: List<CommandEntity>,
    oneHandedMode: OneHandedMode,
    isBottomVaultExpanded: Boolean,
    onCommandClick: (CommandEntity) -> Unit,
    onCycleOneHanded: () -> Unit,
    onToggleVault: () -> Unit,
    onOpenApp: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("related_commands_section")
    ) {
        // Header Row: "Related Commands" + Simple Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Related Commands",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (activeQuery.isNotBlank()) {
                    Text(
                        text = "• \"$activeQuery\"",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = TermuxGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 150.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // One-Handed Ergonomic Toggle
                SmallToolbarChip(
                    label = when (oneHandedMode) {
                        OneHandedMode.FULL -> "1-Hand"
                        OneHandedMode.LEFT -> "◀ Left"
                        OneHandedMode.RIGHT -> "Right ▶"
                    },
                    isActive = oneHandedMode != OneHandedMode.FULL,
                    onClick = onCycleOneHanded,
                    testTag = "one_handed_mode_button"
                )

                // Toggle Clipboard & Most-Used Bottom Deck
                SmallToolbarChip(
                    label = if (isBottomVaultExpanded) "Hide Panel" else "Clipboard",
                    isActive = !isBottomVaultExpanded,
                    onClick = onToggleVault,
                    testTag = "toggle_vault_button"
                )

                if (onOpenApp != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(TerminalKeySurface)
                            .clickable(onClick = onOpenApp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("open_companion_app_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Open Termux Keyboard Settings",
                            tint = TermuxGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Suggestion Chips Row: ls -la | cd | pwd | git status
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("related_commands_row"),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 1.dp)
        ) {
            itemsIndexed(suggestions, key = { _, item -> "rel_${item.id}_${item.command}" }) { index, cmd ->
                val isTopMatch = index == 0 && activeQuery.isNotBlank()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                if (isTopMatch) TermuxGreen else TerminalSurfaceElevated
                            )
                            .border(
                                width = 1.dp,
                                color = if (isTopMatch) TermuxGreen else TerminalBorder,
                                shape = RoundedCornerShape(7.dp)
                            )
                            .clickable { onCommandClick(cmd) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("related_cmd_chip_$index")
                    ) {
                        Text(
                            text = cmd.command.trim(),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = if (isTopMatch) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isTopMatch) Color(0xFF00210E) else TextPrimary
                        )
                    }

                    if (index < suggestions.lastIndex) {
                        Text(
                            text = " | ",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallToolbarChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (isActive) ElectricCyanContainer else TerminalKeySurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = if (isActive) ElectricCyan else TextSecondary
        )
    }
}

@Composable
private fun TermuxShortcutRow(
    ctrlLatched: Boolean,
    altLatched: Boolean,
    onShortcutClick: (TermuxShortcutKey) -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceElevated)
            .horizontalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .testTag("termux_shortcut_bar"),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TermuxShortcutKey.entries.forEach { key ->
            val isLatched = (key == TermuxShortcutKey.CTRL && ctrlLatched) ||
                (key == TermuxShortcutKey.ALT && altLatched)
            val isRepeatableArrow = key in listOf(
                TermuxShortcutKey.UP,
                TermuxShortcutKey.DOWN,
                TermuxShortcutKey.LEFT,
                TermuxShortcutKey.RIGHT
            )

            val bgColor = when {
                isLatched -> TermuxGreen
                key in listOf(TermuxShortcutKey.CTRL, TermuxShortcutKey.ALT, TermuxShortcutKey.ESC, TermuxShortcutKey.TAB) ->
                    TerminalKeyModifier
                else -> TerminalKeySurface
            }
            val textColor = when {
                isLatched -> Color(0xFF00210E)
                key in listOf(TermuxShortcutKey.PIPE, TermuxShortcutKey.AND_AND, TermuxShortcutKey.TILDE) ->
                    ElectricCyan
                else -> TextPrimary
            }

            val baseModifier = Modifier
                .height(32.dp)
                .widthIn(min = 38.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .border(
                    width = 1.dp,
                    color = if (isLatched) TermuxGreen else TerminalBorder,
                    shape = RoundedCornerShape(6.dp)
                )

            val interactiveModifier = if (isRepeatableArrow) {
                baseModifier.pointerInput(key) {
                    detectTapGestures(
                        onPress = {
                            onShortcutClick(key)
                            val job = coroutineScope.launch {
                                delay(350)
                                while (true) {
                                    onShortcutClick(key)
                                    delay(65)
                                }
                            }
                            tryAwaitRelease()
                            job.cancel()
                        }
                    )
                }
            } else {
                baseModifier.clickable { onShortcutClick(key) }
            }

            Box(
                modifier = interactiveModifier
                    .padding(horizontal = 8.dp)
                    .testTag("shortcut_key_${key.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (key == TermuxShortcutKey.CTRL || key == TermuxShortcutKey.ALT) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isLatched) Color(0xFF00210E) else TextMuted)
                        )
                    }
                    Text(
                        text = key.label,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun NormalQwertyKeyboard(
    shiftState: ShiftState,
    isSymbolLayer: Boolean,
    ctrlLatched: Boolean,
    altLatched: Boolean,
    onKeyPress: (String) -> Unit,
    onShiftClick: () -> Unit,
    onBackspace: () -> Unit,
    onSymbolToggle: () -> Unit,
    onSwitchIme: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit
) {
    val rows = if (isSymbolLayer) symbolRows else qwertyRows
    val isUpper = shiftState != ShiftState.OFF && !isSymbolLayer
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalBg)
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .testTag("normal_keyboard_section"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Row 0: Dedicated Number Row (1 - 0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rows[0].forEach { digit ->
                TactileKeycap(
                    label = digit,
                    modifier = Modifier
                        .weight(1f)
                        .height(33.dp),
                    fontSizeSp = 13,
                    backgroundColor = TerminalSurfaceElevated,
                    textColor = TextSecondary,
                    onClick = { onKeyPress(digit) }
                )
            }
        }

        // Row 1: Q - P
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rows[1].forEach { rawKey ->
                val label = if (isUpper) rawKey.uppercase() else rawKey
                TactileKeycap(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }
        }

        // Row 2: A - L
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSymbolLayer) 0.dp else 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rows[2].forEach { rawKey ->
                val label = if (isUpper) rawKey.uppercase() else rawKey
                TactileKeycap(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }
        }

        // Row 3: Shift + Z - M + Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSymbolLayer) {
                val shiftActive = shiftState != ShiftState.OFF
                TactileKeycap(
                    label = when (shiftState) {
                        ShiftState.OFF -> "⇧"
                        ShiftState.ONCE -> "⬆"
                        ShiftState.CAPS_LOCK -> "⇪"
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(40.dp)
                        .testTag("key_shift"),
                    fontSizeSp = 16,
                    backgroundColor = if (shiftActive) TermuxGreen else TerminalKeyModifier,
                    textColor = if (shiftActive) Color(0xFF00210E) else TextPrimary,
                    onClick = onShiftClick
                )
            }

            rows[3].forEach { rawKey ->
                val label = if (isUpper) rawKey.uppercase() else rawKey
                TactileKeycap(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }

            // Hold-to-repeat Backspace Key
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(TerminalKeyModifier)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(7.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onBackspace()
                                val job = coroutineScope.launch {
                                    delay(350)
                                    while (true) {
                                        onBackspace()
                                        delay(50)
                                    }
                                }
                                tryAwaitRelease()
                                job.cancel()
                            }
                        )
                    }
                    .testTag("key_backspace"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Row 4: ?123 | Globe | / | Spacebar | . | Enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileKeycap(
                label = if (isSymbolLayer) "ABC" else "?123",
                modifier = Modifier
                    .weight(1.25f)
                    .height(40.dp)
                    .testTag("key_symbol_toggle"),
                fontSizeSp = 12,
                backgroundColor = TerminalKeyModifier,
                textColor = ElectricCyan,
                onClick = onSymbolToggle
            )

            // Switch Keyboard (Globe) button
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(TerminalKeyModifier)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(7.dp))
                    .clickable(onClick = onSwitchIme)
                    .testTag("key_switch_ime"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch Keyboard",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            TactileKeycap(
                label = "/",
                modifier = Modifier
                    .weight(0.85f)
                    .height(40.dp),
                fontSizeSp = 15,
                backgroundColor = TerminalKeyModifier,
                onClick = { onKeyPress("/") }
            )

            // Spacebar
            val spaceInteraction = remember { MutableInteractionSource() }
            val spacePressed by spaceInteraction.collectIsPressedAsState()
            val spaceBg by animateColorAsState(
                targetValue = if (spacePressed) TerminalKeyModifier else TerminalKeySurface,
                label = "spaceBg"
            )
            Box(
                modifier = Modifier
                    .weight(3.6f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(spaceBg)
                    .border(
                        width = 1.dp,
                        color = if (ctrlLatched || altLatched) TermuxGreen else TerminalBorder,
                        shape = RoundedCornerShape(7.dp)
                    )
                    .clickable(
                        interactionSource = spaceInteraction,
                        indication = null,
                        onClick = onSpace
                    )
                    .testTag("key_space"),
                contentAlignment = Alignment.Center
            ) {
                val spaceLabel = when {
                    ctrlLatched && altLatched -> "CTRL + ALT"
                    ctrlLatched -> "CTRL ACTIVE"
                    altLatched -> "ALT ACTIVE"
                    else -> "Termux"
                }
                Text(
                    text = spaceLabel,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (ctrlLatched || altLatched) TermuxGreen else TextSecondary
                )
            }

            TactileKeycap(
                label = "-",
                modifier = Modifier
                    .weight(0.85f)
                    .height(40.dp),
                fontSizeSp = 15,
                backgroundColor = TerminalKeyModifier,
                onClick = { onKeyPress("-") }
            )

            // Enter / Return Key
            val enterInteraction = remember { MutableInteractionSource() }
            val enterPressed by enterInteraction.collectIsPressedAsState()
            val enterScale by animateFloatAsState(
                targetValue = if (enterPressed) 0.94f else 1f,
                animationSpec = spring(),
                label = "enterScale"
            )
            Box(
                modifier = Modifier
                    .weight(1.45f)
                    .height(40.dp)
                    .scale(enterScale)
                    .clip(RoundedCornerShape(7.dp))
                    .background(TermuxGreen)
                    .clickable(
                        interactionSource = enterInteraction,
                        indication = null,
                        onClick = onEnter
                    )
                    .testTag("key_enter"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = Color(0xFF00210E),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun TactileKeycap(
    label: String,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 15,
    backgroundColor: Color = TerminalKeySurface,
    textColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "keyScale"
    )
    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) TermuxGreen.copy(alpha = 0.25f) else backgroundColor,
        animationSpec = tween(60),
        label = "keyBg"
    )
    val borderColor = if (isPressed) TermuxGreen else TerminalBorder

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(7.dp))
            .background(animatedBg)
            .border(1.dp, borderColor, RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSizeSp.sp,
            color = if (isPressed) TermuxGreen else textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentAndMostUsedDeck(
    activeQuery: String,
    recentThree: List<ClipboardEntity>,
    mostUsedAndRelated: List<RankedSuggestion>,
    onClipboardItemClick: (ClipboardEntity) -> Unit,
    onRankedSuggestionClick: (RankedSuggestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        // =========================================================================
        // 4. RECENT (3 Most Recently Copied Items)
        // ├──────────────────────────────────┤
        // │ RECENT                           │
        // │ 1. latest copied item            │
        // │ 2. second copied item            │
        // │ 3. third copied item             │
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recent_clipboard_section"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = ElectricCyan,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "Tap to insert",
                fontFamily = SpaceGroteskFamily,
                fontSize = 9.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        if (recentThree.isEmpty()) {
            Text(
                text = "1. (Copy any text in Termux to appear here)",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                recentThree.take(3).forEachIndexed { idx, clip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .background(TerminalSurfaceElevated)
                            .clickable { onClipboardItemClick(clip) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("recent_clip_item_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. ",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                        Text(
                            text = clip.text.replace("\n", " ↵ "),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        SubtleDivider()
        Spacer(modifier = Modifier.height(4.dp))

        // =========================================================================
        // 5. MOST USED / RELATED
        // ├──────────────────────────────────┤
        // │ MOST USED / RELATED              │
        // │ pkg update                       │
        // │ git status                       │
        // │ cd ~/projects                    │
        // └──────────────────────────────────┘
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("most_used_related_section"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (activeQuery.isNotBlank()) {
                    "MOST USED / RELATED (\"$activeQuery\")"
                } else {
                    "MOST USED / RELATED"
                },
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = AmberAlert,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (activeQuery.isNotBlank()) "Matching history" else "By frequency",
                fontFamily = SpaceGroteskFamily,
                fontSize = 9.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            mostUsedAndRelated.take(3).forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(TerminalSurfaceElevated)
                        .clickable { onRankedSuggestionClick(item) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("most_used_item_$idx"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.displayLabel.replace("\n", " ↵ "),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (item.isClipboard) ElectricCyan else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.isClipboard) {
                        Text(
                            text = "CLIP",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            color = ElectricCyan,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    } else if (item.usageCount > 0) {
                        Text(
                            text = "×${item.usageCount}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OneHandedSidebar(
    isLeft: Boolean,
    onExpandFull: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(48.dp)
            .height(260.dp)
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder)
            .clickable(onClick = onExpandFull)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = "Expand Full Width",
            tint = ElectricCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isLeft) "◀\nFULL" else "▶\nFULL",
            fontFamily = SpaceGroteskFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SubtleDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TerminalBorder)
    )
}
