package com.aynclub.akilleffect.managers;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FlatFile {
    private static final String DEFAULT_EFFECT_NAME = "lightning";
    private static final String NO_EFFECT_NAME = "none";

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
            cfg.set(uuid.toString(), NO_EFFECT_NAME);
        }

        try {
            cfg.save(getDatabaseFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getValue(UUID uuid) {
        YamlConfiguration cfg = getConfig();
        User user = User.getUser(uuid);
        if (!cfg.contains(uuid.toString())) {
            user.setEffectKill(getDefaultEffect());
            return;
        }

        String effectName = cfg.getString(uuid.toString());
        if (effectName == null || effectName.equalsIgnoreCase(NO_EFFECT_NAME)) {
            user.setEffectKill(null);
            return;
        }

        MainEffectKill effectKill = Main.getInstance().getEffectKill().get(effectName);
        user.setEffectKill(effectKill == null ? getDefaultEffect() : effectKill);
    }

    private static MainEffectKill getDefaultEffect() {
        return Main.getInstance().getEffectKill().get(DEFAULT_EFFECT_NAME);
    }

    private static File getDatabaseFile() {
        return new File(Main.getInstance().getDataFolder(), "database.yml");
    }

    private static YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(getDatabaseFile());
    }
}
