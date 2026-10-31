package com.lindedaoqwq.linclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lindedaoqwq.linclient.LinClient;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON config store. The whole document lives in memory as a {@link JsonObject};
 * callers read typed values and write through freely, {@link #markDirty()} schedules
 * a debounced save (sliders change values every frame - writing per change would
 * thrash the disk). On load, a corrupt file falls back to the newest valid backup.
 *
 * File: config/linclient.json. Schema version 1. Legacy linclient.cfg (Forge
 * Configuration from before the rewrite) is imported once, then renamed away.
 */
public final class ConfigManager {
    public static final int SCHEMA_VERSION = 1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_BACKUPS = 10;
    private static final long BACKUP_MIN_INTERVAL_MS = 5 * 60 * 1000L;

    private static JsonObject root = new JsonObject();
    private static boolean dirty = false;
    private static long lastBackupAt = 0L;
    private static long lastSaveAttempt = 0L;
    private static boolean loaded = false;

    private ConfigManager() { }

    // ---------- lifecycle ----------

    /** Load (or import/recover) the config. Safe to call before modules register. */
    public static synchronized void load() {
        File file = file();
        tryImportLegacyCfg(file);
        if (file.isFile()) {
            try {
                Reader r = new FileReader(file);
                JsonElement parsed = new JsonParser().parse(r);
                r.close();
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                }
            } catch (Throwable t) {
                LinClient.LOGGER.warn("Config unreadable, trying backups", t);
                if (!tryRestoreBackup()) {
                    root = new JsonObject();
                }
            }
        }
        migrate();
        loaded = true;
    }

    /** Debounced save; call once per tick from the client tick handler. */
    public static synchronized void tickSave() {
        if (!dirty) return;
        long now = System.currentTimeMillis();
        if (now - lastSaveAttempt < 500L) return;
        saveNow();
    }

    /** Force a save (GUI close, world exit, ...). */
    public static synchronized void saveNow() {
        if (!loaded) return;
        lastSaveAttempt = System.currentTimeMillis();
        dirty = false;
        maybeBackup();
        File file = file();
        try {
            file.getParentFile().mkdirs();
            root.addProperty("schemaVersion", SCHEMA_VERSION);
            FileWriter w = new FileWriter(file);
            GSON.toJson(root, w);
            w.close();
        } catch (Throwable t) {
            LinClient.LOGGER.warn("Failed to save config", t);
        }
    }

    // ---------- typed accessors ----------

    public static boolean getBool(String key, boolean def) {
        JsonElement e = at(key);
        return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : def;
    }

    public static float getNum(String key, float def) {
        JsonElement e = at(key);
        return e != null && e.isJsonPrimitive() ? e.getAsFloat() : def;
    }

    public static int getColor(String key, int def) {
        JsonElement e = at(key);
        if (e == null || !e.isJsonPrimitive()) return def;
        try {
            return Integer.parseInt(e.getAsString().replace("#", ""), 16);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public static String getStr(String key, String def) {
        JsonElement e = at(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : def;
    }

    /** Write a value. Booleans/numbers/strings are stored natively; ints that look like
     *  colours (key ends with .color or high-bit set) go out as hex strings so the file
     *  stays human-editable. */
    public static void set(String key, Object v) {
        String[] parts = key.split("\\.");
        JsonObject node = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement next = node.get(parts[i]);
            if (next == null || !next.isJsonObject()) {
                next = new JsonObject();
                node.add(parts[i], next);
            }
            node = next.getAsJsonObject();
        }
        String leaf = parts[parts.length - 1];
        if (v instanceof Boolean) node.addProperty(leaf, (Boolean) v);
        else if (v instanceof Number) {
            Number n = (Number) v;
            if (n instanceof Integer || n instanceof Long) {
                long lv = n.longValue();
                boolean colorish = leaf.equals("color") || (lv >>> 24) != 0;
                if (colorish) node.addProperty(leaf, String.format("#%06X", lv & 0xFFFFFF));
                else node.addProperty(leaf, lv);
            } else {
                node.addProperty(leaf, n.floatValue());
            }
        } else if (v instanceof String) node.addProperty(leaf, (String) v);
        dirty = true;
    }

    // ---------- client-level (non-module) settings ----------

    public static String getLang() { return getStr("client.lang", "zh_CN"); }

    public static void setLang(String l) {
        set("client.lang", l == null || l.isEmpty() ? "zh_CN" : l);
    }

    public static boolean isZh() {
        String l = getLang();
        return l != null && l.startsWith("zh");
    }

    // ---------- internals ----------

    /** Schedule a debounced save. Call after any out-of-band change to the document. */
    public static void markDirty() { dirty = true; }

    private static JsonElement at(String key) {
        String[] parts = key.split("\\.");
        JsonElement node = root;
        for (String part : parts) {
            if (node == null || !node.isJsonObject()) return null;
            node = node.getAsJsonObject().get(part);
        }
        return node;
    }

    private static File file() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/linclient.json");
    }

    /** Schema migrations: step the document forward one version at a time. v1 is the
     *  first version, so nothing to do yet - the scaffolding is here for v2+. */
    private static void migrate() {
        int v = (int) getNum("schemaVersion", 1);
        // if (v < 2) { ...; v = 2; } ...
        if (v != SCHEMA_VERSION) {
            root.addProperty("schemaVersion", SCHEMA_VERSION);
        }
    }

    /** Legacy Forge .cfg import (pre-rewrite format). Runs once; renames the old file. */
    private static void tryImportLegacyCfg(File jsonFile) {
        File old = new File(jsonFile.getParentFile(), "linclient.cfg");
        if (!old.isFile()) return;
        try {
            List<String> lines = Files.readAllLines(old.toPath(), StandardCharsets.UTF_8);
            String section = null;
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1);
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0 || section == null) continue;
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                int colon = v.indexOf(':');   // Forge values may be "value:comment"
                if (colon >= 0) v = v.substring(0, colon).trim();
                importLegacyValue(section, k, v);
            }
            File done = new File(old.getParentFile(), "linclient.cfg.imported.bak");
            old.renameTo(done);
            LinClient.LOGGER.info("Imported legacy linclient.cfg into linclient.json");
        } catch (Throwable t) {
            LinClient.LOGGER.warn("Legacy config import failed (ignoring)", t);
        }
    }

    /** Map one legacy key. Legacy shapes:
     *  modules.<id> / settings.<module>.<key> / colors.<id> / relpos.<id>.{x,y} /
     *  client.lang. */
    private static void importLegacyValue(String section, String key, String value) {
        try {
            if (section.equals("modules")) {
                set("modules." + key + ".enabled", Boolean.parseBoolean(value));
            } else if (section.equals("settings")) {
                set("modules." + key, Float.parseFloat(value));
            } else if (section.equals("colors")) {
                int c = Integer.parseInt(value);
                if (c != -1) set("modules." + key + ".settings.color", String.format("#%06X", c & 0xFFFFFF));
            } else if (section.equals("relpos")) {
                int dot = key.lastIndexOf('.');
                if (dot > 0) {
                    String id = key.substring(0, dot);
                    String axis = key.substring(dot + 1);
                    double d = Double.parseDouble(value);
                    if (axis.equals("x")) set("hud." + id + ".x", (float) d);
                    if (axis.equals("y")) set("hud." + id + ".y", (float) d);
                }
            } else if (section.equals("client") && key.equals("lang")) {
                set("client.lang", value);
            }
        } catch (Throwable ignored) {
            // one bad line never aborts the import
        }
    }

    // ---------- backups ----------

    private static File backupDir() {
        return new File(file().getParentFile(), "linclient-backups");
    }

    private static void maybeBackup() {
        long now = System.currentTimeMillis();
        if (now - lastBackupAt < BACKUP_MIN_INTERVAL_MS) return;
        lastBackupAt = now;
        try {
            File dir = backupDir();
            dir.mkdirs();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
            File out = new File(dir, "linclient-" + fmt.format(new Date(now)) + ".json");
            Files.copy(file().toPath(), out.toPath());
            pruneBackups(dir);
        } catch (Throwable ignored) {
            // best effort
        }
    }

    private static void pruneBackups(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length <= MAX_BACKUPS) return;
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File a, File b) { return b.getName().compareTo(a.getName()); }
        });
        for (int i = MAX_BACKUPS; i < files.length; i++) files[i].delete();
    }

    /** Fall back to the newest backup whose JSON parses. Returns true on success. */
    private static boolean tryRestoreBackup() {
        File dir = backupDir();
        File[] files = dir.listFiles();
        if (files == null) return false;
        List<File> sorted = new ArrayList<File>(Arrays.asList(files));
        java.util.Collections.sort(sorted, new Comparator<File>() {
            public int compare(File a, File b) { return b.getName().compareTo(a.getName()); }
        });
        for (File f : sorted) {
            try {
                Reader r = new FileReader(f);
                JsonElement parsed = new JsonParser().parse(r);
                r.close();
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                    LinClient.LOGGER.warn("Restored config from backup {}", f.getName());
                    return true;
                }
            } catch (Throwable ignored) {
                // try the next one
            }
        }
        return false;
    }

    /** Test helper: direct map view of the top-level document. */
    static Set<Map.Entry<String, JsonElement>> topLevel() {
        return root.entrySet();
    }
}
