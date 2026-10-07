package com.example.data

object TermuxCommandSeedData {

    fun getInitialCommands(): List<CommandEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // ================= TERMUX =================
            CommandEntity(
                command = "pkg update && pkg upgrade -y",
                name = "Update & Upgrade Packages",
                description = "Refreshes Termux package repositories and upgrades all installed packages without prompting.",
                usage = "pkg update && pkg upgrade [-y]",
                examples = "pkg update && pkg upgrade -y\npkg update",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "pkg install, pkg search, pkg clean, termux-change-repo",
                tags = "update packages, upgrade termux, apt update, refresh repo, pkg update",
                usageCount = 18,
                lastUsedTimestamp = now - 60_000L
            ),
            CommandEntity(
                command = "pkg update",
                name = "Update Package Lists",
                description = "Updates the local Termux package index from configured mirrors.",
                usage = "pkg update",
                examples = "pkg update\npkg update && pkg install git",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "pkg upgrade, pkg install, termux-change-repo",
                tags = "update, pkg update, apt, mirror",
                usageCount = 24,
                lastUsedTimestamp = now - 30_000L
            ),
            CommandEntity(
                command = "termux-setup-storage",
                name = "Grant Shared Storage Access",
                description = "Requests Android storage permission and creates ~/storage symlinks (shared, downloads, dcim).",
                usage = "termux-setup-storage",
                examples = "termux-setup-storage\ncd ~/storage/downloads",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "cd ~/storage/shared, ls -la ~/storage",
                tags = "storage permission, sdcard, downloads, setup storage, android files",
                usageCount = 9,
                lastUsedTimestamp = now - 300_000L
            ),
            CommandEntity(
                command = "termux-change-repo",
                name = "Change Termux Mirror Repository",
                description = "Opens an interactive GUI dialog in terminal to select the fastest Termux package mirror.",
                usage = "termux-change-repo",
                examples = "termux-change-repo",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "pkg update, pkg upgrade",
                tags = "mirror, repo, slow download, repository error"
            ),
            CommandEntity(
                command = "termux-wake-lock",
                name = "Acquire CPU Wake Lock",
                description = "Prevents Android from putting Termux background processes or SSH servers to sleep.",
                usage = "termux-wake-lock",
                examples = "termux-wake-lock\nsshd",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "termux-wake-unlock, sshd, tmux",
                tags = "wake lock, background, keep alive, battery optimization"
            ),
            CommandEntity(
                command = "termux-wake-unlock",
                name = "Release CPU Wake Lock",
                description = "Releases the active Termux wake lock so the device can sleep normally.",
                usage = "termux-wake-unlock",
                examples = "termux-wake-unlock",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "termux-wake-lock",
                tags = "unlock, sleep, battery save"
            ),
            CommandEntity(
                command = "pkg search ",
                name = "Search Available Packages",
                description = "Searches the Termux repository for packages matching a keyword.",
                usage = "pkg search <query>",
                examples = "pkg search python\npkg search clang\npkg search openssh",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "pkg install, pkg show, pkg list-all",
                tags = "find package, search pkg, apt search"
            ),
            CommandEntity(
                command = "pkg list-installed",
                name = "List Installed Packages",
                description = "Displays all packages currently installed in your Termux environment.",
                usage = "pkg list-installed",
                examples = "pkg list-installed\npkg list-installed | grep python",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "pkg uninstall, pkg search",
                tags = "installed packages, list pkg, dpkg"
            ),
            CommandEntity(
                command = "termux-reload-settings",
                name = "Reload Termux Styling & Properties",
                description = "Reloads ~/.termux/termux.properties, color schemes, and font settings immediately.",
                usage = "termux-reload-settings",
                examples = "nano ~/.termux/termux.properties && termux-reload-settings",
                category = CommandCategory.TERMUX.displayName,
                relatedCommands = "nano ~/.termux/termux.properties",
                tags = "reload settings, properties, extra keys, font"
            ),

