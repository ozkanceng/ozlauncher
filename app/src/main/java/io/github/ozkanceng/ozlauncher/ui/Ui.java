package io.github.ozkanceng.ozlauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public final class Ui {
    private Ui() { }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(int color, float radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable tileBackground(Context context, ThemePalette palette,
                                                   int radiusDp, boolean focused) {
        // The tile itself stays deliberately quiet. The app icon is the visual unit; the
        // focus ring is the only large coloured surface the viewer needs to parse.
        GradientDrawable drawable = rounded(focused ? palette.surface : Color.TRANSPARENT,
                radiusDp, context);
        int stroke = focused ? dp(context, 2) : dp(context, 1);
        int strokeColor = focused ? palette.accent : 0x00000000;
        drawable.setStroke(stroke, strokeColor);
        return drawable;
    }

    /** A consistent small stage for legacy, adaptive, dark and white app icons alike. */
    public static GradientDrawable iconStage(Context context, ThemePalette palette, boolean focused) {
        int[] colors = focused
                ? new int[] { blend(palette.surface, palette.accent, 0.18f), palette.surface }
                : new int[] { lighten(palette.surface, 0.055f), palette.surface };
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(dp(context, 22));
        drawable.setStroke(dp(context, focused ? 2 : 1), focused ? palette.accent : 0x33FFFFFF);
        return drawable;
    }

    private static int lighten(int color, float amount) { return blend(color, Color.WHITE, amount); }

    private static int blend(int from, int to, float amount) {
        float inverse = 1f - amount;
        return Color.argb(Math.round(Color.alpha(from) * inverse + Color.alpha(to) * amount),
                Math.round(Color.red(from) * inverse + Color.red(to) * amount),
                Math.round(Color.green(from) * inverse + Color.green(to) * amount),
                Math.round(Color.blue(from) * inverse + Color.blue(to) * amount));
    }

    public static void hideSystemUi(View decor) {
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
