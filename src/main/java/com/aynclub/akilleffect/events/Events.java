package com.aynclub.akilleffect.events;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.managers.FlatFile;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FlatFile.getValue(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FlatFile.setValue(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() == null || event.getCurrentItem() == null || event.getWhoClicked() == null) {
            return;
        }

        String menuTitle = Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.effectKill")));
        if (!event.getInventory().getTitle().equalsIgnoreCase(menuTitle)) {
            return;
        }

        event.setCancelled(true);
        if (!event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
            return;
        }

        User user = User.getUser(event.getWhoClicked().getUniqueId());
        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
        String despawnPrefix = Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.despawn")));
        String spawnPrefix = Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.spawn")));

        if (displayName.startsWith(despawnPrefix)) {
            if (user.getEffectKill() != null) {
                user.getEffectKill().despawn(user);
                user.getPlayer().sendMessage(Utils.colorize(String.valueOf(Utils.gfc("messages", "remove")).replace("%prefix%", Main.PREFIX)));
            }
            event.getWhoClicked().closeInventory();
            return;
        }

        if (!displayName.startsWith(spawnPrefix)) {
            return;
        }

        String effectName = extractContent(displayName);
        if (effectName == null || effectName.isEmpty()) {
            return;
        }

        MainEffectKill effectKill = Main.getInstance().getEffectKill().get(effectName.toLowerCase());
        if (effectKill == null) {
            return;
        }

        if (user.getEffectKill() != null) {
            user.getEffectKill().despawn(user);
        }

        user.setEffectKill(effectKill);
        String configuredName = String.valueOf(Utils.gfc("messages", "effectKill." + effectKill.getName() + ".name"));
        user.getPlayer().sendMessage(Utils.colorize(String.valueOf(Utils.gfc("messages", "spawn"))
                .replace("%prefix%", Main.PREFIX)
                .replace("%effectname%", configuredName)));
        event.getWhoClicked().closeInventory();
    }

    public static String extractContent(String input) {
        int startIndex = input.lastIndexOf("(");
        int endIndex = input.lastIndexOf(")");

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return input.substring(startIndex + 1, endIndex);
        }

        return "";
    }

    // Capture match membership and death position before Practice transitions the victim.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        User deadUser = User.getUser(event.getEntity().getUniqueId());
        User killerUser = User.getUser(killer.getUniqueId());
        if (killerUser.getEffectKill() != null) {
            killerUser.getEffectKill().update(deadUser, killerUser);
        }
    }
}
