package com.aynclub.akilleffect.utils;

import com.aynclub.akilleffect.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Captures the originating match, so delayed effects cannot follow a player into another match. */
public final class MatchAudience {
    private static boolean failureLogged;
    private final Plugin plugin;
    private final Object handler;
    private final Method lookup;
    private final Object match;

    private MatchAudience(Plugin plugin, Object handler, Method lookup, Object match) {
        this.plugin = plugin;
        this.handler = handler;
        this.lookup = lookup;
        this.match = match;
    }

    public static MatchAudience capture(Player victim) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MicetPvP");
        if (plugin == null || !plugin.isEnabled() || victim == null) {
            return null;
        }
        try {
            Object handler = plugin.getClass().getMethod("getMatchHandler").invoke(plugin);
            Method lookup = handler.getClass().getMethod("getMatchPlayingOrSpectating", Player.class);
            Object match = lookup.invoke(handler, victim);
            return match == null ? null : new MatchAudience(plugin, handler, lookup, match);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            logFailure(exception);
            return null;
        }
    }

    public List<Player> getPlayers(Location location) {
        if (!plugin.isEnabled() || location.getWorld() == null) {
            return Collections.emptyList();
        }
        List<Player> players = new ArrayList<Player>();
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Use current membership: eliminated spectators remain eligible; lobby players do not.
                if (player.isOnline() && player.getWorld().equals(location.getWorld())
                        && lookup.invoke(handler, player) == match) {
                    players.add(player);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            logFailure(exception);
            return Collections.emptyList();
        }
        return players;
    }

    private static void logFailure(Throwable exception) {
        if (!failureLogged) {
            failureLogged = true;
            Main.getInstance().getLogger().warning("Could not resolve MicetPvP kill-effect audience; effect suppressed: " + exception);
        }
    }
}
