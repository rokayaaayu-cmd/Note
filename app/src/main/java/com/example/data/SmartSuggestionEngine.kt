package com.example.data

import kotlin.math.min

data class RankedSuggestion(
    val id: String,
    val insertText: String,
    val displayLabel: String,
    val subtitle: String,
    val category: String,
    val usageCount: Int,
    val isClipboard: Boolean,
    val score: Double,
    val commandEntity: CommandEntity? = null,
    val clipboardEntity: ClipboardEntity? = null
)

object SmartSuggestionEngine {

    // Direct Natural Language Intent -> Canonical Termux Command mappings
    private val intentPhraseMappings: Map<String, List<String>> = mapOf(
        "install node" to listOf("pkg install nodejs", "npm init -y", "npm install"),
        "install nodejs" to listOf("pkg install nodejs", "npm install"),
        "install python" to listOf("pkg install python", "python3 -m venv .venv && source .venv/bin/activate"),
        "list files" to listOf("ls -la", "pwd", "find . -name \"\"", "du -sh *"),
        "show files" to listOf("ls -la", "pwd"),
        "open project" to listOf("cd ~/projects", "ls -la", "git status", "opencode"),
        "open projects" to listOf("cd ~/projects", "ls -la", "git status"),
        "git status" to listOf("git status", "git add . && git commit -m \"\"", "git diff", "git log --oneline --graph -n 15"),
        "update packages" to listOf("pkg update && pkg upgrade -y", "pkg update"),
        "update termux" to listOf("pkg update && pkg upgrade -y", "termux-change-repo"),
        "start ssh" to listOf("sshd", "pkg install openssh && sshd", "ifconfig"),
        "check ip" to listOf("ifconfig", "ping -c 4 8.8.8.8"),
        "disk space" to listOf("df -h", "du -sh *"),
        "storage" to listOf("termux-setup-storage", "df -h"),
        "opencode" to listOf("opencode", "opencode run \"\"", "opencode init", "opencode --model "),
        "claude" to listOf("claude", "claude --continue", "claude -p \"\"", "npm install -g @anthropic-ai/claude-code"),
        "kimi" to listOf("kimi", "kimi chat --model moonshot-v1-128k", "export MOONSHOT_API_KEY=\"\"")
    )

    private val defaultQuickBarCommands = listOf(
        "ls -la",
        "cd ~/projects",
        "pwd",
        "git status",
        "pkg update",
        "opencode",
        "claude",
        "kimi"
    )

    /**
     * Extracts the active command segment being typed (e.g. after the last newline, '|', '&&', or ';').
     */
    fun extractActiveQuery(rawInput: String): String {
        val lastLine = rawInput.substringAfterLast('\n')
        val afterChain = lastLine
            .substringAfterLast("&&")
            .substringAfterLast("|")
            .substringAfterLast(";")
        return afterChain.trim()
    }

    /**
     * Computes top ranked command suggestions for the "Related Commands" bar above the keyboard.
     */
    fun rankCommandSuggestions(
        rawInput: String,
        commands: List<CommandEntity>,
        limit: Int = 10
    ): List<CommandEntity> {
        if (commands.isEmpty()) return emptyList()
        val query = extractActiveQuery(rawInput).lowercase()

        // If user hasn't typed anything on the current prompt, show canonical Termux quick commands
        // starting with ls -la | cd ~/projects | pwd | git status plus top used commands
        if (query.isBlank()) {
            val byCommand = commands.associateBy { it.command.trim() }
            val pinnedDefaults = defaultQuickBarCommands.mapNotNull { byCommand[it] }
            val remainingMostUsed = commands
                .filter { cmd -> pinnedDefaults.none { it.id == cmd.id } }
                .sortedWith(compareByDescending<CommandEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp })
            return (pinnedDefaults + remainingMostUsed).take(limit)
        }

        val queryTokens = query.split(Regex("\\s+")).filter { it.isNotBlank() }

        // Check if query matches any natural language intent mapping
        val matchedIntentTargets = LinkedHashSet<String>()
        for ((intentPhrase, targetCommands) in intentPhraseMappings) {
            if (intentPhrase == query ||
                intentPhrase.startsWith(query) ||
                query.contains(intentPhrase) ||
                (queryTokens.size >= 2 && queryTokens.all { token -> intentPhrase.contains(token) })
            ) {
                matchedIntentTargets.addAll(targetCommands)
            }
        }

        val scored = commands.mapNotNull { cmd ->
            val cmdLower = cmd.command.trim().lowercase()
            val nameLower = cmd.name.lowercase()
            val tagsLower = cmd.tags.lowercase()
            val descLower = cmd.description.lowercase()
            val categoryLower = cmd.category.lowercase()
            val relatedLower = cmd.relatedCommands.lowercase()

            var score = 0.0

            // 1. Direct intent phrase match (highest priority: e.g. "install node" -> "pkg install nodejs")
            val intentIndex = matchedIntentTargets.indexOfFirst { it.trim().equals(cmd.command.trim(), ignoreCase = true) }
            if (intentIndex == 0) {
                score += 500.0
            } else if (intentIndex > 0) {
                score += (350.0 - intentIndex * 30.0).coerceAtLeast(180.0)
            }

            // 2. Exact command or prefix match
            if (cmdLower == query) {
                score += 420.0
            } else if (cmdLower.startsWith(query)) {
                score += 280.0
            } else if (cmdLower.contains(query)) {
                score += 170.0
            }

            // 3. Name, tag, or category match
            if (nameLower == query || tagsLower.split(",").any { it.trim() == query }) {
                score += 260.0
            } else if (nameLower.contains(query) || tagsLower.contains(query)) {
                score += 140.0
            } else if (categoryLower.startsWith(query)) {
                score += 110.0
            }

            // 4. Multi-token matching (e.g., "list files", "open project", "install python")
            if (queryTokens.isNotEmpty()) {
                var matchedTokens = 0
                for (token in queryTokens) {
                    if (token.length < 2) continue
                    val inCmd = cmdLower.contains(token)
                    val inName = nameLower.contains(token)
                    val inTags = tagsLower.contains(token)
                    val inDesc = descLower.contains(token)
                    val inRelated = relatedLower.contains(token)
                    if (inCmd || inName || inTags || inDesc || inRelated) {
                        matchedTokens++
                        if (inCmd) score += 45.0
                        if (inName) score += 40.0
                        if (inTags) score += 35.0
                        if (inDesc) score += 15.0
                        if (inRelated) score += 20.0
                    }
                }
                if (matchedTokens == queryTokens.size && queryTokens.size > 1) {
                    score += 120.0
                }
            }

            // 5. Related commands expansion if the user already typed a full known command
            if (relatedLower.split(",").any { it.trim() == query }) {
                score += 95.0
            }

            if (score <= 0.0) {
                null
            } else {
                // Boost by historical usage frequency so frequently used commands rank higher
                val usageBoost = min(cmd.usageCount * 4.5, 90.0)
                Pair(cmd, score + usageBoost)
            }
        }

