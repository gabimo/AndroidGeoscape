package com.lawnscape;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public static JobListFragment newInstance(String param1) {
        JobListFragment fragment = new JobListFragment();
        Bundle args = new Bundle();
        args.putString(jobList, param1);
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
            myListRef = database.getReference("Jobs");
            jobsToFetch = null;
            myListRef.addValueEventListener(
                    new JobListVEListener(getContext(), allPostDetailsList, jobsAdapter));
        }else {
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
        }

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
