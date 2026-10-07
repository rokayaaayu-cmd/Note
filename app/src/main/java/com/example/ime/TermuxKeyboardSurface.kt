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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
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
        tonalElevation = 8.dp
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

                ThinDivider()

                // =========================================================================
                // 2. TERMUX SHORTCUT KEYS BAR (CTRL ALT ESC TAB ↑ ↓ ← → / ~ | && > < - _)
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

                ThinDivider()

                // =========================================================================
                // 3. NORMAL KEYBOARD (Fast QWERTY + Symbols + 1-Tap Switch IME Key)
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
                    },
                    onSwipeUpFromBottom = {
                        performTick()
                        handler.onSetBottomVaultExpanded(true)
                    }
                )

                ThinDivider()

                // =========================================================================
                // 4. BOTTOM SWIPE-UP HANDLE & TWO-COLUMN PANEL:
                //    - Column 1: RELATED (3 related Termux commands with typed words)
                //    - Column 2: RECENT (Scrolling down 15 recent copies)
                // =========================================================================
                BottomSwipeUpHandleBar(
                    isExpanded = state.isBottomVaultExpanded,
                    activeQuery = state.activeQuery,
                    onSwipeUp = {
                        performTick()
                        handler.onSetBottomVaultExpanded(true)
                    },
                    onSwipeDown = {
                        performTick()
                        handler.onSetBottomVaultExpanded(false)
                    },
                    onToggleClick = {
                        performTick()
                        handler.onToggleBottomVault()
                    }
                )

                AnimatedVisibility(
                    visible = state.isBottomVaultExpanded,
                    enter = expandVertically(animationSpec = tween(180)),
                    exit = shrinkVertically(animationSpec = tween(180))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ThinDivider()
                        SwipeUpTwoColumnDrawer(
                            activeQuery = state.activeQuery,
                            relatedThreeCommands = state.relatedCommands.take(3),
                            recentFifteenCopies = state.recentClipboardFifteen.take(15),
                            onCommandClick = { cmd ->
                                performTick()
                                handler.onSelectCommandSuggestion(cmd)
                            },
                            onClipboardItemClick = { clip ->
                                performTick()
                                handler.onSelectClipboardItem(clip)
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
private fun BottomSwipeUpHandleBar(
    isExpanded: Boolean,
    activeQuery: String,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onToggleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .pointerInput(isExpanded) {
                var totalDragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount
                        if (totalDragY < -14f && !isExpanded) {
                            onSwipeUp()
                            totalDragY = 0f
                        } else if (totalDragY > 14f && isExpanded) {
                            onSwipeDown()
                            totalDragY = 0f
                        }
                    }
                )
            }
            .clickable(onClick = onToggleClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("bottom_swipe_up_handle"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (isExpanded) "Swipe down to close" else "Swipe up for Related & Recent",
            tint = if (isExpanded) AmberAlert else TermuxGreen,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isExpanded) {
                "Swipe down to close • Related (3) & Recent (15)"
            } else if (activeQuery.isNotBlank()) {
                "Swipe up ▲ Related for \"$activeQuery\" (3) & Recent (15)"
            } else {
                "Swipe up ▲ Related Commands (3) & Recent Copies (15)"
            },
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (isExpanded) AmberAlert else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Two-Column Swipe-Up Panel:
 * - Left Column: RELATED (3 related Termux commands matching the currently typed words)
 * - Right Column: RECENT (Vertically scrolling list of 15 most recently copied items)
 */
@Composable
private fun SwipeUpTwoColumnDrawer(
    activeQuery: String,
    relatedThreeCommands: List<CommandEntity>,
    recentFifteenCopies: List<ClipboardEntity>,
    onCommandClick: (CommandEntity) -> Unit,
    onClipboardItemClick: (ClipboardEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .background(TerminalSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // =========================================================================
        // COLUMN 1: RELATED (3 Related Termux Commands with Typed Words)
        // =========================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("most_used_related_section"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (activeQuery.isNotBlank()) {
                        "RELATED (\"$activeQuery\")"
                    } else {
                        "RELATED (3 COMMANDS)"
                    },
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TermuxGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                relatedThreeCommands.take(3).forEachIndexed { idx, cmd ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (idx == 0 && activeQuery.isNotBlank()) TermuxGreenContainer else TerminalSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (idx == 0 && activeQuery.isNotBlank()) TermuxGreen else TerminalBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onCommandClick(cmd) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("related_drawer_cmd_$idx"),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TermuxGreen
                            )
                            Text(
                                text = cmd.command.trim(),
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = cmd.name,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Vertical Divider between RELATED and RECENT columns
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(TerminalBorder)
        )

        // =========================================================================
        // COLUMN 2: RECENT (Scrolling Down 15 Recent Copies)
        // =========================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("recent_clipboard_section"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECENT (${recentFifteenCopies.size}/15)",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = ElectricCyan
                )
                Text(
                    text = "SCROLL ↓",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }

            if (recentFifteenCopies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Copy text in Termux to save up to 15 recent items here.",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .testTag("recent_15_scroll_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 6.dp)
                ) {
                    itemsIndexed(
                        items = recentFifteenCopies.take(15),
                        key = { idx, clip -> "recent15_${clip.id}_$idx" }
                    ) { idx, clip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(TerminalSurfaceElevated)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(5.dp))
                                .clickable { onClipboardItemClick(clip) }
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                                .testTag("recent_clip_item_$idx"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. ",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
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
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("related_commands_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Related Commands",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                if (activeQuery.isNotBlank()) {
                    Text(
                        text = "• \"$activeQuery\"",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.SemiBold,
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallUtilityPill(
                    label = when (oneHandedMode) {
                        OneHandedMode.FULL -> "1-Hand"
                        OneHandedMode.LEFT -> "◀ Left"
                        OneHandedMode.RIGHT -> "Right ▶"
                    },
                    isActive = oneHandedMode != OneHandedMode.FULL,
                    onClick = onCycleOneHanded,
                    testTag = "one_handed_mode_button"
                )

                SmallUtilityPill(
                    label = if (isBottomVaultExpanded) "Recent ▼" else "Recent ▲",
                    isActive = isBottomVaultExpanded,
                    onClick = onToggleVault,
                    testTag = "toggle_vault_button"
                )

                if (onOpenApp != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalKeySurface)
                            .clickable(onClick = onOpenApp)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                            .testTag("open_companion_app_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Keyboard App Data & Settings",
                            tint = TermuxGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

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
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTopMatch) TermuxGreen else TerminalSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isTopMatch) TermuxGreen else TerminalBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onCommandClick(cmd) }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
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
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallUtilityPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) ElectricCyanContainer else TerminalKeySurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 1.5.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
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
            .padding(horizontal = 5.dp, vertical = 4.dp)
            .testTag("termux_shortcut_bar"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                .height(29.dp)
                .widthIn(min = 35.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(bgColor)
                .border(
                    width = 1.dp,
                    color = if (isLatched) TermuxGreen else TerminalBorder,
                    shape = RoundedCornerShape(5.dp)
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
                                    delay(60)
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
                    .padding(horizontal = 7.dp)
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
                                .size(4.dp)
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
    onEnter: () -> Unit,
    onSwipeUpFromBottom: () -> Unit
) {
    val rows = if (isSymbolLayer) symbolRows else qwertyRows
    val isUpper = shiftState != ShiftState.OFF && !isSymbolLayer
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalBg)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("normal_keyboard_section"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 0: Number Row (1 - 0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            rows[0].forEach { digit ->
                TactileKeycap(
                    label = digit,
                    modifier = Modifier
                        .weight(1f)
                        .height(31.dp),
                    fontSizeSp = 12,
                    backgroundColor = TerminalSurfaceElevated,
                    textColor = TextSecondary,
                    onClick = { onKeyPress(digit) }
                )
            }
        }

        // Row 1: Q - P
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            rows[1].forEach { rawKey ->
                val label = if (isUpper) rawKey.uppercase() else rawKey
                TactileKeycap(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }
        }

        // Row 2: A - L
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSymbolLayer) 0.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            rows[2].forEach { rawKey ->
                val label = if (isUpper) rawKey.uppercase() else rawKey
                TactileKeycap(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }
        }

        // Row 3: Shift + Z - M + Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
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
                        .height(38.dp)
                        .testTag("key_shift"),
                    fontSizeSp = 15,
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
                        .height(38.dp),
                    fontSizeSp = 15,
                    onClick = { onKeyPress(label) }
                )
            }

            // Hold-to-repeat Backspace Key
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalKeyModifier)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
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
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Row 4: ?123 | Switch Keyboard (Globe) | / | Spacebar (also supports swipe up!) | - | Enter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var dragY = 0f
                    detectVerticalDragGestures(
                        onDragStart = { dragY = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragY += dragAmount
                            if (dragY < -18f) {
                                onSwipeUpFromBottom()
                                dragY = 0f
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileKeycap(
                label = if (isSymbolLayer) "ABC" else "?123",
                modifier = Modifier
                    .weight(1.2f)
                    .height(38.dp)
                    .testTag("key_symbol_toggle"),
                fontSizeSp = 11,
                backgroundColor = TerminalKeyModifier,
                textColor = ElectricCyan,
                onClick = onSymbolToggle
            )

            // Switch Keyboard (Globe) button - allows instant switching with existing mobile keyboard
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalKeyModifier)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onSwitchIme)
                    .testTag("key_switch_ime"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch Keyboard",
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            TactileKeycap(
                label = "/",
                modifier = Modifier
                    .weight(0.85f)
                    .height(38.dp),
                fontSizeSp = 14,
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
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(spaceBg)
                    .border(
                        width = 1.dp,
                        color = if (ctrlLatched || altLatched) TermuxGreen else TerminalBorder,
                        shape = RoundedCornerShape(6.dp)
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
                    .height(38.dp),
                fontSizeSp = 14,
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
                    .weight(1.4f)
                    .height(38.dp)
                    .scale(enterScale)
                    .clip(RoundedCornerShape(6.dp))
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
                    modifier = Modifier.size(18.dp)
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
        animationSpec = tween(50),
        label = "keyBg"
    )
    val borderColor = if (isPressed) TermuxGreen else TerminalBorder

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(animatedBg)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
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
private fun OneHandedSidebar(
    isLeft: Boolean,
    onExpandFull: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(46.dp)
            .height(230.dp)
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
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TerminalBorder)
    )
}
