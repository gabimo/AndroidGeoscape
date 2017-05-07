package com.lawnscape.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lawnscape.ValueEventListeners.JobListVEListener;
import com.lawnscape.POJO.Job;

import java.util.ArrayList;

/**
 * Created by Mellis on 3/6/2017.
 */

public class SearchTextWatcher implements TextWatcher {

    private DatabaseReference database;
    private Query searchRef;
    private ArrayList<Job> searchResults;
    private ArrayList<String> resultIDs;
    private JobListAdapter jobsAdapter;
    private Context context;
    private String keyword;
    private ValueEventListener searchVEListener;
    private int startIndex, endIndex;

    public SearchTextWatcher(Context ctxt, EditText etSearch, ArrayList<Job> results, JobListAdapter jAdapt){

        database = FirebaseDatabase.getInstance().getReference();
        searchRef = database.child("Jobs").orderByKey();
        context = ctxt;
        searchResults = results;
        jobsAdapter = jAdapt;
        resultIDs = new ArrayList<>();

        searchVEListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot job : dataSnapshot.getChildren()) {
                    if (job.hasChild("title") && job.child("title").getValue().toString().contains(keyword)) {
                        resultIDs.add(job.getKey());
                    } else if (job.hasChild("description") && job.child("description").getValue().toString().contains(keyword)) {
                        resultIDs.add(job.getKey());
                    } else if (job.hasChild("location") && job.child("location").getValue().toString().contains(keyword)) {
                        resultIDs.add(job.getKey());
                    }
                }
                searchRef.addListenerForSingleValueEvent(
                        new JobListVEListener(context, searchResults, jobsAdapter, resultIDs));
                searchRef.removeEventListener(searchVEListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                searchRef.removeEventListener(searchVEListener);
            }
        };
    }
    @Override
    public void beforeTextChanged( CharSequence s, int start, int count, int after) {
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        keyword = s.toString();
        resultIDs.clear();
        searchRef.addListenerForSingleValueEvent(searchVEListener);
    }
}
