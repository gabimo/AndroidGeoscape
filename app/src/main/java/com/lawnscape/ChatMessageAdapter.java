package com.lawnscape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/23/2017.
 */

public class ChatMessageAdapter extends BaseAdapter {
    private ArrayList<ChatMessage> messages;
    private LayoutInflater layoutInflater;

    public ChatMessageAdapter(Context aContext, ArrayList<ChatMessage> messageData) {
        this.messages = messageData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_chat_message, null);
            holder = new ViewHolder();
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvMessageLeft);
            holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessageRight);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTime.setText(messages.get(position).getDate());
        holder.tvMessage.setText(messages.get(position).getTextMsg());
        return convertView;
    }

    static class ViewHolder {
        TextView tvTime;
        TextView tvMessage;
    }
}
