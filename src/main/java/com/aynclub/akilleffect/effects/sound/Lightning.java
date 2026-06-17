package com.aynclub.akilleffect.effect.sound;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Lightning extends MainEffectKill {

    public Lightning() {
        super("lightning", getConfiguredName(), new ArrayList<String>(Arrays.asList("&7Micet 风格的默认雷击。", "&7击杀时落下一道无伤闪电。")), Heads.Lightning.getTexture());
    }

    @Override
    public void update(User user) {
        Location location = user.getPlayer().getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        float thunderSoundPitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.2F;
        float explodeSoundPitch = 0.5F + ThreadLocalRandom.current().nextFloat() * 0.2F;

        world.playSound(location, Sound.AMBIENCE_THUNDER, 10000F, thunderSoundPitch);
        world.playSound(location, Sound.EXPLODE, 2.0F, explodeSoundPitch);
        world.strikeLightningEffect(location);
    }

    private static String getConfiguredName() {
        if (!YAMLUtils.get("messages").getFile().exists()) {
            return "&b雷击";
        }

        Object configuredName = Utils.gfc("messages", "effectKill.lightning.name");
        return configuredName instanceof String ? (String) configuredName : "&b雷击";
    }
}
