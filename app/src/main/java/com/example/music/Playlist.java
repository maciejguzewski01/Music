package com.example.music;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Vector;

public class Playlist extends AppCompatActivity {

    private String name;
    public void setName(String newName){name=newName;}
    public String getName(){return name;}


    private Vector<Uri> filesUri=new Vector<>();
    private Vector<String> titles=new Vector<>();



    private Button newSongButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        newSongButton=findViewById(R.id.new_play);

        restorePlaylistData();

        listView=findViewById(R.id.songsListView);
        adapter = new ArrayAdapter<>(this, R.layout.playlistrow, titles);

        newSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSong();
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                System.out.println(selectedItem);

            }
        });


    }


    private void restorePlaylistData()
    {
        titles.add("Piosenka 1");
        titles.add("Piosenka 2");
        titles.add("Piosenka 3");

    }





    private void addSong()
    {

    }








}
