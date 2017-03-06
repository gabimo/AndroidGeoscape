package com.lawnscape;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SearchActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    ArrayList<Job> searchResults;
    JobListAdapter jobsAdapter;

    EditText searchBar;

    ListView searchResultsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        searchBar = (EditText) findViewById(R.id.etSearchBar);

        currentUser = auth.getCurrentUser();
        //Gonna hold all the jobs, must init for adaptor
        searchResults = new ArrayList<Job>();
        //Put the jobs into the adaptor
        //Find the listview widget and set up a connection to our ArrayList
        searchResultsView = (ListView) findViewById(R.id.lvSearchResults);
        jobsAdapter = new JobListAdapter(SearchActivity.this, searchResults);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you MUST call jobsAdaptor.notifyDataSetChanged(); to update the listview
        searchResultsView.setAdapter(jobsAdapter);
        SearchTextWatcher searchTextWatcher = new SearchTextWatcher(this, searchBar,searchResults,jobsAdapter);
        searchBar.addTextChangedListener(searchTextWatcher);

    }
}
