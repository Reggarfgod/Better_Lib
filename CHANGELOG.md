# Better_Lib Changelog

## Version 1.0.113

### 🌟 What's New
- **Welcome Popup Messages**: Added a customizable welcome popup system to show helpful messages, links, and news when players join a world or start up the game.
- **Modern Settings Menu**: Redesigned in-game config screens with clean buttons, sliders, color pickers, and smooth animations.
- **Manage Other Mod Settings**: Conveniently view and change configuration options for your other installed mods in one simple screen.

### 🔧 Fixes & Improvements
- **Villager & Workstation Fixes**: Fixed custom villager professions, job workstations, and custom trades so villagers work properly and trade reliably.
- **Multi-Loader Compatibility**: Fixed issues and improved stability across Fabric, Forge, and NeoForge on Minecraft 1.20.1, 1.21.1, 1.21.11, and 26.1+.
- **User Interface Polish**: Fixed text box inputs, visual styling, and overall responsiveness.

---

## Version 1.0.112 (Multi-Version & Multi-Loader Architecture)

### Multi-Version Environment Overhaul
- **Multi-Version Project Architecture (Parity with zap-hosting-server-integration-menu Multi-Ver-dev)**:
  - Transitioned the entire repository from single-version root structure into organized version-based subprojects:
    - **Minecraft 1.20.1**: Forge (`1.20.1:forge`) and Fabric (`1.20.1:fabric`).
    - **Minecraft 1.21.1**: NeoForge (`1.21.1:neoforge`), Fabric (`1.21.1:fabric`), and Forge (`1.21.1:forge`).
    - **Minecraft 1.21.11**: NeoForge (`1.21.11:neoforge`), Fabric (`1.21.11:fabric`), and Forge (`1.21.11:forge`).
    - **Minecraft 26.1 and above**: NeoForge (`26.1:neoforge`), Fabric (`26.1:fabric`), and Forge (`26.1:forge`).
  - **Unified Root Build**:
    - Centralized `settings.gradle` coordinating all version subprojects and plugin management repositories.
    - Simplified root `build.gradle` with clean all-projects repository and artifact metadata handling.
    - Centralized `gradle.properties` managing version specifications, JVM args, and loader compatibility targets across Java 17, 21, and 25.
  - **Shared Common Module**:
    - Central `:common` subproject providing shared resources (textures, lang files, branding assets, logos, and config models) seamlessly linked to all loaders.

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
  - **Color Picker**: Live color swatch with typable hex code input and smooth corner rendering.
  - **Text Box**: Typable string configuration with full cursor positioning and click navigation.

- **Multi-Loader Architecture & Clean Refactoring**:
  - Renamed all UI classes to clean, independent Better_Lib components (`BetterScreenBase`, `BetterWidgetBase`, `BetterIcons`, `BetterLibConfigScreen`) removing legacy references.
  - Native multi-platform adapters for NeoForge (`ModConfigAdapter`), Forge (`ForgeModConfigAdapter`), and Fabric (`FabricModHelper`).
  - Clean compilation across all configured loaders and environments.
