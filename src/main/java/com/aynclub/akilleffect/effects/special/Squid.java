package com.aynclub.akilleffect.effect.special;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class Squid extends MainEffectKill {

    public Squid() {
        super("squid", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.squid.name")) : ("&cSquid"), new ArrayList<String>(Arrays.asList("&7A simple gadget...", "&7To make your kills even more entertaining!")), Heads.SQUID.getTexture());
    }

    @Override
    public void update(User user) {
        final Entity squid = user.getPlayer().getWorld().spawnEntity(user.getPlayer().getLocation().clone().add(0.0, -1.3, 0.0), EntityType.SQUID);
        spawnedEntities.add(squid);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (squid.isDead()) {
                    spawnedEntities.remove(squid);
                    cancel();
                    return;
                }

                squid.teleport(squid.getLocation().add(0.0, 0.5, 0.0));
                Particle.play(squid.getLocation().clone().add(0.0, -0.2, 0.0), Effect.FLAME);

                if (ticks >= 20) {
                    Location loc = squid.getLocation().clone();
                    spawnedEntities.remove(squid);
                    squid.remove();
                    Particle.play(loc.clone().add(0.0, 0.5, 0.0), Effect.EXPLOSION_HUGE, 1);
                    World world = loc.getWorld();
                    world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }
}
