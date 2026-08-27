package io.github.ozkanceng.ozlauncher.data;

import static org.junit.Assert.assertEquals;

import io.github.ozkanceng.ozlauncher.model.LaunchItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CatalogStateTest {
    private final LaunchItem netflix = LaunchItem.input("Netflix", "netflix");
    private final LaunchItem youtube = LaunchItem.input("YouTube", "youtube");
    private final LaunchItem hdmi = LaunchItem.input("HDMI 1", "hdmi1");

    @Test public void explicitOrderWinsThenLabelsSort() {
        List<LaunchItem> result = CatalogState.applyOrder(
                Arrays.asList(youtube, hdmi, netflix), Arrays.asList(netflix.id));
        assertEquals(netflix, result.get(0));
        assertEquals(hdmi, result.get(1));
        assertEquals(youtube, result.get(2));
    }

    @Test public void hiddenItemsAreRemovedWithoutChangingSource() {
        List<LaunchItem> source = Arrays.asList(netflix, youtube, hdmi);
        List<LaunchItem> result = CatalogState.visible(source, new HashSet<>(Arrays.asList(youtube.id)));
        assertEquals(Arrays.asList(netflix, hdmi), result);
        assertEquals(3, source.size());
    }

    @Test public void searchIgnoresCaseAndDiacritics() {
        LaunchItem accented = LaunchItem.input("Müzik", "music");
        assertEquals(Arrays.asList(accented), CatalogState.search(Arrays.asList(accented, youtube), "muz"));
    }

    @Test public void favoritesFollowConfiguredOrder() {
        assertEquals(Arrays.asList(youtube, netflix), CatalogState.favorites(
                Arrays.asList(netflix, youtube, hdmi), Arrays.asList(youtube.id, netflix.id, "missing")));
    }
}