        if (scored.isEmpty()) {
            // Fallback to most used if no lexical match
            return commands
                .sortedWith(compareByDescending<CommandEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp })
                .take(limit)
        }

        return scored
            .sortedWith(compareByDescending<Pair<CommandEntity, Double>> { it.second }.thenByDescending { it.first.usageCount })
            .map { it.first }
            .take(limit)
    }

    /**
     * Computes items for the "MOST USED / RELATED" section:
     * - When typing a query (e.g. "opencode"), surfaces saved clipboard items AND saved commands
     *   containing or related to the current text, ranked by relevance + usage frequency.
     * - When idle, displays the highest usage-frequency commands and clipboard items (e.g. pkg update, git status, cd ~/projects).
     */
    fun rankMostUsedAndRelated(
        rawInput: String,
        commands: List<CommandEntity>,
        clipboardItems: List<ClipboardEntity>,
        limit: Int = 6
    ): List<RankedSuggestion> {
        val query = extractActiveQuery(rawInput).lowercase()

        if (query.isBlank()) {
            val topCommands = commands
                .sortedWith(compareByDescending<CommandEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp })
                .take(limit)
                .map { cmd ->
                    RankedSuggestion(
                        id = "cmd_${cmd.id}",
                        insertText = cmd.command,
                        displayLabel = cmd.command.trim(),
                        subtitle = "${cmd.name} • ${cmd.category}",
                        category = cmd.category,
                        usageCount = cmd.usageCount,
                        isClipboard = false,
                        score = cmd.usageCount * 10.0,
                        commandEntity = cmd
                    )
                }

            val topClips = clipboardItems
                .filter { clip -> topCommands.none { it.insertText.trim() == clip.text.trim() } }
                .sortedWith(compareByDescending<ClipboardEntity> { it.usageCount }.thenByDescending { it.lastUsedTimestamp })
                .take(3)
                .map { clip ->
                    RankedSuggestion(
                        id = "clip_${clip.id}",
                        insertText = clip.text,
                        displayLabel = clip.text.trim(),
                        subtitle = if (clip.label.isNotBlank()) "Clipboard • ${clip.label}" else "Saved Clipboard",
                        category = "CLIP",
                        usageCount = clip.usageCount,
                        isClipboard = true,
                        score = clip.usageCount * 9.5,
                        clipboardEntity = clip
                    )
                }

            return (topCommands + topClips)
                .sortedWith(compareByDescending<RankedSuggestion> { it.usageCount }.thenByDescending { it.score })
                .take(limit)
        }

        val queryTokens = query.split(Regex("\\s+")).filter { it.isNotBlank() }

        // 1. Find Related Clipboard Suggestions matching current typed text (Feature 6)
        val relatedClips = clipboardItems.mapNotNull { clip ->
            val textLower = clip.text.lowercase()
            val labelLower = clip.label.lowercase()
            var score = 0.0

            if (textLower.startsWith(query)) score += 320.0
            else if (textLower.contains(query)) score += 250.0
            if (labelLower.contains(query)) score += 160.0

            for (token in queryTokens) {
                if (token.length >= 2 && (textLower.contains(token) || labelLower.contains(token))) {
                    score += 65.0
                }
            }

            if (score <= 0.0) null
            else {
                RankedSuggestion(
                    id = "clip_${clip.id}",
                    insertText = clip.text,
                    displayLabel = clip.text.trim(),
                    subtitle = if (clip.label.isNotBlank()) "Related Clip • ${clip.label}" else "Related Clipboard Match",
                    category = "CLIP",
                    usageCount = clip.usageCount,
                    isClipboard = true,
                    score = score + min(clip.usageCount * 6.0, 90.0),
                    clipboardEntity = clip
                )
            }
        }

        // 2. Find Related Commands matching current typed text
        val matchedCmds = rankCommandSuggestions(rawInput, commands, limit = limit).mapIndexed { index, cmd ->
            RankedSuggestion(
                id = "cmd_${cmd.id}",
                insertText = cmd.command,
                displayLabel = cmd.command.trim(),
                subtitle = "${cmd.name} • ${cmd.category}",
                category = cmd.category,
                usageCount = cmd.usageCount,
                isClipboard = false,
                score = (240.0 - index * 20.0) + min(cmd.usageCount * 5.0, 80.0),
                commandEntity = cmd
            )
        }

        return (relatedClips + matchedCmds)
            .distinctBy { it.insertText.trim() }
            .sortedByDescending { it.score }
            .take(limit)
    }
}
