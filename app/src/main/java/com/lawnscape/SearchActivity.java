package com.lawnscape;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchActivity extends Activity {
    private ArrayList<Job> searchResults;
    private JobListAdapter jobsAdapter;

    private EditText searchBar;
    private ListView searchResultsView;
    private SearchTextWatcher searchTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //make sure user is logged in and has an account
        searchBar = (EditText) findViewById(R.id.etSearchBar);
        //Gonna hold all the jobs, must init for adaptor
        searchResults = new ArrayList<>();
        //Find the listview widget and set up a connection to our ArrayList
        searchResultsView = (ListView) findViewById(R.id.lvSearchResults);
        jobsAdapter = new JobListAdapter(this, searchResults);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you MUST call jobsAdaptor.notifyDataSetChanged(); to update the listview
        searchResultsView.setAdapter(jobsAdapter);
        searchTextWatcher = new SearchTextWatcher(this, searchBar,searchResults,jobsAdapter);
        searchBar.addTextChangedListener(searchTextWatcher);
    }
    @Override
    protected void onStop(){
        super.onStop();
        searchBar.removeTextChangedListener(searchTextWatcher);
    }
    @Override
    protected void onStart(){
        super.onStart();
        searchBar.addTextChangedListener(searchTextWatcher);
    }
}
