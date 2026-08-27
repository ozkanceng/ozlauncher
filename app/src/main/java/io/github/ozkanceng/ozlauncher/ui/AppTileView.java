package io.github.ozkanceng.ozlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.ozkanceng.ozlauncher.icons.IconRepository;
import io.github.ozkanceng.ozlauncher.model.LaunchItem;

public final class AppTileView extends LinearLayout {
    private final FrameLayout iconStage;
    private final ImageView icon;
    private final TextView label;
    private LaunchItem item;
    private ThemePalette palette;
    private int radiusDp;
    private boolean reducedMotion;

    public AppTileView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setFocusable(true);
        setClickable(true);
        setLongClickable(true);
        int pad = Ui.dp(context, 7);
        setPadding(pad, pad, pad, Ui.dp(context, 8));

        iconStage = new FrameLayout(context);
        iconStage.setForegroundGravity(Gravity.CENTER);
        iconStage.setClipToOutline(true);
        addView(iconStage, new LayoutParams(Ui.dp(context, 92), Ui.dp(context, 92)));

        icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        int iconSize = Ui.dp(context, 66);
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER);
        iconStage.addView(icon, iconLp);

        label = new TextView(context);
        label.setGravity(Gravity.CENTER);
        label.setTextColor(Color.WHITE);
        label.setTextSize(15);
        label.setLetterSpacing(0.01f);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(context, 8);
        addView(label, lp);

        setOnFocusChangeListener((view, focused) -> updateFocus(focused));
    }

    public void style(ThemePalette palette, int radiusDp, boolean reducedMotion) {
        this.palette = palette;
        this.radiusDp = radiusDp;
        this.reducedMotion = reducedMotion;
        label.setTextColor(palette.text);
        updateFocus(isFocused());
    }

    public void bind(LaunchItem item, IconRepository icons, int iconPx) {
        this.item = item;
        if (item == null) {
            setVisibility(INVISIBLE);
            setContentDescription(null);
            return;
        }
        setVisibility(VISIBLE);
        label.setText(item.label);
        setContentDescription(item.label);
        Bitmap cached = icons.cached(item.id);
        if (cached != null) icon.setImageBitmap(cached); else icon.setImageDrawable(new ColorDrawable(0x2238BDF8));
        icons.request(item, iconPx, (id, bitmap) -> {
            if (this.item != null && this.item.id.equals(id) && bitmap != null) icon.setImageBitmap(bitmap);
        });
    }

    public LaunchItem item() { return item; }

    private void updateFocus(boolean focused) {
        if (palette == null) return;
        setBackground(Ui.tileBackground(getContext(), palette, radiusDp, focused));
        iconStage.setBackground(Ui.iconStage(getContext(), palette, focused));
        label.setTextColor(palette.text);
        label.setAlpha(focused ? 1f : 0.86f);
        float scale = focused && !reducedMotion ? 1.045f : 1f;
        float iconScale = focused && !reducedMotion ? 1.06f : 1f;
        if (reducedMotion) { setScaleX(scale); setScaleY(scale); }
        else {
            animate().scaleX(scale).scaleY(scale).setDuration(120).start();
            iconStage.animate().scaleX(iconScale).scaleY(iconScale).setDuration(120).start();
        }
        if (reducedMotion) { iconStage.setScaleX(iconScale); iconStage.setScaleY(iconScale); }
        setElevation(focused ? Ui.dp(getContext(), 10) : 0);
    }
}
