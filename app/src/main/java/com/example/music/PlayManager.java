package com.example.music;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class PlayManager {
    private PlayManager() {
    }

    private static PlayManager instance;

    public static PlayManager getInstance() {
        if (instance == null) {
            instance = new PlayManager();
        }
        return instance;
    }

    public void setActivity(Activity newActivity) {
        activity = newActivity;
        notifier = new Notifier(activity);
    }

    private Notifier notifier;

    private Mode mode;

    public void setMode(Mode newMode) {
        mode = newMode;
    }

    public Mode getMode() {
        return mode;
    }

    private int nowPlay = 0;

    public int getNowPlay() {
        return nowPlay;
    }

    private int songFragment = 0;

    public int getSongFragment() {
        return songFragment;
    }

    public void setSongFragment(int newSongFragment) {
        songFragment = newSongFragment;
    }

    private MediaPlayer mediaPlayer;

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private boolean isPlaying = false;

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean newIsPlaying) {
        isPlaying = newIsPlaying;
    }

    private boolean isChosen = false;

    public boolean getIsChosen() {
        return isChosen;
    }

    public void setIsChosen(boolean newIsChosen) {
        isChosen = newIsChosen;
    }

    boolean newSong = false;

    private Vector<Integer> randomVector;
    private int nowPlayRandom = 0;

    private Handler handler;
    private Runnable runnable;

    private Vector<Uri> filesUri = new Vector<Uri>();

    public Vector<Uri> getFilesUri() {
        return filesUri;
    }

    public void setFilesUri(Vector<Uri> newFilesUri) {
        filesUri = newFilesUri;
    }

    private Activity activity;

    private SeekBar seekBar;
    private ImageButton buttonPlay;
    private TextView textTitle;
    private TextView textArtist;
    private TextView textAlbum;
    private ImageView imageCover;


    public void intro(SeekBar seekBarRef, ImageButton buttonPlayRef, TextView textTitleRef, TextView textArtistRef, TextView textAlbumRef, ImageView imageCoverRef) {
        seekBar = seekBarRef;
        buttonPlay = buttonPlayRef;
        textTitle = textTitleRef;
        textArtist = textArtistRef;
        textAlbum = textAlbumRef;
        imageCover = imageCoverRef;
    }


    public void setInfo(int num) {
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


    public void generateRandomOrder() {
        randomVector = new Vector<>();

        int size = filesUri.size();
        for (int i = 0; i < size; ++i) {
            randomVector.add(i);
        }
        Collections.shuffle(randomVector);
    }

    private boolean confirmFileExist(Uri uri)
    {
        ContentResolver contentResolver = activity.getContentResolver();
        Cursor cursor = null;

        cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) return true;
        else return false;
    }

    private void handlePlayCommandIfIsPlayingIsTrue()
    {
        mediaPlayer.pause();
        isPlaying = false;
        buttonPlay.setImageResource(R.drawable.play_button);
        songFragment = mediaPlayer.getCurrentPosition();
    }

    private void handlePlayCommandIfIsPlayingIsFalse(Uri uri)
    {
        if ((mediaPlayer == null) || (newSong == true)) {
            if (confirmFileExist(uri) == true)
            {
                mediaPlayer = MediaPlayer.create(activity, uri);
                newSong = false;
                songFragment = 0;
            }
            else
            {
                Toast toast = Toast.makeText(activity, "Changes in music folder detected. Please chose folder in settings once again.", Toast.LENGTH_LONG);
                toast.show();
            }

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

    }


    private void playFromUri(Uri uri)
    {
        if (isPlaying == false)
        {
            handlePlayCommandIfIsPlayingIsFalse(uri);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(selectedSong+1<selectedPlaylist.size()) selectedSong++;
                    else selectedSong = 0;
                    newSong = true;
                    isPlaying=false;
                    playFromUri(selectedPlaylist.get(selectedSong));
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
        } else
        {
            handlePlayCommandIfIsPlayingIsTrue();
        }
        notifier.notification(this);
    }


    private void play(int num) {
        if (isPlaying == false)
        {
            handlePlayCommandIfIsPlayingIsFalse(filesUri.get(num));

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mode == Mode.ORDER) {
                        if (nowPlay + 1 < filesUri.size()) nowPlay++;
                        else nowPlay = 0;
                        newSong = true;
                        isPlaying=false;
                        play(nowPlay);
                    } else if(mode==Mode.RANDOM){
                        if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
                        else nowPlayRandom = 0;
                        newSong = true;
                        isPlaying=false;
                        play(randomVector.elementAt(nowPlayRandom));
                    }
                    else
                    {
                        playFromUri(selectedPlaylist.get(selectedSong));
                    }
                }
            });

            setInfo(num);
        } else
        {
            handlePlayCommandIfIsPlayingIsTrue();
        }
        notifier.notification(this);
    }


    private void changeSongProcedures()
    {
        mediaPlayer.pause();
        isPlaying = false;
        buttonPlay.setImageResource(R.drawable.play_button);
        newSong = true;
        notifier.notification(this);
    }


    public void playPreviousSong()
    {
        if (isChosen == false) return;
        boolean wasPlaying=isPlaying;
        seekBar.setProgress(0);
        if (mode == Mode.ORDER) {
            if (nowPlay - 1 >= 0) nowPlay--;
            else nowPlay = filesUri.size() - 1;

            changeSongProcedures();
            setInfo(nowPlay);

            if(wasPlaying==true) play(nowPlay);
        } else if(mode == Mode.RANDOM){
            if (nowPlayRandom - 1 >= 0) nowPlayRandom--;
            else generateRandomOrder();

            changeSongProcedures();
            setInfo(randomVector.elementAt(nowPlayRandom));
            if(wasPlaying==true) play(randomVector.elementAt(nowPlayRandom));
        }
        else
        {
            if(selectedSong-1>0) selectedSong--;
            else selectedSong = selectedPlaylist.size()-1;

            changeSongProcedures();
            if(wasPlaying==true) playFromUri(selectedPlaylist.get(selectedSong));
        }

    }

    public void playNextSong()
    {
        if (isChosen == false) return;
        boolean wasPlaying=isPlaying;
        seekBar.setProgress(0);
        if (mode == Mode.ORDER) {
            if (nowPlay + 1 < filesUri.size()) nowPlay++;
            else nowPlay = 0;

            changeSongProcedures();
            setInfo(nowPlay);

            if(wasPlaying==true) play(nowPlay);
        } else if(mode== Mode.RANDOM){

            if (nowPlayRandom + 1 < randomVector.size()) nowPlayRandom++;
            else generateRandomOrder();

            changeSongProcedures();
            setInfo(randomVector.elementAt(nowPlayRandom));

            if(wasPlaying==true) play(randomVector.elementAt(nowPlayRandom));
        }
        else
        {
            if(selectedSong+1<selectedPlaylist.size()) selectedSong++;
            else selectedSong=0;

            changeSongProcedures();
            if(wasPlaying==true) playFromUri(selectedPlaylist.get(selectedSong));
        }

    }


    public void playOrPauseSong() {
        if (isChosen == false) return;
        if (mode == Mode.ORDER)
        {
            play(nowPlay);
        }
        else if (mode == Mode.RANDOM) play(randomVector.elementAt(nowPlayRandom));
        else {
            getSelectedPlaylistData();
            if(selectedPlaylist!=null)
            {
                if(selectedPlaylist.isEmpty()==true)
                {
                    Toast toast = Toast.makeText(activity, "Chose a playlist!", Toast.LENGTH_LONG);
                    toast.show();
                }
                else playFromUri(selectedPlaylist.get(selectedSong));
            }
            else
            {
                Toast toast = Toast.makeText(activity, "Chose a playlist!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }


    public void saveData()
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isChosen",isChosen);
        editor.putString("mode",mode.name());
        editor.putInt("nowPlay",nowPlay);
        editor.putInt("songFragment",songFragment);
        editor.putInt("nowPlayRandom",nowPlayRandom);

        Gson gson = new Gson();
        String jsonRandomVector=gson.toJson(randomVector);
        editor.putString("randomVector",jsonRandomVector);

        Vector<String> filesUriStrings = new Vector<>();
        for(Uri uri: filesUri) {filesUriStrings.add(uri.toString());}
        String jsonFilesUri=gson.toJson(filesUriStrings);
        editor.putString("filesUri",jsonFilesUri);

        editor.putInt("selectedSong",selectedSong);
        if(selectedPlaylist!=null)
        {
            String jsonSelectedPlaylist=gson.toJson(selectedPlaylist);
            editor.putString("selectedPlaylist",jsonSelectedPlaylist);
        }

        editor.apply();
    }

    public void restoreSavedData()
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        isChosen = sharedPreferences.getBoolean("isChosen", false);
        if(isChosen==false) return;
        mode = Mode.valueOf(sharedPreferences.getString("mode","ORDER"));
        nowPlay = sharedPreferences.getInt("nowPlay",0);
        songFragment = sharedPreferences.getInt("songFragment",0);
        nowPlayRandom=sharedPreferences.getInt("nowPlayRandom",0);

        Gson gson = new Gson();
        String jsonRandomVector=sharedPreferences.getString("randomVector","");
        Type typeRV = new TypeToken<Vector<Integer>>() {}.getType();
        randomVector = gson.fromJson(jsonRandomVector,typeRV);

        String jsonFilesUri = sharedPreferences.getString("filesUri", "");
        if(jsonFilesUri.isEmpty()==false)
        {
            Type typeFU = new TypeToken<Vector<String>>() {}.getType();
            Vector<String> uriStrings = gson.fromJson(jsonFilesUri, typeFU);
            filesUri.clear();
            for (String uriString : uriStrings) {
                filesUri.add(Uri.parse(uriString));
            }
        }




        selectedSong=sharedPreferences.getInt("selectedSong",0);
        String jsonSelectedPlaylist = sharedPreferences.getString("selectedPlaylist", "");
        if(jsonSelectedPlaylist.isEmpty()==false)
        {
            try {
                Type typeSP = new TypeToken<Vector<String>>() {}.getType();
                Vector<String> selectedPlaylistUriStrings = gson.fromJson(jsonSelectedPlaylist, typeSP);
                selectedPlaylist.clear();
                for (String selectedPlaylistUriString : selectedPlaylistUriStrings) {
                    selectedPlaylist.add(Uri.parse(selectedPlaylistUriString));
                }
            }catch (JsonSyntaxException e)
            {
                selectedPlaylist=new Vector<>();
            }
        }
        setInfoAndSeekBar();
    }

    private void setInfoAndSeekBar()
    {
        if(mode==Mode.ORDER)
        {
            setInfo(nowPlay);
            mediaPlayer = MediaPlayer.create(activity, filesUri.elementAt(nowPlay));
        }
        else if(mode==Mode.RANDOM)
        {
            setInfo(nowPlayRandom);
            mediaPlayer = MediaPlayer.create(activity, filesUri.elementAt(nowPlayRandom));
        }
        else//mode==Mode.PLAYLIST
        {
            if(selectedPlaylist==null)
            {
                mode=Mode.ORDER;
                setInfo(nowPlay);
                mediaPlayer = MediaPlayer.create(activity, filesUri.elementAt(nowPlay));
            }
            else if (selectedPlaylist.isEmpty()==true)
            {
                mode=Mode.ORDER;
                setInfo(nowPlay);
                mediaPlayer = MediaPlayer.create(activity, filesUri.elementAt(nowPlay));
            }
            else
            {
                setInfo(filesUri.indexOf(selectedPlaylist.elementAt(selectedSong)));
                mediaPlayer = MediaPlayer.create(activity, selectedPlaylist.elementAt(selectedSong));
            }
        }
        mediaPlayer.seekTo(songFragment);
        double fragment_d = (100.0 * songFragment) / mediaPlayer.getDuration();
        int fragment = ((int) fragment_d);
        seekBar.setProgress(fragment);
    }

    private ChosenPlaylist chosenPlaylist;
    private Vector<Uri> selectedPlaylist=new Vector<>();
    private int selectedSong=0;
    private void getSelectedPlaylistData()
    {
        chosenPlaylist=ChosenPlaylist.getInstance();
        if((chosenPlaylist.getSelectedPlaylist()!=null)&&(chosenPlaylist.getSelectedPlaylist().isEmpty()==false))selectedPlaylist=chosenPlaylist.getSelectedPlaylist();
        selectedSong=chosenPlaylist.getSelectedSong();
    }



}
