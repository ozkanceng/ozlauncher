package io.github.ozkanceng.ozlauncher.data;

import io.github.ozkanceng.ozlauncher.model.LaunchItem;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Pure collection transforms used by the UI and covered by local unit tests. */
public final class CatalogState {
    private CatalogState() { }

    public static List<LaunchItem> applyOrder(List<LaunchItem> source, List<String> order) {
        Map<String, Integer> positions = new HashMap<>();
        for (int i = 0; i < order.size(); i++) positions.put(order.get(i), i);
        Collator collator = Collator.getInstance(Locale.getDefault());
        ArrayList<LaunchItem> result = new ArrayList<>(source);
        result.sort((a, b) -> {
            int ai = positions.getOrDefault(a.id, Integer.MAX_VALUE);
            int bi = positions.getOrDefault(b.id, Integer.MAX_VALUE);
            if (ai != bi) return Integer.compare(ai, bi);
            return collator.compare(a.label, b.label);
        });
        return result;
    }

    public static List<LaunchItem> visible(List<LaunchItem> source, Set<String> hidden) {
        ArrayList<LaunchItem> result = new ArrayList<>();
        for (LaunchItem item : source) if (!hidden.contains(item.id)) result.add(item);
        return result;
    }

    public static List<LaunchItem> favorites(List<LaunchItem> source, Collection<String> ids) {
        Map<String, LaunchItem> byId = new HashMap<>();
        for (LaunchItem item : source) byId.put(item.id, item);
        ArrayList<LaunchItem> result = new ArrayList<>();
        for (String id : ids) {
            LaunchItem item = byId.get(id);
            if (item != null) result.add(item);
        }
        return result;
    }

    public static List<LaunchItem> search(List<LaunchItem> source, String query) {
        String needle = normalize(query);
        if (needle.isEmpty()) return new ArrayList<>(source);
        ArrayList<LaunchItem> result = new ArrayList<>();
        for (LaunchItem item : source) if (normalize(item.label).contains(needle)) result.add(item);
        return result;
    }

    public static List<LaunchItem> dedupeById(List<LaunchItem> source) {
        ArrayList<LaunchItem> result = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        for (LaunchItem item : source) if (seen.add(item.id)) result.add(item);
        return result;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim().toLowerCase(Locale.getDefault()), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
