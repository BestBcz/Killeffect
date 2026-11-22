package com.aynclub.akilleffect.effect.special;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.MatchAudience;
import com.aynclub.akilleffect.utils.User;
import net.minecraft.server.v1_7_R4.EntityEnderDragon;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.CraftSound;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class EnderDragonDeath extends MainEffectKill {
    private static final int DRAGON_GROWL_TICK = 12;
    private static final int FINAL_BURST_TICK = 94;
    private static final int DURATION_TICKS = 100;
    // Vanilla 1.7.10 advances the dying dragon by this amount on each client tick.
    private static final double RISE_PER_TICK = 0.1D;

    private final Map<Object, Animation> active = new IdentityHashMap<Object, Animation>();
    private BukkitTask ticker;

    public EnderDragonDeath() {
        super("enderdragon", "&5末影龙陨落", new ArrayList<String>(Arrays.asList(
                "&7击杀时引爆 TNT，末影龙随即升起。", "&7伴随原版末影龙的低吼。")), new ItemStack(Material.DRAGON_EGG));
    }

    @Override
    public void update(User user) {
        Player victim = user.getPlayer();
        MatchAudience audience = MatchAudience.capture(victim);
        if (audience == null) {
            return;
        }

        Object match = audience.getMatchIdentity();
        Animation previous = active.remove(match);
        if (previous != null) {
            previous.destroy();
        }

        Location deathLocation = victim.getLocation();
        Location origin = victim.getEyeLocation().add(0, 1.0D, 0);
        List<Player> viewers = audience.getPlayers(origin);
        if (viewers.isEmpty()) {
            return;
        }

        // Serialize a detached dragon. The client runs the death animation from its zero health.
        EntityEnderDragon dragon = new EntityEnderDragon(((CraftWorld) origin.getWorld()).getHandle());
        dragon.setPositionRotation(origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), 0.0F);
        dragon.setHealth(0.0F);
        Packet spawn = new PacketPlayOutSpawnEntityLiving(dragon);

        // Match the vanilla explosion used by the TNT kill effect with client-only packets.
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float tntPitch = (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F;
        Packet tntSound = soundAt(deathLocation, Sound.EXPLODE, 4.0F, tntPitch);
        Packet tntBurst = explosionAt(deathLocation);
        for (Iterator<Player> iterator = viewers.iterator(); iterator.hasNext();) {
            Player viewer = iterator.next();
            sendPacket(viewer, tntSound);
            sendPacket(viewer, tntBurst);
            if (!sendPacket(viewer, spawn)) {
                iterator.remove();
            }
        }
        if (viewers.isEmpty()) {
            return;
        }

        active.put(match, new Animation(audience, origin, viewers, dragon.getId()));
        if (ticker == null) {
            ticker = Main.getInstance().getServer().getScheduler().runTaskTimer(Main.getInstance(), this::tick, 1L, 1L);
        }
    }

    private void tick() {
        active.values().removeIf(Animation::advance);
        if (active.isEmpty()) {
            ticker.cancel();
            ticker = null;
        }
    }

    @Override
    public void shutdown() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        for (Animation animation : active.values()) {
            animation.destroy();
        }
        active.clear();
    }

    private static Packet soundAt(Location location, Sound sound, float volume, float pitch) {
        return new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound),
                location.getX(), location.getY(), location.getZ(), volume, pitch);
    }

    private static Packet explosionAt(Location location) {
        return new PacketPlayOutWorldParticles(Effect.EXPLOSION_HUGE.getName(),
                (float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 0, 1);
    }

    private static boolean sendPacket(Player player, Packet packet) {
        if (!(player instanceof CraftPlayer)) {
            return false;
        }
        try {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            if (connection == null || connection.isDisconnected()) {
                return false;
            }
            connection.sendPacket(packet);
            return true;
        } catch (RuntimeException ignored) {
            // A closing connection must not interrupt other recipients or the kill handler.
            return false;
        }
    }

    private static final class Animation {
        private final MatchAudience audience;
        private final Location origin;
        private final List<Player> viewers;
        private final Packet destroy;
        private int age;

        private Animation(MatchAudience audience, Location origin, List<Player> viewers, int entityId) {
            this.audience = audience;
            this.origin = origin;
            this.viewers = viewers;
            this.destroy = new PacketPlayOutEntityDestroy(entityId);
        }

        private boolean advance() {
            for (Iterator<Player> iterator = viewers.iterator(); iterator.hasNext();) {
                Player viewer = iterator.next();
                if (!audience.contains(viewer, origin.getWorld())) {
                    sendPacket(viewer, destroy);
                    iterator.remove();
                }
            }
            if (viewers.isEmpty()) {
                return true;
            }

            age++;
            if (age == DRAGON_GROWL_TICK) {
                broadcast(soundAt(dragonLocation(), Sound.ENDERDRAGON_GROWL, 0.8F, 1.0F));
            } else if (age == FINAL_BURST_TICK) {
                broadcast(explosionAt(dragonLocation()));
            } else if (age == DURATION_TICKS) {
                destroy();
                return true;
            }
            return false;
        }

        private Location dragonLocation() {
            return origin.clone().add(0, age * RISE_PER_TICK, 0);
        }

        private void broadcast(Packet packet) {
            for (Player viewer : viewers) {
                sendPacket(viewer, packet);
            }
        }

        private void destroy() {
            broadcast(destroy);
            viewers.clear();
        }
    }
}
