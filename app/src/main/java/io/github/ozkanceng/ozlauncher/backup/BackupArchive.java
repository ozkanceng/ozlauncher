package io.github.ozkanceng.ozlauncher.backup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Strict, bounded encoder/decoder for the public .ozbackup archive format. */
public final class BackupArchive {
    private static final int MAX_TOTAL = 8 * 1024 * 1024;
    private static final int MAX_ENTRY = 1024 * 1024;
    private static final int MAX_ENTRIES = 300;

    public static final class Restored {
        public final JSONObject manifest;
        public final Map<String, byte[]> icons;
        Restored(JSONObject manifest, Map<String, byte[]> icons) {
            this.manifest = manifest;
            this.icons = icons;
        }
    }

    private BackupArchive() { }

    public static void write(OutputStream output, JSONObject manifest, Map<String, File> icons)
            throws IOException, JSONException {
        JSONObject iconIndex = new JSONObject();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Map.Entry<String, File> entry : icons.entrySet()) {
                String path = "icons/" + safeName(entry.getKey()) + ".png";
                iconIndex.put(entry.getKey(), path);
                zip.putNextEntry(new ZipEntry(path));
                try (FileInputStream in = new FileInputStream(entry.getValue())) { copyBounded(in, zip, MAX_ENTRY); }
                zip.closeEntry();
            }
            manifest.put("icons", iconIndex);
            zip.putNextEntry(new ZipEntry("manifest.json"));
            zip.write(manifest.toString(2).getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    public static Restored read(InputStream input) throws IOException, JSONException {
        HashMap<String, byte[]> entries = new HashMap<>();
        int total = 0;
        int count = 0;
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++count > MAX_ENTRIES || entry.isDirectory()) throw new IOException("Too many entries");
                String name = entry.getName();
                if (!name.equals("manifest.json") && !name.matches("icons/[a-f0-9]{64}\\.png")) {
                    throw new IOException("Unexpected entry");
                }
                byte[] bytes = readBounded(zip, MAX_ENTRY);
                total += bytes.length;
                if (total > MAX_TOTAL || entries.put(name, bytes) != null) throw new IOException("Invalid archive");
                zip.closeEntry();
            }
        }
        byte[] manifestBytes = entries.get("manifest.json");
        if (manifestBytes == null) throw new IOException("Missing manifest");
        JSONObject manifest = new JSONObject(new String(manifestBytes, StandardCharsets.UTF_8));
        if (manifest.optInt("schemaVersion", -1) != 1) throw new IOException("Unsupported schema");
        JSONObject iconIndex = manifest.optJSONObject("icons");
        LinkedHashMap<String, byte[]> icons = new LinkedHashMap<>();
        if (iconIndex != null) {
            java.util.Iterator<String> ids = iconIndex.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                if (id.length() > 512) throw new IOException("Invalid item id");
                String path = iconIndex.optString(id, "");
                byte[] bytes = entries.get(path);
                if (!path.matches("icons/[a-f0-9]{64}\\.png") || bytes == null) throw new IOException("Missing icon");
                icons.put(id, bytes);
            }
        }
        return new Restored(manifest, icons);
    }

    private static String safeName(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : digest) out.append(String.format(java.util.Locale.US, "%02x", b));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static byte[] readBounded(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyBounded(input, out, limit);
        return out.toByteArray();
    }

    private static void copyBounded(InputStream input, OutputStream output, int limit) throws IOException {
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > limit) throw new IOException("Entry too large");
            output.write(buffer, 0, read);
        }
    }
}
