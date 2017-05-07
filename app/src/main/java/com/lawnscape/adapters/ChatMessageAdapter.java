package com.lawnscape.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lawnscape.classes.ChatMessage;
import com.lawnscape.R;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/23/2017.
 */

public class ChatMessageAdapter extends BaseAdapter {
    private ArrayList<ChatMessage> messages;
    private LayoutInflater layoutInflater;
    private String currentUserID;
    public ChatMessageAdapter(Context aContext, ArrayList<ChatMessage> messageData, String curUserID) {
        this.messages = messageData;
        layoutInflater = LayoutInflater.from(aContext);
        currentUserID = curUserID;
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
        if(currentUserID.equals(messages.get(position).getSentByUid())) {
            holder.tvTime.setText(messages.get(position).getDate());
            holder.tvMessage.setText(messages.get(position).getTextMsg());
        }else {
            holder.tvTime.setText(messages.get(position).getTextMsg());
            holder.tvMessage.setText(messages.get(position).getDate());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTime;
        TextView tvMessage;
    }
}
