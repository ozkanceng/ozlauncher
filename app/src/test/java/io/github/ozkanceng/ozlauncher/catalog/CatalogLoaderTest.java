package io.github.ozkanceng.ozlauncher.catalog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CatalogLoaderTest {
    @Test public void ordersMediaTekHdmiHardwareIdsByPort() {
        assertEquals(5, CatalogLoader.hardwareOrder(
                "com.mediatek.tvinput/.hdmi.HDMIInputService/HW5"));
        assertEquals(6, CatalogLoader.hardwareOrder(
                "com.mediatek.tvinput/.hdmi.HDMIInputService/HW6"));
        assertEquals(7, CatalogLoader.hardwareOrder(
                "com.mediatek.tvinput/.hdmi.HDMIInputService/HW7"));
    }

    @Test public void unknownHardwareIdsSortLast() {
        assertEquals(Integer.MAX_VALUE, CatalogLoader.hardwareOrder(null));
        assertEquals(Integer.MAX_VALUE, CatalogLoader.hardwareOrder("input/HDMI"));
        assertEquals(Integer.MAX_VALUE, CatalogLoader.hardwareOrder("input/HWbad"));
    }
}
