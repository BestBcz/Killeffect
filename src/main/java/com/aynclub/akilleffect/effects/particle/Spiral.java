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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class Spiral extends MainEffectKill {

    public Spiral() {
        super("spiral", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.spiral.name")) : ("&5螺旋"), new ArrayList<String>(Arrays.asList("&7魔法暴击粒子螺旋升起。", "&7将目标包进上升粒子中。")), Heads.WAVE.getTexture());
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone();
        new BukkitRunnable() {
            private int step = 0;
            private double angle = 0.0D;

            @Override
            public void run() {
                step++;
                for (double y = 0.0D; y <= 2.2D; y += 0.2D) {
                    double radius = 0.35D + (y * 0.18D);
                    double x = Math.cos(angle + (y * 2.5D)) * radius;
                    double z = Math.sin(angle + (y * 2.5D)) * radius;
                    Particle.play(base.clone().add(x, y, z), Effect.MAGIC_CRIT);
                }
                angle += Math.PI / 8.0D;
                if (step >= 18) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }
}