            // ================= FILES =================
            CommandEntity(
                command = "ls -la",
                name = "List All Files (Detailed)",
                description = "Lists all files and directories including hidden dotfiles with permissions, size, and timestamps.",
                usage = "ls -la [path]",
                examples = "ls -la\nls -la ~/projects\nls -lah",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "cd, pwd, git status, tree, find . -name",
                tags = "list files, show hidden files, dir, directory contents, ls",
                usageCount = 19,
                lastUsedTimestamp = now - 45_000L
            ),
            CommandEntity(
                command = "cd ~/projects",
                name = "Open Projects Directory",
                description = "Changes the current working directory to your ~/projects workspace folder.",
                usage = "cd ~/projects",
                examples = "cd ~/projects\nmkdir -p ~/projects && cd ~/projects",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "ls -la, pwd, git status, code .",
                tags = "open project, go to projects, workspace, change directory, cd projects",
                usageCount = 20,
                lastUsedTimestamp = now - 25_000L
            ),
            CommandEntity(
                command = "pwd",
                name = "Print Working Directory",
                description = "Outputs the full absolute path of the current working directory.",
                usage = "pwd",
                examples = "pwd",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "ls -la, cd .., cd ~",
                tags = "current folder, where am i, working directory, path",
                usageCount = 11,
                lastUsedTimestamp = now - 120_000L
            ),
            CommandEntity(
                command = "cd ..",
                name = "Go Up One Directory",
                description = "Moves to the parent directory of the current folder.",
                usage = "cd ..",
                examples = "cd ..\ncd ../..",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "ls -la, pwd, cd -",
                tags = "parent folder, back directory, up folder, cd",
                usageCount = 14,
                lastUsedTimestamp = now - 90_000L
            ),
            CommandEntity(
                command = "mkdir -p ",
                name = "Create Directory Tree",
                description = "Creates a new directory and any necessary parent directories.",
                usage = "mkdir -p <dir_path>",
                examples = "mkdir -p ~/projects/my-app/src",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "cd, ls -la, touch",
                tags = "make folder, new directory, create folder, mkdir"
            ),
            CommandEntity(
                command = "rm -rf ",
                name = "Remove Directory or Files Recursively",
                description = "Forcefully deletes files and directories recursively. Use with caution.",
                usage = "rm -rf <target>",
                examples = "rm -rf node_modules\nrm -rf dist/",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "ls -la, rm, trash",
                tags = "delete folder, remove file, clean directory, rm"
            ),
            CommandEntity(
                command = "cp -r ",
                name = "Copy Files or Directories",
                description = "Copies files or entire directories recursively from source to destination.",
                usage = "cp -r <source> <destination>",
                examples = "cp -r .env.example .env\ncp -r src/ backup_src/",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "mv, rsync -avz, ls -la",
                tags = "copy folder, duplicate file, cp"
            ),
            CommandEntity(
                command = "mv ",
                name = "Move or Rename File",
                description = "Moves or renames files and directories.",
                usage = "mv <source> <destination>",
                examples = "mv old_name.kt new_name.kt\nmv app.apk ~/storage/downloads/",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "cp -r, ls -la",
                tags = "rename file, move file, mv"
            ),
            CommandEntity(
                command = "nano ",
                name = "Edit File in Nano",
                description = "Opens a terminal text editor friendly for mobile touch and shortcut bars.",
                usage = "nano <filename>",
                examples = "nano ~/.bashrc\nnano config.json",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "cat, vim, micro",
                tags = "edit file, text editor, open file, nano"
            ),
            CommandEntity(
                command = "cat ",
                name = "View File Contents",
                description = "Prints the entire contents of a file to standard output.",
                usage = "cat <file>",
                examples = "cat package.json\ncat ~/.ssh/id_ed25519.pub",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "head -n 20, tail -f, less, grep",
                tags = "read file, show file, print file, cat"
            ),
            CommandEntity(
                command = "find . -name \"\"",
                name = "Find Files by Name",
                description = "Recursively searches the current directory tree for files matching a pattern.",
                usage = "find <path> -name \"<pattern>\"",
                examples = "find . -name \"*.py\"\nfind ~/projects -name \"*.apk\"",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "grep -rn, ls -la",
                tags = "find file, search filename, locate file"
            ),
            CommandEntity(
                command = "tail -f ",
                name = "Follow File Log Output Live",
                description = "Outputs the last lines of a file and streams new lines as they are appended.",
                usage = "tail -f <logfile>",
                examples = "tail -f server.log\ntail -n 50 app.log",
                category = CommandCategory.FILES.displayName,
                relatedCommands = "cat, head -n 20, grep",
                tags = "watch log, live log, tail file"
            ),

