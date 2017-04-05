package com.lawnscape;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Mellis on 3/6/2017.
 */

public class SearchTextWatcher implements TextWatcher {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference jobsRef = database.getReference("Jobs");
    private ArrayList<Job> searchResults;
    private ArrayList<String> resultIDs;
    private EditText searchBar;
    private JobListAdapter jobsAdapter;
    private Context context;
    private String keyword;
    private ValueEventListener searchVeListener;

    SearchTextWatcher(Context ctxt, EditText etSearch, ArrayList<Job> results, JobListAdapter jAdapt){
        context = ctxt;
        searchResults = results;
        searchBar = etSearch;
        jobsAdapter = jAdapt;
        resultIDs = new ArrayList<>();
        searchVeListener = new ValueEventListener() {
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
                jobsRef.addListenerForSingleValueEvent(
                        new JobListVEListener(context, searchResults, jobsAdapter, resultIDs));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }
    @Override
    public void beforeTextChanged( CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        resultIDs.clear();
        keyword = s.toString();
        jobsRef.addListenerForSingleValueEvent(searchVeListener);
        jobsRef.removeEventListener(searchVeListener);
    }
}
