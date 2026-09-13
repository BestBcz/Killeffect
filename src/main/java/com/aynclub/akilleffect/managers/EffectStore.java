package com.aynclub.akilleffect.managers;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** No Player or User objects cross the writer boundary. */
final class EffectStore {
    private final Map<UUID, String> effects = new ConcurrentHashMap<UUID, String>();
    private final ScheduledThreadPoolExecutor writer;
    private final File file;
    private final Logger logger;
    private final long delayMillis;
    private ScheduledFuture<?> pending;
    private long generation;
    private long savedGeneration;
    private long scheduleTicket;
    private boolean closed;

    EffectStore(File file, Logger logger, long delayMillis) throws IOException {
        this.file = file;
        this.logger = logger;
        this.delayMillis = delayMillis;
        YamlConfiguration yaml = new YamlConfiguration();
        if (file.exists()) {
            try {
                yaml.load(file);
            } catch (InvalidConfigurationException e) {
                throw new IOException("Invalid database.yml; refusing to overwrite it", e);
            }
        }
        for (String key : yaml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid database UUID: " + key, e);
            }
            String value = yaml.getString(key);
            if ("lightning".equalsIgnoreCase(value)) {
                generation++;
            } else {
                effects.put(uuid, value == null ? "none" : value);
            }
        }
        writer = new ScheduledThreadPoolExecutor(1, task -> {
            Thread thread = new Thread(task, "aKilleffect-persistence");
            thread.setDaemon(true);
            return thread;
        });
        writer.setRemoveOnCancelPolicy(true);
        if (generation != 0) {
            schedule();
        }
    }

    String get(UUID uuid) {
        return effects.get(uuid);
    }

    synchronized void put(UUID uuid, String value) {
        if (closed) {
            throw new IllegalStateException("Persistence is closed");
        }
        String normalized = "lightning".equalsIgnoreCase(value) ? null : value;
        String old = normalized == null ? effects.remove(uuid) : effects.put(uuid, normalized);
        if (!java.util.Objects.equals(old, normalized)) {
            generation++;
            schedule();
        }
    }

    private synchronized void schedule() {
        if (closed) {
            return;
        }
        if (pending != null) {
            pending.cancel(false);
        }
        long ticket = ++scheduleTicket;
        pending = writer.schedule(() -> save(false, ticket), delayMillis, TimeUnit.MILLISECONDS);
    }

    private void save(boolean force, long ticket) {
        long version;
        synchronized (this) {
            // A canceled task may already be waiting for this lock.
            if (!force && (closed || ticket != scheduleTicket)) {
                return;
            }
            pending = null;
            version = generation;
            if (!force && savedGeneration == version) {
                return;
            }
        }
        // Copy on the writer without holding the hot-path lock. Concurrent changes
        // advance generation and receive another save, even if copied here.
        Map<UUID, String> snapshot = new HashMap<UUID, String>(effects);
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<UUID, String> entry : snapshot.entrySet()) {
                yaml.set(entry.getKey().toString(), entry.getValue());
            }
            File parent = file.getAbsoluteFile().getParentFile();
            Files.createDirectories(parent.toPath());
            File temp = new File(parent, file.getName() + ".tmp");
            yaml.save(temp);
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            synchronized (this) {
                savedGeneration = version;
            }
        } catch (IOException | RuntimeException e) {
            logger.log(Level.SEVERE, "Could not save database.yml; unsaved changes retained", e);
        } finally {
            synchronized (this) {
                if (savedGeneration != generation && pending == null) {
                    schedule();
                }
            }
        }
    }

    void close() {
        Future<?> finalSave;
        synchronized (this) {
            closed = true;
            if (pending != null) {
                pending.cancel(false);
            }
            // Runs after any in-flight snapshot, so older data cannot overwrite it.
            finalSave = writer.submit(() -> save(true, 0));
            writer.shutdown();
        }
        boolean interrupted = false;
        try {
            for (;;) {
                try {
                    finalSave.get();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    logger.log(Level.SEVERE, "Final database save failed", e.getCause());
                    break;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
