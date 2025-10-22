package com.example.myapplication.ui;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

public final class UiUtils {
    private UiUtils() {}

    public static void applyExpressiveCard(MaterialCardView card) {
        int bg = MaterialColors.getColor(card, com.google.android.material.R.attr.colorSurfaceVariant);
        card.setCardBackgroundColor(bg);
        card.setUseCompatPadding(true);
        float radius = card.getResources().getDisplayMetrics().density * 20f;
        card.setRadius(radius);
        card.setCardElevation(card.getResources().getDisplayMetrics().density * 4f);
        card.setClickable(true);
        card.setFocusable(true);
        card.setForeground(androidx.appcompat.content.res.AppCompatResources.getDrawable(card.getContext(), com.google.android.material.R.drawable.mtrl_dropdown_arrow));
    }

    public static void animateRowEnter(View v) {
        v.setScaleX(0.98f);
        v.setScaleY(0.98f);
        v.setAlpha(0f);
        v.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(160)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}










