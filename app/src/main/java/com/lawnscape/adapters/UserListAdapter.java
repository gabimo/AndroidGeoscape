package com.lawnscape.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawnscape.classes.User;
import com.lawnscape.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserListAdapter extends BaseAdapter {
    private final ArrayList<User> usersList;
    private final LayoutInflater layoutInflater;
    private final Context ctx;
    // Create a storage reference from our app
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public UserListAdapter(Context aContext, ArrayList<User> listData) {
        this.usersList = listData;
        layoutInflater = LayoutInflater.from(aContext);
        ctx = aContext;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public User getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Displays the view
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_user_list_item, null);
            holder = new UserListAdapter.ViewHolder();
            holder.ivPic = (ImageView) convertView.findViewById(R.id.ivUserListProfilePhoto);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvUserListItemName);
            holder.tvLoc = (TextView) convertView.findViewById(R.id.tvUserListItemLocation);
            convertView.setTag(holder);
        } else {
            holder = (UserListAdapter.ViewHolder) convertView.getTag();
        }
        //The ImageView is usually dirty, this makes the UI better
        holder.ivPic.setImageDrawable(null);
        StorageReference pathReference = storage.getReference().child("userprofilephotos").child(usersList.get(position).getUserid());
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(ctx).load(uri.toString()).into(holder.ivPic);
            }
        });

        holder.tvName.setText(usersList.get(position).getName());
        holder.tvLoc.setText(usersList.get(position).getLocation());
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvLoc;
        ImageView ivPic;
    }
}
