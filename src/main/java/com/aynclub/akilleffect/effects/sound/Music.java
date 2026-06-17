package com.aynclub.akilleffect.effect.sound;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.Particle;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class Music extends MainEffectKill {

    private static final Sound[] SOUNDS = new Sound[]{Sound.NOTE_PIANO, Sound.NOTE_PLING, Sound.ORB_PICKUP, Sound.LEVEL_UP};
    private static final float[] PITCHES = new float[]{0.8F, 1.0F, 1.2F, 1.0F};

    public Music() {
        super("music", YAMLUtils.get("messages").getFile().exists() ? ((String) Utils.gfc("messages", "effectKill.music.name")) : ("&a音符"), new ArrayList<String>(Arrays.asList("&7播放一段短促旋律。", "&7音符会在目标上方升起。")), Heads.Redsoup.getTexture());
    }

    @Override
    public void update(User user) {
        final Location base = user.getPlayer().getLocation().clone();
        final World world = base.getWorld();
        if (world == null) {
            return;
        }

        new BukkitRunnable() {
            private int step = 0;

            @Override
            public void run() {
                if (step >= SOUNDS.length) {
                    cancel();
                    return;
                }
                world.playSound(base, SOUNDS[step], 0.9F, PITCHES[step]);
                Particle.play(base.clone().add(0.0D, 1.1D + (step * 0.15D), 0.0D), Effect.NOTE, step * 3);
                step++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 6L);
    }
}
