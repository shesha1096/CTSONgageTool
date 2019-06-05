package com.projects.jsheshashankar.stepupapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameText;
    private EditText emailText;
    private EditText passwordTxt;
    private EditText employeeId;
    private Spinner teamText;
    private EditText hobbyText;
    private Button signUpBtn;
    private String employeeIdString;
    private String nameString;
    private String emailString;
    private String passwordString;
    private String teamString;
    private String hobbyString;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore firebaseFirestore;
    private Map<String,String> userInfoMap;
    private ArrayAdapter<String> teamAdapter ;
    private ProgressDialog loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        nameText = (EditText) findViewById(R.id.signUpNameId);
        emailText = (EditText) findViewById(R.id.signupEmailId);
        passwordTxt = (EditText) findViewById(R.id.signUpPasswordId);
        teamText = (Spinner) findViewById(R.id.signUpTeamId);
        hobbyText = (EditText) findViewById(R.id.signUpHobbiesId);
        employeeId = (EditText) findViewById(R.id.signUpemployeeId);
        signUpBtn = (Button) findViewById(R.id.confirmSignUpBtnId);
        teamAdapter  = new ArrayAdapter<String>(SignUpActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.teamNames));

        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamText.setAdapter(teamAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null)
                {

                   // Toast.makeText(SignUpActivity.this,"User is signed in",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(SignUpActivity.this,"User is not signed in",Toast.LENGTH_SHORT).show();
                }


            }
        };
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameString = nameText.getText().toString();
                emailString = emailText.getText().toString();
                passwordString = passwordTxt.getText().toString();
                teamString = teamText.getSelectedItem().toString();
                hobbyString = hobbyText.getText().toString();
                employeeIdString = employeeId.getText().toString();
                if(nameString.equals("") || emailString.equals("") || passwordString.equals("")
                        || teamText.getSelectedItem() == null || hobbyString.equals("")
                        || employeeIdString.equals("")) {
                    Toast.makeText(SignUpActivity.this, "Please enter all values properly", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loading = ProgressDialog.show(SignUpActivity.this,"Saving Data","Please wait");

                    mAuth.createUserWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {

                                saveDetails();

                            }

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUpActivity.this,"Failed to Create Account",Toast.LENGTH_SHORT).show();
                                    loading.dismiss();

                                }
                            });
                }


            }
        });

    }
    public void saveDetails()
    {
        userInfoMap = new HashMap<String,String>();
        userInfoMap.put("Employee Name",nameString);
        userInfoMap.put("Email ID",emailString);
        userInfoMap.put("Team Name",teamString);
        userInfoMap.put("Password",passwordString);
        userInfoMap.put("Hobbies",hobbyString);
        userInfoMap.put("Employee ID",employeeIdString);
        firebaseFirestore.collection(teamString).document(emailString).set(userInfoMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    loading.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(SignUpActivity.this,"Account created Successfully",Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(SignUpActivity.this,MainActivity.class);
                    startActivity(loginIntent);
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                    }
                });
    }

    @Override
    public void onBackPressed() {

    }
}
