package com.example.music;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class PlayManager {

    private Mode mode;
    public void setMode(Mode newMode) {mode=newMode;}
    public Mode getMode() {return mode;}

    private int nowPlay=0;
    public int getNowPlay() {return nowPlay;}
    public void setNowPlay(int newNowPlay) {nowPlay=newNowPlay;}

    private int nowPlayIdx=0;
    public int getNowPlayIdx() {return nowPlayIdx;}
    public void setNowPlayIdx(int newNowPlayIdx) {nowPlayIdx=newNowPlayIdx;}

    private int songFragment=0;
    public int getSongFragment(){return songFragment;}
    public void setSongFragment(int newSongFragment) {songFragment=newSongFragment;}

    private MediaPlayer mediaPlayer;
    public MediaPlayer getMediaPlayer(){return mediaPlayer;}

    private boolean isPlaying = false;
    public boolean getIsPlaying(){return isPlaying;}
    public void setIsPlaying(boolean newIsPlaying) {isPlaying=newIsPlaying;}

    private boolean isChosen = false;
    public boolean getIsChosen(){return isChosen;}
    public void setIsChosen(boolean newIsChosen) {isChosen=newIsChosen;}

    boolean newSong = false;

    private List<Uri> chosen_playlist= new ArrayList<>();
    private int chosen_playlist_number=0;

    private Vector<Integer> randomVector;
    private int nowPlayRandom = 0;

    private Handler handler;
    private Runnable runnable;

    private Vector<Uri> filesUri = new Vector<Uri>();
    public Vector<Uri> getFilesUri(){return filesUri;}
    public void setFilesUri(Vector<Uri> newFilesUri){filesUri=newFilesUri;}

    private Activity activity;

    private SeekBar seekBar;
    private ImageButton buttonPlay;
    private TextView textTitle;
    private TextView textArtist;
    private TextView textAlbum;
    private ImageView imageCover;

    public void intro(SeekBar seekBarRef,ImageButton buttonPlayRef,TextView textTitleRef,TextView textArtistRef,TextView textAlbumRef,ImageView imageCoverRef)
    {
        seekBar=seekBarRef;
        buttonPlay=buttonPlayRef;
        textTitle=textTitleRef;
        textArtist=textArtistRef;
        textAlbum=textAlbumRef;
        imageCover=imageCoverRef;
    }

    PlayManager(Activity act) {activity=act;}

    public void info(int num) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(activity, filesUri.get(num));

        String songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

        byte[] cover = metadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if (cover != null) {
            bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        }

        textTitle.setText(songTitle);
        textArtist.setText(artistName);
        textAlbum.setText(albumName);
        imageCover.setImageBitmap(bitmap);
    }



    public void generate_random_order()
    {
        randomVector= new Vector<>();

        int size=filesUri.size();
        for(int i=0;i<size;++i)
        {
            randomVector.add(i);
        }
        Collections.shuffle(randomVector);
    }


    private void play_from_uri(Uri uri)
    {
        if (isPlaying == false)
        {
            if ((mediaPlayer == null) || (newSong == true)) {
                mediaPlayer = MediaPlayer.create(activity, uri);
                newSong = false;
                songFragment = 0;
            }


            //handler is used to update position of seekBar
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isPlaying == false) return;
                    songFragment = mediaPlayer.getCurrentPosition();
                    double fragment_d = (100.0 * songFragment) / mediaPlayer.getDuration();
                    int fragment = ((int) fragment_d);
                    seekBar.setProgress(fragment);
                    handler.postDelayed(this, 1000);

                }
            };
            handler.postDelayed(runnable, 200);

            mediaPlayer.seekTo(songFragment);
            mediaPlayer.start();
            isPlaying = true;
            buttonPlay.setImageResource(R.drawable.pause_button);


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {




                    if(chosen_playlist_number+1<chosen_playlist.size()) chosen_playlist_number++;
                    else chosen_playlist_number = 0;
                    newSong = true;
                    play_from_uri(chosen_playlist.get(chosen_playlist_number));



                }
            });

            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

            metadataRetriever.setDataSource(activity, uri);

            String songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            byte[] cover = metadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = null;
            if (cover != null) {
                bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            }

            textTitle.setText(songTitle);
            textArtist.setText(artistName);
            textAlbum.setText(albumName);
            imageCover.setImageBitmap(bitmap);


        } else //music is playing
        {
            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            songFragment = mediaPlayer.getCurrentPosition();
        }
    }


    private void play(int num) {

        if (isPlaying == false) //music is not playing
        {
            if ((mediaPlayer == null) || (newSong == true)) {
                mediaPlayer = MediaPlayer.create(activity, filesUri.get(num));
                newSong = false;
                songFragment = 0;
            }


            //handler is used to update position of seekBar
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isPlaying == false) return;
                    songFragment = mediaPlayer.getCurrentPosition();
                    double fragment_d = (100.0 * songFragment) / mediaPlayer.getDuration();
                    int fragment = ((int) fragment_d);
                    seekBar.setProgress(fragment);
                    handler.postDelayed(this, 1000);

                }
            };
            handler.postDelayed(runnable, 200);

            mediaPlayer.seekTo(songFragment);
            mediaPlayer.start();
            isPlaying = true;
            buttonPlay.setImageResource(R.drawable.pause_button);


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {


                    if (mode == Mode.ORDER) {
                        if (nowPlay + 1 < filesUri.size()) nowPlay++;
                        else nowPlay = 0;
                        newSong = true;
                        play(nowPlay);
                    } else if(mode==Mode.RANDOM){
                        if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
                        else nowPlayRandom = 0;
                        newSong = true;
                        play(randomVector.elementAt(nowPlayRandom));
                    }
                    else
                    {

                        play_from_uri(chosen_playlist.get(chosen_playlist_number));
                    }
                }
            });

            info(num);


        } else
        {
            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            songFragment = mediaPlayer.getCurrentPosition();
        }

    }

    public void previous_song()
    {
        if (isChosen == false) return;

        if (mode == Mode.ORDER) {
            if (nowPlay - 1 >= 0) nowPlay--;
            else nowPlay = filesUri.size() - 1;

            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            info(nowPlay);
            newSong = true;
            play(nowPlay);
        } else if(mode == Mode.RANDOM){
            if (nowPlayRandom - 1 >= 0) nowPlayRandom--;
            else generate_random_order();

            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            info(randomVector.elementAt(nowPlayRandom));
            newSong = true;
            play(randomVector.elementAt(nowPlayRandom));
        }
        else
        {
            if(chosen_playlist_number-1>0) chosen_playlist_number--;
            else chosen_playlist_number = chosen_playlist.size()-1;
            newSong = true;
            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            play_from_uri(chosen_playlist.get(chosen_playlist_number));
        }

    }

    public void next_song()
    {
        if (isChosen == false) return;

        if (mode == Mode.ORDER) {
            if (nowPlay + 1 < filesUri.size()) nowPlay++;
            else nowPlay = 0;

            if (isPlaying == true) mediaPlayer.pause();

            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            info(nowPlay);
            newSong = true;
            play(nowPlay);
        } else if(mode== Mode.RANDOM){

            if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
            else generate_random_order();

            if (isPlaying == true) mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            info(randomVector.elementAt(nowPlayRandom));
            newSong = true;
            play(randomVector.elementAt(nowPlayRandom));
        }
        else
        {
            if(chosen_playlist_number+1<chosen_playlist.size()) chosen_playlist_number++;
            else chosen_playlist_number = 0;
            newSong = true;
            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            play_from_uri(chosen_playlist.get(chosen_playlist_number));
        }

    }


    public void play_pause_song() {
        if (isChosen == false) return;

        if (mode == Mode.ORDER) play(nowPlay);
        else if (mode == Mode.RANDOM) play(randomVector.elementAt(nowPlayRandom));
        else {

            if (chosen_playlist.isEmpty() == true) {
                Toast toast = Toast.makeText(activity, "Chose a playlist!", Toast.LENGTH_LONG);
                toast.show();
            } else play_from_uri(chosen_playlist.get(chosen_playlist_number));
        }
    }



}
