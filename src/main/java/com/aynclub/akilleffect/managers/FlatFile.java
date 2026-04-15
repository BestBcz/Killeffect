package com.aynclub.akilleffect.managers;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FlatFile {

    public static void checkDatabase() {
        File cfgFile = getDatabaseFile();
        File parent = cfgFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!cfgFile.exists()) {
            try {
                cfgFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setValue(UUID uuid) {
        YamlConfiguration cfg = getConfig();
        if (User.getUsers().containsKey(uuid) && User.getUsers().get(uuid).getEffectKill() != null) {
            cfg.set(uuid.toString(), User.getUser(uuid).getEffectKill().getName());
        } else {
            cfg.set(uuid.toString(), null);
        }

        try {
            cfg.save(getDatabaseFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getValue(UUID uuid) {
        YamlConfiguration cfg = getConfig();
        if (!cfg.contains(uuid.toString())) {
            return;
        }

        User user = User.getUser(uuid);
        String effectName = cfg.getString(uuid.toString());
        MainEffectKill effectKill = Main.getInstance().getEffectKill().get(effectName);
        user.setEffectKill(effectKill);
    }

    private static File getDatabaseFile() {
        return new File(Main.getInstance().getDataFolder(), "database.yml");
    }

    private static YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(getDatabaseFile());
    }
}
