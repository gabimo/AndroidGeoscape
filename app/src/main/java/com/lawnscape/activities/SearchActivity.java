package com.lawnscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.lawnscape.adapters.JobListAdapter;
import com.lawnscape.classes.Job;
import com.lawnscape.R;
import com.lawnscape.adapters.SearchTextWatcher;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private JobListAdapter jobsAdapter;

    private EditText searchBar;
    private SearchTextWatcher searchTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //make sure user is logged in and has an account
        searchBar = (EditText) findViewById(R.id.etSearchBar);
        //Gonna hold all the jobs, must init for adaptor
        ArrayList<Job> searchResults = new ArrayList<>();
        //Find the listview widget and set up a connection to our ArrayList
        ListView searchResultsView = (ListView) findViewById(R.id.lvSearchResults);
        jobsAdapter = new JobListAdapter(this, searchResults);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you MUST call jobsAdaptor.notifyDataSetChanged(); to update the listview
        searchResultsView.setAdapter(jobsAdapter);
        searchTextWatcher = new SearchTextWatcher(this, searchResults,jobsAdapter);
        //This handles clicks on individual job items from the list
        // and bring you to a job specific page with details
        searchResultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Job selectedJob = jobsAdapter.getItem(position);
                Intent singleJobViewIntent = new Intent(SearchActivity.this, ViewSingleJobActivity.class);
                singleJobViewIntent.putExtra("Job",selectedJob);
                startActivity(singleJobViewIntent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("View", "all");
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder builder = TaskStackBuilder.create(this);
                    builder.addNextIntentWithParentStack(upIntent);
                    builder.startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    this.startActivity(upIntent);
                    this.finish();
                }
                return true;
        }
        return false;
    }
}
