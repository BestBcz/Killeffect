package com.aynclub.akilleffect.effect.particle;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import com.aynclub.akilleffect.utils.maths.MathUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class WoolBreak extends MainEffectKill {

    public WoolBreak() {
        super("woolbreak", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.woolbreak.name")) : ("&f羊毛破碎"), new ArrayList<String>(Arrays.asList("&7随机一种羊毛颜色碎裂飞散。", "&7伴随羊毛方块被破坏的声音。")), Heads.Snow.getTexture());
    }

    @Override
    public void update(User user) {
        Location base = user.getPlayer().getLocation().clone().add(0.0D, 1.0D, 0.0D);
        World world = base.getWorld();
        byte color = (byte) MathUtils.randomRange(1, 15);

        world.playSound(base, Sound.DIG_WOOL, 1.8F, 1.0F);
        for (int i = 0; i < 42; i++) {
            Particle.play(base.clone().add(
                    MathUtils.randomRange(-0.9f, 0.9f),
                    MathUtils.randomRange(-0.45f, 0.7f),
                    MathUtils.randomRange(-0.9f, 0.9f)), Effect.TILE_BREAK, Material.WOOL.getId(), color, 0.15F, 0.15F, 0.15F, 0.02F, 6);
        }
    }
}
