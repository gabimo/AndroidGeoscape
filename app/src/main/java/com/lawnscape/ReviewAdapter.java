package com.lawnscape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mellis on 4/2/2017.
 */

public class ReviewAdapter extends BaseAdapter {


    private ArrayList<String> reviews;
    private LayoutInflater layoutInflater;

    public ReviewAdapter(Context aContext, ArrayList<String> reviewData) {
        this.reviews = reviewData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public String getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessageAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_chat_message, null);
            holder = new ChatMessageAdapter.ViewHolder();

            convertView.setTag(holder);
        } else {
            holder = (ChatMessageAdapter.ViewHolder) convertView.getTag();
        }


        return convertView;
    }

    static class ViewHolder {
        TextView tvTime;
        TextView tvMessage;
    }
}
