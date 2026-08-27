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
        // A launcher should not repaint the developer's icon. Only a focused icon receives
        // a small outline so D-pad position is obvious at a distance.
        GradientDrawable drawable = rounded(Color.TRANSPARENT, radiusDp, context);
        drawable.setStroke(focused ? dp(context, 2) : 0, focused ? palette.accent : Color.TRANSPARENT);
        return drawable;
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
