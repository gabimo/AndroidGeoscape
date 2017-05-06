package com.lawnscape;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;


public class ChatFragment extends Fragment {
    private static final String otherid = "otherid";
    private String otherUserid;
    private FirebaseUser currentUser;
    private ArrayList<ChatMessage> allMessages;
    private ChatMessageAdapter messageAdapter;
    private FirebaseDatabase database;
    private EditText messageText;

    private ListView messagesWindow;
    private OnFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String otherUserID) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(otherid, otherUserID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        allMessages = new ArrayList<ChatMessage>();
        if (getArguments() != null) {
            otherUserid = getArguments().getString(otherid);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        rootView.findViewById(R.id.buttonChatMessageSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(null);
            }
        });
        messagesWindow = (ListView) rootView.findViewById(R.id.lvChatMessageView);
        messageAdapter = new ChatMessageAdapter(getContext(),allMessages, currentUser.getUid());
        messagesWindow.setAdapter(messageAdapter);
        //puts a "send" button on the keyboard
        messageText = (EditText) rootView.findViewById(R.id.etChatMessage);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(null);
                    handled = true;
                }
                return handled;
            }
        });
        return rootView;
    }

    public void sendMessage(View v){
        //Find the chat
        super.onStart();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ChatMessage chatMessage = new ChatMessage();

        DatabaseReference chatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids").child(otherUserid);
        //set the message details below
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm");
        chatMessage.setDate(sdf.format(new Date()));
        chatMessage.setTextMsg(messageText.getText().toString());
        chatMessage.setSentByUid(currentUser.getUid());
        messageText.setText("");
        // User listeners to push data to the database
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thechatid = dataSnapshot.getValue().toString();
                DatabaseReference ourChatRef = database.getReference("Chats").child(thechatid).getRef().push();
                chatMessage.setMsgId(ourChatRef.getKey());
                ourChatRef.getRef().setValue(chatMessage);
                messageAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onStart(){
        super.onStart();
         /*
        If there is no chat ID shared by the two users then this listener will make one
        if there is a chat id shared by the two users, this listener will grab its ref
         */
        final DatabaseReference myChatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
        myChatRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //chat id
                String idToFetch = "";
                // The chatid ref will have key-value pairs like "OtherUserID":"chatid"

                if(dataSnapshot.hasChild(getArguments().getString(otherid))){
                    //Found an existing chat
                    idToFetch = dataSnapshot.child(otherUserid).getValue().toString();
                }else{
                    //make new chat and give both users the id in their chat list
                    DatabaseReference allChatsRef = database.getReference("Chats");
                    DatabaseReference newChatid = allChatsRef.push();
                    String newChatID = newChatid.getKey().toString();
                    newChatid.child("Members").addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(getContext(),currentUser.getUid().toString(),"true",false));
                    newChatid.child("Members").addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(getContext(),otherUserid.toString(),"true",false));
                    DatabaseReference addChatIDRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(getContext(),otherUserid,newChatID));
                    /*****      now give the other user the chat id       ******/
                    addChatIDRef = database.getReference("Users").child(otherUserid).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(getContext(),currentUser.getUid().toString(),newChatID));
                    idToFetch = newChatid.getKey().toString();
                }

                //Set up the listview to grab all the messages continuously
                final DatabaseReference chatRef = database.getReference("Chats").child(idToFetch);
                chatRef.addValueEventListener(
                        new ChatMessageListVEListener(getContext(), messageAdapter, allMessages));
                //Currently selected last message
                messagesWindow.setSelection(messageAdapter.getCount() - 1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /********************* MENU STUFF ACTION BAR ********************/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        //getActivity().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getHostFragmentManager().popBackStack();
                return true;
            case R.id.chatMenuViewUserProfile:
                Intent viewProfileIntent = new Intent(getContext(), ViewUserProfileActivity.class);
                viewProfileIntent.putExtra("UserID", otherUserid);
                startActivity(viewProfileIntent);
                return true;
            case R.id.chatMenuMyJobPosts:
                startActivity(new Intent(getContext(), ViewMyPostsActivity.class));
                getActivity().finish();
                return true;
            case R.id.chatMenuMyProfile:
                startActivity( new Intent( getContext(), ViewMyProfileActivity.class));
                return true;
            case R.id.chatMenuSearch:
                Intent SearchIntent = new Intent(getContext(), SearchActivity.class);
                startActivity(SearchIntent);
                return true;
            case R.id.chatMenuSignOut:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                return true;
        }
        return false;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public FragmentManager getHostFragmentManager() {
        FragmentManager fm = getFragmentManager();
        if (fm == null && isAdded()) {
            fm = ((AppCompatActivity)getActivity()).getSupportFragmentManager();
        }
        return fm;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
