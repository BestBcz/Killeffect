package com.aynclub.akilleffect.effect.sound;

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

public class Thunder extends MainEffectKill {

    public Thunder() {
        super("thunder", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.thunder.name")) : ("&9雷暴"), new ArrayList<String>(Arrays.asList("&7召唤更强烈的雷暴收尾。", "&7带有雷声、烟雾和爆炸冲击。")), Heads.ANGRY.getTexture());
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone();
        final World world = base.getWorld();
        if (world == null) {
            return;
        }

        world.playSound(base, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
        world.strikeLightningEffect(base);
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                tick++;
                for (int i = 0; i < 4; i++) {
                    Particle.play(base.clone().add((Math.random() - 0.5D) * 2.0D, 1.0D + Math.random(), (Math.random() - 0.5D) * 2.0D), Effect.LARGE_SMOKE);
                }
                if (tick >= 8) {
                    world.playSound(base, Sound.EXPLODE, 0.8F, 0.9F);
                    world.playEffect(base, Effect.EXPLOSION_LARGE, 0);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 2L);
    }
}
