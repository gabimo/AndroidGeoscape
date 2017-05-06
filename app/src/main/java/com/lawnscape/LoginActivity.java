package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                //Find current user User ID string
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in, so they dont need to login again
                    Toast.makeText(LoginActivity.this, "Logged in",
                            Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(LoginActivity.this, ViewJobsListsActivity.class);
                    //necessary for telling the listActivity which jobs to view
                    loginIntent.putExtra("View", "all");
                    startActivity(loginIntent);
                    finish();

                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        //This 'activates' the mAuthListener object that was made in onCreate()
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /*************** LOGIN ***************/
    public void login(final View v) {
        // Find the widgets and grab the user input
        final EditText emailBox = (EditText) findViewById(R.id.etLoginEmail);
        final EditText passBox = (EditText) findViewById(R.id.etLoginPassword);
        final String email = emailBox.getText().toString();
        final String password = passBox.getText().toString();
        //Check the user input
        if (!email.equals("") && !password.equals("")) {
            //This lets the user know that the app is trying to log them in
            v.setEnabled(false);
            emailBox.setEnabled(false);
            passBox.setEnabled(false);
            //Starts a connection with Firebase Authentication to see if a valid user has logged in
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Incorrect Username or Password",
                                        Toast.LENGTH_SHORT).show();
                                v.setEnabled(true);
                                emailBox.setEnabled(true);
                                passBox.setEnabled(true);
                            } else {
                                Intent i = new Intent(LoginActivity.this, ViewMyProfileActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
        }else{
            Toast.makeText(LoginActivity.this, "Incorrect Username or Password",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /*********** SIGN UP ***********/
    public void createAccount(final View v) {
        final EditText emailBox = (EditText) findViewById(R.id.etSignUpEmail);
        final EditText passBox = (EditText) findViewById(R.id.etSignUpPassword);
        final EditText etName = (EditText) findViewById(R.id.etSignUpName);
        final EditText etLocation = (EditText) findViewById(R.id.etSignUpLocation);
        final String newName = etName.getText().toString();
        final String newLoc = etLocation.getText().toString();

        if(!(newName.equals("")||newLoc.equals(""))){
            String email = emailBox.getText().toString();
            String password = passBox.getText().toString();
            if (!email.equals("") && !password.equals("")) {
                v.setEnabled(false);
                emailBox.setEnabled(false);
                passBox.setEnabled(false);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                // Log.d("EVENT", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Could Not Create Account, Please Try Again With Valid Password And Email",
                                            Toast.LENGTH_SHORT).show();
                                    v.setEnabled(true);
                                    emailBox.setEnabled(true);
                                    passBox.setEnabled(true);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Account Created Successfully, Please Wait",
                                            Toast.LENGTH_SHORT).show();
                                    // Set name of user and location
                                    database = FirebaseDatabase.getInstance();
                                    DatabaseReference usersRef = database.getReference("Users").child(currentUser.getUid().toString());
                                    if (!newName.isEmpty() && !newLoc.isEmpty()) {
                                        DatabaseReference newUserRef = usersRef;
                                        newUserRef.setValue(new User(newName, newLoc));
                                    }
                                    startActivity(new Intent(LoginActivity.this, ViewMyProfileActivity.class));
                                    finish();
                                }
                            }
                        });
                }
        }
    }
    /************** Switch to SIGN UP view ****************/
    public void signup(View v){
        //switch the user's screen to the sign up view
        setContentView(R.layout.activity_sign_up);
    }
    /************** Switch to LOG IN view ****************/
    public void backToLogin(View v){
        //switch the user's screen to the login view
        setContentView(R.layout.activity_login);
    }

}
