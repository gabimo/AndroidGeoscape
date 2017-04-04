package com.lawnscape;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/24/2017.
 */

public class UserListAdapter extends BaseAdapter {
    private ArrayList<User> usersList;
    private LayoutInflater layoutInflater;
    private Context ctx;
    // Create a storage reference from our app
    private FirebaseStorage storage = FirebaseStorage.getInstance();

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
        StorageReference pathReference = storage.getReference().child("userprofilephotos").child(usersList.get(position).getUserid());
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(ctx).load(uri.toString()).into(holder.ivPic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Default no image
                holder.ivPic.setImageDrawable(null);
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
