package io.github.ozkanceng.ozlauncher.icons;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import io.github.ozkanceng.ozlauncher.model.LaunchItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Memory-bounded icon loader with private custom-icon storage and no network access. */
public final class IconRepository {
    public interface Callback { void onIcon(String itemId, Bitmap bitmap); }

    private static final String INDEX_KEY = "custom_icon_index";
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final LruCache<String, Bitmap> memory;
    private final File customDir;
    private final SharedPreferences indexPrefs;

    public IconRepository(Context context) {
        this.context = context.getApplicationContext();
        int maxKb = Math.min(8 * 1024, Math.max(2 * 1024,
                (int) (Runtime.getRuntime().maxMemory() / 1024 / 24)));
        memory = new LruCache<String, Bitmap>(maxKb) {
            @Override protected int sizeOf(String key, Bitmap value) { return value.getAllocationByteCount() / 1024; }
        };
        customDir = new File(context.getFilesDir(), "custom-icons");
        if (!customDir.exists()) customDir.mkdirs();
        indexPrefs = context.getSharedPreferences("ozlauncher_icons", Context.MODE_PRIVATE);
    }

    public Bitmap cached(String id) { return memory.get(id); }

    public void request(LaunchItem item, int sizePx, Callback callback) {
        Bitmap cached = memory.get(item.id);
        if (cached != null) { callback.onIcon(item.id, cached); return; }
        executor.execute(() -> {
            Bitmap bitmap = loadCustom(item.id, sizePx);
            if (bitmap == null) bitmap = item.kind == LaunchItem.Kind.TV_INPUT
                    ? drawInput(sizePx) : loadApplication(item, sizePx);
            if (bitmap != null) memory.put(item.id, bitmap);
            Bitmap delivered = bitmap;
            main.post(() -> callback.onIcon(item.id, delivered));
        });
    }