            // ================= LINUX =================
            CommandEntity(
                command = "chmod +x ",
                name = "Make Script Executable",
                description = "Grants executable permission to a shell script or binary.",
                usage = "chmod +x <script.sh>",
                examples = "chmod +x start.sh && ./start.sh\nchmod +x gradlew",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "ls -la, bash, ./",
                tags = "executable permission, run script, chmod, permission denied"
            ),
            CommandEntity(
                command = "grep -rn \"\" .",
                name = "Search Text Inside Files",
                description = "Recursively searches all files in the current directory for matching text with line numbers.",
                usage = "grep -rn \"<pattern>\" <path>",
                examples = "grep -rn \"TODO\" .\ngrep -rn \"API_KEY\" src/",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "find . -name, cat, awk",
                tags = "search code, find text in files, grep"
            ),
            CommandEntity(
                command = "ps aux | grep ",
                name = "Find Running Process",
                description = "Lists running processes and filters by name or command.",
                usage = "ps aux | grep <process_name>",
                examples = "ps aux | grep node\nps aux | grep python",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "kill -9, htop, pkill",
                tags = "running processes, task manager, check process, ps"
            ),
            CommandEntity(
                command = "kill -9 ",
                name = "Force Kill Process by PID",
                description = "Sends SIGKILL to immediately terminate a stuck process by its PID.",
                usage = "kill -9 <PID>",
                examples = "kill -9 14820",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "ps aux | grep, pkill -f",
                tags = "stop process, kill pid, force quit"
            ),
            CommandEntity(
                command = "df -h",
                name = "Check Disk Space Usage",
                description = "Displays available and used disk storage space in human-readable units (GB/MB).",
                usage = "df -h",
                examples = "df -h\ndu -sh *",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "du -sh *, free -m, pkg clean",
                tags = "disk space, storage full, free space, df"
            ),
            CommandEntity(
                command = "du -sh *",
                name = "Folder Sizes in Current Directory",
                description = "Calculates and prints the total size of each file and subfolder in the current directory.",
                usage = "du -sh *",
                examples = "du -sh *\ndu -sh node_modules",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "df -h, ls -la",
                tags = "folder size, directory size, disk usage, du"
            ),
            CommandEntity(
                command = "tar -xzvf ",
                name = "Extract .tar.gz Archive",
                description = "Extracts a gzipped tar archive into the current directory.",
                usage = "tar -xzvf <archive.tar.gz>",
                examples = "tar -xzvf backup.tar.gz\ntar -czvf archive.tar.gz folder/",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "unzip, tar -czvf",
                tags = "extract tar, unzip targz, unpack archive"
            ),
            CommandEntity(
                command = "tmux new -s dev",
                name = "Start Named Tmux Session",
                description = "Creates a persistent terminal multiplexer session named 'dev' with split panes and background persistence.",
                usage = "tmux new -s <session_name>",
                examples = "tmux new -s dev\ntmux attach -t dev\ntmux ls",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "tmux attach -t dev, tmux ls, htop",
                tags = "tmux session, split terminal, background terminal, multiplexer"
            ),
            CommandEntity(
                command = "htop",
                name = "Interactive System Monitor",
                description = "Launches an interactive CPU, memory, and process viewer inside Termux.",
                usage = "htop",
                examples = "htop\npkg install htop && htop",
                category = CommandCategory.LINUX.displayName,
                relatedCommands = "ps aux | grep, free -m, top",
                tags = "cpu usage, ram monitor, system monitor, htop"
            ),

            // ================= GIT =================
            CommandEntity(
                command = "git status",
                name = "Check Git Repository Status",
                description = "Shows modified, staged, and untracked files in the current Git repository.",
                usage = "git status [-s]",
                examples = "git status\ngit status -s",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git add ., git diff, git commit -m \"\", git log --oneline",
                tags = "git status, check changes, modified files, repo status",
                usageCount = 22,
                lastUsedTimestamp = now - 20_000L
            ),
            CommandEntity(
                command = "git add . && git commit -m \"\"",
                name = "Stage All & Commit Changes",
                description = "Stages all modified and new files and creates a Git commit with your message.",
                usage = "git add . && git commit -m \"<message>\"",
                examples = "git add . && git commit -m \"feat: add smart suggestions\"",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git status, git push origin main, git diff",
                tags = "commit changes, save git, stage all, git commit",
                usageCount = 12,
                lastUsedTimestamp = now - 150_000L
            ),
            CommandEntity(
                command = "git push origin main",
                name = "Push Commits to Remote Main",
                description = "Uploads local branch commits to the remote origin main branch.",
                usage = "git push <remote> <branch>",
                examples = "git push origin main\ngit push -u origin feat-branch",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git pull --rebase, git status, git remote -v",
                tags = "push code, upload github, git push"
            ),
            CommandEntity(
                command = "git pull --rebase",
                name = "Pull & Rebase Latest Changes",
                description = "Fetches latest commits from remote and rebases your local work cleanly on top.",
                usage = "git pull --rebase",
                examples = "git pull --rebase\ngit pull origin main",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git status, git stash, git push origin main",
                tags = "pull changes, sync repo, update git, git pull"
            ),
            CommandEntity(
                command = "git clone ",
                name = "Clone Remote Git Repository",
                description = "Downloads a Git repository from GitHub, GitLab, or any SSH/HTTPS URL.",
                usage = "git clone <repo_url> [folder]",
                examples = "git clone https://github.com/termux/termux-app.git\ngit clone --depth 1 <url>",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "cd ~/projects, ls -la, git status",
                tags = "download repo, clone github, git clone"
            ),
            CommandEntity(
                command = "git log --oneline --graph -n 15",
                name = "Compact Visual Commit History",
                description = "Displays the last 15 commits as a clean, colorized ASCII branch graph.",
                usage = "git log --oneline --graph -n <count>",
                examples = "git log --oneline --graph -n 15",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git status, git diff, git show",
                tags = "commit history, git log, branch graph"
            ),
            CommandEntity(
                command = "git checkout -b ",
                name = "Create & Switch to New Branch",
                description = "Creates a new Git branch and checks it out immediately.",
                usage = "git checkout -b <branch_name>",
                examples = "git checkout -b feature/ime-shortcuts",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git branch -a, git status, git push -u origin",
                tags = "new branch, switch branch, git checkout"
            ),
            CommandEntity(
                command = "git diff",
                name = "View Unstaged Line Changes",
                description = "Shows exact line-by-line additions and deletions in modified files.",
                usage = "git diff [file]",
                examples = "git diff\ngit diff --staged",
                category = CommandCategory.GIT.displayName,
                relatedCommands = "git status, git add .",
                tags = "compare changes, diff code, git diff"
            ),

