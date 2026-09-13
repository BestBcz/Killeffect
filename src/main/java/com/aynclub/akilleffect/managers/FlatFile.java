package com.aynclub.akilleffect.managers;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.BotDetector;
import com.aynclub.akilleffect.utils.User;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Player/User access is confined to the server thread. */
public final class FlatFile {
    private static final Set<UUID> TRANSIENT_BOTS = new HashSet<UUID>();
    private static EffectStore store;

    public static void loadAll() throws IOException {
        store = new EffectStore(new File(Main.getInstance().getDataFolder(), "database.yml"),
                Main.getInstance().getLogger(), 5000);
    }

    public static void join(Player player) {
        UUID uuid = player.getUniqueId();
        if (BotDetector.isMineralBot(player)) {
            TRANSIENT_BOTS.add(uuid);
            User.getUser(uuid).setEffectKill(getDefaultEffect());
            return;
        }
        getValue(uuid);
    }

    public static void quit(UUID uuid) {
        if (!TRANSIENT_BOTS.remove(uuid)) {
            setValue(uuid);
        }
        User.removeUser(uuid);
    }

    public static void setValue(UUID uuid) {
        if (TRANSIENT_BOTS.contains(uuid) || store == null) {
            return;
        }
        User user = User.getUsers().get(uuid);
        if (user != null) {
            store.put(uuid, user.getEffectKill() == null ? "none" : user.getEffectKill().getName());
        }
    }

    public static void getValue(UUID uuid) {
        User user = User.getUser(uuid);
        if (TRANSIENT_BOTS.contains(uuid)) {
            user.setEffectKill(getDefaultEffect());
            return;
        }
        String name = store.get(uuid);
        MainEffectKill effect = name == null ? getDefaultEffect() : Main.getInstance().getEffectKill().get(name);
        user.setEffectKill("none".equalsIgnoreCase(name) ? null : effect == null ? getDefaultEffect() : effect);
    }

    public static MainEffectKill getDefaultEffect() {
        return Main.getInstance().getEffectKill().get("lightning");
    }

    public static void shutdown() {
        try {
            if (store != null) {
                for (UUID uuid : User.getUsers().keySet()) {
                    setValue(uuid);
                }
                store.close();
            }
        } finally {
            store = null;
            TRANSIENT_BOTS.clear();
            User.clearUsers();
            BotDetector.reset();
        }
    }
}
