package com.example.music;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

public class Notifier {
    private Activity activity;
    public Notifier(Activity activityRef){ activity=activityRef; }

    private  String ACTION_PREVIOUS = "com.example.music.action.PREVIOUS";
    private  String ACTION_PLAY_PAUSE = "com.example.music.action.PLAY_PAUSE";
    private  String ACTION_NEXT = "com.example.music.action.NEXT";

    public void notification(PlayManager playManager) {
        if(playManager.getIsChosen()==false) return;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;


        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        metadataRetriever.setDataSource(activity, playManager.getFilesUri().get(playManager.getNowPlay()));
        String tittle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);


        byte[] cover = metadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if (cover != null) {
            bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            ImageView im = new ImageView(activity);
            im.setImageBitmap(bitmap);
        }

        Intent intent1 = new Intent(activity, NotificationHandler.class);
        intent1.setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent=  PendingIntent.getBroadcast(activity, 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        Intent intent2 = new Intent(activity, NotificationHandler.class);
        intent2.setAction(ACTION_PLAY_PAUSE);
        PendingIntent pausePendingIntent=  PendingIntent.getBroadcast(activity, 0, intent2, PendingIntent.FLAG_IMMUTABLE);

        Intent intent3 = new Intent(activity, NotificationHandler.class);
        intent3.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent=  PendingIntent.getBroadcast(activity, 0, intent3, PendingIntent.FLAG_IMMUTABLE);

        Bitmap dmp= BitmapFactory.decodeResource(activity.getResources(),R.drawable.default_music_cover);

        int play_pause_icon;
        if(playManager.getIsPlaying()==true) play_pause_icon=R.mipmap.pause;
        else play_pause_icon=R.mipmap.play;
        Notification notification = new NotificationCompat.Builder(activity, "not_chan_1")
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.default_music_cover)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(activity,   PlaybackStateCompat.ACTION_STOP)))
                .addAction(R.mipmap.previous, "Previous", prevPendingIntent)
                .addAction(play_pause_icon, "Pause", pausePendingIntent)
                .addAction(R.mipmap.next, "Next", nextPendingIntent)
                .setContentTitle(tittle)
                .setContentText(artist)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(activity, PlaybackStateCompat.ACTION_STOP))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);

        notificationManager.notify(1,notification);

    }
}