            // ================= NODE / NPM =================
            CommandEntity(
                command = "pkg install nodejs",
                name = "Install Node.js & NPM",
                description = "Installs the latest Node.js runtime and npm package manager in Termux.",
                usage = "pkg install nodejs [-y]",
                examples = "pkg install nodejs\npkg install nodejs-lts",
                category = CommandCategory.NODE_NPM.displayName,
                relatedCommands = "node -v, npm -v, npm init -y, npm install",
                tags = "install node, install nodejs, install npm, javascript runtime",
                usageCount = 15,
                lastUsedTimestamp = now - 80_000L
            ),
            CommandEntity(
                command = "npm install",
                name = "Install Project NPM Dependencies",
                description = "Installs all dependencies listed in package.json into node_modules.",
                usage = "npm install [package_name]",
                examples = "npm install\nnpm install typescript -D\nnpm i express",
                category = CommandCategory.NODE_NPM.displayName,
                relatedCommands = "npm run dev, npm init -y, pkg install nodejs",
                tags = "install packages, npm i, node modules, dependencies"
            ),
            CommandEntity(
                command = "npm run dev",
                name = "Start Development Server",
                description = "Executes the 'dev' script defined in package.json.",
                usage = "npm run dev",
                examples = "npm run dev\nnpm run dev -- --host 0.0.0.0",
                category = CommandCategory.NODE_NPM.displayName,
                relatedCommands = "npm run build, npm start, npm install",
                tags = "run server, start dev, vite, next dev, npm run"
            ),
            CommandEntity(
                command = "npm init -y",
                name = "Initialize New package.json",
                description = "Creates a new package.json file with default values in the current directory.",
                usage = "npm init -y",
                examples = "mkdir my-app && cd my-app && npm init -y",
                category = CommandCategory.NODE_NPM.displayName,
                relatedCommands = "npm install, pkg install nodejs",
                tags = "new node project, create package.json, npm init"
            ),
            CommandEntity(
                command = "npx ",
                name = "Execute NPM Package Binary",
                description = "Runs a CLI tool from local node_modules or downloads and executes it on the fly.",
                usage = "npx <command> [args]",
                examples = "npx tsx index.ts\nnpx create-vite@latest",
                category = CommandCategory.NODE_NPM.displayName,
                relatedCommands = "npm install, npm run dev",
                tags = "run npx, execute cli, npx"
            ),

            // ================= PYTHON =================
            CommandEntity(
                command = "pkg install python",
                name = "Install Python 3 & Pip",
                description = "Installs Python 3 interpreter and pip package installer in Termux.",
                usage = "pkg install python [-y]",
                examples = "pkg install python\npython3 --version",
                category = CommandCategory.PYTHON.displayName,
                relatedCommands = "python3 -m venv .venv, pip install, python3",
                tags = "install python, python3, pip, setup python",
                usageCount = 10,
                lastUsedTimestamp = now - 200_000L
            ),
            CommandEntity(
                command = "python3 -m venv .venv && source .venv/bin/activate",
                name = "Create & Activate Python Virtualenv",
                description = "Creates an isolated Python virtual environment in .venv and activates it in the current shell.",
                usage = "python3 -m venv .venv && source .venv/bin/activate",
                examples = "python3 -m venv .venv && source .venv/bin/activate\ndeactivate",
                category = CommandCategory.PYTHON.displayName,
                relatedCommands = "pip install -r requirements.txt, python3 main.py",
                tags = "virtual env, venv, activate python, isolate pip"
            ),
            CommandEntity(
                command = "pip install -r requirements.txt",
                name = "Install Python Requirements",
                description = "Installs all Python libraries specified in requirements.txt.",
                usage = "pip install -r requirements.txt",
                examples = "pip install -r requirements.txt\npip freeze > requirements.txt",
                category = CommandCategory.PYTHON.displayName,
                relatedCommands = "python3 -m venv .venv, pip list",
                tags = "pip install, requirements, python packages"
            ),
            CommandEntity(
                command = "python3 -m http.server 8080",
                name = "Start Quick Local HTTP Server",
                description = "Serves files from the current directory over HTTP on port 8080.",
                usage = "python3 -m http.server <port>",
                examples = "python3 -m http.server 8080\npython3 -m http.server 3000 --bind 0.0.0.0",
                category = CommandCategory.PYTHON.displayName,
                relatedCommands = "ifconfig, curl -I http://localhost:8080",
                tags = "web server, share files, local server, http server"
            ),

