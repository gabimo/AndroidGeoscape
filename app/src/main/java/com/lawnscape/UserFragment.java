package com.lawnscape;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class UserFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private UserListAdapter userAdapter;
    private ListView userListView;
    private ArrayList<User> usersList;
    private ArrayList<String> useridList;
    private FirebaseDatabase database;
    private static FirebaseUser currentUser;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserFragment() {
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Set the adapter
        final Context context = view.getContext();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            usersList = new ArrayList<User>();
            useridList = new ArrayList<String>();
            database = FirebaseDatabase.getInstance();
            DatabaseReference myChatsRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
            userListView = (ListView) view;
            userAdapter = new UserListAdapter(context, usersList);
            //Get the users the current user has chat messages with
            myChatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference myUserRef = database.getReference("Users");
                    useridList.clear();
                    for (DataSnapshot curUserid : dataSnapshot.getChildren()) {
                        useridList.add(curUserid.getKey().toString());
                    }
                    //Causes the listview to update with a list of user objects using UserListAdapter
                    myUserRef.addValueEventListener(
                            new UserListVEListener(context, usersList, useridList, userAdapter));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            userListView.setAdapter(userAdapter);

            //This handles clicks on individual user items from the list
            // and bring you to a job specific chat page with the user
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    User selectedUser = (User) userAdapter.getItem(position);
                    Intent chatIntent = new Intent(context, ChatActivity.class);
                    chatIntent.putExtra("otherid", selectedUser.getUserid());
                    startActivity(chatIntent);
                }
            });
            // Hold down on a user in the chat list to get a popup
            userListView.setOnItemLongClickListener(longClickListener);
        }
        return view;
    }
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                       long id) {
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(getContext(), view);
            popup.getMenuInflater().inflate(R.menu.popup_user_menu, popup.getMenu());
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    final User selectedUser = (User) userAdapter.getItem(position);
                    switch (item.getItemId()){
                        case R.id.longclickDeleteChat:
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //remove the chat from the list of all chats for both users with a listener
                            DatabaseReference myChatidRef = database.getReference("Users").child(currentUser.getUid().toString()).child("chatids");
                            //doesnt delete the actual chat log ;)
                            myChatidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.child(selectedUser.getUserid()).getRef().removeValue();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {  }
                            });
                            myChatidRef = database.getReference("Users").child(selectedUser.getUserid()).child("chatids");
                            myChatidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.child(currentUser.getUid()).getRef().removeValue();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {  }
                            });
                            usersList.remove(selectedUser);
                            userAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.longclickViewProfile:
                            Intent viewProfileIntent = new Intent(getContext(), ViewUserProfileActivity.class);
                            viewProfileIntent.putExtra("UserID", selectedUser.getUserid());
                            startActivity(viewProfileIntent);
                            return true;
                    }
                    return true;
                }
            });
            popup.show();//showing popup menu
            return true;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(User user);
    }
}
