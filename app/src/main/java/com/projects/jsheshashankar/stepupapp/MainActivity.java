package com.projects.jsheshashankar.stepupapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.FocusFinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText teamName;
    private EditText teamPassword;
    private String teamNameString;
    private String teamNamePassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button signUpBtn;
    private FirebaseFirestore firebaseFirestore;
    private String team;
    private String[] teamNames = new String[]{"CTSO Eagles","Gully Boys","A Team","Rampage","Goal Digger",
                                            "Professional Pirates","TrailBlazers"};
    private FirebaseUser user;
    private  String teamNameFromDb ;
    private int found ;
    private ProgressDialog loading;
    private TextView privatePolicy;
    private Button adminAcessBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.loginButton);
        teamName = (EditText) findViewById(R.id.teamNameInput);
        teamPassword = (EditText) findViewById(R.id.teamNamePassword);
        signUpBtn = (Button) findViewById(R.id.signUpBtnId);
        privatePolicy = (TextView) findViewById(R.id.privacyPolicyId);
        adminAcessBtn = (Button) findViewById(R.id.adminAcessBtnId);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 user = firebaseAuth.getCurrentUser();
                if(user!=null)
                {

                    Toast.makeText(MainActivity.this,"User is signed in",Toast.LENGTH_SHORT).show();

                    Log.d("Email Id",user.getEmail());
                    teamNameFromDb = new String();
                    //teamNameFromDb = determineTeamName();
                    loading = ProgressDialog.show(MainActivity.this,"Logging In","Please wait");
                    determineTeamName();
                }
                else
                {
                    Toast.makeText(MainActivity.this,"User is not signed in",Toast.LENGTH_SHORT).show();
                }


            }
        };
        adminAcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AdminAccessActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teamNameString = teamName.getText().toString();
                teamNamePassword = teamPassword.getText().toString();
                if(teamNameString.equals("") || teamNamePassword.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please enter proper credentials",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.signInWithEmailAndPassword(teamNameString,teamNamePassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                 loading = ProgressDialog.show(MainActivity.this,"Logging In","Please wait");
                                determineTeamName();



                            }
                            else
                            {
                                Toast.makeText(MainActivity.this,"Failed to login, please try again",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });
        privatePolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shesha1096.wixsite.com/website/post/ctso-ngage-tool-one-stop-app-for-all-your-needs"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void determineTeamName()
    {

        for(String team: teamNames) {
            firebaseFirestore.collection(team).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> nameDocuments = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot documentSnapshot : nameDocuments) {
                        if (documentSnapshot.getString("Team Name") != null && documentSnapshot.getString("Email ID").equals(user.getEmail())) {
                            // Log.d("Team Name",documentSnapshot.getString("Team Name"));
                            if(user!=null)
                            {
                                Intent submitIntent = new Intent(MainActivity.this,SubmitActivity.class);
                                submitIntent.putExtra("Team Name", documentSnapshot.getString("Team Name"));
                                startActivity(submitIntent);
                            }
                            loading.dismiss();
                            Intent submitIntent = new Intent(MainActivity.this,SubmitActivity.class);
                            submitIntent.putExtra("Team Name", documentSnapshot.getString("Team Name"));
                            startActivity(submitIntent);
                        }
                    }

                }
            });
        }

    }

    @Override
    public void onBackPressed() {

    }
}
