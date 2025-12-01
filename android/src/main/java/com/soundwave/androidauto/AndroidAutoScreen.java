package com.soundwave.androidauto;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Template;
import androidx.car.app.model.MessageTemplate;
import androidx.core.graphics.drawable.IconCompat;

public class AndroidAutoScreen extends Screen {
    private static final String TAG = "AndroidAutoScreen";
    private final AndroidAutoService service;

    public AndroidAutoScreen(@NonNull CarContext carContext, AndroidAutoService service) {
        super(carContext);
        this.service = service;
        Log.d(TAG, "🖼️ Schermo creato");
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Log.d(TAG, "📐 Costruzione template UI");
        
        String title = service.getCurrentTitle();
        String artist = service.getCurrentArtist();
        boolean isPlaying = service.isPlaying();
        
        Log.d(TAG, "🎵 Visualizzazione: " + title + " - " + artist);
        Log.d(TAG, "▶️ Stato: " + (isPlaying ? "Playing" : "Paused"));

        // Costruisci il messaggio da visualizzare
        String message = "🎵 " + title + "\n" +
                        "👤 " + artist + "\n\n" +
                        (isPlaying ? "▶️ In riproduzione" : "⏸️ In pausa");

        // Crea i bottoni nella action strip
        ActionStrip actionStrip = new ActionStrip.Builder()
            .addAction(
                new Action.Builder()
                    .setTitle("⏮️ Prev")
                    .setOnClickListener(() -> {
                        Log.d(TAG, "⏮️ Previous premuto");
                        service.onButtonPressed("previous");
                    })
                    .build()
            )
            .addAction(
                new Action.Builder()
                    .setTitle(isPlaying ? "⏸️ Pause" : "▶️ Play")
                    .setOnClickListener(() -> {
                        String button = isPlaying ? "pause" : "play";
                        Log.d(TAG, (isPlaying ? "⏸️" : "▶️") + " " + button + " premuto");
                        service.onButtonPressed(button);
                    })
                    .build()
            )
            .addAction(
                new Action.Builder()
                    .setTitle("⏭️ Next")
                    .setOnClickListener(() -> {
                        Log.d(TAG, "⏭️ Next premuto");
                        service.onButtonPressed("next");
                    })
                    .build()
            )
            .addAction(
                new Action.Builder()
                    .setTitle("⏹️ Stop")
                    .setOnClickListener(() -> {
                        Log.d(TAG, "⏹️ Stop premuto");
                        service.onButtonPressed("stop");
                    })
                    .build()
            )
            .build();

        // Crea template con messaggio e bottoni
        return new MessageTemplate.Builder(message)
            .setTitle("Music Player")
            .setHeaderAction(Action.APP_ICON)
            .addAction(
                new Action.Builder()
                    .setTitle("🔄 Refresh")
                    .setOnClickListener(() -> {
                        Log.d(TAG, "🔄 Refresh richiesto");
                        invalidate();
                    })
                    .build()
            )
            .setActionStrip(actionStrip)
            .build();
    }

    public void updateUI() {
        Log.d(TAG, "🔄 Refresh UI richiesto");
        invalidate();
    }
}
