package com.lawnscape;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Mellis on 3/23/2017.
 */

public class PhotoGridAdapter extends BaseAdapter {
    ArrayList<Uri> photoUriList;
    private LayoutInflater layoutInflater;
    Context context;
    PhotoGridAdapter(Context ctx, ArrayList<Uri> pList){
        context = ctx;
        photoUriList = pList;
        layoutInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoGridAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_photo_grid_item, null);
            holder = new PhotoGridAdapter.ViewHolder();
            holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhotoGrid);
            convertView.setTag(holder);
        } else {
            holder = (PhotoGridAdapter.ViewHolder) convertView.getTag();
        }
      
        Picasso.with(context).load(photoUriList.get(position)).into(holder.ivPhoto);
        //holder.ivPhoto.setImageURI(photoUriList.get(position));
        return convertView;
    }

    static class ViewHolder {
        ImageView ivPhoto;
    }
}
