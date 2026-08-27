package io.github.ozkanceng.ozlauncher.wallpaper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WallpaperStoreTest {
    @Test public void sampleSizeUsesPowersOfTwoWithoutUndershootingScreen() {
        assertEquals(2, WallpaperStore.sampleSize(3840, 2160, 1920, 1080));
        assertEquals(4, WallpaperStore.sampleSize(7680, 4320, 1920, 1080));
        assertEquals(1, WallpaperStore.sampleSize(1200, 4000, 1920, 1080));
    }
}
