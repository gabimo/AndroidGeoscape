package com.lawnscape.VElisteners;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lawnscape.adapters.ChatMessageAdapter;
import com.lawnscape.classes.ChatMessage;

import java.util.ArrayList;

public class ChatMessageListVEListener implements ValueEventListener {
    private final Context thisContext;
    private final ChatMessageAdapter messageAdapter;
    private final ArrayList<ChatMessage> messageList;

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