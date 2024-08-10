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
    static private Button button_new;
    static private Button button_one;
    static private Button button_two;
    static private Button button_three;
    static private Button button_four;
    static private Button button_five;

    static private Button button_test;

    static  private Button button_playlist_num;

   static private Vector<Uri> filesUri;
    private Context context= this;
    static  private int now=0;

   static private ListView listView;

static private int n=0;

static private Playlist p1;
    static  private Playlist p2;
    static  private Playlist p3;
    static  private Playlist p4;
    static private Playlist p5;
    static private int number=0;
    private int song_number=0;
    private List<Uri> selected_playlist=new ArrayList<>();


int get_song_number()
    {
        return song_number;
    }

List<Uri> get_playlist()
{
    return selected_playlist;
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

                switch(number)
                {
                    case 1:
                        button_one.setVisibility(View.VISIBLE);
                        button_one.setText(answer);
                        p1.set_name(answer);
                        break;
                    case 2:
                        button_two.setVisibility(View.VISIBLE);
                        button_two.setText(answer);
                        p2.set_name(answer);
                        break;
                    case 3:
                        button_three.setVisibility(View.VISIBLE);
                        button_three.setText(answer);
                        p3.set_name(answer);
                        break;
                    case 4:
                        button_four.setVisibility(View.VISIBLE);
                        button_four.setText(answer);
                        p4.set_name(answer);
                        break;
                    case 5:
                        button_five.setVisibility(View.VISIBLE);
                        button_five.setText(answer);
                        p5.set_name(answer);
                        break;
                }


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
             switch(number)
             {
                 case 1:
                    p1.add_song_to_playlist(SongUri);
                     break;
                 case 2:
                     p2.add_song_to_playlist(SongUri);
                     break;
                 case 3:
                     p3.add_song_to_playlist(SongUri);
                     break;
                 case 4:
                     p4.add_song_to_playlist(SongUri);
                     break;
                 case 5:
                     p5.add_song_to_playlist(SongUri);
                     break;
             }

                Toast.makeText(context, "Chosen file: " + SongUri.toString(), Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

private void view_playlist(Playlist p_num)
{
    setContentView(R.layout.play);
    listView=findViewById(R.id.songListView);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, p_num.get_list());
    listView.setAdapter(adapter);

    Button add_song;
    add_song=findViewById(R.id.add_song);
    add_song.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast toast = Toast.makeText(context, "Wybierz plik", Toast.LENGTH_LONG);
            toast.show();

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, 0);
        }
    });


    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String selected = p_num.get_list().get(position);
            song_number=position;

            selected_playlist=p_num.get_list_uri();
            returnData();
finish();
        }
    });
}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);
        button_test=findViewById(R.id.button_playlist1);

        if(n==0)
        {
            p1=new Playlist(context);
            p2=new Playlist(context);
            p3=new Playlist(context);
            p4=new Playlist(context);
            p5=new Playlist(context);
            n++;
        }

        button_new= findViewById(R.id.new_play);
        button_one=findViewById(R.id.button_playlist1);
        button_two=findViewById(R.id.button_playlist2);
        button_three=findViewById(R.id.button_playlist3);
        button_four=findViewById(R.id.button_playlist4);
        button_five=findViewById(R.id.button_playlist5);

            switch(number)
            {
                case 0:
                    button_one.setVisibility(View.INVISIBLE);
                    button_two.setVisibility(View.INVISIBLE);
                    button_three.setVisibility(View.INVISIBLE);
                    button_four.setVisibility(View.INVISIBLE);
                    button_five.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    button_one.setText(p1.get_name());
                    button_two.setVisibility(View.INVISIBLE);
                    button_three.setVisibility(View.INVISIBLE);
                    button_four.setVisibility(View.INVISIBLE);
                    button_five.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    button_one.setText(p1.get_name());
                    button_two.setText(p2.get_name());
                    button_three.setVisibility(View.INVISIBLE);
                    button_four.setVisibility(View.INVISIBLE);
                    button_five.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    button_one.setText(p1.get_name());
                    button_two.setText(p2.get_name());
                    button_three.setText(p3.get_name());
                    button_four.setVisibility(View.INVISIBLE);
                    button_five.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    button_one.setText(p1.get_name());
                    button_two.setText(p2.get_name());
                    button_three.setText(p3.get_name());
                    button_four.setText(p4.get_name());
                    button_five.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }


        button_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(number==5)
                 {
                        Toast.makeText(context, "Nie można dodać kolejnej playlisty ", Toast.LENGTH_LONG);
                 }
                else
                 {
                   add_playlist();
                 }
            }
        });


        button_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view_playlist(p1);
            }
        });


       button_two.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view_playlist(p2);
        }
       });

        button_three.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        view_playlist(p3);
        }
        });


        button_four.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        view_playlist(p4);
        }
        });



        button_five.setOnClickListener(new View.OnClickListener() {
         @Override
          public void onClick(View view) {
        view_playlist(p5);
        }
        });

    }


    private void returnData() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("t", (ArrayList<Uri>) selected_playlist);

        setResult(RESULT_OK, intent);
        finish();
    }


}
