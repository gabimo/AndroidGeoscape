package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ViewSingleJobActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_job);
        Intent jobIntent = getIntent();
        Job jobPost = jobIntent.getParcelableExtra("Job");
        Toast.makeText(this,jobPost.getTitle(),Toast.LENGTH_SHORT).show();

        TextView tvTitle = (TextView) findViewById(R.id.tvSingleJobTitle);
        TextView tvLoc = (TextView) findViewById(R.id.tvSingleJobLocation);
        TextView tvDesc = (TextView) findViewById(R.id.tvSingleJobDescription);

        tvTitle.setText(jobPost.getTitle());
        tvLoc.setText(jobPost.getLocation());
        tvDesc.setText(jobPost.getDescription());
    }
}
