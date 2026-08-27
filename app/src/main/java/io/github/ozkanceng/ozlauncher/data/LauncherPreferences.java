package io.github.ozkanceng.ozlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** All persisted user choices. The JSON snapshot is the public backup contract. */
public final class LauncherPreferences {
    public static final int SCHEMA_VERSION = 1;
    public static final int CLOCK_FULL = 0;
    public static final int CLOCK_TIME = 1;
    public static final int CLOCK_OFF = 2;

    private static final String FILE = "ozlauncher_preferences";
    private static final String FAVORITES = "favorites";
    private static final String HIDDEN = "hidden";
    private static final String ORDER = "order";
    private static final String SHORTCUTS = "shortcuts";
    private static final String INITIALIZED = "favorites_initialized";
    private static final String THEME = "theme";
    private static final String COLUMNS = "columns";
    private static final String RADIUS = "radius";
    private static final String CLOCK = "clock";
    private static final String HIGH_CONTRAST = "high_contrast";
    private static final String REDUCED_MOTION = "reduced_motion";

    private final SharedPreferences prefs;

    public LauncherPreferences(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean favoritesInitialized() { return prefs.getBoolean(INITIALIZED, false); }

    public void initializeFavorites(List<String> ids) {
        if (favoritesInitialized()) return;
        prefs.edit().putString(FAVORITES, encodeArray(ids)).putBoolean(INITIALIZED, true).apply();
    }

    public List<String> favorites() { return decodeArray(prefs.getString(FAVORITES, "[]")); }
    public void setFavorites(Collection<String> ids) { putArray(FAVORITES, ids); }
    public List<String> order() { return decodeArray(prefs.getString(ORDER, "[]")); }
    public void setOrder(Collection<String> ids) { putArray(ORDER, ids); }
    public Set<String> hidden() { return new HashSet<>(decodeArray(prefs.getString(HIDDEN, "[]"))); }
    public void setHidden(Collection<String> ids) { putArray(HIDDEN, ids); }

    public int theme() { return clamp(prefs.getInt(THEME, 0), 0, 5); }
    public void setTheme(int value) { prefs.edit().putInt(THEME, clamp(value, 0, 5)).apply(); }
    public int columns() { return clamp(prefs.getInt(COLUMNS, 6), 5, 8); }
    public void setColumns(int value) { prefs.edit().putInt(COLUMNS, clamp(value, 5, 8)).apply(); }
    public int radiusDp() {
        int value = prefs.getInt(RADIUS, 16);
        return value == 0 || value == 8 || value == 16 || value == 24 ? value : 16;
    }
    public void setRadiusDp(int value) { prefs.edit().putInt(RADIUS, value).apply(); }
    public int clockMode() { return clamp(prefs.getInt(CLOCK, CLOCK_FULL), CLOCK_FULL, CLOCK_OFF); }
    public void setClockMode(int value) { prefs.edit().putInt(CLOCK, clamp(value, 0, 2)).apply(); }
    public boolean highContrast() { return prefs.getBoolean(HIGH_CONTRAST, false); }
    public void setHighContrast(boolean value) { prefs.edit().putBoolean(HIGH_CONTRAST, value).apply(); }
    public boolean reducedMotion() { return prefs.getBoolean(REDUCED_MOTION, false); }
    public void setReducedMotion(boolean value) { prefs.edit().putBoolean(REDUCED_MOTION, value).apply(); }

    public Map<Integer, String> shortcuts() {
        LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
        try {
            JSONObject json = new JSONObject(prefs.getString(SHORTCUTS, "{}"));
            java.util.Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(Integer.parseInt(key), json.optString(key, ""));
            }
        } catch (JSONException | NumberFormatException ignored) { }
        return result;
    }

