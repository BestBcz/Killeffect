package com.aynclub.akilleffect.effect.sound;

import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.Heads;
import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Lightning extends MainEffectKill {
    private static final double VIEW_DISTANCE_SQUARED = 128.0D * 128.0D;

    public Lightning() {
        super("lightning", getConfiguredName(), new ArrayList<String>(Arrays.asList("&7Micet 风格的默认雷击。", "&7击杀时落下一道无伤闪电。")), Heads.Lightning.getTexture());
    }

    @Override
    public void update(User user) {
        Location location = user.getPlayer().getLocation().clone();
        if (location.getWorld() == null) {
            return;
        }

        PacketPlayOutSpawnEntityWeather lightningPacket = createLightningPacket(location);
        float thunderSoundPitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.2F;
        float explodeSoundPitch = 0.5F + ThreadLocalRandom.current().nextFloat() * 0.2F;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(location.getWorld()) || player.getLocation().distanceSquared(location) > VIEW_DISTANCE_SQUARED) {
                continue;
            }

            player.playSound(location, Sound.AMBIENCE_THUNDER, 10000F, thunderSoundPitch);
            player.playSound(location, Sound.EXPLODE, 2.0F, explodeSoundPitch);
            sendLightningPacket(player, lightningPacket);
        }
    }

    private PacketPlayOutSpawnEntityWeather createLightningPacket(Location location) {
        EntityLightning lightning = new EntityLightning(((CraftWorld) location.getWorld()).getHandle(),
                location.getX(), location.getY(), location.getZ(), true);
        return new PacketPlayOutSpawnEntityWeather(lightning);
    }

    private void sendLightningPacket(Player player, PacketPlayOutSpawnEntityWeather packet) {
        try {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } catch (RuntimeException ignored) {
            // A failed client packet should not interrupt the kill flow.
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
