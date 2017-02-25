package com.lawnscape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/24/2017.
 */

public class UserListAdapter extends BaseAdapter {
    private ArrayList<User> usersList;
    private LayoutInflater layoutInflater;

    public UserListAdapter(Context aContext, ArrayList<User> listData) {
        this.usersList = listData;
        layoutInflater = LayoutInflater.from(aContext);
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
        UserListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_user_list_item, null);
            holder = new UserListAdapter.ViewHolder();
            holder.tvLeft = (TextView) convertView.findViewById(R.id.tvUserListItemLeft);
            holder.tvRight = (TextView) convertView.findViewById(R.id.tvUserListItemRight);
            convertView.setTag(holder);
        } else {
            holder = (UserListAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvLeft.setText(usersList.get(position).getName());
        holder.tvRight.setText(usersList.get(position).getLocation());
        return convertView;
    }

    static class ViewHolder {
        TextView tvLeft;
        TextView tvRight;
    }
}