    @SuppressLint("ApplySharedPref")
    public boolean saveCustom(String itemId, Uri uri) {
        Bitmap source = null;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return false;
            source = BitmapFactory.decodeStream(in);
            if (source == null) return false;
            Bitmap normalized = fit(source, 256);
            String fileName = sha256(itemId) + ".png";
            File temp = new File(customDir, fileName + ".tmp");
            try (FileOutputStream out = new FileOutputStream(temp)) {
                if (!normalized.compress(Bitmap.CompressFormat.PNG, 100, out)) return false;
                out.getFD().sync();
            }
            File target = new File(customDir, fileName);
            File previous = new File(customDir, fileName + ".previous");
            if (previous.exists()) previous.delete();
            if (target.exists() && !target.renameTo(previous)) return false;
            if (!temp.renameTo(target)) {
                previous.renameTo(target);
                return false;
            }
            previous.delete();
            JSONObject index = index();
            index.put(itemId, fileName);
            indexPrefs.edit().putString(INDEX_KEY, index.toString()).commit();
            memory.remove(itemId);
            if (normalized != source) normalized.recycle();
            return true;
        } catch (IOException | JSONException | SecurityException ignored) {
            return false;
        } finally {
            if (source != null && !source.isRecycled()) source.recycle();
        }
    }

    public void resetCustom(String itemId) {
        JSONObject index = index();
        String fileName = index.optString(itemId, "");
        if (!fileName.isEmpty()) new File(customDir, fileName).delete();
        index.remove(itemId);
        indexPrefs.edit().putString(INDEX_KEY, index.toString()).apply();
        memory.remove(itemId);
    }

    public Map<String, File> customFiles() {
        LinkedHashMap<String, File> result = new LinkedHashMap<>();
        JSONObject index = index();
        Iterator<String> keys = index.keys();
        while (keys.hasNext()) {
            String id = keys.next();
            File file = new File(customDir, index.optString(id));
            if (file.isFile()) result.put(id, file);
        }
        return result;
    }

    public Map<String, byte[]> customFileBytes() {
        LinkedHashMap<String, byte[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, File> entry : customFiles().entrySet()) {
            try { result.put(entry.getKey(), java.nio.file.Files.readAllBytes(entry.getValue().toPath())); }
            catch (IOException ignored) { }
        }
        return result;
    }

    @SuppressLint("ApplySharedPref")
    public boolean replaceCustomFiles(Map<String, byte[]> icons) {
        File stage = new File(context.getCacheDir(), "restore-icons");
        File previous = new File(context.getFilesDir(), "custom-icons.previous");
        deleteTree(stage);
        deleteTree(previous);
        if (!stage.mkdirs()) return false;
        JSONObject next = new JSONObject();
        String oldIndex = indexPrefs.getString(INDEX_KEY, "{}");
        try {
            for (Map.Entry<String, byte[]> entry : icons.entrySet()) {
                if (entry.getKey().length() > 512 || entry.getValue().length > 1024 * 1024) return false;
                Bitmap decoded = BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length);
                if (decoded == null) return false;
                decoded.recycle();
                String fileName = sha256(entry.getKey()) + ".png";
                try (FileOutputStream out = new FileOutputStream(new File(stage, fileName))) {
                    out.write(entry.getValue());
                }
                next.put(entry.getKey(), fileName);
            }
            if (customDir.exists() && !customDir.renameTo(previous)) return false;
            if (!stage.renameTo(customDir)) {
                previous.renameTo(customDir);
                return false;
            }
            if (!indexPrefs.edit().putString(INDEX_KEY, next.toString()).commit()) {
                deleteTree(customDir);
                previous.renameTo(customDir);
                indexPrefs.edit().putString(INDEX_KEY, oldIndex).commit();
                return false;
            }
            deleteTree(previous);
            memory.evictAll();
            return true;
        } catch (IOException | JSONException ignored) {
            return false;
        } finally {
            if (stage.exists()) deleteTree(stage);
            if (previous.exists() && customDir.exists()) deleteTree(previous);
        }
    }

    public void shutdown() { executor.shutdownNow(); memory.evictAll(); }

    private Bitmap loadCustom(String id, int sizePx) {
        String name = index().optString(id, "");
        if (name.isEmpty()) return null;
        Bitmap decoded = BitmapFactory.decodeFile(new File(customDir, name).getAbsolutePath());
        if (decoded == null) return null;
        Bitmap result = fit(decoded, sizePx);
        if (result != decoded) decoded.recycle();
        return result;
    }

    private Bitmap loadApplication(LaunchItem item, int sizePx) {
        try {
            Drawable drawable = item.resolveInfo == null ? null : item.resolveInfo.loadIcon(context.getPackageManager());
            if (drawable == null) return null;
            Bitmap out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(out);
            int inset = Math.max(1, sizePx / 14);
            drawable.setBounds(inset, inset, sizePx - inset, sizePx - inset);
            drawable.draw(canvas);
            return out;
        } catch (RuntimeException | OutOfMemoryError ignored) {
            return null;
        }
    }

    private static Bitmap drawInput(int size) {
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF22D3EE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3, size / 18f));
        Rect rect = new Rect(size / 8, size / 5, size * 7 / 8, size * 3 / 4);
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, size / 12f, size / 12f, paint);
        canvas.drawLine(size / 3f, size * 7 / 8f, size * 2 / 3f, size * 7 / 8f, paint);
        canvas.drawLine(size / 2f, size * 3 / 4f, size / 2f, size * 7 / 8f, paint);
        return out;
    }

    private static Bitmap fit(Bitmap source, int target) {
        if (source.getWidth() == target && source.getHeight() == target) return source;
        Bitmap out = Bitmap.createBitmap(target, target, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        float scale = Math.min((float) target / source.getWidth(), (float) target / source.getHeight());
        float width = source.getWidth() * scale;
        float height = source.getHeight() * scale;
        canvas.drawBitmap(source, null,
                new android.graphics.RectF((target - width) / 2f, (target - height) / 2f,
                        (target + width) / 2f, (target + height) / 2f), paint);
        return out;
    }

    private JSONObject index() {
        try { return new JSONObject(indexPrefs.getString(INDEX_KEY, "{}")); }
        catch (JSONException ignored) { return new JSONObject(); }
    }

    private static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format(java.util.Locale.US, "%02x", b));
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static void deleteTree(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteTree(child);
        file.delete();
    }
}
