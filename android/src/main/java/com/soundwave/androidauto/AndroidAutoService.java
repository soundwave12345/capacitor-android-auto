package com.soundwave.androidauto;

import android.app.Notification;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.ArrayList;
import java.util.List;

public class AndroidAutoService extends MediaBrowserServiceCompat {
    private static final String TAG = "AndroidAutoService";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    // Helpers
    private MediaLibraryManager libraryManager;
    private ImageLoader imageLoader;
    private NotificationHelper notificationHelper;

    // Stato player corrente
    private String currentTitle = "No Track";
    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "";
    private String currentArtworkUrl = "";
    private Bitmap currentArtwork = null;
    private boolean isPlaying = false;
    private int duration = 0;
    private int position = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 MediaBrowserService creato");

        // Inizializza helpers
        libraryManager = new MediaLibraryManager();
        imageLoader = new ImageLoader();
        notificationHelper = new NotificationHelper(this);

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
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                );
        mediaSession.setPlaybackState(stateBuilder.build());

        // Imposta callback per i controlli
        mediaSession.setCallback(new MediaSessionCallback());

        // Imposta il session token
        setSessionToken(mediaSession.getSessionToken());

        // Attiva la sessione
        mediaSession.setActive(true);

        // Collega al plugin
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().setService(this);
            Log.d(TAG, "✅ Collegato al plugin");
        }

        Log.d(TAG, "✅ MediaSession inizializzata");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(TAG, "📱 onGetRoot chiamato da: " + clientPackageName);
        
        // Bundle per dichiarare le capacità del servizio
        Bundle extras = new Bundle();
        extras.putBoolean("android.media.browse.SEARCH_SUPPORTED", true);
        extras.putBoolean("android.media.browse.CONTENT_STYLE_SUPPORTED", true);
        
        return new BrowserRoot(MediaLibraryManager.MEDIA_ROOT_ID, extras);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "📂 onLoadChildren chiamato per: " + parentId);

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        switch (parentId) {
            case MediaLibraryManager.MEDIA_ROOT_ID:
                mediaItems.addAll(libraryManager.getRootItems());
                break;
            case MediaLibraryManager.MEDIA_RECENT_ID:
                mediaItems.addAll(libraryManager.getRecentTracks());
                break;
            case MediaLibraryManager.MEDIA_PLAYLISTS_ID:
                mediaItems.addAll(libraryManager.getPlaylists());
                break;
            case MediaLibraryManager.MEDIA_ALBUMS_ID:
                mediaItems.addAll(libraryManager.getAlbums());
                break;
            case MediaLibraryManager.MEDIA_ARTISTS_ID:
                mediaItems.addAll(libraryManager.getArtists());
                break;
            default:
                if (parentId.startsWith("playlist_")) {
                    List<MediaBrowserCompat.MediaItem> items = libraryManager.getPlaylistItems(parentId.substring(9));
                    if (items != null) mediaItems.addAll(items);
                } else if (parentId.startsWith("album_")) {
                    List<MediaBrowserCompat.MediaItem> items = libraryManager.getAlbumItems(parentId.substring(6));
                    if (items != null) mediaItems.addAll(items);
                } else if (parentId.startsWith("artist_")) {
                    List<MediaBrowserCompat.MediaItem> items = libraryManager.getArtistItems(parentId.substring(7));
                    if (items != null) mediaItems.addAll(items);
                }
                break;
        }

        result.sendResult(mediaItems);
    }

    @Override
    public void onSearch(@NonNull String query, Bundle extras, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "🔍🔍🔍 onSearch CHIAMATO!");
        Log.d(TAG, "🔍 Query: " + query);
        Log.d(TAG, "🔍 Extras: " + (extras != null ? extras.toString() : "null"));
        
        List<MediaBrowserCompat.MediaItem> searchResults = libraryManager.search(query);
        Log.d(TAG, "🔍 Trovati " + searchResults.size() + " risultati");
        
        result.sendResult(searchResults);
    }

    public void setMediaLibrary(String jsonLibrary) {
        Log.d(TAG, "📚 Impostazione libreria musicale");
        libraryManager.parseLibrary(jsonLibrary);
        
        // Notifica cambiamenti
        notifyChildrenChanged(MediaLibraryManager.MEDIA_ROOT_ID);
        notifyChildrenChanged(MediaLibraryManager.MEDIA_RECENT_ID);
        notifyChildrenChanged(MediaLibraryManager.MEDIA_PLAYLISTS_ID);
        notifyChildrenChanged(MediaLibraryManager.MEDIA_ALBUMS_ID);
        notifyChildrenChanged(MediaLibraryManager.MEDIA_ARTISTS_ID);
        
        Log.d(TAG, "🎉 Libreria musicale aggiornata");
    }

    public void updatePlayerState(String title, String artist, String album,
                                  String artworkUrl, boolean playing, int dur, int pos) {
        this.currentTitle = title;
        this.currentArtist = artist;
        this.currentAlbum = album;
        this.isPlaying = playing;
        this.duration = dur;
        this.position = pos;

        // Carica artwork se l'URL è cambiato
        if (!artworkUrl.equals(this.currentArtworkUrl)) {
            this.currentArtworkUrl = artworkUrl;
            if (!artworkUrl.isEmpty()) {
                imageLoader.loadImage(artworkUrl, bitmap -> {
                    this.currentArtwork = bitmap;
                    updateMetadata();
                    updateNotification();
                });
            } else {
                this.currentArtwork = null;
            }
        }

        updateMetadata();
        updatePlaybackState();
        updateNotification();
    }

    private void updateMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentAlbum)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);

        if (currentArtwork != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentArtwork);
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, currentArtwork);
        }

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updatePlaybackState() {
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        PlaybackStateCompat playbackState = stateBuilder
                .setState(state, position, 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    private void updateNotification() {
        Notification notification = notificationHelper.buildNotification(
                mediaSession.getSessionToken(),
                currentTitle,
                currentArtist,
                currentAlbum,
                currentArtwork,
                isPlaying
        );

        if (isPlaying) {
            startForeground(notificationHelper.getNotificationId(), notification);
        } else {
            stopForeground(false);
            notificationHelper.notify(notification);
        }
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
        public void onPlay() { notifyButtonPressed("play"); }

        @Override
        public void onPause() { notifyButtonPressed("pause"); }

        @Override
        public void onSkipToNext() { notifyButtonPressed("next"); }

        @Override
        public void onSkipToPrevious() { notifyButtonPressed("previous"); }

        @Override
        public void onStop() { notifyButtonPressed("stop"); }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "🎵 onPlayFromMediaId: " + mediaId);
            if (AndroidAutoPlugin.getInstance() != null) {
                AndroidAutoPlugin.getInstance().notifyMediaItemSelected(mediaId);
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            Log.d(TAG, "🔍🔍🔍 onPlayFromSearch CHIAMATO!");
            Log.d(TAG, "🔍 Query: " + query);
            Log.d(TAG, "🔍 Extras: " + (extras != null ? extras.toString() : "null"));
            
            if (query == null || query.isEmpty()) {
                Log.d(TAG, "🔍 Query vuota, avvio riproduzione generica");
                notifyButtonPressed("play");
                return;
            }
            
            Log.d(TAG, "🔍 Invio richiesta ricerca al plugin");
            if (AndroidAutoPlugin.getInstance() != null) {
                AndroidAutoPlugin.getInstance().notifySearchRequest(query);
            }
        }
        
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            // Opzionale
        }
    }

    private void notifyButtonPressed(String button) {
        Log.d(TAG, "🎯 Button premuto: " + button);
        if (AndroidAutoPlugin.getInstance() != null) {
            AndroidAutoPlugin.getInstance().notifyButtonPressed(button);
        }
    }
}
