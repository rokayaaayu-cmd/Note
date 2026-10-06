package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.CommandCategory
import com.example.data.CommandEntity
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PurpleAccent
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommandsScreen(
    commands: List<CommandEntity>,
    searchQuery: String,
    selectedCategory: CommandCategory,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (CommandCategory) -> Unit,
    onInsertIntoTerminal: (CommandEntity) -> Unit,
    onCopyCommand: (String) -> Unit,
    onSaveCustomCommand: (
        id: Long,
        command: String,
        name: String,
        description: String,
        usage: String,
        examples: String,
        category: String,
        relatedCommands: String,
        tags: String,
        usageCount: Int
    ) -> Unit,
    onDeleteCommand: (CommandEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedCommand by remember { mutableStateOf<CommandEntity?>(null) }
    var editingCommand by remember { mutableStateOf<CommandEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TerminalBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCommand = null
                    showCreateDialog = true
                },
                containerColor = TermuxGreen,
                contentColor = Color(0xFF00210E),
                modifier = Modifier.testTag("add_custom_command_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Custom Command")
                    Text(
                        text = "New Command",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("command_db_search_input"),
                placeholder = {
                    Text(
                        text = "Search commands, intents (e.g. install node, opencode, ssh)...",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TermuxGreen
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TermuxGreen,
                    unfocusedBorderColor = TerminalBorder,
                    focusedContainerColor = TerminalSurface,
                    unfocusedContainerColor = TerminalSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Category Filter Chips (Termux, Linux, Files, Git, Python, Node/NPM, Android, Networking, SSH, ADB, OpenCode, Claude, Kimi, Custom)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("command_category_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(CommandCategory.entries, key = { it.name }) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TermuxGreen else TerminalSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) TermuxGreen else TerminalBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("category_chip_${cat.name.lowercase()}")
                    ) {
                        Text(
                            text = cat.displayName,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color(0xFF00210E) else TextPrimary
                        )
                    }
                }
            }

            // Count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${commands.size} commands in ${selectedCategory.displayName}",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Tap card for usage & examples",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Command Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("commands_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(commands, key = { "${it.id}_${it.command}" }) { cmd ->
                    CommandDatabaseCard(
                        command = cmd,
                        onCardClick = { inspectedCommand = cmd },
                        onInsertClick = { onInsertIntoTerminal(cmd) },
                        onCopyClick = { onCopyCommand(cmd.command) },
                        onEditClick = {
                            editingCommand = cmd
                            showCreateDialog = true
                        }
                    )
                }
            }
        }
    }

    // Command Details Bottom Sheet
    inspectedCommand?.let { cmd ->
        ModalBottomSheet(
            onDismissRequest = { inspectedCommand = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = TerminalSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryPill(category = cmd.category, isCustom = cmd.isCustom)
                        if (cmd.usageCount > 0) {
                            Text(
                                text = "Used ${cmd.usageCount}×",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = AmberAlert
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                inspectedCommand = null
                                editingCommand = cmd
                                showCreateDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Command",
                                tint = ElectricCyan
                            )
                        }
                        IconButton(
                            onClick = {
                                onDeleteCommand(cmd)
                                inspectedCommand = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Command",
                                tint = CoralDanger
                            )
                        }
                    }
                }

                Text(
                    text = cmd.name,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                // Command Syntax Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalBg)
                        .border(1.dp, TermuxGreen, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "$ ${cmd.command}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TermuxGreen
                    )
                }

                Text(
                    text = cmd.description,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                // Usage Section
                DetailSectionBlock(title = "USAGE SYNTAX", monoContent = cmd.usage)

                // Examples Section
                if (cmd.exampleList().isNotEmpty()) {
                    DetailSectionBlock(
                        title = "PRACTICAL EXAMPLES",
                        monoContent = cmd.exampleList().joinToString("\n") { "$ $it" }
                    )
                }

                // Related Commands Chips
                if (cmd.relatedList().isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "RELATED COMMANDS (TAP TO COPY)",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cmd.relatedList().forEach { rel ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TerminalSurfaceElevated)
                                        .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                                        .clickable { onCopyCommand(rel) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = rel,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = ElectricCyan
                                    )
                                }
                            }
                        }
                    }
                }

                // Tags
                if (cmd.tagList().isNotEmpty()) {
                    Text(
                        text = "Intent triggers & tags: ${cmd.tagList().joinToString(" • ")}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onInsertIntoTerminal(cmd)
                            inspectedCommand = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TermuxGreen,
                            contentColor = Color(0xFF00210E)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Insert in Sandbox",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onCopyCommand(cmd.command)
                            inspectedCommand = null
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Command",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Custom Command Dialog
    if (showCreateDialog) {
        CustomCommandFormDialog(
            initialCommand = editingCommand,
            onDismiss = {
                showCreateDialog = false
                editingCommand = null
            },
            onConfirm = { id, cmdText, name, desc, usage, examples, category, related, tags, usageCount ->
                onSaveCustomCommand(id, cmdText, name, desc, usage, examples, category, related, tags, usageCount)
                showCreateDialog = false
                editingCommand = null
            }
        )
    }
}

@Composable
private fun CommandDatabaseCard(
    command: CommandEntity,
    onCardClick: () -> Unit,
    onInsertClick: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onCardClick)
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
                CategoryPill(category = command.category, isCustom = command.isCustom)
                Text(
                    text = command.name,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (command.usageCount > 0) {
                Text(
                    text = "×${command.usageCount}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AmberAlert
                )
            }
        }

        // Command Monospace Line + Quick Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBg)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = command.command.trim(),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TermuxGreen,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TermuxGreenContainer)
                        .clickable(onClick = onInsertClick)
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
                        .clickable(onClick = onCopyClick)
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

                if (command.isCustom) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalKeySurface)
                            .clickable(onClick = onEditClick)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EDIT",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = PurpleAccent
                        )
                    }
                }
            }
        }

        Text(
            text = command.description,
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (command.relatedList().isNotEmpty()) {
            Text(
                text = "Related: ${command.relatedList().take(4).joinToString(" | ")}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryPill(category: String, isCustom: Boolean) {
    val bgColor = when {
        isCustom || category.equals("Custom", ignoreCase = true) -> Color(0xFF28194B)
        category.equals("OpenCode", ignoreCase = true) ||
            category.equals("Claude", ignoreCase = true) ||
            category.equals("Kimi", ignoreCase = true) -> ElectricCyanContainer
        else -> TermuxGreenContainer
    }
    val textColor = when {
        isCustom || category.equals("Custom", ignoreCase = true) -> PurpleAccent
        category.equals("OpenCode", ignoreCase = true) ||
            category.equals("Claude", ignoreCase = true) ||
            category.equals("Kimi", ignoreCase = true) -> ElectricCyan
        else -> TermuxGreen
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isCustom && !category.equals("Custom", ignoreCase = true)) {
                "$category • CUSTOM"
            } else {
                category.uppercase()
            },
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = textColor
        )
    }
}

