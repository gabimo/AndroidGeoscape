package com.lawnscape;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends Activity {
    private FirebaseUser currentUser;
    private String otherUserid;
    private FirebaseAuth auth;

    ArrayList<ChatMessage> allMessages;
    ChatMessageAdapter messageAdapter;

    ListView messagesWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        otherUserid = getIntent().getExtras().get("posterid").toString();
        allMessages = new ArrayList<ChatMessage>();
        messagesWindow = (ListView) findViewById(R.id.lvMessageWindow);
        messageAdapter = new ChatMessageAdapter(this,allMessages);
        messagesWindow.setAdapter(messageAdapter);

        final DatabaseReference myChatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
        myChatRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean chatAlreadyExists = false;
                String idToFetch = "";
                for(DataSnapshot chatid: dataSnapshot.getChildren()){
                    if(otherUserid.equals(chatid.getKey().toString())){
                        //Found an existing chat
                        chatAlreadyExists = true;
                        idToFetch = chatid.getValue().toString();
                    }
                }
                if(!chatAlreadyExists){
                    //make new chat and give both users the id in their chat list
                    DatabaseReference allChatsRef = database.getReference("Chats");
                    DatabaseReference newChatid = allChatsRef.push();
                    String newChatID = newChatid.getKey().toString();
                    DatabaseReference addChatIDRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this,otherUserid,newChatID));
                    //now give the other user the chat id
                    addChatIDRef = database.getReference("Users").child(otherUserid).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this,currentUser.getUid().toString(),newChatID));
                }

                //Set up the listview to grab all the messages continuously
                final DatabaseReference chatRef = database.getReference("Chats").child(idToFetch);
                chatRef.addValueEventListener(
                        new ChatMessageListVEListener(ChatActivity.this, messageAdapter, allMessages));
                //Currently selected last message
                messagesWindow.setSelection(messageAdapter.getCount() - 1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void sendMessage(View v){
        //Find the chat
        super.onStart();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference chatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
        //set the message details below
        EditText messageText = (EditText) findViewById(R.id.etEnterMessage);
        final ChatMessage chatMessage = new ChatMessage();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        chatMessage.setDate(sdf.format(new Date()));
        chatMessage.setTextMsg(messageText.getText().toString());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thechatid = dataSnapshot.child(otherUserid).getValue().toString();
                DatabaseReference ourChatRef = database.getReference("Chats").child(thechatid).getRef().push();
                ourChatRef.getRef().setValue(chatMessage);
                messageAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
}
