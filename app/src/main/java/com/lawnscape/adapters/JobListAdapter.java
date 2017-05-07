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
import com.lawnscape.classes.Job;
import com.lawnscape.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/19/2017.
 *
 * Simple class, specifically made for the job object
 *
 *  Interacts well with JobListVEListener
 *
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
        //This makes ListView the same as RecycleView because it reduces calls
        //to findViewById and allows us to keep the views fresh and clean
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_job_post, null);
            holder = new ViewHolder();
            holder.ivJobPic = (ImageView) convertView.findViewById(R.id.ivPostLayoutThumbnail);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvPostLayoutTitle);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvPostLayoutDescription);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvPostLayoutLocation);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvPostLayoutDate);
            holder.tvCategory = (TextView) convertView.findViewById(R.id.tvPostLayoutCategory);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //The ImageView is usually dirty, this makes the UI better
        holder.ivJobPic.setImageDrawable(null);
        //This finds the photo data by the job id from firebase storage
        StorageReference pathReference = storage.getReference().child("jobphotos").child(jobPostDetails.get(position).getPostid()).child("mainphoto");
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Pass it to Picasso to download, show in ImageView and Picasso handles caching
                Picasso.with(ctx).load(uri.toString()).into(holder.ivJobPic);
            }
        });
        //Set the rest of the view
        holder.tvTitle.setText(jobPostDetails.get(position).getTitle());
        holder.tvDescription.setText(jobPostDetails.get(position).getDescription());
        holder.tvLocation.setText(jobPostDetails.get(position).getLocation());
        holder.tvDate.setText(jobPostDetails.get(position).getDate());
        holder.tvCategory.setText(jobPostDetails.get(position).getCategory());
        return convertView;
    }
    //Represents all the widgets in a layout resouce file
    static class ViewHolder {
        ImageView ivJobPic;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvLocation;
        TextView tvDate;
        TextView tvCategory;
    }
}
