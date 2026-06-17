package com.aynclub.akilleffect.effect.particle;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class TNT extends MainEffectKill {

    public TNT() {
        super("tnt", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.tnt.name")) : ("&cTNT 爆炸"), new ArrayList<String>(Arrays.asList("&7在击杀位置播放巨型爆炸特效。", "&7不会破坏方块。")), Heads.TNT.getTexture());
    }

    @Override
    public void update(User user) {
        Location loc = user.getPlayer().getLocation();
        World world = loc.getWorld();
        world.playEffect(loc, Effect.EXPLOSION_HUGE, 0);
        float power = 0f; // 爆炸强度
        boolean setFire = false; // 是否引燃方块
        world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, false);
    }
}
