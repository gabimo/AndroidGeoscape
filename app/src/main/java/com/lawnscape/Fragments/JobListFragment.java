package com.lawnscape.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.lawnscape.Activities.ViewJobRequestsActivity;
import com.lawnscape.Activities.ViewSingleJobActivity;
import com.lawnscape.Adapters.JobListAdapter;
import com.lawnscape.ValueEventListeners.JobListVEListener;
import com.lawnscape.POJO.Job;
import com.lawnscape.R;
import com.lawnscape.ValueEventListeners.ToggleAddIDVEListener;

import java.util.ArrayList;

public class JobListFragment extends Fragment {
    private static final String jobList = "jobset";
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private String jobSet;
    private OnFragmentInteractionListener mListener;
    private ArrayList<Job> allPostDetailsList;
    private ArrayList<String> jobsToFetch;
    private JobListAdapter jobsAdapter;
    private DatabaseReference myListRef;

    private ListView allPostsList;

    public JobListFragment() {
        // Required empty public constructor
    }

    public static JobListFragment newInstance(String jobsToGet) {
        JobListFragment fragment = new JobListFragment();
        Bundle args = new Bundle();
        args.putString(jobList, jobsToGet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobSet = getArguments().getString(jobList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        View rootView = inflater.inflate(R.layout.fragment_job_list, container, false);
        allPostDetailsList = new ArrayList<>();
        allPostsList = (ListView) rootView.findViewById(R.id.lvJobListFrag);
        jobsAdapter = new JobListAdapter(getContext(), allPostDetailsList);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you must call jobsAdaptor.notifyDataSetChanged(); to update the listview
        allPostsList.setAdapter(jobsAdapter);

        if(jobSet == null){
            /* ALL JOBS */
            myListRef = database.getReference("Jobs");
            jobsToFetch = null;
            myListRef.addValueEventListener(
                    new JobListVEListener(getContext(), allPostDetailsList, jobsAdapter));
        }else if(jobSet.equals("myjobs")) {
            /* CURRENT USER JOBS */
            DatabaseReference myUserRef = database.getReference("Users").child(currentUser.getUid().toString()).child("jobs");
            //Async add user's own jobs to the list view
            myUserRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Find each job made by the user and add it to the listview
                            // This is done with the ValueEventListener object above
                            ArrayList<String> myJobIDs = new ArrayList<>();
                            DatabaseReference myJobsRef = database.getReference("Jobs");
                            for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                myJobIDs.add(messageSnapshot.getValue().toString());
                                //For each job posted by the user call the custom listener
                            }
                            myJobsRef.addListenerForSingleValueEvent(new JobListVEListener(getContext(),allPostDetailsList,jobsAdapter,myJobIDs));
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

            allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    Job selectedJob = (Job) jobsAdapter.getItem(position);
                    Intent singleJobViewIntent = new Intent(getContext(), ViewSingleJobActivity.class);
                    singleJobViewIntent.putExtra("Job",selectedJob);
                    startActivity(singleJobViewIntent);
                }
            });
            allPostsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                               long id) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    popup.getMenuInflater().inflate(R.menu.popup_post_menu, popup.getMenu());
                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            final Job selectedJob = (Job) jobsAdapter.getItem(position);
                            switch (item.getItemId()){
                                case R.id.longclickDeletePost:
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    //remove the job from the list of all jobs with a listener
                                    DatabaseReference myJobRef = database.getReference("Jobs");
                                    myJobRef.addListenerForSingleValueEvent(
                                            new ToggleAddIDVEListener(getContext(),selectedJob.getPostid()));
                                    //remove the job from the users job list with a listener*
                                    DatabaseReference myUserJobsRef = database.getReference("Users")
                                            .child(currentUser.getUid()).child("jobs");
                                    myUserJobsRef.addListenerForSingleValueEvent(
                                            new ToggleAddIDVEListener(getContext(), selectedJob.getPostid()));
                                    allPostDetailsList.remove(selectedJob);
                                    jobsAdapter.notifyDataSetChanged();
                                    return true;
                                case R.id.longclickAssignJob:
                                    //nothing yet
                                    Intent assignJobIntent = new Intent(getContext(), ViewJobRequestsActivity.class);
                                    assignJobIntent.putExtra("Job", selectedJob);
                                    startActivity(assignJobIntent);
                                    return true;
                            }
                            return true;
                        }
                    });
                    popup.show();//showing popup menu
                    return true;
                }
            });
        }else{
            /* LIST OF PARTICULAR JOBS */
            jobsToFetch = new ArrayList<>();
            myListRef = database.getReference("Users").child(currentUser.getUid().toString()).child(jobSet).getRef();
            myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Find each job made saved by the user
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        //make a list of the user's saved jobs
                        jobsToFetch.add(messageSnapshot.getValue().toString());
                    }
                    DatabaseReference myJobsRef = database.getReference("Jobs");
                    myJobsRef.addListenerForSingleValueEvent(
                            new JobListVEListener(getContext(), allPostDetailsList, jobsAdapter, jobsToFetch));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {/* idk what we would do*/ }
            });
            //Clicking a list item will bring you to a page for that item
            allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    Job selectedJob = jobsAdapter.getItem(position);
                    Intent singleJobViewIntent = new Intent(getContext(), ViewSingleJobActivity.class);
                    //The Job class implements 'Parcelable' in order to be passed as an intent extra
                    singleJobViewIntent.putExtra("Job",selectedJob);
                    startActivity(singleJobViewIntent);
                }
            });
        }

        return rootView;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
