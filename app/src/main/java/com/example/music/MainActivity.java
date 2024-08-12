package com.example.music;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    private int songProgress;

    private Button buttonSettings;
    private Button buttonPlaylists;
    private ImageButton buttonPlay;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton buttonPlayType;
    private ImageButton buttonYT;
    private SeekBar seekBar;

    private TextView textTitle;
    private TextView textArtist;
    private TextView textAlbum;
    private ImageView imageCover;

    private Context con = this;

    private NotificationChannel channel;
    private NotificationManager notificationManager1;
    private  String ACTION_PREVIOUS = "com.example.music.action.PREVIOUS";
    private  String ACTION_PLAY_PAUSE = "com.example.music.action.PLAY_PAUSE";
    private  String ACTION_NEXT = "com.example.music.action.NEXT";


    private Permissions permissions= new Permissions();
    private Introduction introduction = new Introduction(this);
    private PlayManager playManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playManager=PlayManager.getInstance();
        playManager.setActivity(this);

        setupComponents();

        permissions.checkPermissionsAndAskIfMissing(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("not_chan_1", "channel", NotificationManager.IMPORTANCE_DEFAULT);
             notificationManager1 = getSystemService(NotificationManager.class);
            notificationManager1.createNotificationChannel(channel);
        }


        if (playManager.getIsChosen() == false) {
            Context context = getApplicationContext();
            CharSequence text = "Chose a folder!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        handleButtons();
    }

    private void setupComponents()
    {
        buttonSettings = findViewById(R.id.settings_button);
        buttonPlaylists = findViewById(R.id.playlists_button);
        buttonPlay = findViewById(R.id.play_button);
        buttonNext = findViewById(R.id.next_button);
        buttonPrevious = findViewById(R.id.previous_button);
        buttonPlayType = findViewById(R.id.play_type);
        buttonYT = findViewById(R.id.yt_button);
        textTitle = findViewById(R.id.song_title);
        textArtist = findViewById(R.id.artist_name);
        textAlbum = findViewById(R.id.album_name);
        imageCover = findViewById(R.id.cover);
        seekBar = findViewById(R.id.seekbar);

        playManager.setIsPlaying(false);
        playManager.setMode(Mode.ORDER);
        playManager.intro(seekBar,buttonPlay,textTitle,textArtist,textAlbum,imageCover);
    }



    private void handleButtons()
    {

        ActivityResultLauncher<Intent> chooseFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();

                            introduction.handleActivityResult(data);
                            playManager.setIsChosen(introduction.getIsChosen());
                            playManager.setFilesUri(introduction.getFilesUri());

                            if ( playManager.getFilesUri() != null) playManager.setInfo(0);
                        }
                    }
                });

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFolderIntent = introduction.createChooseFolderIntent();
                chooseFolderLauncher.launch(chooseFolderIntent);
            }
        });


        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playManager.playOrPauseSong();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playManager.playNextSong();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playManager.playPreviousSong();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playManager.getIsChosen() == false) return;
                songProgress = progress; //progress of seekbar== progress of song (file)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (playManager.getIsChosen() == false) return;

                int fullLength = playManager.getMediaPlayer().getDuration();

                playManager.setSongFragment((songProgress * fullLength) / 100);

                songProgress = 0;
                playManager.getMediaPlayer().seekTo(playManager.getSongFragment());
                playManager.getMediaPlayer().getCurrentPosition();
            }
        });


        buttonPlayType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (playManager.getMode() == Mode.ORDER) {
                    playManager.setMode(Mode.RANDOM);
                    buttonPlayType.setImageResource(R.drawable.random);
                    if (playManager.getIsChosen() == true) playManager.generateRandomOrder();
                } else if(playManager.getMode()==Mode.RANDOM) {
                    playManager.setMode(Mode.PLAYLIST);
                    buttonPlayType.setImageResource(R.drawable.playlists_mode);
                }
                else //(mode==PLAYLIST)
                {
                    playManager.setMode(Mode.ORDER);
                    buttonPlayType.setImageResource(R.drawable.order);
                }
            }
        });


        buttonYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playManager.getIsChosen() == false) return;
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

                metadataRetriever.setDataSource(con, playManager.getFilesUri().get(playManager.getNowPlay()));
                String tittle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                Intent openYt = new Intent(Intent.ACTION_VIEW);
                openYt.setData(Uri.parse("https://www.youtube.com/results?search_query=" + tittle + "+" + artist));
                startActivity(openYt);
            }
        });



        buttonPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Playlists.class);
                startActivityForResult(intent,1);
            }
        });

    }


    private void restoreData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        songProgress=sharedPreferences.getInt("songProgress",0);

        playManager.restoreSavedData();
    }

    private void saveData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("songProgress",songProgress);
        editor.apply();
        playManager.saveData();
    }



    @Override
    protected void onResume() {
        super.onResume();
         if(playManager==null) restoreData();
         else if(playManager.getIsPlaying()==false) restoreData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }
}






