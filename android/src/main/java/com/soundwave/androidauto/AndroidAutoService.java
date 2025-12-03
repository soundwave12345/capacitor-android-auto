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

import java.util.ArrayList;
import java.util.List;

public class AndroidAutoService extends MediaBrowserServiceCompat {
    private static final String TAG = "AndroidAutoService";
    private static final String MEDIA_ROOT_ID = "root";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root";
    private static final String NOTIFICATION_CHANNEL_ID = "music_playback";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    
    private String currentTitle = "No Track";
    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "";
    private String currentArtworkUrl = "";
    private boolean isPlaying = false;
    private int duration = 0;
    private int position = 0;

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
                PlaybackStateCompat.ACTION_STOP
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
        
        // Accetta tutte le connessioni (Android Auto, app principale, ecc.)
        // In produzione, potresti voler validare il clientPackageName
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "📂 onLoadChildren chiamato per: " + parentId);
        
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Qui potresti caricare la tua libreria musicale
            // Per ora ritorniamo una lista vuota
            // L'app principale gestirà la riproduzione
            Log.d(TAG, "📋 Ritorno lista vuota (gestita dall'app)");
        }
        
        result.sendResult(mediaItems);
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
        
        // Se hai un'immagine di copertina, aggiungila qui
        // metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        
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

        // Aggiungi azioni
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

    // Callback per gestire i controlli multimediali
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
            // Gestisci riproduzione da ID specifico
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
}
