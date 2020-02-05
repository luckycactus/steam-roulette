package ru.luckycactus.steamroulette.presentation.utils.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pools;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * From com.bumptech.glide.load.model.MultiModelLoader
 */
class MultiFetcher<Data> implements DataFetcher<Data>, DataFetcher.DataCallback<Data> {

    private final List<DataFetcher<Data>> fetchers;
    private final Pools.Pool<List<Throwable>> throwableListPool;
    private int currentIndex;
    private Priority priority;
    private DataCallback<? super Data> callback;
    @Nullable
    private List<Throwable> exceptions;
    private boolean isCancelled;

    MultiFetcher(
            @NonNull List<DataFetcher<Data>> fetchers,
            @NonNull Pools.Pool<List<Throwable>> throwableListPool) {
        this.throwableListPool = throwableListPool;
        Preconditions.checkNotEmpty(fetchers);
        this.fetchers = fetchers;
        currentIndex = 0;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Data> callback) {
        this.priority = priority;
        this.callback = callback;
        exceptions = throwableListPool.acquire();
        fetchers.get(currentIndex).loadData(priority, this);

        // If a race occurred where we cancelled the fetcher in cancel() and then called loadData here
        // immediately after, make sure that we cancel the newly started fetcher. We don't bother
        // checking cancelled before loadData because it's not required for correctness and would
        // require an unlikely race to be useful.
        if (isCancelled) {
            cancel();
        }
    }

    @Override
    public void cleanup() {
        if (exceptions != null) {
            throwableListPool.release(exceptions);
        }
        exceptions = null;
        for (DataFetcher<Data> fetcher : fetchers) {
            fetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        for (DataFetcher<Data> fetcher : fetchers) {
            fetcher.cancel();
        }
    }

    @NonNull
    @Override
    public Class<Data> getDataClass() {
        return fetchers.get(0).getDataClass();
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return fetchers.get(0).getDataSource();
    }

    @Override
    public void onDataReady(@Nullable Data data) {
        if (data != null) {
            callback.onDataReady(data);
        } else {
            startNextOrFail();
        }
    }

    @Override
    public void onLoadFailed(@NonNull Exception e) {
        Preconditions.checkNotNull(exceptions).add(e);
        startNextOrFail();
    }

    private void startNextOrFail() {
        if (isCancelled) {
            return;
        }

        if (currentIndex < fetchers.size() - 1) {
            currentIndex++;
            loadData(priority, callback);
        } else {
            Preconditions.checkNotNull(exceptions);
            callback.onLoadFailed(new GlideException("Fetch failed", new ArrayList<>(exceptions)));
        }
    }
}
