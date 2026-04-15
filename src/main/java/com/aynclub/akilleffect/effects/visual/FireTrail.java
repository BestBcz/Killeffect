package com.aynclub.akilleffect.effect.visual;

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

public class FireTrail extends MainEffectKill {

    public FireTrail() {
        super("firetrail", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.firetrail.name")) : ("&cFireTrail"), new ArrayList<String>(Arrays.asList("&7Flame circles the victim.", "&7A short burning trail effect.")), Heads.Fire.getTexture());
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone().add(0.0D, 0.1D, 0.0D);
        final World world = base.getWorld();
        if (world == null) {
            return;
        }

        world.playSound(base, Sound.FIRE_IGNITE, 1.0F, 1.0F);
        new BukkitRunnable() {
            private int step = 0;

            @Override
            public void run() {
                step++;
                double radius = 0.7D + (step * 0.03D);
                for (int i = 0; i < 10; i++) {
                    double angle = (Math.PI * 2.0D * i) / 10.0D + (step * 0.35D);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location flameLocation = base.clone().add(x, 0.0D, z);
                    Particle.play(flameLocation, Effect.FLAME);
                    if (i % 2 == 0) {
                        Particle.play(flameLocation, Effect.SMALL_SMOKE);
                    }
                }
                if (step >= 16) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }
}
