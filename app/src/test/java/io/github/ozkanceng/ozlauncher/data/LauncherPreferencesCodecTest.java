package io.github.ozkanceng.ozlauncher.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

public class LauncherPreferencesCodecTest {
    @Test public void arrayRoundTripsUnicodeAndOrder() {
        String encoded = LauncherPreferences.encodeArray(Arrays.asList("app:netflix", "input:HDMI-1", "Müzik"));
        assertEquals(Arrays.asList("app:netflix", "input:HDMI-1", "Müzik"),
                LauncherPreferences.decodeArray(encoded));
    }

    @Test public void invalidJsonFallsBackToEmptyArray() {
        assertTrue(LauncherPreferences.decodeArray("not-json").isEmpty());
    }

    @Test public void backupValidationRejectsUnsafeLayoutValues() throws Exception {
        JSONObject valid = new JSONObject().put("schemaVersion", 1).put("theme", 0)
                .put("columns", 6).put("cornerRadiusDp", 16).put("clockMode", 0)
                .put("favorites", new JSONArray()).put("hidden", new JSONArray()).put("order", new JSONArray());
        assertTrue(LauncherPreferences.isValidJson(valid));
        valid.put("columns", 99);
        assertFalse(LauncherPreferences.isValidJson(valid));
    }
}
