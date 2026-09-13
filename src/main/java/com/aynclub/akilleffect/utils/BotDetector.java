package com.aynclub.akilleffect.utils;

import com.aynclub.akilleffect.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;

/** Optional integration resolved once per MicetPvP instance; server thread only. */
public final class BotDetector {
    private static Plugin source;
    private static Method getInstance;
    private static Method isBotUsername;
    private static boolean resolved;
    private static boolean warned;

    public static boolean isMineralBot(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MicetPvP");
        if (player == null || plugin == null || !plugin.isEnabled()) {
            return false;
        }
        if (source != plugin) {
            reset();
            source = plugin;
        }
        try {
            if (!resolved) {
                resolved = true;
                Class<?> type = Class.forName("eu.bcz.micetpvp.Micet.botduel.BotDuelHandler",
                        true, plugin.getClass().getClassLoader());
                getInstance = type.getMethod("getInstance");
                isBotUsername = type.getMethod("isBotUsername", String.class);
            }
            if (getInstance == null || isBotUsername == null) {
                return false;
            }
            Object handler = getInstance.invoke(null);
            return handler != null && Boolean.TRUE.equals(isBotUsername.invoke(handler, player.getName()));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            if (!warned) {
                warned = true;
                Main.getInstance().getLogger().warning("MicetPvP bot detection unavailable; using normal player persistence: " + e);
            }
            return false;
        }
    }

    public static void reset() {
        source = null;
        getInstance = null;
        isBotUsername = null;
        resolved = false;
        warned = false;
    }
}
