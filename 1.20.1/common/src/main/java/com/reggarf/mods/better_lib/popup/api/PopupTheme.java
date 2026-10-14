package com.reggarf.mods.better_lib.popup.api;

public enum PopupTheme {
    MODERN(
            "Modern Dark",
            0xFF16191B, // Card BG
            0xFF2D3236, // Card Border
            0xFF131517, // Sidebar BG
            0xFF25292C, // Sidebar Border
            0xFF5CB85C, // Emerald Green Default Accent
            0xFF17221A, // Highlight BG
            0xFF2A5034, // Highlight Border
            0xFFFFFFFF, // Text Primary
            0xFF8E9396, // Text Secondary
            0xFF202325, // Button Inactive BG
            0xFF353A3D, // Button Hover BG
            false       // Rainbow Bar
    ),
    CLASSIC(
            "Classic Slate",
            0xFF1C1E22, // Slate Navy BG
            0xFF454B52, // Steel Gray Border
            0xFF16181A, // Dark Slate Sidebar
            0xFF33383E, // Sidebar Border
            0xFF4A90E2, // Royal Blue Accent
            0xFF1E2633, // Highlight BG
            0xFF3A557A, // Highlight Border
            0xFFFFFFFF, // Text Primary
            0xFFA0A6AD, // Text Secondary
            0xFF25282E, // Button Inactive BG
            0xFF3A3F47, // Button Hover BG
            false
    ),
    RUSTIC(
            "Rustic Wood",
            0xFF2B2117, // Dark Oak Tone BG
            0xFF5A442E, // Oak Wood Border
            0xFF211912, // Deep Earth Sidebar
            0xFF423222, // Sidebar Border
            0xFFFFAA00, // Vanilla Gold Accent
            0xFF38291B, // Highlight BG
            0xFF8A6539, // Highlight Border
            0xFFFFF3D6, // Text Primary (Cream)
            0xFFC8B293, // Text Secondary (Muted Tan)
            0xFF3B2E21, // Button Inactive BG
            0xFF4D3D2D, // Button Hover BG
            false
    ),
    SAKURA(
            "Sakura Rose",
            0xFF251720, // Midnight Plum / Velvet BG
            0xFF5E354D, // Pastel Rose Border
            0xFF1E1219, // Deep Berry Sidebar
            0xFF48283A, // Sidebar Border
            0xFFFF69B4, // Sakura Pink Accent
            0xFF331D2A, // Highlight BG
            0xFF854167, // Highlight Border
            0xFFFFF0F5, // Text Primary (Lavender Blush)
            0xFFE0B0C8, // Text Secondary (Pastel Pink)
            0xFF35202D, // Button Inactive BG
            0xFF4A2E40, // Button Hover BG
            false
    ),
    CYBER(
            "Cyber Neon",
            0xFF101826, // Deep Space Navy BG
            0xFF1E3A5F, // Steel Cyan Border
            0xFF0C121D, // Cyber Dark Sidebar
            0xFF172B47, // Sidebar Border
            0xFF00E5FF, // Electric Cyan Accent
            0xFF10263E, // Highlight BG
            0xFF1A5A96, // Highlight Border
            0xFFE8F7FF, // Text Primary (Ice Blue)
            0xFF7CA1C4, // Text Secondary (Muted Cyan)
            0xFF152238, // Button Inactive BG
            0xFF203554, // Button Hover BG
            false
    ),
    PRISM(
            "Prism Rainbow",
            0xFF1A1325, // Cosmic Violet BG
            0xFF50357A, // Radiant Purple Border
            0xFF140D1D, // Twilight Sidebar
            0xFF3A2359, // Sidebar Border
            0xFFFF4081, // Vibrant Magenta Accent
            0xFF28193D, // Highlight BG
            0xFF6F3DBA, // Highlight Border
            0xFFFFFFFF, // Text Primary
            0xFFD1BBEB, // Text Secondary (Lilac)
            0xFF281C38, // Button Inactive BG
            0xFF3D2B57, // Button Hover BG
            true        // Full 6-color Rainbow Bar
    );

    // Aliases for backwards compatibility
    public static final PopupTheme GIRLISH = SAKURA;
    public static final PopupTheme BOYISH = CYBER;
    public static final PopupTheme LGBT = PRISM;
    public static final PopupTheme MINECRAFTY = RUSTIC;

    private final String displayName;
    private final int cardBackground;
    private final int cardBorder;
    private final int sidebarBackground;
    private final int sidebarBorder;
    private final int defaultAccent;
    private final int highlightBackground;
    private final int highlightBorder;
    private final int textPrimary;
    private final int textSecondary;
    private final int buttonInactiveBackground;
    private final int buttonHoverBackground;
    private final boolean rainbowBar;

    PopupTheme(String displayName, int cardBackground, int cardBorder, int sidebarBackground, int sidebarBorder,
               int defaultAccent, int highlightBackground, int highlightBorder, int textPrimary, int textSecondary,
               int buttonInactiveBackground, int buttonHoverBackground, boolean rainbowBar) {
        this.displayName = displayName;
        this.cardBackground = cardBackground;
        this.cardBorder = cardBorder;
        this.sidebarBackground = sidebarBackground;
        this.sidebarBorder = sidebarBorder;
        this.defaultAccent = defaultAccent;
        this.highlightBackground = highlightBackground;
        this.highlightBorder = highlightBorder;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.buttonInactiveBackground = buttonInactiveBackground;
        this.buttonHoverBackground = buttonHoverBackground;
        this.rainbowBar = rainbowBar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCardBackground() {
        return cardBackground;
    }

    public int getCardBorder() {
        return cardBorder;
    }

    public int getSidebarBackground() {
        return sidebarBackground;
    }

    public int getSidebarBorder() {
        return sidebarBorder;
    }

    public int getDefaultAccent() {
        return defaultAccent;
    }

    public int getHighlightBackground() {
        return highlightBackground;
    }

    public int getHighlightBorder() {
        return highlightBorder;
    }

    public int getTextPrimary() {
        return textPrimary;
    }

    public int getTextSecondary() {
        return textSecondary;
    }

    public int getButtonInactiveBackground() {
        return buttonInactiveBackground;
    }

    public int getButtonHoverBackground() {
        return buttonHoverBackground;
    }

    public boolean isRainbowBar() {
        return rainbowBar;
    }

    public PopupTheme next() {
        PopupTheme[] all = values();
        return all[(this.ordinal() + 1) % all.length];
    }

    public static PopupTheme fromString(String name) {
        if (name == null) return MODERN;
        String upper = name.trim().toUpperCase();
        return switch (upper) {
            case "GIRLISH", "SAKURA", "ROSE", "PASTEL" -> SAKURA;
            case "BOYISH", "CYBER", "NEON" -> CYBER;
            case "LGBT", "PRISM", "RAINBOW" -> PRISM;
            case "MINECRAFTY", "RUSTIC", "VANILLA", "WOOD" -> RUSTIC;
            case "CLASSIC", "SLATE" -> CLASSIC;
            default -> {
                try {
                    yield valueOf(upper);
                } catch (Exception e) {
                    yield MODERN;
                }
            }
        };
    }
}
