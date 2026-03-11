# Enchant Modal

A Minecraft Forge mod that lets you configure enchantments on any item through a GUI.

## Features

- **`/enchantmodal` command** — Opens an enchantment configuration screen for the item in your main hand
- **Full enchantment list** — Browse all registered enchantments with ON/OFF toggles
- **Level control** — Adjust enchantment levels from 1 to 255
- **Multi-language support** — Automatically follows your Minecraft language setting (English, Korean)

## Requirements

- Minecraft 1.21.11
- Forge 61.1.3+
- Java 21

## Installation

Download the mod JAR from [Releases](https://github.com/dbswnschl/minecraft_enchantmodal/releases) and place it in your `mods/` folder.

## Usage

1. Hold the item you want to enchant in your main hand
2. Run `/enchantmodal` (requires operator permissions)
3. Toggle enchantments ON/OFF and adjust levels
4. Click **Apply** to save or **Cancel** to discard

## Building from Source

```bash
# Java 21 required
./gradlew build
```

The output JAR will be in `build/libs/`.

## License

MIT
