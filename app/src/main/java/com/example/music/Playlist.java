package com.example.music;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Playlist {
   static private String name;
   static private Vector<Uri> songs;
    private Context con;


    Playlist(String n)
    {
        name=n;
        songs= new Vector<>();

    }

    Playlist()
    {
        songs= new Vector<>();
    }
    Playlist(Context context)
    {
        songs= new Vector<>();
        con=context;
    }

    void set_name(String n){ name=n;}
    String get_name()
    {
        return name;
    }

    void add_song_to_playlist(Uri uri)
    {

        songs.add(uri);


    }

    int get_size_of_playlist()
    {
        return songs.size();
    }

    Uri get_uri_of_song_number_x(int number)
    {
        return songs.elementAt(number);
    }

    void remove_song_number_x_from_playlist(int number)
    {
        songs.remove(number);
    }

    List<String> get_list()
    {

        List<String> answer = new ArrayList<>();
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        for(int i=0;i<songs.size();++i)
        {
            metadataRetriever.setDataSource(con, songs.elementAt(i));
            String data = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            answer.add(data);

        }
        return answer;
    }


    List<Uri> get_list_uri()
    {
        List<Uri> answer = new ArrayList<>();

        for(int i=0;i<songs.size();++i)
        {
            answer.add(songs.elementAt(i));
        }
        return answer;
    }


}
