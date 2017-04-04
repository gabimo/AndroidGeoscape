package com.lawnscape;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/23/2017.
 */

public class ChatMessageListVEListener implements ValueEventListener {
    private Context thisContext;
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> messageList;

    public ChatMessageListVEListener(Context aContext, ChatMessageAdapter adapter, ArrayList<ChatMessage> messages){
        thisContext = aContext;
        messageAdapter = adapter;
        messageList = messages;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot){
        //Add all the messages to the array list
        messageList.clear();
        for (DataSnapshot chatNode : dataSnapshot.getChildren()) {
            if(chatNode.hasChild("date")&&chatNode.hasChild("textMsg")) {
                String date = chatNode.child("date").getValue().toString();
                String msg = chatNode.child("textMsg").getValue().toString();
                String sentID = chatNode.child("sentByUid").getValue().toString();
                ChatMessage newMsg = new ChatMessage();
                newMsg.setTextMsg(msg);
                newMsg.setDate(date);
                newMsg.setSentByUid(sentID);
                messageList.add(newMsg);
                messageAdapter.notifyDataSetChanged();
            }
            //Tell the listview adaptor to update the listview based on the ArrayList updates
        }
    }

    @Override
    public void onCancelled(DatabaseError firebaseError) {
        Toast.makeText(thisContext ,"DB ERROR: Could not get jobs",Toast.LENGTH_SHORT).show();
    }
}