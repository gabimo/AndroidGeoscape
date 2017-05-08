package com.lawnscape.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lawnscape.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mellis on 3/23/2017.
 *
 * Used to populate List containers(GridView, Listview, etc.) with ImageViews via Uri List
 *
 */

public class PhotoAdapter extends BaseAdapter {
    private final ArrayList<Uri> photoUriList;
    private final LayoutInflater layoutInflater;
    private final Context context;
    public PhotoAdapter(Context ctx, ArrayList<Uri> pList){
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
        return convertView;
    }
    //Holds the java objects for the widgets in the R.layout.layout_photo_grid_item above
    static class ViewHolder {
        ImageView ivPhoto;
    }
}