    public void setShortcut(int keyCode, String id) {
        Map<Integer, String> map = shortcuts();
        if (id == null || id.isEmpty()) map.remove(keyCode); else map.put(keyCode, id);
        JSONObject json = new JSONObject();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            try { json.put(Integer.toString(entry.getKey()), entry.getValue()); }
            catch (JSONException ignored) { }
        }
        prefs.edit().putString(SHORTCUTS, json.toString()).apply();
    }

    public JSONObject toJson(Map<String, String> iconFiles) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.put("theme", theme());
        root.put("columns", columns());
        root.put("cornerRadiusDp", radiusDp());
        root.put("clockMode", clockMode());
        root.put("highContrast", highContrast());
        root.put("reducedMotion", reducedMotion());
        root.put("favorites", new JSONArray(favorites()));
        root.put("hidden", new JSONArray(hidden()));
        root.put("order", new JSONArray(order()));
        JSONObject keys = new JSONObject();
        for (Map.Entry<Integer, String> entry : shortcuts().entrySet()) {
            keys.put(Integer.toString(entry.getKey()), entry.getValue());
        }
        root.put("shortcuts", keys);
        JSONObject icons = new JSONObject();
        for (Map.Entry<String, String> entry : iconFiles.entrySet()) icons.put(entry.getKey(), entry.getValue());
        root.put("icons", icons);
        return root;
    }

    public boolean applyJson(JSONObject root) {
        if (!isValidJson(root)) return false;
        int columns = root.optInt("columns", 6);
        int radius = root.optInt("cornerRadiusDp", 16);
        int clock = root.optInt("clockMode", CLOCK_FULL);
        int theme = root.optInt("theme", 0);
        SharedPreferences.Editor edit = prefs.edit()
                .putInt(THEME, theme).putInt(COLUMNS, columns).putInt(RADIUS, radius)
                .putInt(CLOCK, clock).putBoolean(HIGH_CONTRAST, root.optBoolean("highContrast"))
                .putBoolean(REDUCED_MOTION, root.optBoolean("reducedMotion"))
                .putString(FAVORITES, validArray(root.optJSONArray("favorites")))
                .putString(HIDDEN, validArray(root.optJSONArray("hidden")))
                .putString(ORDER, validArray(root.optJSONArray("order")))
                .putBoolean(INITIALIZED, true);
        JSONObject shortcuts = root.optJSONObject("shortcuts");
        edit.putString(SHORTCUTS, shortcuts == null ? "{}" : shortcuts.toString());
        return edit.commit();
    }

    public static boolean isValidJson(JSONObject root) {
        if (root == null || root.optInt("schemaVersion", -1) != SCHEMA_VERSION) return false;
        int columns = root.optInt("columns", 6);
        int radius = root.optInt("cornerRadiusDp", 16);
        int clock = root.optInt("clockMode", CLOCK_FULL);
        int theme = root.optInt("theme", 0);
        if (columns < 5 || columns > 8 || (radius != 0 && radius != 8 && radius != 16 && radius != 24)
                || clock < 0 || clock > 2 || theme < 0 || theme > 5) return false;
        JSONArray favorites = root.optJSONArray("favorites");
        JSONArray hidden = root.optJSONArray("hidden");
        JSONArray order = root.optJSONArray("order");
        return favorites != null && hidden != null && order != null
                && favorites.length() <= 1000 && hidden.length() <= 1000 && order.length() <= 1000;
    }

    private static String validArray(JSONArray array) {
        ArrayList<String> values = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length() && i < 1000; i++) {
                String value = array.optString(i, "");
                if (!value.isEmpty() && value.length() <= 512 && !values.contains(value)) values.add(value);
            }
        }
        return encodeArray(values);
    }

    private void putArray(String key, Collection<String> values) {
        prefs.edit().putString(key, encodeArray(values)).apply();
    }

    public static String encodeArray(Collection<String> values) {
        return new JSONArray(values).toString();
    }

    public static List<String> decodeArray(String encoded) {
        ArrayList<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(encoded == null ? "[]" : encoded);
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "");
                if (!value.isEmpty() && !result.contains(value)) result.add(value);
            }
        } catch (JSONException ignored) { }
        return result;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
