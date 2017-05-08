package com.lawnscape.fragments;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lawnscape.R;
import com.lawnscape.VElisteners.ToggleAddIDVEListener;
import com.lawnscape.VElisteners.UserListVEListener;
import com.lawnscape.activities.ChatActivity;
import com.lawnscape.activities.ViewProfileActivity;
import com.lawnscape.adapters.UserListAdapter;
import com.lawnscape.classes.User;

import java.util.ArrayList;

public class JobRequestFragment extends Fragment {
    private static final String JobID = "JobID";
    private String jobid;

    private ArrayList<User> userList;
    private ArrayList<String> usersToDisplay;
    private UserListAdapter reuesterAdapter;
    public JobRequestFragment() {
        // Required empty public constructor
    }

    public static JobRequestFragment newInstance(String job) {
        JobRequestFragment fragment = new JobRequestFragment();
        Bundle args = new Bundle();
        args.putString(JobID, job);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobid = getArguments().getString(JobID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_job_request, container, false);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        userList = new ArrayList<>();
        usersToDisplay = new ArrayList<>();
        final ListView allRequestersList = (ListView) rootView.findViewById(R.id.lvJobRequesters);
        reuesterAdapter = new UserListAdapter(getContext(), userList);

        DatabaseReference jobRequestersRef = database.getReference("Jobs").child(jobid).child("requesters");
        jobRequestersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userid: dataSnapshot.getChildren()){
                    usersToDisplay.add(userid.getValue().toString());
                }
                allRequestersList.setAdapter(reuesterAdapter);
                //This handles clicks on individual user items from the list
                // and bring you to a job specific chat page with the user
                allRequestersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        User selectedUser = reuesterAdapter.getItem(position);
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("otherid", selectedUser.getUserid());
                        startActivity(chatIntent);
                    }
                });
                // Hold down on a user in the chat list to get a popup
                allRequestersList.setOnItemLongClickListener(longClickListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {    }
        });
        jobRequestersRef = database.getReference("Users");
        jobRequestersRef.addListenerForSingleValueEvent(
                new UserListVEListener(userList, usersToDisplay, reuesterAdapter));
        // need to add some onclick listeners
        return rootView;
    }
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
        long id) {
            //Creating the instance of PopupMenu
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            PopupMenu popup = new PopupMenu(getContext(), view);
            popup.getMenuInflater().inflate(R.menu.popup_assign_jobs, popup.getMenu());
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    DatabaseReference jobRef;
                    final User selectedUser = reuesterAdapter.getItem(position);
                    switch (item.getItemId()){
                        case R.id.longclickAssignJob:
                            //remove the job from the list of all jobs with a listener
                            jobRef = database.getReference("Jobs").child(jobid);
                            jobRef.child("activeworkers").addListenerForSingleValueEvent(new ToggleAddIDVEListener(getContext(),selectedUser.getUserid()));
                            DatabaseReference userRef = database.getReference("Users").child(selectedUser.getUserid());
                            userRef.child("activejobs").addListenerForSingleValueEvent(new ToggleAddIDVEListener(getContext(),jobid));
                            //Also delete the request
                            return true;
                        case R.id.longclickDeleteChat:
                            //remove the job from the list of all jobs with a listener
                            jobRef = database.getReference("Jobs").child(jobid).child("requesters");
                            jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot reqUser: dataSnapshot.getChildren()) {
                                        System.out.println(reqUser.getKey());
                                        if (reqUser.getValue().toString().equals(selectedUser.getUserid())) {
                                            DatabaseReference userReqRef = database.getReference("Users").child(reqUser.getValue().toString()).child("requestedjobs");
                                            userReqRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(getContext(),jobid));
                                            reqUser.getRef().removeValue();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {  }
                            });
                            userList.remove(selectedUser);
                            reuesterAdapter.notifyDataSetChanged();
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

}
