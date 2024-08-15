package com.example.music;

import android.net.Uri;

import java.util.Vector;

public class ChosenPlaylist {

    private ChosenPlaylist() {
    }

    private static ChosenPlaylist instance;

    public static ChosenPlaylist getInstance() {
        if (instance == null) {
            instance = new ChosenPlaylist();
        }
        return instance;
    }

    private Vector<Uri> selectedPlaylist;
    private int selectedSong;
    public Vector<Uri> getSelectedPlaylist(){return selectedPlaylist;}
    public void setSelectedPlaylist(Vector<Uri> newSelectedPlaylist){selectedPlaylist=newSelectedPlaylist;}
    public int getSelectedSong(){return selectedSong;}
    public void setSelectedSong(int newSelectedSong){selectedSong=newSelectedSong;}
}
