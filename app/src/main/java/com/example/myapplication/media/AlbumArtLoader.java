package com.example.myapplication.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AlbumArtLoader {
    private static final LruCache<String, Bitmap> cache;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 16; // 1/16th of available memory
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    private AlbumArtLoader() {}

    public static void loadInto(@NonNull String filePath, @NonNull ImageView target, int placeholderResId) {
        target.setImageResource(placeholderResId);
        target.setTag(filePath);
        Bitmap cached = cache.get(filePath);
        if (cached != null) {
            if (filePath.equals(target.getTag())) {
                target.setImageBitmap(cached);
            }
            return;
        }
        executor.execute(() -> {
            Bitmap bmp = null;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(filePath);
                byte[] art = mmr.getEmbeddedPicture();
                if (art != null) {
                    Bitmap decoded = BitmapFactory.decodeByteArray(art, 0, art.length);
                    if (decoded != null) {
                        bmp = Bitmap.createScaledBitmap(decoded, 256, 256, true);
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                try { mmr.release(); } catch (Throwable ignored) {}
            }
            if (bmp != null) {
                cache.put(filePath, bmp);
                final Bitmap deliver = bmp;
                target.post(() -> {
                    if (filePath.equals(target.getTag())) {
                        target.setImageBitmap(deliver);
                    }
                });
            }
        });
    }
}


