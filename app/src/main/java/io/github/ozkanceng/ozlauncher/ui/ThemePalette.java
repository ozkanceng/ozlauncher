package io.github.ozkanceng.ozlauncher.ui;

/** Six deliberately dark TV-safe palettes. Index zero is OZLauncher navy/cyan. */
public final class ThemePalette {
    private static final int[][] COLORS = {
            {0xFF0F172A, 0xFF22D3EE, 0xDD1E293B, 0xFFF8FAFC},
            {0xFF111827, 0xFFA3E635, 0xDD1F2937, 0xFFF9FAFB},
            {0xFF1C1917, 0xFFF59E0B, 0xDD292524, 0xFFFAFAF9},
            {0xFF18181B, 0xFFA78BFA, 0xDD27272A, 0xFFFAFAFA},
            {0xFF0B1F1A, 0xFF34D399, 0xDD163029, 0xFFF0FDF4},
            {0xFF020617, 0xFFE2E8F0, 0xEE0F172A, 0xFFFFFFFF}
    };

    public final int background;
    public final int accent;
    public final int surface;
    public final int text;

    private ThemePalette(int[] colors, boolean highContrast) {
        background = highContrast ? 0xFF000000 : colors[0];
        accent = highContrast ? 0xFFFFFFFF : colors[1];
        surface = highContrast ? 0xFF111111 : colors[2];
        text = 0xFFFFFFFF;
    }

    public static ThemePalette of(int index, boolean highContrast) {
        return new ThemePalette(COLORS[Math.max(0, Math.min(COLORS.length - 1, index))], highContrast);
    }
}
