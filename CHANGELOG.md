# Version: BetterLib v1.0.104
## Type: Bug Fix
### Change Log:

-  Fixed a server startup crash on NeoForge 1.21.1 caused by client-only class (net.minecraft.client.gui.screens.Screen) being loaded on the dedicated server.

-  All client-related code is now properly wrapped with Dist.CLIENT checks or moved to client-only initialization to ensure server compatibility.
