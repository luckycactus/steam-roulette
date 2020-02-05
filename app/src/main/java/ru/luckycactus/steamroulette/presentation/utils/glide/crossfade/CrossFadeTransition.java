package ru.luckycactus.steamroulette.presentation.utils.glide.crossfade;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.transition.Transition;

/**
 * https://github.com/bumptech/glide/issues/363
 * https://gist.github.com/kevinvanmierlo/c46f66027e3ae37ebea85a8d2e12aaba
 */
public class CrossFadeTransition implements Transition<Drawable> {
    @Override
    public boolean transition(Drawable current, ViewAdapter adapter) {
        Drawable previous = adapter.getCurrentDrawable();
        if (previous == null) {
            previous = new ColorDrawable(Color.TRANSPARENT);
        }

        CrossFadeDrawable crossFadeDrawable = new CrossFadeDrawable(previous, current);
        crossFadeDrawable.setCrossFadeEnabled(true);
        crossFadeDrawable.startTransition();
        adapter.setDrawable(crossFadeDrawable);
        return true;
    }
}
