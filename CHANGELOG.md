# Better_Lib Changelog

## Version 1.0.112 (Minecraft 1.21.1)

### Features & Enhancements
- **Other Mod Configuration Support (Create Mod Parity)**:
  - Added "Other Mods" browser (`OtherModConfigsScreen`) to discover and inspect all installed mods.
  - Full bidirectional config integration: view, edit, and save configurations of other mods directly inside Better_Lib's modern UI.
  - Supports deep category hierarchies and nested groups (e.g., Create mod's kinetics, logistics, train categories).
  - Automatically writes modifications directly to `.toml` files (`ModConfigSpec.save()`) and dispatches reload events (`spec.afterReload()`).
  - Added dedicated permission check (`isServerLocked()`): server settings in multiplayer are protected and require operator permission level 2.

- **Modern Configuration Dashboard**:
  - Two-column dashboard with category sidebar navigation, fast search filter, and instant scope switching (Client, Common, Server).
  - Fullscreen scenic background with custom header banner and anti-aliased card containers.
  - Suppressed Minecraft 1.21 full-screen blur shader pass to maintain crisp UI elements and typography.

- **Hybrid Input Controls**:
  - **Boolean Switch**: Smooth animated pill toggle.
  - **Int & Float Sliders**: Slide freely or right-click to enter precise numeric values with overflow prevention.
  - **Enum & Dropdown Selectors**: Click-to-cycle selectors.
  - **Color Picker**: Live color swatch with typable hex code input.
  - **Text Box**: Typable string configuration.

- **Multi-Loader Architecture & Clean Refactoring**:
  - Renamed all UI classes to clean, independent Better_Lib components (`BetterScreenBase`, `BetterWidgetBase`, `BetterIcons`, `BetterLibConfigScreen`) removing legacy references.
  - Native multi-platform adapters for NeoForge (`ModConfigAdapter`), Forge (`ForgeModConfigAdapter`), and Fabric (`FabricModHelper`).
  - Full support on Java 21 across NeoForge, Forge, and Fabric.
