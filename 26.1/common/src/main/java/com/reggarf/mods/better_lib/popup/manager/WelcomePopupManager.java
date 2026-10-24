package com.reggarf.mods.better_lib.popup.manager;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugins;
import com.reggarf.mods.better_lib.popup.config.WelcomePopupStorage;
import com.reggarf.mods.better_lib.popup.gui.WelcomePopupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class WelcomePopupManager {

    private static volatile boolean pendingPopup = false;
    private static volatile int delayTicks = 0;
    private static volatile boolean forceShow = false;
    @Nullable
    private static volatile String targetModId = null;

    /**
     * Trigger a welcome popup with a delay (e.g. 20 ticks after receiving packet from server).
     */
    public static void triggerPopup(int ticks) {
        triggerPopup(ticks, null, false);
    }

    public static void triggerPopup(int ticks, @Nullable String modId, boolean force) {
        if (!WelcomePopupPlugins.hasAny()) {
            Better_lib.LOG.info("[WelcomePopup] Suppressed: No popup plugins registered");
            return;
        }

        if (!force) {
            WelcomePopupStorage.ensureGlobalLoaded();
            if (!WelcomePopupStorage.isGlobalEnabled()) {
                Better_lib.LOG.info("[WelcomePopup] Suppressed: WelcomePopupStorage.isGlobalEnabled() is false");
                return;
            }
        }

        Better_lib.LOG.info("[WelcomePopup] Queued popup in {} ticks (targetModId={}, force={})", ticks, modId, force);
        pendingPopup = true;
        delayTicks = ticks;
        targetModId = modId;
        forceShow = force;
    }

    /**
     * Immediately open the welcome popup screen.
     */
    public static void openScreen(@Nullable Screen parentScreen) {
        openScreenForMod(null, parentScreen);
    }

    public static void openScreenForMod(@Nullable String modId, @Nullable Screen parentScreen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            Better_lib.LOG.info("[WelcomePopup] Opening WelcomePopupScreen directly for mod: {}", modId);
            mc.execute(() -> mc.setScreen(new WelcomePopupScreen(parentScreen, modId)));
        }
    }

    /**
     * Called from client tick event on all loaders.
     */
    public static void onClientTick() {
        if (!pendingPopup) {
            return;
        }

        delayTicks--;
        if (delayTicks <= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && mc.level != null) {
                if (mc.screen == null) {
                    pendingPopup = false;
                    Better_lib.LOG.info("[WelcomePopup] Ticks elapsed, setting WelcomePopupScreen (targetModId={})", targetModId);
                    mc.setScreen(new WelcomePopupScreen(null, targetModId));
                } else {
                    // Retry in 20 ticks if player is currently in another screen (e.g. loading or chat)
                    delayTicks = 20;
                }
            }
        }
    }
}
