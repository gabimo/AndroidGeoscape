package com.lawnscape;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mellis on 3/23/2017.
 */

public class PhotoAdapter extends BaseAdapter {
    ArrayList<Uri> photoUriList;
    private LayoutInflater layoutInflater;
    Context context;
    PhotoAdapter(Context ctx, ArrayList<Uri> pList){
        context = ctx;
        photoUriList = pList;
        layoutInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return photoUriList.size();
    }

    @Override
    public Object getItem(int position) {
        return photoUriList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_photo_grid_item, null);
            holder = new PhotoAdapter.ViewHolder();
            holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhotoGrid);
            convertView.setTag(holder);
        } else {
            holder = (PhotoAdapter.ViewHolder) convertView.getTag();
        }
      
        Picasso.with(context).load(photoUriList.get(position)).into(holder.ivPhoto);
        //holder.ivPhoto.setImageURI(photoUriList.get(position));
        return convertView;
    }

    static class ViewHolder {
        ImageView ivPhoto;
    }
}