@Composable
private fun DetailSectionBlock(title: String, monoContent: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalSurfaceElevated)
                .padding(10.dp)
        ) {
            Text(
                text = monoContent,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun CustomCommandFormDialog(
    initialCommand: CommandEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Long,
        command: String,
        name: String,
        description: String,
        usage: String,
        examples: String,
        category: String,
        relatedCommands: String,
        tags: String,
        usageCount: Int
    ) -> Unit
) {
    var commandText by remember { mutableStateOf(initialCommand?.command ?: "") }
    var name by remember { mutableStateOf(initialCommand?.name ?: "") }
    var description by remember { mutableStateOf(initialCommand?.description ?: "") }
    var usage by remember { mutableStateOf(initialCommand?.usage ?: "") }
    var examples by remember { mutableStateOf(initialCommand?.examples ?: "") }
    var category by remember {
        mutableStateOf(initialCommand?.category ?: CommandCategory.CUSTOM.displayName)
    }
    var relatedCommands by remember { mutableStateOf(initialCommand?.relatedCommands ?: "") }
    var tags by remember { mutableStateOf(initialCommand?.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = if (initialCommand == null) "Save Custom Command" else "Edit Command",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Command to Insert *", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. opencode run \"deploy staging\"") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_cmd_input_command")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name *", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. Deploy Staging with OpenCode") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_cmd_input_name")
                )

                // Category Picker Row
                Text(
                    text = "Category:",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CommandCategory.selectableCategories.forEach { cat ->
                        val selected = category.equals(cat.displayName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) TermuxGreen else TerminalSurfaceElevated)
                                .clickable { category = cat.displayName }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color(0xFF00210E) else TextPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("What does this command do?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_cmd_input_description")
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Optional Tags / Intent Keywords", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. deploy, staging, opencode") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_cmd_input_tags")
                )

                OutlinedTextField(
                    value = relatedCommands,
                    onValueChange = { relatedCommands = it },
                    label = { Text("Related Commands (comma-separated)", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. git status, npm run build") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usage,
                    onValueChange = { usage = it },
                    label = { Text("Usage Syntax (optional)", fontFamily = SpaceGroteskFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = examples,
                    onValueChange = { examples = it },
                    label = { Text("Examples (optional)", fontFamily = SpaceGroteskFamily) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commandText.isNotBlank()) {
                        onConfirm(
                            initialCommand?.id ?: 0L,
                            commandText,
                            name,
                            description,
                            usage,
                            examples,
                            category,
                            relatedCommands,
                            tags,
                            initialCommand?.usageCount ?: 0
                        )
                    }
                },
                enabled = commandText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TermuxGreen,
                    contentColor = Color(0xFF00210E)
                ),
                modifier = Modifier.testTag("save_custom_command_confirm_btn")
            ) {
                Text("Save Command", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
