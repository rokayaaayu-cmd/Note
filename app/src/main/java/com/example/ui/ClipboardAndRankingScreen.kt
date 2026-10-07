package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntity
import com.example.data.CommandEntity
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CoralDanger
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
fun ClipboardVaultScreen(
    recentFifteen: List<ClipboardEntity>,
    allClipboardItems: List<ClipboardEntity>,
    onInsertClip: (ClipboardEntity) -> Unit,
    onCopyClip: (String, String) -> Unit,
    onTogglePin: (ClipboardEntity) -> Unit,
    onDeleteClip: (ClipboardEntity) -> Unit,
    onClearUnpinned: () -> Unit,
    onAddManualClip: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TerminalBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricCyan,
                contentColor = Color(0xFF001E2F),
                modifier = Modifier.testTag("add_clipboard_item_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Save Snippet to Clipboard")
                    Text(
                        text = "Save Snippet",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("clipboard_vault_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 84.dp)
        ) {
            // Privacy Assurance Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TerminalSurface)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Local Storage",
                            tint = TermuxGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Local Offline Clipboard Manager",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Clipboard items are stored strictly in on-device SQLite and never uploaded.",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onClearUnpinned,
                        modifier = Modifier.testTag("clear_unpinned_clipboard_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Unpinned Clipboard History",
                            tint = CoralDanger
                        )
                    }
                }
            }

            // Top 3 RECENT Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TerminalSurface)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Recent 3",
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "RECENT (15 COPIES ON KEYBOARD SWIPE-UP)",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ElectricCyan
                            )
                        }
                        Text(
                            text = "Swipe up on keyboard",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    recentFifteen.take(15).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerminalSurfaceElevated)
                                .clickable { onInsertClip(item) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ElectricCyan
                                )
                                Text(
                                    text = item.text,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "COPY",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = TermuxGreen,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ALL SAVED CLIPBOARD ITEMS (${allClipboardItems.size})",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            itemsIndexed(allClipboardItems, key = { _, item -> item.id }) { _, clip ->
                ClipboardCardItem(
                    clip = clip,
                    onInsert = { onInsertClip(clip) },
                    onCopy = { onCopyClip(clip.text, clip.label) },
                    onTogglePin = { onTogglePin(clip) },
                    onDelete = { onDeleteClip(clip) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddClipboardSnippetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { text, label ->
                onAddManualClip(text, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ClipboardCardItem(
    clip: ClipboardEntity,
    onInsert: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricCyanContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = clip.label.ifBlank { "CLIPBOARD" }.uppercase(),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ElectricCyan
                    )
                }

                if (clip.usageCount > 0) {
                    Text(
                        text = "Used ${clip.usageCount}×",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = AmberAlert
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (clip.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Snippet",
                        tint = if (clip.isPinned) AmberAlert else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Snippet",
                        tint = CoralDanger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBg)
                .clickable(onClick = onInsert)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = clip.text,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TermuxGreenContainer)
                        .clickable(onClick = onInsert)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "INSERT",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = TermuxGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalKeySurface)
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "COPY",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun AddClipboardSnippetDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "Save Snippet to Local Clipboard",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Command or Snippet Text *") },
                    placeholder = { Text("e.g. opencode run --agent architect") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_dialog_text_input")
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (Optional)") },
                    placeholder = { Text("e.g. OpenCode Architect Flag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onSave(text, label) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF001E2F)
                ),
                modifier = Modifier.testTag("clip_dialog_save_btn")
            ) {
                Text("Save to Clipboard", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun UsageRankingScreen(
    allCommands: List<CommandEntity>,
    allClipboardItems: List<ClipboardEntity>,
    onInsertCommand: (CommandEntity) -> Unit,
    onInsertClipboard: (ClipboardEntity) -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankedCommands = remember(allCommands) {
        allCommands.sortedWith(
            compareByDescending<CommandEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp }
        ).take(15)
    }
    val rankedClips = remember(allClipboardItems) {
        allClipboardItems.sortedWith(
            compareByDescending<ClipboardEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp }
        ).take(8)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("usage_ranking_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Most Used Ranking",
                        tint = AmberAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Most Used Commands & Copies",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Automatically ranked by usage frequency to power your keyboard's MOST USED / RELATED bar.",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onResetStats,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("reset_usage_stats_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "TOP RANKED COMMANDS BY FREQUENCY",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AmberAlert
            )
        }

        itemsIndexed(rankedCommands, key = { _, cmd -> "rank_cmd_${cmd.id}" }) { index, cmd ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    .clickable { onInsertCommand(cmd) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (index < 3) AmberContainer else TerminalSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#${index + 1}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (index < 3) AmberAlert else TextSecondary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cmd.command.trim(),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TermuxGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${cmd.name} • ${cmd.category}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TermuxGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${cmd.usageCount} uses",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TermuxGreen
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TOP RANKED CLIPBOARD COPIES",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ElectricCyan
            )
        }

        itemsIndexed(rankedClips, key = { _, clip -> "rank_clip_${clip.id}" }) { index, clip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    .clickable { onInsertClipboard(clip) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "#${index + 1}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = clip.text,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (clip.label.isNotBlank()) {
                            Text(
                                text = clip.label,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyanContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${clip.usageCount} uses",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}
