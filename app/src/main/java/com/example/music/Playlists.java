package com.example.music;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Playlists extends AppCompatActivity {

    private Button newPlaylistButton;
    private Vector<String> playlistsNames = new Vector<>();

    private ListView listView;
    private ArrayAdapter<String> adapter;
//----------------------------------------

    private Vector<Uri> filesUri;
    private Context context= this;
    static  private int now=0;


    static private int n=0;


    static private int number=0;
    private int song_number=0;
    private List<Uri> selected_playlist=new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);
        newPlaylistButton = findViewById(R.id.new_play);

        restoreData();

        listView= findViewById(R.id.playlistsListView);
        adapter = new ArrayAdapter<>(this, R.layout.playlistrow, playlistsNames);
        listView.setAdapter(adapter);


        newPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_playlist();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem =  parent.getItemAtPosition(position).toString();
                System.out.println(selectedItem);

                Intent intent = new Intent(Playlists.this, Playlist.class);
                startActivityForResult(intent,1);
            }
        });

    }


    //later in this function set restoring data from sharedPreferences or in onResume
    private void restoreData() {
        playlistsNames.add("English");
        playlistsNames.add("Polish");
        playlistsNames.add("Spanish");
        playlistsNames.add("Korean");

    }

























    private void add_playlist()
    {
        setContentView(R.layout.add_playlist);
        Button button_add;
        EditText name_edit;
        button_add= findViewById(R.id.playlist_add);
        name_edit= findViewById(R.id.editTextName);

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

         String answer = name_edit.getText().toString();

           number++;
           now=number-1;


                Toast toast = Toast.makeText(context, "Chose a file", Toast.LENGTH_LONG);
                toast.show();

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 0);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri SongUri = data.getData();


                Toast.makeText(context, "Chosen file: " + SongUri.toString(), Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }







}
