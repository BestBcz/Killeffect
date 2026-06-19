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

import java.util.ArrayList;
import java.util.Arrays;

public class TNTBreak extends MainEffectKill {

    public TNTBreak() {
        super("tntbreak", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.tntbreak.name")) : ("&cTNT 碎裂"), new ArrayList<String>(Arrays.asList("&7让 TNT 碎裂粒子四散。", "&7爆裂感十足。")), Heads.TNT.getTexture());
    }

    @Override
    public void update(User user) {
        Location base = user.getPlayer().getLocation().clone().add(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 12; i++) {
            user.getPlayer().getWorld().playEffect(base.clone().add(
                    MathUtils.randomRange(-0.85f, 0.85f),
                    MathUtils.randomRange(-0.35f, 0.65f),
                    MathUtils.randomRange(-0.85f, 0.85f)), Effect.STEP_SOUND, Material.TNT);
        }
    }
}
