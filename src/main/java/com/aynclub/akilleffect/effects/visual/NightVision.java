package com.aynclub.akilleffect.effect.visual;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class NightVision extends MainEffectKill {

    public NightVision() {
        super("nightvision", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.nightvision.name")) : ("&cNightVision"), new ArrayList<String>(Arrays.asList("&7Grants the killer night vision.", "&7Leaves potion swirls at the victim.")), Heads.ANGRY.getTexture());
    }

    @Override
    public void update(User user) {
        Player player = user.getPlayer();
        if (player != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
            Particle.play(player.getLocation().clone().add(0.0D, 1.0D, 0.0D), Effect.POTION_SWIRL_TRANSPARENT);
        }
    }

    @Override
    public void update(User victim, User killer) {
        if (killer != null && killer.getPlayer() != null) {
            killer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        }
        if (victim != null && victim.getPlayer() != null) {
            Particle.play(victim.getPlayer().getLocation().clone().add(0.0D, 1.0D, 0.0D), Effect.POTION_SWIRL_TRANSPARENT);
            Particle.play(victim.getPlayer().getLocation().clone().add(0.0D, 1.6D, 0.0D), Effect.POTION_SWIRL_TRANSPARENT);
        }
    }
}
