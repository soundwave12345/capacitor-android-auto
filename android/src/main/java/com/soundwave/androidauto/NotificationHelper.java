package com.soundwave.androidauto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "music_playback";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback controls");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "📢 Canale notifiche creato");
            }
        }
    }

    public Notification buildNotification(MediaSessionCompat.Token sessionToken, 
                                        String title, String artist, String album, 
                                        Bitmap artwork, boolean isPlaying) {
        
        androidx.media.app.NotificationCompat.MediaStyle style =
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0, 1, 2);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(album)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setStyle(style)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        if (artwork != null) {
            builder.setLargeIcon(artwork);
        }

        builder.addAction(new NotificationCompat.Action(
                android.R.drawable.ic_media_previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        ));

        if (isPlaying) {
            builder.addAction(new NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
            ));
        } else {
            builder.addAction(new NotificationCompat.Action(
                    android.R.drawable.ic_media_play,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
            ));
        }

        builder.addAction(new NotificationCompat.Action(
                android.R.drawable.ic_media_next,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        ));

        return builder.build();
    }

    public void notify(Notification notification) {
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
