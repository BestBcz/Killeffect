package com.aynclub.akilleffect.effect.special;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.ItemsUtils;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import com.aynclub.akilleffect.utils.maths.MathUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class Satan extends MainEffectKill {

    public Satan() {
        super("satan", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.satan.name")) : ("&cSatan"), new ArrayList<String>(Arrays.asList("&7A simple gadget...", "&7To make your kills even more entertaining!")), Heads.DEVIL.getTexture());
    }

    @Override
    public void update(User user) {
        final Location baseLocation = user.getPlayer().getLocation().clone().add(0.0, -1.35, 0.0);
        final Zombie skullCarrier = (Zombie) user.getPlayer().getWorld().spawnEntity(baseLocation, EntityType.ZOMBIE);
        skullCarrier.getEquipment().setHelmet(ItemsUtils.getNamedSkull(user.getPlayer().getName()));
        skullCarrier.getEquipment().setItemInHand(null);
        skullCarrier.setCanPickupItems(false);
        skullCarrier.setCustomName(Utils.colorize("&c&l" + user.getPlayer().getName() + " Fall asleep! :)"));
        skullCarrier.setCustomNameVisible(true);
        skullCarrier.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 1));
        skullCarrier.setVelocity(new Vector(0.0, 0.0, 0.0));
        spawnedEntities.add(skullCarrier);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (skullCarrier.isDead()) {
                    spawnedEntities.remove(skullCarrier);
                    cancel();
                    return;
                }

                skullCarrier.teleport(baseLocation);
                skullCarrier.setVelocity(new Vector(0.0, 0.0, 0.0));
                skullCarrier.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1), true);

                for (int i = 0; i < 4; i++) {
                    Particle.play(baseLocation.clone().add(MathUtils.randomRange(-1.0F, 1.0F), 3.1D, MathUtils.randomRange(-1.0F, 1.0F)), Effect.LARGE_SMOKE);
                }
                Particle.play(baseLocation.clone().add(MathUtils.randomRange(-0.8F, 0.8F), 2.6D, MathUtils.randomRange(-0.8F, 0.8F)), Effect.FLAME);
                Particle.play(baseLocation.clone().add(MathUtils.randomRange(-0.8F, 0.8F), 2.6D, MathUtils.randomRange(-0.8F, 0.8F)), Effect.LAVADRIP);

                if (ticks >= 100) {
                    spawnedEntities.remove(skullCarrier);
                    skullCarrier.remove();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }
}