            // ================= OPENCODE =================
            CommandEntity(
                command = "opencode",
                name = "Launch OpenCode Terminal Agent",
                description = "Starts the OpenCode interactive coding agent in the current project directory.",
                usage = "opencode [project_path]",
                examples = "opencode\nopencode ~/projects/my-app",
                category = CommandCategory.OPENCODE.displayName,
                relatedCommands = "opencode run, opencode init, opencode --model, export OPENCODE_API_KEY=",
                tags = "opencode, ai coding agent, terminal agent, start opencode",
                usageCount = 16,
                lastUsedTimestamp = now - 50_000L
            ),
            CommandEntity(
                command = "opencode run \"\"",
                name = "Run Non-Interactive OpenCode Task",
                description = "Executes a single coding prompt with OpenCode directly from the command line.",
                usage = "opencode run \"<prompt>\"",
                examples = "opencode run \"fix failing unit tests\"\nopencode run \"refactor database layer\"",
                category = CommandCategory.OPENCODE.displayName,
                relatedCommands = "opencode, opencode --model",
                tags = "opencode run, execute prompt, opencode cli, automate code",
                usageCount = 8,
                lastUsedTimestamp = now - 110_000L
            ),
            CommandEntity(
                command = "opencode init",
                name = "Initialize OpenCode Project Context",
                description = "Analyzes the repository and generates an AGENTS.md / OpenCode configuration file.",
                usage = "opencode init",
                examples = "cd ~/projects/app && opencode init",
                category = CommandCategory.OPENCODE.displayName,
                relatedCommands = "opencode, opencode config set",
                tags = "opencode init, setup opencode, agents.md"
            ),
            CommandEntity(
                command = "opencode --model ",
                name = "Run OpenCode with Specific Model",
                description = "Starts OpenCode using a specified provider and model identifier.",
                usage = "opencode --model <provider/model>",
                examples = "opencode --model anthropic/claude-3-7-sonnet\nopencode --model google/gemini-2.5-pro",
                category = CommandCategory.OPENCODE.displayName,
                relatedCommands = "opencode, opencode run \"\"",
                tags = "opencode model, switch model, provider"
            ),

            // ================= CLAUDE =================
            CommandEntity(
                command = "claude",
                name = "Start Claude Code REPL",
                description = "Launches the interactive Claude Code CLI session in the current working directory.",
                usage = "claude [prompt]",
                examples = "claude\nclaude \"explain the architecture of this repo\"",
                category = CommandCategory.CLAUDE.displayName,
                relatedCommands = "claude --continue, claude -p \"\", npm install -g @anthropic-ai/claude-code",
                tags = "claude, claude code, anthropic cli, ai assistant",
                usageCount = 13,
                lastUsedTimestamp = now - 70_000L
            ),
            CommandEntity(
                command = "claude --continue",
                name = "Resume Most Recent Claude Session",
                description = "Continues the latest Claude Code conversation in the current directory.",
                usage = "claude --continue",
                examples = "claude --continue\nclaude -c",
                category = CommandCategory.CLAUDE.displayName,
                relatedCommands = "claude, claude --resume",
                tags = "claude continue, resume session, claude history"
            ),
            CommandEntity(
                command = "claude -p \"\"",
                name = "Run Headless Claude Prompt",
                description = "Queries Claude Code in non-interactive print mode and exits after outputting the result.",
                usage = "claude -p \"<prompt>\"",
                examples = "claude -p \"write a git commit message for staged changes\"\ngit diff | claude -p \"review this diff\"",
                category = CommandCategory.CLAUDE.displayName,
                relatedCommands = "claude, claude --continue",
                tags = "claude print, headless claude, pipe to claude"
            ),
            CommandEntity(
                command = "npm install -g @anthropic-ai/claude-code",
                name = "Install / Update Claude Code CLI",
                description = "Globally installs or updates the official Claude Code package via npm.",
                usage = "npm install -g @anthropic-ai/claude-code",
                examples = "npm install -g @anthropic-ai/claude-code",
                category = CommandCategory.CLAUDE.displayName,
                relatedCommands = "claude, pkg install nodejs",
                tags = "install claude, update claude code, anthropic npm"
            ),

