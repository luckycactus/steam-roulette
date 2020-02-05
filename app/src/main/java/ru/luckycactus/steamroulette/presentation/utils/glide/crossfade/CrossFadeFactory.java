package ru.luckycactus.steamroulette.presentation.utils.glide.crossfade;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.NoTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

/**
 * https://github.com/bumptech/glide/issues/363
 * https://gist.github.com/kevinvanmierlo/c46f66027e3ae37ebea85a8d2e12aaba
 */
public class CrossFadeFactory implements TransitionFactory<Drawable> {
    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        if (dataSource == DataSource.MEMORY_CACHE) {
            return NoTransition.get();
        }
        return new CrossFadeTransition();
    }
}