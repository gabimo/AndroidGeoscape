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
 * Created by Mellis on 2/19/2017.
 *
 * Simple class, specifically made for the job object
 *
 *  Interacts well with JobListVEListener
 *                  a class that implements the firebase interace ValueEventListener
 */

public class JobListAdapter extends BaseAdapter {
    private ArrayList<Job> jobPostDetails;
    private LayoutInflater layoutInflater;
    Context ctx;
    // Create a storage reference from our app
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public JobListAdapter(Context aContext, ArrayList<Job> listData) {
        this.jobPostDetails = listData;
        layoutInflater = LayoutInflater.from(aContext);
        ctx = aContext;
    }

    @Override
    public int getCount() {
        return jobPostDetails.size();
    }

    @Override
    public Job getItem(int position) {
        return jobPostDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_job_post, null);
            holder = new ViewHolder();
            holder.ivJobPic = (ImageView) convertView.findViewById(R.id.ivPostLayoutThumbnail);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvPostLayoutTitle);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvPostLayoutDescription);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvPostLayoutLocation);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvPostLayoutDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //This finds the photo data by the job id from firebase storage, nothing is passed around
        StorageReference pathReference = storage.getReference().child("jobphotos").child(jobPostDetails.get(position).getPostid());
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(ctx).load(uri.toString()).into(holder.ivJobPic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        holder.tvTitle.setText(jobPostDetails.get(position).getTitle());
        holder.tvDescription.setText(jobPostDetails.get(position).getDescription());
        holder.tvLocation.setText(jobPostDetails.get(position).getLocation());
        holder.tvDate.setText(jobPostDetails.get(position).getDate());
        return convertView;
    }

    static class ViewHolder {
        ImageView ivJobPic;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvLocation;
        TextView tvDate;
    }
}
