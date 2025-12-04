package com.soundwave.androidauto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidAutoService extends MediaBrowserServiceCompat {
    private static final String TAG = "AndroidAutoService";
    
    // Media IDs
    private static final String MEDIA_ROOT_ID = "root";
    private static final String MEDIA_RECENT_ID = "recent";
    private static final String MEDIA_PLAYLISTS_ID = "playlists";
    private static final String MEDIA_ALBUMS_ID = "albums";
    private static final String MEDIA_ARTISTS_ID = "artists";
    
    private static final String NOTIFICATION_CHANNEL_ID = "music_playback";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    
    // Stato player corrente
    private String currentTitle = "No Track";
    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "";
    private String currentArtworkUrl = "";
    private boolean isPlaying = false;
    private int duration = 0;
    private int position = 0;

    // Libreria musicale
    private List<MediaBrowserCompat.MediaItem> recentTracks = new ArrayList<>();
    private Map<String, List<MediaBrowserCompat.MediaItem>> playlists = new HashMap<>();
    private Map<String, List<MediaBrowserCompat.MediaItem>> albums = new HashMap<>();
    private Map<String, List<MediaBrowserCompat.MediaItem>> artists = new HashMap<>();
    
    // Categorie root
    private List<MediaBrowserCompat.MediaItem> rootCategories = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 MediaBrowserService creato");
        
        // Crea MediaSession
        mediaSession = new MediaSessionCompat(this, TAG);
        
        // Abilita callbacks dal MediaController
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        
        // Imposta lo stato iniziale
        stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_STOP |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
            );
        mediaSession.setPlaybackState(stateBuilder.build());
        
        // Imposta callback per i controlli
        mediaSession.setCallback(new MediaSessionCallback());
        
        // Imposta il session token
        setSessionToken(mediaSession.getSessionToken());
        
        // Attiva la sessione
        mediaSession.setActive(true);
        
        // Crea canale notifiche
        createNotificationChannel();
        
        // Inizializza categorie root di default
        initializeDefaultCategories();
        
        // Collega al plugin
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().setService(this);
            Log.d(TAG, "✅ Collegato al plugin");
        }
        
        Log.d(TAG, "✅ MediaSession inizializzata");
    }

    private void initializeDefaultCategories() {
        rootCategories.clear();
        
        // Categoria: Recenti
        MediaDescriptionCompat recentDesc = new MediaDescriptionCompat.Builder()
            .setMediaId(MEDIA_RECENT_ID)
            .setTitle("Recenti")
            .setSubtitle("Ultime canzoni ascoltate")
            .build();
        rootCategories.add(new MediaBrowserCompat.MediaItem(recentDesc, 
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        
        // Categoria: Playlist
        MediaDescriptionCompat playlistsDesc = new MediaDescriptionCompat.Builder()
            .setMediaId(MEDIA_PLAYLISTS_ID)
            .setTitle("Playlist")
            .setSubtitle("Le tue playlist")
            .build();
        rootCategories.add(new MediaBrowserCompat.MediaItem(playlistsDesc, 
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        
        // Categoria: Album
        MediaDescriptionCompat albumsDesc = new MediaDescriptionCompat.Builder()
            .setMediaId(MEDIA_ALBUMS_ID)
            .setTitle("Album")
            .setSubtitle("Tutti gli album")
            .build();
        rootCategories.add(new MediaBrowserCompat.MediaItem(albumsDesc, 
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        
        // Categoria: Artisti
        MediaDescriptionCompat artistsDesc = new MediaDescriptionCompat.Builder()
            .setMediaId(MEDIA_ARTISTS_ID)
            .setTitle("Artisti")
            .setSubtitle("Tutti gli artisti")
            .build();
        rootCategories.add(new MediaBrowserCompat.MediaItem(artistsDesc, 
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        
        Log.d(TAG, "📁 Categorie root inizializzate: " + rootCategories.size());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(TAG, "📱 onGetRoot chiamato da: " + clientPackageName);
        
        // Accetta tutte le connessioni (Android Auto, app principale, ecc.)
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "📂 onLoadChildren chiamato per: " + parentId);
        
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        
        switch (parentId) {
            case MEDIA_ROOT_ID:
                // Ritorna le categorie principali
                mediaItems.addAll(rootCategories);
                Log.d(TAG, "📋 Ritorno " + mediaItems.size() + " categorie root");
                break;
                
            case MEDIA_RECENT_ID:
                // Ritorna le canzoni recenti
                mediaItems.addAll(recentTracks);
                Log.d(TAG, "🕐 Ritorno " + mediaItems.size() + " canzoni recenti");
                break;
                
            case MEDIA_PLAYLISTS_ID:
                // Ritorna le playlist come categorie navigabili
                for (Map.Entry<String, List<MediaBrowserCompat.MediaItem>> entry : playlists.entrySet()) {
                    String playlistId = entry.getKey();
                    MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                        .setMediaId("playlist_" + playlistId)
                        .setTitle(playlistId)
                        .setSubtitle(entry.getValue().size() + " brani")
                        .build();
                    mediaItems.add(new MediaBrowserCompat.MediaItem(desc, 
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
                }
                Log.d(TAG, "📝 Ritorno " + mediaItems.size() + " playlist");
                break;
                
            case MEDIA_ALBUMS_ID:
                // Ritorna gli album come categorie navigabili
                for (Map.Entry<String, List<MediaBrowserCompat.MediaItem>> entry : albums.entrySet()) {
                    String albumId = entry.getKey();
                    MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                        .setMediaId("album_" + albumId)
                        .setTitle(albumId)
                        .setSubtitle(entry.getValue().size() + " brani")
                        .build();
                    mediaItems.add(new MediaBrowserCompat.MediaItem(desc, 
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
                }
                Log.d(TAG, "💿 Ritorno " + mediaItems.size() + " album");
                break;
                
            case MEDIA_ARTISTS_ID:
                // Ritorna gli artisti come categorie navigabili
                for (Map.Entry<String, List<MediaBrowserCompat.MediaItem>> entry : artists.entrySet()) {
                    String artistId = entry.getKey();
                    MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                        .setMediaId("artist_" + artistId)
                        .setTitle(artistId)
                        .setSubtitle(entry.getValue().size() + " brani")
                        .build();
                    mediaItems.add(new MediaBrowserCompat.MediaItem(desc, 
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
                }
                Log.d(TAG, "🎤 Ritorno " + mediaItems.size() + " artisti");
                break;
                
            default:
                // Gestisci sottocategorie (playlist/album/artista specifici)
                if (parentId.startsWith("playlist_")) {
                    String playlistName = parentId.substring(9);
                    List<MediaBrowserCompat.MediaItem> items = playlists.get(playlistName);
                    if (items != null) {
                        mediaItems.addAll(items);
                    }
                } else if (parentId.startsWith("album_")) {
                    String albumName = parentId.substring(6);
                    List<MediaBrowserCompat.MediaItem> items = albums.get(albumName);
                    if (items != null) {
                        mediaItems.addAll(items);
                    }
                } else if (parentId.startsWith("artist_")) {
                    String artistName = parentId.substring(7);
                    List<MediaBrowserCompat.MediaItem> items = artists.get(artistName);
                    if (items != null) {
                        mediaItems.addAll(items);
                    }
                }
                Log.d(TAG, "📂 Ritorno " + mediaItems.size() + " elementi per " + parentId);
                break;
        }
        
        result.sendResult(mediaItems);
    }

    public void setMediaLibrary(String jsonLibrary) {
        Log.d(TAG, "📚 Impostazione libreria musicale");
        
        try {
            JSONObject library = new JSONObject(jsonLibrary);
            
            // Carica canzoni recenti
            if (library.has("recentTracks")) {
                JSONArray recentArray = library.getJSONArray("recentTracks");
                recentTracks.clear();
                for (int i = 0; i < recentArray.length(); i++) {
                    JSONObject track = recentArray.getJSONObject(i);
                    recentTracks.add(createMediaItem(track, true));
                }
                Log.d(TAG, "✅ Caricate " + recentTracks.size() + " canzoni recenti");
            }
            
            // Carica playlist
            if (library.has("playlists")) {
                JSONArray playlistsArray = library.getJSONArray("playlists");
                playlists.clear();
                for (int i = 0; i < playlistsArray.length(); i++) {
                    JSONObject playlist = playlistsArray.getJSONObject(i);
                    String playlistId = playlist.getString("id");
                    String playlistTitle = playlist.getString("title");
                    
                    List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
                    if (playlist.has("items")) {
                        JSONArray itemsArray = playlist.getJSONArray("items");
                        for (int j = 0; j < itemsArray.length(); j++) {
                            items.add(createMediaItem(itemsArray.getJSONObject(j), true));
                        }
                    }
                    playlists.put(playlistTitle, items);
                }
                Log.d(TAG, "✅ Caricate " + playlists.size() + " playlist");
            }
            
            // Carica album
            if (library.has("albums")) {
                JSONArray albumsArray = library.getJSONArray("albums");
                albums.clear();
                for (int i = 0; i < albumsArray.length(); i++) {
                    JSONObject album = albumsArray.getJSONObject(i);
                    String albumTitle = album.getString("title");
                    
                    List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
                    if (album.has("items")) {
                        JSONArray itemsArray = album.getJSONArray("items");
                        for (int j = 0; j < itemsArray.length(); j++) {
                            items.add(createMediaItem(itemsArray.getJSONObject(j), true));
                        }
                    }
                    albums.put(albumTitle, items);
                }
                Log.d(TAG, "✅ Caricati " + albums.size() + " album");
            }
            
            // Carica artisti
            if (library.has("artists")) {
                JSONArray artistsArray = library.getJSONArray("artists");
                artists.clear();
                for (int i = 0; i < artistsArray.length(); i++) {
                    JSONObject artist = artistsArray.getJSONObject(i);
                    String artistName = artist.getString("title");
                    
                    List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
                    if (artist.has("items")) {
                        JSONArray itemsArray = artist.getJSONArray("items");
                        for (int j = 0; j < itemsArray.length(); j++) {
                            items.add(createMediaItem(itemsArray.getJSONObject(j), true));
                        }
                    }
                    artists.put(artistName, items);
                }
                Log.d(TAG, "✅ Caricati " + artists.size() + " artisti");
            }
            
            // Notifica che la libreria è cambiata
            notifyChildrenChanged(MEDIA_ROOT_ID);
            
            Log.d(TAG, "🎉 Libreria musicale aggiornata con successo");
            
        } catch (JSONException e) {
            Log.e(TAG, "❌ Errore parsing libreria musicale", e);
        }
    }

    private MediaBrowserCompat.MediaItem createMediaItem(JSONObject json, boolean isPlayable) throws JSONException {
        String id = json.getString("id");
        String title = json.getString("title");
        String artist = json.optString("artist", "");
        String album = json.optString("album", "");
        String artworkUrl = json.optString("artworkUrl", "");
        
        MediaDescriptionCompat.Builder descBuilder = new MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist);
        
        if (!album.isEmpty()) {
            descBuilder.setDescription(album);
        }
        
        // TODO: Caricare artwork da URL se necessario
        // if (!artworkUrl.isEmpty()) {
        //     descBuilder.setIconUri(Uri.parse(artworkUrl));
        // }
        
        int flags = isPlayable ? 
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE : 
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;
        
        return new MediaBrowserCompat.MediaItem(descBuilder.build(), flags);
    }

    public void updatePlayerState(String title, String artist, String album, 
                                   String artworkUrl, boolean playing, int dur, int pos) {
        Log.d(TAG, "🔄 Aggiornamento stato player:");
        Log.d(TAG, "   Title: " + title);
        Log.d(TAG, "   Artist: " + artist);
        Log.d(TAG, "   Playing: " + playing);
        
        this.currentTitle = title;
        this.currentArtist = artist;
        this.currentAlbum = album;
        this.currentArtworkUrl = artworkUrl;
        this.isPlaying = playing;
        this.duration = dur;
        this.position = pos;
        
        // Aggiorna metadata
        updateMetadata();
        
        // Aggiorna playback state
        updatePlaybackState();
        
        // Aggiorna notifica
        updateNotification();
        
        Log.d(TAG, "✅ Stato aggiornato");
    }

    private void updateMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentAlbum)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        
        mediaSession.setMetadata(metadataBuilder.build());
        Log.d(TAG, "📝 Metadata aggiornati");
    }

    private void updatePlaybackState() {
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        
        PlaybackStateCompat playbackState = stateBuilder
            .setState(state, position, 1.0f)
            .build();
        
        mediaSession.setPlaybackState(playbackState);
        Log.d(TAG, "▶️ PlaybackState aggiornato: " + (isPlaying ? "PLAYING" : "PAUSED"));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback controls");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "📢 Canale notifiche creato");
            }
        }
    }

    private void updateNotification() {
        if (isPlaying) {
            startForeground(NOTIFICATION_ID, buildNotification());
            Log.d(TAG, "📢 Notifica aggiornata (foreground)");
        } else {
            stopForeground(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, buildNotification());
                Log.d(TAG, "📢 Notifica aggiornata (background)");
            }
        }
    }

    private Notification buildNotification() {
        androidx.media.app.NotificationCompat.MediaStyle style = 
            new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSubText(currentAlbum)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setStyle(style)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true);

        builder.addAction(new NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            "Previous",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        ));

        if (isPlaying) {
            builder.addAction(new NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)
            ));
        } else {
            builder.addAction(new NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
            ));
        }

        builder.addAction(new NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "Next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        ));

        return builder.build();
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "💀 MediaBrowserService distrutto");
        mediaSession.setActive(false);
        mediaSession.release();
        super.onDestroy();
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Log.d(TAG, "▶️ onPlay chiamato");
            notifyButtonPressed("play");
        }

        @Override
        public void onPause() {
            Log.d(TAG, "⏸️ onPause chiamato");
            notifyButtonPressed("pause");
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "⏭️ onSkipToNext chiamato");
            notifyButtonPressed("next");
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "⏮️ onSkipToPrevious chiamato");
            notifyButtonPressed("previous");
        }

        @Override
        public void onStop() {
            Log.d(TAG, "⏹️ onStop chiamato");
            notifyButtonPressed("stop");
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "🎵 onPlayFromMediaId: " + mediaId);
            notifyMediaItemSelected(mediaId);
        }
    }

    private void notifyButtonPressed(String button) {
        Log.d(TAG, "🎯 Button premuto: " + button);
        
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().notifyButtonPressed(button);
            Log.d(TAG, "📤 Evento inoltrato al plugin");
        } else {
            Log.w(TAG, "⚠️ Plugin non disponibile");
        }
    }

    private void notifyMediaItemSelected(String mediaId) {
        Log.d(TAG, "🎵 Media item selezionato: " + mediaId);
        
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().notifyMediaItemSelected(mediaId);
            Log.d(TAG, "📤 Selezione inoltrata al plugin");
        } else {
            Log.w(TAG, "⚠️ Plugin non disponibile");
        }
    }
}
