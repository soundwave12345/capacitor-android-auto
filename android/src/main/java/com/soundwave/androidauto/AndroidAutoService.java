package com.soundwave.androidauto;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public class AndroidAutoService extends CarAppService {
    private static final String TAG = "AndroidAutoService";
    
    private String currentTitle = "No Track";
    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "";
    private boolean isPlaying = false;
    private int duration = 0;
    private int position = 0;
    
    private AndroidAutoScreen currentScreen;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 Servizio Android Auto creato");
        
        // Collega al plugin
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().setService(this);
            Log.d(TAG, "✅ Collegato al plugin");
        }
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        Log.d(TAG, "📱 Nuova sessione Android Auto");
        
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                Log.d(TAG, "🖼️ Creazione schermo Android Auto");
                currentScreen = new AndroidAutoScreen(getCarContext(), AndroidAutoService.this);
                return currentScreen;
            }
        };
    }

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // Accetta tutte le connessioni (per debug)
        // In produzione, usa HostValidator.ALLOW_ALL_HOSTS_VALIDATOR solo per test
        Log.d(TAG, "🔐 Configurazione host validator");
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    public void updatePlayerState(String title, String artist, String album, 
                                   boolean playing, int dur, int pos) {
        Log.d(TAG, "🔄 Aggiornamento stato player:");
        Log.d(TAG, "   Title: " + title);
        Log.d(TAG, "   Artist: " + artist);
        Log.d(TAG, "   Playing: " + playing);
        
        this.currentTitle = title;
        this.currentArtist = artist;
        this.currentAlbum = album;
        this.isPlaying = playing;
        this.duration = dur;
        this.position = pos;
        
        if (currentScreen != null) {
            currentScreen.updateUI();
            Log.d(TAG, "✅ UI aggiornata");
        }
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void onButtonPressed(String button) {
        Log.d(TAG, "🎯 Button premuto nel servizio: " + button);
        
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().notifyButtonPressed(button);
            Log.d(TAG, "📤 Evento inoltrato al plugin");
        } else {
            Log.w(TAG, "⚠️ Plugin non disponibile");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "💀 Servizio Android Auto distrutto");
        super.onDestroy();
    }
}
