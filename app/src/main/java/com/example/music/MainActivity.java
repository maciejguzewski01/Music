package com.example.music;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import android.widget.SeekBar;
import android.Manifest.permission;
import androidx.media.session.MediaButtonReceiver;

public class MainActivity extends AppCompatActivity {

    private Mode mode; //mode of queue
    private int nowPlay = 0; //number of song now to be played
    private int songFragment = 0; //it is used to determinate from where song should start after pause
    private int songProgress = 0; //it is used to determinate position of seekbar

    private MediaPlayer mediaPlayer; //mediaPlayer
    boolean isPlaying = false;  //the song is playing
    boolean isChosen = false; //the folder was chosen
    boolean newSong = false; //new song was played

    private Button buttonSettings;  //in first phase it will be used only to determinate folder with music
    private Button buttonPlaylists; //it will be used in second phase
    private ImageButton buttonPlay;  //play- pause button
    private ImageButton buttonNext; //next song button
    private ImageButton buttonPrevious; //previous song button
    private ImageButton buttonPlayType; //button to determinate if songs are on random on order mode
    private ImageButton buttonYT; //button to open youtube and search for the song

    private TextView textTitle;  //title of song
    private TextView textArtist; //name of artist
    private TextView textAlbum; //name of album

    private ImageView imageCover;  //cover of album

    private Vector<Integer> randomVector;  //vector with random order of songs
    private int nowPlayRandom = 0; //number of now played song in random mode

    private Vector<Uri> filesUri; //vector with uri of files in chosen folder

    private SeekBar seekBar; //seekbar

    private Handler handler; //handler to update position of seekbar
    private Runnable runnable; //runnable to update position of seekbar

    private Context con = this; //context of class
    private NotificationChannel channel;  //notification channel
    private NotificationManager notificationManager1;  //notification manager


//strings used in intents in notification
    private  String ACTION_PREVIOUS = "com.example.music.action.PREVIOUS";
    private  String ACTION_PLAY_PAUSE = "com.example.music.action.PLAY_PAUSE";
    private  String ACTION_NEXT = "com.example.music.action.NEXT";



    //sorting of vector with uri files according title of song
    private void sort() {
        Vector<Uri> uriList = new Vector<>(filesUri);

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        Comparator<Uri> comparator = new Comparator<Uri>() {
            @Override
            public int compare(Uri uri1, Uri uri2) {
                metadataRetriever.setDataSource(con, uri1);
                String title1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                metadataRetriever.setDataSource(con, uri2);
                String title2 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                return title1.compareToIgnoreCase(title2);

            }
        };

        // Collections.sort(filesUri, comparator); //commented because it causes problems in big folder with music
    }



    //downloads Uri of files from chosen music folder to vector after choosing the folder
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        filesUri = new Vector<>();

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            Uri uriTree = data.getData();
            Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(uriTree, DocumentsContract.getTreeDocumentId(uriTree));

