package com.lawnscape.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lawnscape.R;

import java.util.ArrayList;

// --Commented out by Inspection START (5/7/2017 5:38 PM):
//class ReviewAdapter extends BaseAdapter {
//
//
//    private final ArrayList<String> reviews;
//    private final LayoutInflater layoutInflater;
//
//    public ReviewAdapter(Context aContext, ArrayList<String> reviewData) {
//        this.reviews = reviewData;
//        layoutInflater = LayoutInflater.from(aContext);
//    }
//
//    @Override
//    public int getCount() {
//        return reviews.size();
//    }
//
//    @Override
//    public String getItem(int position) {
//        return reviews.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        if (convertView == null) {
//            convertView = layoutInflater.inflate(R.layout.layout_chat_message, null);
//            holder = new ViewHolder();
//            holder.tvReview = (TextView) convertView.findViewById(R.id.tvMessageLeft);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        holder.tvReview.setText(reviews.get(position));
//
//        return convertView;
//    }
//
//    static class ViewHolder {
//        TextView tvReview;
//    }
//}
// --Commented out by Inspection STOP (5/7/2017 5:38 PM)