            // ================= KIMI =================
            CommandEntity(
                command = "kimi",
                name = "Launch Kimi CLI Assistant",
                description = "Starts the Kimi AI command-line interface for long-context code analysis and chat.",
                usage = "kimi [subcommand]",
                examples = "kimi\nkimi chat\nkimi --model moonshot-v1-128k",
                category = CommandCategory.KIMI.displayName,
                relatedCommands = "kimi chat, export MOONSHOT_API_KEY=",
                tags = "kimi, moonshot ai, kimi cli, start kimi",
                usageCount = 7,
                lastUsedTimestamp = now - 160_000L
            ),
            CommandEntity(
                command = "kimi chat --model moonshot-v1-128k",
                name = "Kimi 128k Context Code Chat",
                description = "Starts an interactive Kimi session using the 128k long-context Moonshot model.",
                usage = "kimi chat --model <model_name>",
                examples = "kimi chat --model moonshot-v1-128k",
                category = CommandCategory.KIMI.displayName,
                relatedCommands = "kimi, export MOONSHOT_API_KEY=",
                tags = "kimi chat, moonshot 128k, kimi model"
            ),
            CommandEntity(
                command = "export MOONSHOT_API_KEY=\"\"",
                name = "Set Kimi / Moonshot API Key",
                description = "Exports the MOONSHOT_API_KEY environment variable for Kimi CLI tools.",
                usage = "export MOONSHOT_API_KEY=\"<key>\"",
                examples = "export MOONSHOT_API_KEY=\"sk-...\"",
                category = CommandCategory.KIMI.displayName,
                relatedCommands = "kimi, nano ~/.bashrc",
                tags = "kimi api key, moonshot key, export key"
            ),

            // ================= SSH =================
            CommandEntity(
                command = "pkg install openssh && sshd",
                name = "Install & Start OpenSSH Server",
                description = "Installs OpenSSH and starts the Termux SSH daemon on port 8022.",
                usage = "pkg install openssh && sshd",
                examples = "pkg install openssh && passwd && sshd\nwhoami && ifconfig",
                category = CommandCategory.SSH.displayName,
                relatedCommands = "sshd, pkill sshd, ssh-keygen -t ed25519, whoami",
                tags = "start ssh server, install ssh, openssh, port 8022, remote login"
            ),
            CommandEntity(
                command = "sshd",
                name = "Start Termux SSH Daemon",
                description = "Starts the local OpenSSH server in Termux (listens on port 8022 by default).",
                usage = "sshd",
                examples = "sshd\npkill sshd",
                category = CommandCategory.SSH.displayName,
                relatedCommands = "pkill sshd, ifconfig, whoami",
                tags = "start ssh, run sshd, ssh server"
            ),
            CommandEntity(
                command = "ssh -p 8022 ",
                name = "Connect to Remote SSH Host",
                description = "Connects to an SSH server (defaults to port 8022 for Termux targets or 22 for standard servers).",
                usage = "ssh [-p port] <user>@<host>",
                examples = "ssh -p 8022 u0_a245@192.168.1.50\nssh root@10.0.0.1",
                category = CommandCategory.SSH.displayName,
                relatedCommands = "ssh-keygen -t ed25519, scp -P 8022",
                tags = "ssh connect, remote shell, login server"
            ),
            CommandEntity(
                command = "ssh-keygen -t ed25519 -C \"termux\"",
                name = "Generate Ed25519 SSH Keypair",
                description = "Generates a modern, secure Ed25519 SSH keypair in ~/.ssh/id_ed25519.",
                usage = "ssh-keygen -t ed25519 -C \"<comment>\"",
                examples = "ssh-keygen -t ed25519 -C \"termux\"\ncat ~/.ssh/id_ed25519.pub",
                category = CommandCategory.SSH.displayName,
                relatedCommands = "cat ~/.ssh/id_ed25519.pub, ssh-copy-id",
                tags = "generate ssh key, github ssh key, ed25519, keygen"
            ),

            // ================= NETWORKING =================
            CommandEntity(
                command = "ifconfig",
                name = "Show Network Interfaces & Local IP",
                description = "Displays active network interfaces and IPv4/IPv6 addresses (e.g., wlan0 local IP).",
                usage = "ifconfig [interface]",
                examples = "ifconfig\nifconfig wlan0",
                category = CommandCategory.NETWORKING.displayName,
                relatedCommands = "ip addr show, ping -c 4 8.8.8.8, sshd",
                tags = "my ip address, local ip, wifi ip, network interface, ifconfig"
            ),
            CommandEntity(
                command = "curl -sL ",
                name = "Fetch URL / API Response with cURL",
                description = "Downloads or inspects an HTTP/HTTPS endpoint silently while following redirects.",
                usage = "curl -sL <url>",
                examples = "curl -sL https://api.github.com/zen\ncurl -I https://example.com",
                category = CommandCategory.NETWORKING.displayName,
                relatedCommands = "wget -c, ping -c 4 8.8.8.8",
                tags = "http request, download url, api test, curl"
            ),
            CommandEntity(
                command = "ping -c 4 8.8.8.8",
                name = "Test Internet Connectivity (Ping)",
                description = "Sends 4 ICMP echo requests to 8.8.8.8 to measure latency and packet loss.",
                usage = "ping -c <count> <host>",
                examples = "ping -c 4 8.8.8.8\nping -c 4 github.com",
                category = CommandCategory.NETWORKING.displayName,
                relatedCommands = "ifconfig, dig +short, curl -I",
                tags = "test network, check internet, latency, ping"
            ),
            CommandEntity(
                command = "netstat -tulnp",
                name = "List Open Ports & Listening Services",
                description = "Shows all active TCP/UDP listening ports and their associated process IDs.",
                usage = "netstat -tulnp",
                examples = "netstat -tulnp\nss -tulnp",
                category = CommandCategory.NETWORKING.displayName,
                relatedCommands = "ps aux | grep, kill -9, nmap -sV",
                tags = "check ports, open ports, listening port, netstat"
            ),
            CommandEntity(
                command = "wget -c ",
                name = "Download File with Resume Support",
                description = "Downloads a file from HTTP/HTTPS/FTP and automatically resumes interrupted transfers.",
                usage = "wget -c <url>",
                examples = "wget -c https://example.com/archive.tar.gz",
                category = CommandCategory.NETWORKING.displayName,
                relatedCommands = "curl -LO, ls -la, tar -xzvf",
                tags = "download file, resume download, wget"
            ),

