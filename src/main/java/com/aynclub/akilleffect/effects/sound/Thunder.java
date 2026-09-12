package com.aynclub.akilleffect.effect.sound;

import org.bukkit.inventory.ItemStack;

import org.bukkit.Material;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.MatchAudience;
import com.aynclub.akilleffect.utils.LocalLightning;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Thunder extends MainEffectKill {

    public Thunder() {
        super("thunder", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.thunder.name")) : ("&9雷暴"), new ArrayList<String>(Arrays.asList("&7召唤更强烈的雷暴收尾。", "&7带有雷声、烟雾和爆炸冲击。")), new ItemStack(Material.FIREBALL));
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone();
        final MatchAudience audience = MatchAudience.capture(user.getPlayer());
        if (base.getWorld() == null || audience == null) {
            return;
        }

        List<Player> players = audience.getPlayers(base);
        if (players.isEmpty()) {
            return;
        }
        LocalLightning.play(base, players);
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                List<Player> players = audience.getPlayers(base);
                if (players.isEmpty()) {
                    cancel();
                    return;
                }
                tick++;
                for (int i = 0; i < 4; i++) {
                    Location smoke = base.clone().add((Math.random() - 0.5D) * 2.0D, 1.0D + Math.random(), (Math.random() - 0.5D) * 2.0D);
                    for (Player player : players) {
                        player.spigot().playEffect(smoke, Effect.LARGE_SMOKE, 0, 0, 0, 0, 0, 0, 1, 128);
                    }
                }
                if (tick >= 8) {
                    for (Player player : players) {
                        player.playSound(base, Sound.EXPLODE, 0.8F, 0.9F);
                        player.spigot().playEffect(base, Effect.EXPLOSION_LARGE, 0, 0, 0, 0, 0, 0, 1, 128);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 2L);
    }
}
