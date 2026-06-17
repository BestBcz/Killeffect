package com.aynclub.akilleffect.effect.particle;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class Firework extends MainEffectKill {

    public Firework() {
        super("firework", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.firework.name")) : ("&e烟花"), new ArrayList<String>(Arrays.asList("&7发射一束短促烟花。", "&7在目标上方炸开火花。")), Heads.FIREWORK.getTexture());
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone().add(0.0, 1.0, 0.0);
        final World world = base.getWorld();
        if (world == null) {
            return;
        }

        world.playSound(base, Sound.FIREWORK_LAUNCH, 1.0F, 1.1F);
        new BukkitRunnable() {
            private int step = 0;

            @Override
            public void run() {
                step++;
                double radius = 0.3 + (step * 0.18);
                for (int i = 0; i < 12; i++) {
                    double angle = (Math.PI * 2.0 * i) / 12.0 + (step * 0.25);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLocation = base.clone().add(x, step * 0.12, z);
                    Particle.play(particleLocation, Effect.FIREWORKS_SPARK);
                    if (i % 3 == 0) {
                        Particle.play(particleLocation, Effect.FLAME);
                    }
                }

                if (step >= 6) {
                    world.playSound(base, Sound.FIREWORK_BLAST, 1.0F, 1.0F);
                    world.playEffect(base, Effect.EXPLOSION_LARGE, 0);
                    for (int i = 0; i < 24; i++) {
                        Location burst = base.clone().add((Math.random() - 0.5D) * 2.0D, Math.random() * 1.5D, (Math.random() - 0.5D) * 2.0D);
                        Particle.play(burst, Effect.FIREWORKS_SPARK);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 2L);
    }
}
