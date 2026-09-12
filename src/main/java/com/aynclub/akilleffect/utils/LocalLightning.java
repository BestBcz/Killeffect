package com.aynclub.akilleffect.utils;

import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LocalLightning {
    private LocalLightning() {
    }

    public static void play(Location location, List<Player> players) {
        if (players.isEmpty()) {
            return;
        }
        // This effect-only object is never added to the world or ticked by the server.
        EntityLightning lightning = new EntityLightning(((CraftWorld) location.getWorld()).getHandle(),
                location.getX(), location.getY(), location.getZ(), true);
        PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(lightning);
        float thunderPitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.2F;
        float explodePitch = 0.5F + ThreadLocalRandom.current().nextFloat() * 0.2F;
        for (Player player : players) {
            try {
                player.playSound(location, Sound.AMBIENCE_THUNDER, 10000F, thunderPitch);
                player.playSound(location, Sound.EXPLODE, 2.0F, explodePitch);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            } catch (RuntimeException ignored) {
                // A disconnected client must not interrupt the kill flow or other recipients.
            }
        }
    }
}
