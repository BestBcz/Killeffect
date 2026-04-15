package com.aynclub.akilleffect.effect.visual;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class LightningVisual extends MainEffectKill {

    public LightningVisual() {
        super("lightningvisual", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.lightningvisual.name")) : ("&cLightningVisual"), new ArrayList<String>(Arrays.asList("&7A sharp lightning flash.", "&7Pure visual strike effect.")), Heads.Lightning.getTexture());
    }

    @Override
    public void update(User user) {
        Location base = user.getPlayer().getLocation().clone();
        World world = base.getWorld();
        if (world == null) {
            return;
        }

        world.strikeLightningEffect(base);
        for (int i = 0; i < 8; i++) {
            Particle.play(base.clone().add((Math.random() - 0.5D) * 2.5D, 0.6D + Math.random(), (Math.random() - 0.5D) * 2.5D), Effect.CRIT);
        }
    }
}