            // ================= ADB =================
            CommandEntity(
                command = "pkg install android-tools && adb devices -l",
                name = "Install ADB & List Connected Devices",
                description = "Installs Android Platform Tools (adb/fastboot) in Termux and lists attached devices.",
                usage = "adb devices -l",
                examples = "pkg install android-tools\nadb devices -l",
                category = CommandCategory.ADB.displayName,
                relatedCommands = "adb pair, adb connect, adb shell",
                tags = "install adb, android tools,无线调试, wireless debugging, adb devices"
            ),
            CommandEntity(
                command = "adb pair 127.0.0.1:",
                name = "Pair Wireless ADB Debugging",
                description = "Pairs Termux ADB with Android 11+ Wireless Debugging using localhost and pairing port.",
                usage = "adb pair 127.0.0.1:<pairing_port> <6_digit_code>",
                examples = "adb pair 127.0.0.1:39451\nadb connect 127.0.0.1:41285",
                category = CommandCategory.ADB.displayName,
                relatedCommands = "adb connect 127.0.0.1:, adb devices -l, adb shell",
                tags = "wireless adb, pair adb, localhost adb, android debugging"
            ),
            CommandEntity(
                command = "adb connect 127.0.0.1:",
                name = "Connect to Wireless ADB Port",
                description = "Connects ADB over TCP/IP to local or remote Android wireless debugging port.",
                usage = "adb connect <ip>:<port>",
                examples = "adb connect 127.0.0.1:41285",
                category = CommandCategory.ADB.displayName,
                relatedCommands = "adb pair 127.0.0.1:, adb shell, adb install -r",
                tags = "connect adb, wireless debugging, tcpip"
            ),
            CommandEntity(
                command = "adb install -r ",
                name = "Install or Replace APK via ADB",
                description = "Installs an APK file onto the connected Android device, keeping existing app data (-r).",
                usage = "adb install -r <path_to_apk>",
                examples = "adb install -r app/build/outputs/apk/debug/app-debug.apk",
                category = CommandCategory.ADB.displayName,
                relatedCommands = "adb devices -l, adb shell pm list packages",
                tags = "install apk, sideload apk, adb install"
            ),
            CommandEntity(
                command = "adb logcat *:E",
                name = "Stream Android Error Logs via ADB",
                description = "Streams live Android system and crash logs filtered to Error level and above.",
                usage = "adb logcat [filter]",
                examples = "adb logcat *:E\nadb logcat -d > crash.log",
                category = CommandCategory.ADB.displayName,
                relatedCommands = "adb shell, tail -f",
                tags = "crash log, android logcat, debug errors"
            ),