            Cursor cursor = null;
            cursor = this.getContentResolver().query(uriFolder, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(uriTree, cursor.getString(0));
                    filesUri.add(uriFile);

                } while (cursor.moveToNext());
            }

            if (cursor != null) cursor.close();

            if (filesUri.isEmpty() == false) isChosen = true;

            sort(); //sorting vector

            info(0);

            Toast toast = Toast.makeText(this, "Wybrano folder", Toast.LENGTH_LONG);
            toast.show();

        }
    }



    //creates intent to choose folder with music
    private void choose_folder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 0);
    }




    //extracts meta data from music file (name of song, artist, album and album cover) and displays them
    private void info(int num) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        metadataRetriever.setDataSource(this, filesUri.get(num));

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



    //plays song of given number
    private void play(int num) {

        if (isPlaying == false) //music is not playing
        {
            if ((mediaPlayer == null) || (newSong == true)) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, filesUri.get(num));
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
                    } else {
                        if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
                        else nowPlayRandom = 0;
                        newSong = true;
                        play(randomVector.elementAt(nowPlayRandom));
                    }

                }
            });

            info(num);


        } else //music is playing
        {
            mediaPlayer.pause();
            isPlaying = false;
            buttonPlay.setImageResource(R.drawable.play_button);
            songFragment = mediaPlayer.getCurrentPosition();
        }

        notification();
    }


    //displays notification
    private void notification() {
        if (isChosen == true) {

            Intent intent = new Intent();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);


            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

            metadataRetriever.setDataSource(this, filesUri.get(nowPlay));
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
            if(isPlaying==true) play_pause_icon=R.mipmap.pause;
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

            if (ActivityCompat.checkSelfPermission(this, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

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
            previous_song();
        } else if (action==ACTION_PLAY_PAUSE) {
           play_pause_song();
        } else if (action==ACTION_NEXT) {
            next_song();
        }
    }



    //generates random queue
    private void generate_random_order()
    {
        randomVector= new Vector<>();

        int size=filesUri.size();
        for(int i=0;i<size;++i)
        {
            randomVector.add(i);
        }
        Collections.shuffle(randomVector);
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
        isPlaying = false;
        mode = Mode.ORDER;


        //checks permissions
        if (ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        if (ContextCompat.checkSelfPermission(this, permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("not_chan_1", "channel", NotificationManager.IMPORTANCE_DEFAULT);
             notificationManager1 = getSystemService(NotificationManager.class);
            notificationManager1.createNotificationChannel(channel);
        }


        if (isChosen == false) {
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
                    play_pause_song();
                    notification();
                }
            });

            //next song
            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    next_song();
                }
            });

            //previous song
            buttonPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    previous_song();
                }
            });

             //seekBar
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (isChosen == false) return;
                    songProgress = progress; //progress of seekbar== progress of song (file)
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (isChosen == false) return;
                    if (isPlaying == false) return;

                    int fullLength = mediaPlayer.getDuration(); //full length of song (file)

                    songFragment = (songProgress * fullLength) / 100;  //it determinate at which fraction of song seekbar was placed

                    songProgress = 0;
                    mediaPlayer.seekTo(songFragment);
                    mediaPlayer.getCurrentPosition();
                }
            });


            //mode (random/ order)
            buttonPlayType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mode == Mode.ORDER) {
                        mode = Mode.RANDOM;
                        buttonPlayType.setImageResource(R.drawable.random);
                        if (isChosen == true) generate_random_order();
                    } else {
                        mode = Mode.ORDER;
                        buttonPlayType.setImageResource(R.drawable.order);
                    }
                }
            });


            //yt open
            buttonYT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isChosen == false) return;
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

                    metadataRetriever.setDataSource(con, filesUri.get(nowPlay));
                    String tittle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);


                    Intent openYt = new Intent(Intent.ACTION_VIEW);
                    openYt.setData(Uri.parse("https://www.youtube.com/results?search_query=" + tittle + "+" + artist));
                    startActivity(openYt);
                }
            });
        }


        //plays previous song
        private void previous_song()
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
            } else {
                if (nowPlayRandom - 1 >= 0) nowPlayRandom--;
                else generate_random_order();

                mediaPlayer.pause();
                isPlaying = false;
                buttonPlay.setImageResource(R.drawable.play_button);
                info(randomVector.elementAt(nowPlayRandom));
                newSong = true;
                play(randomVector.elementAt(nowPlayRandom));
            }
        }


        //plays next song
        private void next_song()
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
            } else {

                if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
                else generate_random_order();

                if (isPlaying == true) mediaPlayer.pause();
                isPlaying = false;
                buttonPlay.setImageResource(R.drawable.play_button);
                info(randomVector.elementAt(nowPlayRandom));
                newSong = true;
                play(randomVector.elementAt(nowPlayRandom));
            }
        }

        //plays- pauses song
        private void play_pause_song()
        {
            if (isChosen == false) return;

            if (mode == Mode.ORDER) play(nowPlay);
            else play(randomVector.elementAt(nowPlayRandom));
        }

    }




