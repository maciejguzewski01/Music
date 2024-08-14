package com.example.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Playlists extends AppCompatActivity {

    private Button newPlaylistButton;
    private Vector<String> playlistsNames = new Vector<>();

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private  Activity activity;



    private ListView listViewSinglePlaylist;
    private Button newSongButton;

    private Map<String,Vector<Uri>> playlistsUri= new HashMap<>();
    private Map<String,Vector<String>> playlistsTitles= new HashMap<>();

    private ArrayAdapter<String> adapterPlaylist;

    private Vector<Uri> selectedPlaylist=new Vector<>();
    private int selectedSong;
    public Vector<Uri> getSelectedPlaylist(){return selectedPlaylist;}
    public int getSelectedSong(){return selectedSong;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);

        newPlaylistButton = findViewById(R.id.new_play);
        activity=this;

        restoreData();

        listViewSinglePlaylist= findViewById(R.id.playlistsListView);
        adapter = new ArrayAdapter<>(this, R.layout.playlistrow, playlistsNames);
        listViewSinglePlaylist.setAdapter(adapter);


        newPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlaylist();
            }
        });


        listViewSinglePlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem =  parent.getItemAtPosition(position).toString();
                displayPlaylist(selectedItem);
            }
        });

    }


    //later in this function set restoring data from sharedPreferences or in onResume
    private void restoreData() {
        playlistsNames.add("English");
        playlistsNames.add("Polish");
        playlistsNames.add("Spanish");
        playlistsNames.add("Korean");

        //odzyskac pliki w playlsitach

    }



//---------------------------------------------------------------------------

    private Vector<Uri> currentPlaylistUri=new Vector<>();
    private Vector<String> currentPlaylistString=new Vector<>();
    boolean songAdded;

    private void displayPlaylist(String name) {
        setContentView(R.layout.play);
        newSongButton=findViewById(R.id.new_play);
        songAdded=false;

        currentPlaylistUri=playlistsUri.get(name);
        currentPlaylistString=playlistsTitles.get(name);

        listView=findViewById(R.id.songsListView);
        adapterPlaylist = new ArrayAdapter<>(this, R.layout.playlistrow, currentPlaylistString);

        newSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addSong();
                if(songAdded==true)
                {
                    songAdded=false;

                    playlistsUri.get(name).add(currentPlaylistUri.lastElement());
                    playlistsTitles.get(name).add(currentPlaylistString.lastElement());
                }
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSong=position;
                selectedPlaylist=playlistsUri.get(name);
            }
        });


    }

    ActivityResultLauncher<Intent> chooseFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data==null) return;

                        Uri uriTree = data.getData();

                        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                        metadataRetriever.setDataSource(activity, uriTree);
                        String songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                        currentPlaylistUri.add(uriTree);
                        currentPlaylistString.add(songTitle);
                        songAdded=true;


                    }
                }
            });



    private void addSong()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        chooseFileLauncher.launch(intent);
    }

    private void addPlaylist()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Playlist");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_playlist, null);
        builder.setView(dialogView);

        EditText name_edit = dialogView.findViewById(R.id.editTextName);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = name_edit.getText().toString();
                if (name.isEmpty()==false)
                {
                    Vector<Uri> vec1 = new Vector<>();
                    playlistsUri.put(name, vec1);
                    Vector<String> vec2 = new Vector<>();
                    playlistsTitles.put(name, vec2);

                    adapter.notifyDataSetChanged();
                }
                else
                {
                    Toast toast = Toast.makeText(activity, "You need to chose a name!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
