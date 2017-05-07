package com.lawnscape.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.lawnscape.activities.LoginActivity;
import com.lawnscape.activities.MapJobsActivity;
import com.lawnscape.activities.PostJobActivity;
import com.lawnscape.activities.SearchActivity;
import com.lawnscape.activities.ViewJobsListsActivity;
import com.lawnscape.activities.ViewProfileActivity;
import com.lawnscape.adapters.UserListAdapter;
import com.lawnscape.classes.User;
import com.lawnscape.R;
import com.lawnscape.VElisteners.UserListVEListener;

import java.util.ArrayList;

public class ChatListFragment extends Fragment {

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
    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                    ChatFragment chatFrag = new ChatFragment();
                    User selectedUser = userAdapter.getItem(position);

                    Bundle args = new Bundle();
                    args.putString("otherid", selectedUser.getUserid());
                    chatFrag.setArguments(args);

                    FragmentManager fragmentManager = getHostFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.chatsFrameLayout, chatFrag, selectedUser.getUserid());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
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
                            Intent viewProfileIntent = new Intent(getContext(), ViewProfileActivity.class);
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
    public FragmentManager getHostFragmentManager() {
        FragmentManager fm = getFragmentManager();
        if (fm == null && isAdded()) {
            fm = ((AppCompatActivity)getActivity()).getSupportFragmentManager();
        }
        return fm;
    }




    @Override
    public void  onCreateOptionsMenu( Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_posts, menu);
        //WHile on the chat list activity replace the chat list icon with a link to view jobs
        menu.findItem(R.id.viewPostsMenuAllChats).setIcon(R.drawable.view_list_icon);
        menu.findItem(R.id.viewPostsMenuPostJob).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getHostFragmentManager();
                if(fm.getBackStackEntryCount()>0) {
                    fm.popBackStack();
                }else {
                    if(getActivity().isTaskRoot()) {
                        Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
                        upIntent.putExtra("View", "all");
                        if (upIntent != null && NavUtils.shouldUpRecreateTask(getActivity(), upIntent)) {
                            TaskStackBuilder builder = TaskStackBuilder.create(getContext());
                            builder.addNextIntentWithParentStack(upIntent);
                            builder.startActivities();
                        } else {
                            if (upIntent != null) {
                                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                this.startActivity(upIntent);
                            } else {
                                upIntent = new Intent(getContext(), ViewJobsListsActivity.class);
                                upIntent.putExtra("View", "all");
                                startActivity(upIntent);
                            }
                        }
                    }
                    getActivity().finish();
                }
                return true;
            case R.id.viewPostsMenuPostJob:
                startActivity(new Intent(getContext(), PostJobActivity.class));
                getActivity().finish();
                return true;
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent(getContext(), ViewProfileActivity.class));
                return true;
            case R.id.viewPostsMenuMyJobs:
                intent = new Intent(getContext(), ViewJobsListsActivity.class);
                intent.putExtra("View","myjobs");
                startActivity(intent);
                getActivity().finish();
                return true;
            case R.id.viewPostsMenuSearch:
                intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.viewPostsMenuJobsMap:
                intent = new Intent(getContext(), MapJobsActivity.class);
                startActivity(intent);
                return true;
            case R.id.viewPostsMenuSignOut:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                return true;
        }
        return false;
    }
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
