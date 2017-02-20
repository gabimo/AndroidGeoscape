package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ViewSingleJobActivity extends Activity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser currentUser;

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

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ViewSingleJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    /************** End LifeCycle ****************/
    public void signout(View v){
        auth.signOut();
    }
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewPostsMenu1:
                startActivity( new Intent( ViewSingleJobActivity.this, ProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu2:
                startActivity(new Intent(ViewSingleJobActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu3:
                startActivity(new Intent(ViewSingleJobActivity.this, ViewAllJobsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu4:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewSingleJobActivity.this, PostJobActivity.class));
        finish();
    }
}
