package com.aynclub.akilleffect.managers;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.utils.BotDetector;
import com.aynclub.akilleffect.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Standalone regression suite; run with test-classes, classes and libs/spigot.jar. */
public final class PersistenceRegression {
    private static final Logger LOG = Logger.getLogger("persistence-test");

    public static void main(String[] args) throws Exception {
        File dir = Files.createTempDirectory(new File("target").toPath(), "persistence-test-").toFile();
        sparseAndLoadOnce(new File(dir, "legacy.yml"));
        debounceAndConcurrentSave(new File(dir, "concurrent.yml"));
        retry(new File(dir, "retry.yml"));
        lifecycle(new File(dir, "bots.yml"));
        File invalid = new File(dir, "invalid.yml");
        Files.write(invalid.toPath(), "[broken: yaml".getBytes("UTF-8"));
        try {
            new EffectStore(invalid, LOG, 50);
            throw new AssertionError("Invalid YAML accepted");
        } catch (java.io.IOException expected) {
            check(new String(Files.readAllBytes(invalid.toPath()), "UTF-8").equals("[broken: yaml"), "Invalid YAML preserved");
        }
        System.out.println("PASS: sparse/load-once, debounce, concurrent updates, retry, final flush, 10000 bots, invalid YAML");
    }

    private static void sparseAndLoadOnce(File file) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        UUID custom = UUID.randomUUID(), none = UUID.randomUUID();
        for (int i = 0; i < 34000; i++) { yaml.set(new UUID(0, i).toString(), "lightning"); }
        yaml.set(custom.toString(), "redstone");
        yaml.set(none.toString(), "none");
        yaml.save(file);
        EffectStore store = new EffectStore(file, LOG, 60000);
        check(map(store).size() == 2, "Legacy defaults discarded");
        // Corrupt disk after startup: all subsequent player operations must use memory.
        Files.write(file.toPath(), "[broken".getBytes("UTF-8"));
        for (int i = 0; i < 100; i++) {
            check("redstone".equals(store.get(custom)), "Cached custom selection");
            UUID fresh = UUID.randomUUID();
            check(store.get(fresh) == null, "New player default");
            store.put(fresh, "lightning");
        }
        store.put(custom, "lightning");
        store.close();
        yaml = YamlConfiguration.loadConfiguration(file);
        check(yaml.getKeys(false).size() == 1 && "none".equals(yaml.getString(none.toString())), "Sparse final save");
        EffectStore reopened = new EffectStore(file, LOG, 60000);
        check(reopened.get(custom) == null && "none".equals(reopened.get(none)), "Restart semantics");
        reopened.close();
        System.out.println("PASS: 34000 legacy defaults compacted; cached access survives changed disk");
    }

    private static void debounceAndConcurrentSave(File file) throws Exception {
        EffectStore store = new EffectStore(file, LOG, 150);
        BlockingMap blocked = new BlockingMap();
        field(EffectStore.class, "effects").set(store, blocked);
        UUID id = UUID.randomUUID();
        for (int i = 0; i < 100; i++) { store.put(id, "effect" + i); }
        check(blocked.entered.await(5, TimeUnit.SECONDS), "Writer started");
        store.put(id, "none");
        blocked.release.countDown();
        awaitSaved(store);
        check("none".equals(YamlConfiguration.loadConfiguration(file).getString(id.toString())), "Concurrent edit persisted");
        ScheduledThreadPoolExecutor writer = (ScheduledThreadPoolExecutor) field(EffectStore.class, "writer").get(store);
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (writer.getCompletedTaskCount() < 2 && System.nanoTime() < end) { Thread.sleep(10); }
        check(writer.getCompletedTaskCount() == 2, "100 edits coalesced plus one concurrent follow-up");
        store.close();
        check(writer.awaitTermination(5, TimeUnit.SECONDS), "Writer stopped");
        check(writer.getCompletedTaskCount() == 3, "Exactly one final flush");
    }

    private static void retry(File file) throws Exception {
        File temp = new File(file.getPath() + ".tmp");
        check(temp.mkdir(), "Create simulated write failure");
        EffectStore store = new EffectStore(file, LOG, 50);
        UUID id = UUID.randomUUID();
        store.put(id, "none");
        ScheduledThreadPoolExecutor writer = (ScheduledThreadPoolExecutor) field(EffectStore.class, "writer").get(store);
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (writer.getCompletedTaskCount() == 0 && System.nanoTime() < end) { Thread.sleep(10); }
        check(writer.getCompletedTaskCount() > 0, "Failed save attempted");
        Files.delete(temp.toPath());
        awaitSaved(store);
        check("none".equals(YamlConfiguration.loadConfiguration(file).getString(id.toString())), "Automatic retry");
        store.close();
    }

    private static void lifecycle(File file) throws Exception {
        // Allocate fixtures without booting a Minecraft server or effect constructors.
        Field unsafeField = field(Unsafe.class, "theUnsafe");
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        Main main = (Main) unsafe.allocateInstance(Main.class);
        Map<String, MainEffectKill> effects = new HashMap<String, MainEffectKill>();
        for (String name : new String[] {"lightning", "redstone"}) {
            DummyEffect effect = (DummyEffect) unsafe.allocateInstance(DummyEffect.class);
            field(MainEffectKill.class, "name").set(effect, name);
            effects.put(name, effect);
        }
        field(Main.class, "effectKillMap").set(main, effects);
        field(Main.class, "instance").set(null, main);
        Plugin plugin = proxy(Plugin.class, (p, m, a) -> m.getName().equals("isEnabled") ? true : null);
        PluginManager manager = proxy(PluginManager.class, (p, m, a) -> m.getName().equals("getPlugin") ? plugin : null);
        // The legacy Server interface has two binary-compatible getOnlinePlayers
        // signatures that Java dynamic proxies cannot implement together.
        Class<?> serverType = Class.forName("org.bukkit.craftbukkit.v1_7_R4.CraftServer");
        Server server = (Server) unsafe.allocateInstance(serverType);
        field(serverType, "pluginManager").set(server, manager);
        field(Bukkit.class, "server").set(null, server);
        EffectStore store = new EffectStore(file, LOG, 60000);
        field(FlatFile.class, "store").set(null, store);
        GuardMap guarded = new GuardMap();
        field(EffectStore.class, "effects").set(store, guarded);
        for (int i = 0; i < 10000; i++) {
            UUID id = UUID.randomUUID();
            FlatFile.join(player(id, "TEST_BOT"));
            check(User.getUser(id).getEffectKill() == effects.get("lightning"), "Bot default lightning");
            User.getUser(id).setEffectKill(effects.get("redstone"));
            FlatFile.setValue(id);
            FlatFile.getValue(id);
            check(User.getUser(id).getEffectKill() == effects.get("lightning"), "Bot load bypass");
            FlatFile.quit(id);
        }
        check(map(store).isEmpty(), "Bot persistent cache empty");
        check(User.getUsers().isEmpty(), "Bot User cache empty");
        check(((Set<?>) field(FlatFile.class, "TRANSIENT_BOTS").get(null)).isEmpty(), "Bot marker cache empty");
        check(field(EffectStore.class, "generation").getLong(store) == 0, "Bots never dirty database");
        check(!file.exists(), "Bots never trigger writes");
        guarded.reject = false;
        UUID human = UUID.randomUUID();
        FlatFile.join(player(human, "human"));
        User.getUser(human).setEffectKill(null);
        FlatFile.setValue(human);
        FlatFile.quit(human);
        FlatFile.join(player(human, "human"));
        check(User.getUser(human).getEffectKill() == null, "Human none restored");
        User.getUser(human).setEffectKill(effects.get("redstone"));
        FlatFile.setValue(human);
        FlatFile.quit(human);
        FlatFile.join(player(human, "human"));
        check(User.getUser(human).getEffectKill() == effects.get("redstone"), "Human custom restored");
        UUID bot = UUID.randomUUID();
        FlatFile.join(player(bot, "TEST_BOT"));
        User.getUser(bot).setEffectKill(effects.get("redstone"));
        FlatFile.shutdown();
        check(User.getUsers().isEmpty(), "Disable releases online users");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        check(yaml.getKeys(false).size() == 1 && "redstone".equals(yaml.getString(human.toString())), "Disable saves only human");
        System.out.println("PASS: 10000 bot lifecycles: persistent=0, users=0, transient=0, generation=0, disk writes=0");
        BotDetector.reset();
    }

    private static Player player(UUID id, String name) throws Exception {
        Unsafe unsafe = (Unsafe) field(Unsafe.class, "theUnsafe").get(null);
        TestPlayer player = (TestPlayer) unsafe.allocateInstance(TestPlayer.class);
        player.id = id;
        player.username = name;
        return player;
    }
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(PersistenceRegression.class.getClassLoader(), new Class<?>[] {type}, handler));
    }
    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field;
    }
    private static Map<?, ?> map(EffectStore store) throws Exception {
        return (Map<?, ?>) field(EffectStore.class, "effects").get(store);
    }
    private static void awaitSaved(EffectStore store) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < end) {
            synchronized (store) {
                if (field(EffectStore.class, "generation").getLong(store) == field(EffectStore.class, "savedGeneration").getLong(store)) {
                    return;
                }
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Save did not finish");
    }
    private static void check(boolean ok, String message) {
        if (!ok) { throw new AssertionError(message); }
    }
    public static final class BlockingMap extends ConcurrentHashMap<UUID, String> {
        final CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        @Override public Set<Map.Entry<UUID, String>> entrySet() {
            Set<Map.Entry<UUID, String>> snapshot = new java.util.HashSet<Map.Entry<UUID, String>>(super.entrySet());
            entered.countDown();
            try { check(release.await(5, TimeUnit.SECONDS), "Release snapshot"); }
            catch (InterruptedException e) { throw new AssertionError(e); }
            return snapshot;
        }
    }
    public static final class GuardMap extends ConcurrentHashMap<UUID, String> {
        boolean reject = true;
        @Override public String get(Object key) {
            check(!reject, "Bot must not read persistent cache"); return super.get(key);
        }
        @Override public String put(UUID key, String value) {
            check(!reject, "Bot must not enter persistent cache"); return super.put(key, value);
        }
        @Override public String remove(Object key) {
            check(!reject, "Bot must not modify persistent cache"); return super.remove(key);
        }
    }
    public static final class DummyEffect extends MainEffectKill {
        private DummyEffect() { super("unused", null, null, null); }
        @Override public void update(User user) { }
    }
    public static final class TestPlayer extends org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer {
        UUID id;
        String username;
        private TestPlayer() { super(null, null); }
        @Override public UUID getUniqueId() { return id; }
        @Override public String getName() { return username; }
    }
}
