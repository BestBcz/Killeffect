package com.aynclub.akilleffect.effect.particle;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public class Rainbow extends MainEffectKill {

    private static final byte[] COLORS = new byte[]{14, 1, 4, 5, 11, 10, 2};

    public Rainbow() {
        super("rainbow", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.rainbow.name")) : ("&d彩虹"), new ArrayList<String>(Arrays.asList("&7用七彩尘环包围击杀位置。", "&7让胜利变得更鲜明。")), Heads.REDSTONE.getTexture());
    }

    @Override
    public void update(User user) {
        Location base = user.getPlayer().getLocation().clone().add(0.0, 0.2, 0.0);
        for (int ring = 0; ring < 3; ring++) {
            double y = ring * 0.35D;
            double radius = 1.4D + (ring * 0.3D);
            for (int i = 0; i < 28; i++) {
                double angle = (Math.PI * 2.0 * i) / 28.0D;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                byte color = COLORS[i % COLORS.length];
                Particle.play(base.clone().add(x, y, z), Effect.TILE_DUST, Material.WOOL.getId(), color, 0.15F, 0.15F, 0.15F, 0.01F, 3);
            }
        }
    }
}
