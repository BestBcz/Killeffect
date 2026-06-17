package com.aynclub.akilleffect;

import com.aynclub.akilleffect.commands.Help;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.events.Events;
import com.aynclub.akilleffect.managers.FlatFile;
import com.aynclub.akilleffect.utils.Manager;
import com.aynclub.akilleffect.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin {

    public static String PREFIX;

    private static Main instance;
    private static Manager manager;

    private final Map<String, MainEffectKill> effectKillMap = new HashMap<String, MainEffectKill>();

    public static Main getInstance() {
        return instance;
    }

    public static Manager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        instance = this;
        manager = new Manager();

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder.");
        }

        saveDefaultConfig();
        Manager.buildConfigs("config");

        String legacyToken = getConfig().getString("token", "");
        if (!legacyToken.isEmpty()) {
            getLogger().info("Legacy token detected in config.yml; ignored in the open-source build.");
        }

        effectKillMap.clear();
        manager.loadMinions();
        Manager.buildConfigs("messages");

        if (getCommand("killeffect") != null) {
            getCommand("killeffect").setExecutor(new Help());
        } else {
            getLogger().warning("Command 'killeffect' is missing from plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(new Events(), this);
        FlatFile.checkDatabase();

        PREFIX = Utils.colorize(String.valueOf(Utils.gfc("messages", "prefix")));

        for (Player player : Bukkit.getOnlinePlayers()) {
            FlatFile.getValue(player.getUniqueId());
        }

        getLogger().info("Loaded " + MainEffectKill.instanceList.size() + " kill effects.");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FlatFile.setValue(player.getUniqueId());
        }
    }

    public Map<String, MainEffectKill> getEffectKill() {
        return effectKillMap;
    }
}