            // ================= ANDROID =================
            CommandEntity(
                command = "termux-clipboard-get",
                name = "Read Android System Clipboard",
                description = "Prints the current Android clipboard contents to standard output (requires Termux:API).",
                usage = "termux-clipboard-get",
                examples = "termux-clipboard-get\ntermux-clipboard-get > pasted.txt",
                category = CommandCategory.ANDROID.displayName,
                relatedCommands = "termux-clipboard-set, termux-toast, pkg install termux-api",
                tags = "paste clipboard, get clipboard, termux api"
            ),
            CommandEntity(
                command = "termux-clipboard-set \"\"",
                name = "Copy Text to Android Clipboard",
                description = "Copies text or piped command output directly to the Android system clipboard.",
                usage = "termux-clipboard-set \"<text>\" | <cmd> | termux-clipboard-set",
                examples = "cat ~/.ssh/id_ed25519.pub | termux-clipboard-set\ntermux-clipboard-set \"Hello Termux\"",
                category = CommandCategory.ANDROID.displayName,
                relatedCommands = "termux-clipboard-get, cat",
                tags = "copy to clipboard, set clipboard, termux api"
            ),
            CommandEntity(
                command = "termux-battery-status",
                name = "Inspect Android Battery JSON",
                description = "Outputs device battery percentage, temperature, health, and charging status as JSON.",
                usage = "termux-battery-status",
                examples = "termux-battery-status",
                category = CommandCategory.ANDROID.displayName,
                relatedCommands = "termux-wifi-connectioninfo, termux-toast",
                tags = "check battery, battery status, json sensor"
            ),
            CommandEntity(
                command = "termux-open-url ",
                name = "Open URL in Android Default Browser",
                description = "Launches the default Android web browser with the specified URL.",
                usage = "termux-open-url <url>",
                examples = "termux-open-url http://localhost:8080\ntermux-open-url https://github.com",
                category = CommandCategory.ANDROID.displayName,
                relatedCommands = "termux-open, python3 -m http.server 8080",
                tags = "open browser, launch url, localhost preview"
            ),
            CommandEntity(
                command = "termux-notification -t \"Done\" -c \"Build finished\"",
                name = "Send Android System Notification",
                description = "Displays a native Android notification from a shell script or long-running build.",
                usage = "termux-notification -t \"<title>\" -c \"<content>\"",
                examples = "npm run build && termux-notification -t \"Build\" -c \"Success!\"",
                category = CommandCategory.ANDROID.displayName,
                relatedCommands = "termux-toast, termux-vibrate",
                tags = "notify android, push notification, alert script"
            )
        )
    }

    fun getInitialClipboardItems(): List<ClipboardEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            ClipboardEntity(
                text = "pkg update && pkg upgrade -y",
                label = "Termux Full Update",
                copiedTimestamp = now - 10_000L,
                usageCount = 9,
                lastUsedTimestamp = now - 15_000L,
                isPinned = true
            ),
            ClipboardEntity(
                text = "opencode run \"fix build errors and run tests\"",
                label = "OpenCode Auto-Fix Prompt",
                copiedTimestamp = now - 20_000L,
                usageCount = 6,
                lastUsedTimestamp = now - 35_000L,
                isPinned = true
            ),
            ClipboardEntity(
                text = "git status && git diff --stat",
                label = "Git Quick Inspect",
                copiedTimestamp = now - 30_000L,
                usageCount = 5,
                lastUsedTimestamp = now - 55_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "opencode --model anthropic/claude-3-7-sonnet",
                label = "OpenCode Sonnet Flag",
                copiedTimestamp = now - 40_000L,
                usageCount = 4,
                lastUsedTimestamp = now - 95_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "ssh -p 8022 u0_a245@192.168.1.100",
                label = "Termux Local SSH",
                copiedTimestamp = now - 50_000L,
                usageCount = 3,
                lastUsedTimestamp = now - 150_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "cd ~/projects && ls -la",
                label = "Open Projects Workspace",
                copiedTimestamp = now - 60_000L,
                usageCount = 4,
                lastUsedTimestamp = now - 160_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "python3 -m venv .venv && source .venv/bin/activate",
                label = "Python Virtualenv",
                copiedTimestamp = now - 70_000L,
                usageCount = 3,
                lastUsedTimestamp = now - 170_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "npm install && npm run dev",
                label = "Node Dev Server",
                copiedTimestamp = now - 80_000L,
                usageCount = 3,
                lastUsedTimestamp = now - 180_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "claude --continue",
                label = "Resume Claude Session",
                copiedTimestamp = now - 90_000L,
                usageCount = 2,
                lastUsedTimestamp = now - 190_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "kimi chat --model moonshot-v1-128k",
                label = "Kimi 128k Chat",
                copiedTimestamp = now - 100_000L,
                usageCount = 2,
                lastUsedTimestamp = now - 200_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "termux-setup-storage",
                label = "Storage Permission",
                copiedTimestamp = now - 110_000L,
                usageCount = 2,
                lastUsedTimestamp = now - 210_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "adb pair 127.0.0.1:39451",
                label = "Wireless ADB Pair",
                copiedTimestamp = now - 120_000L,
                usageCount = 1,
                lastUsedTimestamp = now - 220_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "tmux new -s dev",
                label = "Start Tmux Session",
                copiedTimestamp = now - 130_000L,
                usageCount = 2,
                lastUsedTimestamp = now - 230_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "curl -sL https://api.github.com/zen",
                label = "Test cURL Endpoint",
                copiedTimestamp = now - 140_000L,
                usageCount = 1,
                lastUsedTimestamp = now - 240_000L,
                isPinned = false
            ),
            ClipboardEntity(
                text = "git add . && git commit -m \"feat: update\"",
                label = "Quick Git Commit",
                copiedTimestamp = now - 150_000L,
                usageCount = 3,
                lastUsedTimestamp = now - 250_000L,
                isPinned = false
            )
        )
    }
}
