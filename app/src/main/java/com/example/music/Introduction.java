package com.example.music;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class Introduction {

    private Vector<Uri> filesUri;
    private boolean isChosen;
    private AppCompatActivity appCompatActivity;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    public Introduction(AppCompatActivity activity) {
        appCompatActivity = activity;
        isChosen = false;

        activityResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        handleActivityResult(data);
                    }
                }
        );
    }


    public Intent createChooseFolderIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        return intent;
    }

    public boolean getIsChosen() { return isChosen; }

    public Vector<Uri> getFilesUri() { return filesUri; }



    public void handleActivityResult(Intent data) {
        if (data==null) return;

        filesUri = new Vector<>();
        Uri uriTree = data.getData();
        Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(uriTree, DocumentsContract.getTreeDocumentId(uriTree));

        Cursor cursor = null;
        cursor = appCompatActivity.getContentResolver().query(uriFolder, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(uriTree, cursor.getString(0));
                filesUri.add(uriFile);
                } while (cursor.moveToNext());
            }

        if (cursor != null) cursor.close();

        if (filesUri.isEmpty() == false) isChosen = true;

        sort();

        Toast toast = Toast.makeText(appCompatActivity, "You have chosen the folder.", Toast.LENGTH_LONG);
        toast.show();

    }

    private void sort() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        Comparator<Uri> comparator = new Comparator<Uri>() {
            @Override
            public int compare(Uri uri1, Uri uri2) {
                metadataRetriever.setDataSource(appCompatActivity, uri1);
                String data1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                metadataRetriever.setDataSource(appCompatActivity, uri2);
                String data2 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                if (data1 == data2) {
                    metadataRetriever.setDataSource(appCompatActivity, uri1);
                    data1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    metadataRetriever.setDataSource(appCompatActivity, uri2);
                    data2 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                }
                return data1.compareToIgnoreCase(data2);
            }
        };

        Collections.sort(filesUri, comparator);
    }
}
