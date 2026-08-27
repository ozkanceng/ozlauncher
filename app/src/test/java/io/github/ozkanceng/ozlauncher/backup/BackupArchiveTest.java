package io.github.ozkanceng.ozlauncher.backup;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupArchiveTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void archiveRoundTripsManifestAndIcons() throws Exception {
        File icon = temporary.newFile("icon.png");
        byte[] bytes = {1, 2, 3, 4};
        Files.write(icon.toPath(), bytes);
        Map<String, File> icons = new LinkedHashMap<>();
        icons.put("app:example", icon);
        JSONObject manifest = manifest();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BackupArchive.write(out, manifest, icons);
        BackupArchive.Restored restored = BackupArchive.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(1, restored.manifest.getInt("schemaVersion"));
        assertArrayEquals(bytes, restored.icons.get("app:example"));
    }

    @Test public void archiveRejectsUnexpectedPaths() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("../escape"));
            zip.write(new byte[] {1});
            zip.closeEntry();
        }
        assertThrows(java.io.IOException.class,
                () -> BackupArchive.read(new ByteArrayInputStream(out.toByteArray())));
    }

    @Test public void archiveRejectsUnknownSchema() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("manifest.json"));
            zip.write("{\"schemaVersion\":2}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        assertThrows(java.io.IOException.class,
                () -> BackupArchive.read(new ByteArrayInputStream(out.toByteArray())));
    }

    private static JSONObject manifest() throws Exception {
        return new JSONObject().put("schemaVersion", 1).put("theme", 0).put("columns", 6)
                .put("cornerRadiusDp", 16).put("clockMode", 0)
                .put("favorites", new JSONArray()).put("hidden", new JSONArray()).put("order", new JSONArray());
    }
}
