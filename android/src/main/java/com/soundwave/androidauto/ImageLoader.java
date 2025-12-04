package com.soundwave.androidauto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface ImageLoadCallback {
        void onImageLoaded(Bitmap bitmap);
    }

    public ImageLoader() {
        this.executorService = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadImage(String url, ImageLoadCallback callback) {
        if (url == null || url.isEmpty()) {
            return;
        }

        Log.d(TAG, "🖼️ Caricamento immagine da: " + url);
        executorService.execute(() -> {
            try {
                Bitmap bitmap = downloadBitmap(url);
                if (bitmap != null) {
                    bitmap = scaleBitmap(bitmap, 512, 512);
                    final Bitmap finalBitmap = bitmap;
                    mainHandler.post(() -> callback.onImageLoaded(finalBitmap));
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Errore caricamento immagine", e);
            }
        });
    }

    private Bitmap downloadBitmap(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(TAG, "Errore download bitmap", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
