package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

public class UploadPhotosActivity extends Activity {
    final int PICK_PHOTO_FROM_GALLERY = 12;//arbitrary
    GridView gvUploadPhotos;
    ArrayList<Uri> uriList;
    PhotoGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photos);
        gvUploadPhotos = (GridView) findViewById(R.id.gvUploadPhotos);
        uriList = new ArrayList<Uri>();
        gridAdapter = new PhotoGridAdapter(this, uriList);
        gvUploadPhotos.setAdapter(gridAdapter);
    }

    public void pickImage(View v){
        Intent photoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        photoGalleryIntent.setType("image/*");
        //photoGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoGalleryIntent, PICK_PHOTO_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            uriList.add(targetURI);
            gridAdapter.notifyDataSetChanged();
        }
    }
}
