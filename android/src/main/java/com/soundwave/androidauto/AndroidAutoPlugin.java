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

    @PluginMethod
    public void setMediaLibrary(PluginCall call) {
        Log.d(TAG, "📚 setMediaLibrary chiamato da JS");
        
        try {
            String libraryJson = call.getData().toString();
            Log.d(TAG, "📦 Dati libreria ricevuti");
            
            if (service != null) {
                service.setMediaLibrary(libraryJson);
                Log.d(TAG, "✅ Libreria aggiornata nel servizio");
                call.resolve();
            } else {
                Log.w(TAG, "⚠️ Servizio non ancora inizializzato");
                call.reject("Servizio non disponibile");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Errore impostazione libreria: " + e.getMessage());
            call.reject("Errore impostazione libreria", e);
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

    public void notifyMediaItemSelected(String mediaId) {
        Log.d(TAG, "🎵 Media item selezionato: " + mediaId);
        
        JSObject ret = new JSObject();
        ret.put("mediaId", mediaId);
        ret.put("timestamp", System.currentTimeMillis());
        
        notifyListeners("mediaItemSelected", ret);
        Log.d(TAG, "📤 Selezione inviata a JS");
    }

    public void notifySearchRequest(String query) {
        Log.d(TAG, "🔍 Richiesta ricerca: " + query);
        
        JSObject ret = new JSObject();
        ret.put("query", query);
        ret.put("timestamp", System.currentTimeMillis());
        
        notifyListeners("searchRequest", ret);
        Log.d(TAG, "📤 Ricerca inviata a JS");
    }
}
