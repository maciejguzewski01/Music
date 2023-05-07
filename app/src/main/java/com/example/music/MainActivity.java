package com.example.music;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int nowPlay=0; //number of song now to be played
    private int songFragment=0; //it is used to determinate from where song should start after pause

    private MediaPlayer mediaPlayer;
    boolean isPlaying=false;
    boolean isChosen=false;
    boolean newSong=false;

    private Button buttonSettings;  //in first phase it will be used only to determinate folder with music
    private Button buttonPlaylists; //it will be used in second phase
    private ImageButton buttonPlay;  //play- pause button
    private ImageButton buttonNext; //next song button
    private ImageButton buttonPrevious; //previous song button

    private TextView textTitle;  //title of song
    private TextView textArtist; //name of artist
    private TextView textAlbum; //name of album

    private ImageView imageCover;  //cover of album


    private Uri filesUri[];  //table with Uris of music files




    //in second phase here user will be choosing folder with music, for now path is explicit
    private void choose_folder()
    {

        //"/storage/emulated/0/Music/Muzyka"
        //"/storage/emulated/0/Download"
        File folder = new File("/storage/emulated/0/Download"); //path to music (now explicit, later to be chosen)
        File[] files = folder.listFiles();


       //Next fragment of code iterates over files in chosen folder and add their Uris to table filesUri (to be later used to play music)
        if (files != null) {

            filesUri= new Uri[files.length];
            int i=0;

            for (File file : files) {
                if (file.isFile()) {

                    String fileName = file.getName();
                   // Uri fileUri = Uri.fromFile(file);
                    Uri fileUri = Uri.parse(file.getAbsolutePath());

                    filesUri[i]= fileUri;
                    i++;




                }
            }
            isChosen=true;
        } else {

            System.out.println("ERROR");
        }

    }


    //extracts meta data from music file (name of song, artist, album and album cover) and displays them
    private void info(int num)
    {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        metadataRetriever.setDataSource(this, filesUri[num]);

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



    private void play(int num)
    {

        if(isPlaying==false) //music is not playing
        {
            if((mediaPlayer==null)||(newSong==true))
            {
                mediaPlayer = MediaPlayer.create(MainActivity.this, filesUri[num]);
                newSong=false;
                songFragment=0;
            }


            mediaPlayer.seekTo(songFragment);
            mediaPlayer.start();
            isPlaying=true;
            buttonPlay.setImageResource(R.drawable.pause_button);


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {


                    if(nowPlay+1< filesUri.length) nowPlay++;
                    else nowPlay=0;
                    newSong=true;
                    play(nowPlay);
                }
            });

            info(num);

        }
        else //music is playing
        {
            mediaPlayer.pause();
            isPlaying=false;
            buttonPlay.setImageResource(R.drawable.play_button);
            songFragment = mediaPlayer.getCurrentPosition();
        }


    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buttonSettings= findViewById(R.id.settings_button);
        buttonPlaylists = findViewById(R.id.playlists_button);
        buttonPlay= findViewById(R.id.play_button);
        buttonNext= findViewById(R.id.next_button);
        buttonPrevious= findViewById(R.id.previous_button);

        textTitle= findViewById(R.id.song_title);
        textArtist= findViewById(R.id.artist_name);
        textAlbum= findViewById(R.id.album_name);

        imageCover= findViewById(R.id.cover);
        isPlaying=false;





        if(isChosen==false)
        {
            Context context = getApplicationContext();
            CharSequence text = "Wybierz Folder!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }

        //settings- for now it does nothing except choosing the (explicit coded) folder
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 choose_folder();
            }
        });


//play/pause button
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isChosen==false) return;

                play(nowPlay);

            }
        });


        //next song
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isChosen==false) return;

                if(nowPlay+1< filesUri.length) nowPlay++;
                else nowPlay=0;

                mediaPlayer.pause();
                isPlaying=false;
                buttonPlay.setImageResource(R.drawable.play_button);
             info(nowPlay);
                newSong=true;

            }
        });


        //previous song
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isChosen==false) return;

                if(nowPlay-1>=0) nowPlay--;
                else nowPlay= filesUri.length-1;

                mediaPlayer.pause();
                isPlaying=false;
                buttonPlay.setImageResource(R.drawable.play_button);
                info(nowPlay);
                newSong=true;
            }
        });



    }


}


