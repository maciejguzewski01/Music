package com.example.music;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import androidx.media.session.MediaButtonReceiver;

public class MainActivity extends AppCompatActivity {

    private int songProgress = 0;

    private Button buttonSettings;
    private Button buttonPlaylists;
    private ImageButton buttonPlay;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton buttonPlayType;
    private ImageButton buttonYT;

    private TextView textTitle;
    private TextView textArtist;
    private TextView textAlbum;

    private ImageView imageCover;

    private SeekBar seekBar;

    private Context con = this;
    private NotificationChannel channel;
    private NotificationManager notificationManager1;

    private  String ACTION_PREVIOUS = "com.example.music.action.PREVIOUS";
    private  String ACTION_PLAY_PAUSE = "com.example.music.action.PLAY_PAUSE";
    private  String ACTION_NEXT = "com.example.music.action.NEXT";


    private Permissions permissions= new Permissions();
    private Introduction introduction = new Introduction(this);
    private PlayManager playManager = new PlayManager(this);


    public void notification() {
        if (playManager.getIsChosen() == true) {

            Intent intent = new Intent();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

            metadataRetriever.setDataSource(this, playManager.getFilesUri().get(playManager.getNowPlay()));
            String tittle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);


            byte[] cover = metadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = null;
            if (cover != null) {
                bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
                // imageCover.setImageBitmap(bitmap);
                ImageView im = new ImageView(this);
                im.setImageBitmap(bitmap);
            }


            Intent intent1 = new Intent(this, MainActivity.class);
            intent1.setAction(ACTION_PREVIOUS);
            PendingIntent prevPendingIntent=  PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);

            Intent intent2 = new Intent(this, MainActivity.class);
            intent2.setAction(ACTION_PLAY_PAUSE);
            PendingIntent pausePendingIntent=  PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_IMMUTABLE);

            Intent intent3 = new Intent(this, MainActivity.class);
            intent3.setAction(ACTION_NEXT);
            PendingIntent nextPendingIntent=  PendingIntent.getActivity(this, 0, intent3, PendingIntent.FLAG_IMMUTABLE);

            Bitmap dmp= BitmapFactory.decodeResource(this.getResources(),R.drawable.default_music_cover);


            int play_pause_icon;
            if(playManager.getIsPlaying()==true) play_pause_icon=R.mipmap.pause;
            else play_pause_icon=R.mipmap.play;

            Notification notification = new NotificationCompat.Builder(this, "not_chan_1")
                    .setLargeIcon(bitmap)
                    .setSmallIcon(R.mipmap.default_music_cover)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setShowCancelButton(true)
                            .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,   PlaybackStateCompat.ACTION_STOP)))
                    .addAction(R.mipmap.previous, "Previous", prevPendingIntent)
                    .addAction(play_pause_icon, "Pause", pausePendingIntent)
                    .addAction(R.mipmap.next, "Next", nextPendingIntent)
                    .setContentTitle(tittle)
                    .setContentText(artist)
                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            notificationManager.notify(1,notification);
        }
    }

    //detects pushing one of notification buttons
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();

        if(action==ACTION_PREVIOUS) {
            playManager.previous_song();
        } else if (action==ACTION_PLAY_PAUSE) {
            playManager.play_pause_song();
        } else if (action==ACTION_NEXT) {
            playManager.next_song();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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



        permissions.checkPermissionsAndAskIfMissing(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("not_chan_1", "channel", NotificationManager.IMPORTANCE_DEFAULT);
             notificationManager1 = getSystemService(NotificationManager.class);
            notificationManager1.createNotificationChannel(channel);
        }


        if (playManager.getIsChosen() == false) {
            Context context = getApplicationContext();
            CharSequence text = "Wybierz Folder!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        ActivityResultLauncher<Intent> chooseFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();

                            introduction.handleActivityResult(data);
                            playManager.setIsChosen(introduction.getIsChosen());
                            //filesUri = new Vector<Uri>();
                            //filesUri=introduction.getFilesUri();
                            playManager.setFilesUri(introduction.getFilesUri());

                            if ( playManager.getFilesUri() != null) {
                                playManager.info(0);
                            }
                        }
                    }
                }
        );
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


                    playManager.play_pause_song();
                    notification();
                }
            });

            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playManager.next_song();
                }
            });

            buttonPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playManager.previous_song();
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
                    if(playManager.getIsPlaying()==false) return;

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
                        if (playManager.getIsChosen() == true) playManager.generate_random_order();
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


    }




