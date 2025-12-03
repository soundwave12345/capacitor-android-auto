package com.soundwave.androidauto;

import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;

@CapacitorPlugin(name = "AndroidAuto")
public class AndroidAutoPlugin extends Plugin {
    private static final String TAG = "AndroidAutoPlugin";
    private static AndroidAutoPlugin instance;
    
    private AndroidAutoService service;

    @Override
    public void load() {
        super.load();
        instance = this;
        Log.d(TAG, "✅ Plugin caricato correttamente");
    }

    public static AndroidAutoPlugin getInstance() {
        return instance;
    }

    @PluginMethod
    public void updatePlayerState(PluginCall call) {
        Log.d(TAG, "📱 updatePlayerState chiamato da JS");
        
        String title = call.getString("title", "Unknown");
        String artist = call.getString("artist", "Unknown");
        String album = call.getString("album", "");
        String artworkUrl = call.getString("artworkUrl", "");
        Boolean isPlaying = call.getBoolean("isPlaying", false);
        Integer duration = call.getInt("duration", 0);
        Integer position = call.getInt("position", 0);

        Log.d(TAG, "🎵 Title: " + title);
        Log.d(TAG, "👤 Artist: " + artist);
        Log.d(TAG, "▶️ Playing: " + isPlaying);
        Log.d(TAG, "🖼️ Artwork: " + artworkUrl);

        if (service != null) {
            service.updatePlayerState(title, artist, album, artworkUrl, isPlaying, duration, position);
            Log.d(TAG, "✅ Stato aggiornato nel servizio");
        } else {
            Log.w(TAG, "⚠️ Servizio non ancora inizializzato");
        }

        call.resolve();
    }

    @PluginMethod
    public void startService(PluginCall call) {
        Log.d(TAG, "🚀 Avvio servizio Android Auto");
        
        try {
            Intent intent = new Intent(getContext(), AndroidAutoService.class);
            getContext().startService(intent);
            Log.d(TAG, "✅ Servizio avviato");
            call.resolve();
        } catch (Exception e) {
            Log.e(TAG, "❌ Errore avvio servizio: " + e.getMessage());
            call.reject("Errore avvio servizio", e);
        }
    }

    @PluginMethod
    public void stopService(PluginCall call) {
        Log.d(TAG, "🛑 Arresto servizio Android Auto");
        
        try {
            Intent intent = new Intent(getContext(), AndroidAutoService.class);
            getContext().stopService(intent);
            Log.d(TAG, "✅ Servizio fermato");
            call.resolve();
        } catch (Exception e) {
            Log.e(TAG, "❌ Errore arresto servizio: " + e.getMessage());
            call.reject("Errore arresto servizio", e);
        }
    }

    public void setService(AndroidAutoService service) {
        this.service = service;
        Log.d(TAG, "🔗 Servizio collegato al plugin");
    }

    public void notifyButtonPressed(String button) {
        Log.d(TAG, "🔘 Button premuto: " + button);
        
        JSObject ret = new JSObject();
        ret.put("button", button);
        ret.put("timestamp", System.currentTimeMillis());
        
        notifyListeners("buttonPressed", ret);
        Log.d(TAG, "📤 Evento inviato a JS");
    }
}
