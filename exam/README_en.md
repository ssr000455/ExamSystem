# ExamSystem User Manual

## System Requirements
- Java 8 or higher
- OpenGL-capable graphics environment (Linux: install mesa-utils)

## How to Launch
./start-exam.sh
or
java -jar ExamSystem.jar

## Features
- Select learning plan (8 systematic modules)
- Multiple question types: coding, choice, fill-in, matching, debugging, completion
- Auto-save progress
- Achievement system and star rewards
- Multi-language UI (7 languages switchable)
- Built-in code editor (syntax highlighting, auto-completion)

## First Use
1. Welcome screen appears
2. Click "Import Plan" to load official plans (or skip if already present)
3. Choose difficulty (Beginner/Intermediate/Advanced)
4. Start answering

## Shortcuts
- Ctrl+S: Save progress
- Ctrl+R: Run code check
- Ctrl+←/→: Previous/Next question
- Esc: Back to section selection

## Config Files
Progress stored in .exam_progress.json, .exam_unlocks.json, .exam_achievements.json.

## Troubleshooting
- If native library missing, ensure OpenGL drivers installed.
- Check Java version: java -version
