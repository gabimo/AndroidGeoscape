package com.lawnscape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/19/2017.
 */

public class JobPostListAdapter extends BaseAdapter {
    private ArrayList<Job> jobPostDetails;
    private LayoutInflater layoutInflater;

    public JobPostListAdapter(Context aContext, ArrayList<Job> listData) {
        this.jobPostDetails = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return jobPostDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return jobPostDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_job_post, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.tvPostLayoutTitle);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.tvPostLayoutDescription);
            holder.reportedDateView = (TextView) convertView.findViewById(R.id.tvPostLayoutLocation);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headlineView.setText(jobPostDetails.get(position).getTitle());
        holder.reporterNameView.setText(jobPostDetails.get(position).getDescription());
        holder.reportedDateView.setText(jobPostDetails.get(position).getLocation());
        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView reporterNameView;
        TextView reportedDateView;
    }
}
