package com.example.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Playlists extends AppCompatActivity {

    private Button newPlaylistButton;
    private  Button deletePlaylistButton;
    private Vector<String> playlistsNames;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private  Activity activity;



    private ListView listViewSinglePlaylist;
    private Button newSongButton;

    private Map<String,Vector<Uri>> playlistsUri;
    private Map<String,Vector<String>> playlistsTitles;

    private ArrayAdapter<String> adapterPlaylist;

    private Vector<Uri> selectedPlaylist;
    private int selectedSong;
    public Vector<Uri> getSelectedPlaylist(){return selectedPlaylist;}
    public int getSelectedSong(){return selectedSong;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);

        playlistsUri= new HashMap<>();
        playlistsTitles= new HashMap<>();
        playlistsNames = new Vector<>();
        selectedPlaylist=new Vector<>();


        newPlaylistButton = findViewById(R.id.new_play);
        deletePlaylistButton=findViewById(R.id.delete_play);

        activity=this;
        restoreData();
        listView= findViewById(R.id.playlistsListView);
        adapter = new ArrayAdapter<>(this, R.layout.playlistrow, playlistsNames);
        listView.setAdapter(adapter);


        newPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlaylist();
            }
        });

        deletePlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePlaylist();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem =  parent.getItemAtPosition(position).toString();
                displayPlaylist(selectedItem);
            }
        });

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

                    playlistsNames.add(name);

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


    private void deletePlaylist()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete playlist");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_playlist, null);
        builder.setView(dialogView);

        EditText nameToDelete = dialogView.findViewById(R.id.editTextName);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameToDelete.getText().toString();
                if (name.isEmpty()==false)
                {
                    if(playlistsNames.contains(name))
                    {
                        playlistsNames.remove(name);
                        playlistsTitles.remove(name);
                        playlistsUri.remove(name);
                        adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast toast = Toast.makeText(activity, "Wrong name", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(activity, "Wrong name", Toast.LENGTH_LONG);
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

//---------------------------------------------------------------------------

    private Vector<Uri> currentPlaylistUri;
    private Vector<String> currentPlaylistString;
    private boolean songAdded;
    private String currentName;
    private Button deleteSongButton;

    private void displayPlaylist(String name) {
        setContentView(R.layout.play);
        newSongButton=findViewById(R.id.new_play);
        deleteSongButton= findViewById(R.id.delete_song);

        songAdded=false;
        currentName=name;

        currentPlaylistUri=new Vector<>();
        currentPlaylistString=new Vector<>();


       currentPlaylistUri=playlistsUri.get(name);
       currentPlaylistString=playlistsTitles.get(name);




        listViewSinglePlaylist=findViewById(R.id.songsListView);
        adapterPlaylist = new ArrayAdapter<>(this, R.layout.playlistrow, currentPlaylistString);
        listViewSinglePlaylist.setAdapter(adapterPlaylist);

        newSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSong();
            }
        });


        deleteSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSong();
            }
        });



        listViewSinglePlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

                        songAdded=true;

                        playlistsUri.get(currentName).add(uriTree);
                        playlistsTitles.get(currentName).add(songTitle);

                        adapterPlaylist.notifyDataSetChanged();
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

    private void deleteSong()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete song");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_playlist, null);
        builder.setView(dialogView);

        EditText nameToDelete = dialogView.findViewById(R.id.editTextName);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameToDelete.getText().toString();
                if (name.isEmpty()==false)
                {
                    if(currentPlaylistString.contains(name))
                    {
                        int position=currentPlaylistString.indexOf(name);
                        currentPlaylistString.remove(position);
                        currentPlaylistUri.remove(position);

                        adapterPlaylist.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast toast = Toast.makeText(activity, "Wrong name", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(activity, "Wrong name", Toast.LENGTH_LONG);
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


    private void restoreData()
    {
        playlistsUri.clear();
        playlistsTitles.clear();

        SharedPreferences sharedPreferences = activity.getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        selectedSong = sharedPreferences.getInt("SelectedSong",0);

        Gson gson = new Gson();

        String jsonPlaylistsNames=sharedPreferences.getString("playlistsNames","");
        Type typePN = new TypeToken<Vector<String>>() {}.getType();
        playlistsNames = gson.fromJson(jsonPlaylistsNames,typePN);
        if(playlistsNames==null) playlistsNames=new Vector<>();


        String jsonPlaylistsUri=sharedPreferences.getString("playlistsUri","");
        if(jsonPlaylistsUri.isEmpty()==false)
        {
            Type typePU = new TypeToken<Map<String,Vector<String>>>() {}.getType();
            Map<String,Vector<String>> playlistsUriStrings=gson.fromJson(jsonPlaylistsUri, typePU);

            for (Map.Entry<String, Vector<String>> entry : playlistsUriStrings.entrySet()) {
                Vector<Uri> uri = new Vector<>();
                for (String uriString : entry.getValue()) {uri.add(Uri.parse(uriString));                }
                playlistsUri.put(entry.getKey(), uri);
            }
        }
        if(playlistsUri==null) playlistsUri=new HashMap<>();


        String jsonPlaylistsTitles=sharedPreferences.getString("playlistsTitles","");
        if(jsonPlaylistsTitles.isEmpty()==false)
        {
            Type typePT = new TypeToken<Map<String,Vector<String>>>() {}.getType();
            playlistsTitles = gson.fromJson(jsonPlaylistsTitles,typePT);
        }
        if(playlistsTitles==null) playlistsTitles=new HashMap<>();



        String jsonSelectedPlaylist=sharedPreferences.getString("selectedPlaylist","");
        if(jsonSelectedPlaylist.isEmpty()==false)
        {
            Type typeSP = new TypeToken<Vector<String>>() {}.getType();
            Vector<String> selectedPlaylistUriStrings = gson.fromJson(jsonSelectedPlaylist, typeSP);

            selectedPlaylist.clear();
            for (String selectedPlaylistUriString : selectedPlaylistUriStrings) {
                selectedPlaylist.add(Uri.parse(selectedPlaylistUriString));
            }
        }
        if(selectedPlaylist==null) selectedPlaylist=new Vector<>();

       


    }


    private void saveData()
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonPlaylistsNames=gson.toJson(playlistsNames);
        editor.putString("playlistsNames",jsonPlaylistsNames);



        Map<String, Vector<String>> playlistsUriString = new HashMap<>();
        for(Map.Entry<String, Vector<Uri>> entry: playlistsUri.entrySet())
        {
            Vector<String> uriString=new Vector<>();
            for(Uri uri: entry.getValue()){uriString.add(uri.toString());}
            playlistsUriString.put(entry.getKey(), uriString);
        }
        String jsonPlaylistsUri=gson.toJson(playlistsUriString);
        editor.putString("playlistsUri",jsonPlaylistsUri);

        String jsonPlaylistsTitles=gson.toJson(playlistsTitles);
        editor.putString("playlistsTitles",jsonPlaylistsTitles);

        Vector<String> selectedPlaylistString = new Vector<>();
        for(Uri uri: selectedPlaylist) {selectedPlaylistString.add(uri.toString());}
        String jsonSelectedPlaylist=gson.toJson(selectedPlaylistString);
        editor.putString("selectedPlaylist",jsonSelectedPlaylist);

        editor.putInt("SelectedSong",selectedSong);

        editor.apply();
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

    //Overwrites data in sharedPreferences with empty data. It has no use in app but is preserved for debugging.
     /*private void saveEmptyData()
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("playlistsNames","");
        editor.putString("playlistsUri","");
        editor.putString("playlistsTitles","");
        editor.putString("selectedPlaylist","");
        editor.putInt("SelectedSong",0);
        editor.apply();
    }*/


}
