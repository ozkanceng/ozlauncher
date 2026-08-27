package io.github.ozkanceng.ozlauncher.wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Stores one display-sized RGB_565 wallpaper. No storage permission is required. */
public final class WallpaperStore {
    public interface Callback { void onLoaded(Bitmap bitmap, boolean success); }

    private final Context context;
    private final File file;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public WallpaperStore(Context context) {
        this.context = context.getApplicationContext();
        this.file = new File(context.getFilesDir(), "wallpaper.jpg");
    }

    public boolean exists() { return file.isFile(); }

    public void load(int width, int height, Callback callback) {
        executor.execute(() -> {
            Bitmap bitmap = file.isFile() ? decodeFile(file, width, height) : null;
            main.post(() -> callback.onLoaded(bitmap, bitmap != null));
        });
    }

    public void save(Uri uri, int width, int height, Callback callback) {
        executor.execute(() -> {
            Bitmap bitmap = decodeUri(uri, width, height);
            boolean success = false;
            if (bitmap != null) {
                File temp = new File(file.getParentFile(), file.getName() + ".tmp");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    success = bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out);
                    out.getFD().sync();
                } catch (IOException ignored) { success = false; }
                if (success) {
                    if (file.exists() && !file.delete()) success = false;
                    if (success && !temp.renameTo(file)) success = false;
                }
                if (!success) temp.delete();
            }
            boolean delivered = success;
            main.post(() -> callback.onLoaded(bitmap, delivered));
        });
    }

    public void clear() { executor.execute(() -> file.delete()); }
    public void shutdown() { executor.shutdownNow(); }

    private Bitmap decodeUri(Uri uri, int width, int height) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, width, height);
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                return in == null ? null : BitmapFactory.decodeStream(in, null, options);
            }
        } catch (IOException | SecurityException | OutOfMemoryError ignored) {
            return null;
        }
    }

    private static Bitmap decodeFile(File file, int width, int height) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, width, height);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try { return BitmapFactory.decodeFile(file.getAbsolutePath(), options); }
        catch (OutOfMemoryError ignored) { return null; }
    }

    public static int sampleSize(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        int sample = 1;
        int safeWidth = Math.max(1, targetWidth);
        int safeHeight = Math.max(1, targetHeight);
        while (sourceWidth / (sample * 2) >= safeWidth && sourceHeight / (sample * 2) >= safeHeight) sample *= 2;
        return sample;
    }
}
