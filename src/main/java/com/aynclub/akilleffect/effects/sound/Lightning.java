package com.aynclub.akilleffect.effect.sound;

import org.bukkit.inventory.ItemStack;

import org.bukkit.Material;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.MatchAudience;
import com.aynclub.akilleffect.utils.LocalLightning;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;

public class Lightning extends MainEffectKill {

    public Lightning() {
        super("lightning", getConfiguredName(), new ArrayList<String>(Arrays.asList("&7Micet 风格的默认雷击。", "&7击杀时落下一道无伤闪电。")), new ItemStack(Material.BLAZE_ROD));
    }

    @Override
    public void update(User user) {
        Location location = user.getPlayer().getLocation().clone();
        if (location.getWorld() == null) {
            return;
        }

        MatchAudience audience = MatchAudience.capture(user.getPlayer());
        if (audience != null) {
            LocalLightning.play(location, audience.getPlayers(location));
        }
    }

    private static String getConfiguredName() {
        if (!YAMLUtils.get("messages").getFile().exists()) {
            return "&b雷击";
        }

        Object configuredName = Utils.gfc("messages", "effectKill.lightning.name");
        return configuredName instanceof String ? (String) configuredName : "&b雷击";
    }
}
