package com.aynclub.akilleffect.effect.particle;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import com.aynclub.akilleffect.utils.maths.MathUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;

public class Redstone extends MainEffectKill {

    public Redstone() {
        super("redstone", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.redstone.name")) : ("&c红石碎裂"), new ArrayList<String>(Arrays.asList("&7让红石碎裂粒子散开。", "&7红色光尘标记你的击杀。")), Heads.REDSTONE.getTexture());
    }

    @Override
    public void update(User user) {
        Location base = user.getPlayer().getLocation().clone().add(0.0D, 1.0D, 0.0D);
        user.getPlayer().getWorld().playSound(base, Sound.DIG_STONE, 2.0F, 1.0F);
        for (int i = 0; i < 8; i++) {
            user.getPlayer().getWorld().playEffect(base.clone().add(
                    MathUtils.randomRange(-0.85f, 0.85f),
                    MathUtils.randomRange(-0.35f, 0.65f),
                    MathUtils.randomRange(-0.85f, 0.85f)), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        }
    }
}
