package com.example.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationHandler extends BroadcastReceiver {
    private  String ACTION_PREVIOUS = "com.example.music.action.PREVIOUS";
    private  String ACTION_PLAY_PAUSE = "com.example.music.action.PLAY_PAUSE";
    private  String ACTION_NEXT = "com.example.music.action.NEXT";

    private PlayManager playManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        playManager=PlayManager.getInstance();

        if (ACTION_PREVIOUS.equals(action)) {
            playManager.playPreviousSong();
        } else if (ACTION_PLAY_PAUSE.equals(action)) {
            playManager.playOrPauseSong();
        } else if (ACTION_NEXT.equals(action)) {
            playManager.playNextSong();
        }
    }
}
