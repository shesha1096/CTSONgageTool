package com.projects.jsheshashankar.stepupapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SubmitActivity extends AppCompatActivity {
    private Bundle extras;
    private ImageView image;
    private Button uploadButton;
    private Button browseButton;
    private String emailId;
    private String teamName;
    private static final int PICK_IMAGE = 100;
    private Uri imageUrl;
    private StorageReference mStorageRef;
    private EditText employeeName;
    private EditText employeeId;
    private String employeeNameString;
    private String employeeIdString;
    private FirebaseFirestore firebaseFirestore;
    private Map<String,String> userInfoMap;
    private Button signOutButton;
    private EditText numberOfSteps;
    private String numberOfStepsString;
    private ImageView teamLogo;
    private Button empDate;
    private String empDateString;
    private DatePickerDialog datePickerDialog;
    private int year;
    private int month;
    private int dayOfMonth;
    private TextView empDateDisplay;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        extras = getIntent().getExtras();
        teamName = extras.getString("Team Name");
        image = (ImageView) findViewById(R.id.imageId);
        uploadButton = (Button) findViewById(R.id.uploadBtnID);
        browseButton = (Button) findViewById(R.id.chooseBtnID);
        mStorageRef = FirebaseStorage.getInstance().getReference(teamName);
        employeeName = (EditText) findViewById(R.id.employeeNameId);
        employeeId = (EditText) findViewById(R.id.employeeID);
        numberOfSteps = (EditText) findViewById(R.id.employeeStepsId);
        signOutButton = (Button) findViewById(R.id.signOutBtnId);
        teamLogo = (ImageView) findViewById(R.id.teamLogoId);
        empDate = (Button) findViewById(R.id.empDateId);
        empDateDisplay = (TextView) findViewById(R.id.empDisplayDateId);
        firebaseFirestore = FirebaseFirestore.getInstance();
        determineUserNameandEmployeeId(teamName);
        determineTeamLogo(teamName);
        numberOfSteps.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SubmitActivity.this,MainActivity.class));

            }
        });
        empDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                empDateString = "";
                calendar  = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(SubmitActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        empDateDisplay.setText(day+"/"+(month+1)+"/"+year);
                        empDateString+=day+"/"+(month+1)+"/"+year;




                    }
                },year,month,dayOfMonth);
                datePickerDialog.show();

            }
        });
        userInfoMap = new HashMap<String,String>();
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employeeNameString = employeeName.getText().toString();
                employeeIdString = employeeId.getText().toString();
                numberOfStepsString = numberOfSteps.getText().toString();
                if(employeeNameString.equals("") || employeeIdString.equals("") || numberOfStepsString.equals(""))
                {
                    Toast.makeText(SubmitActivity.this,"Please enter valid details",Toast.LENGTH_SHORT).show();
                }
                else {
                    openGallery();
                }
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUrl==null)
                {
                    Toast.makeText(SubmitActivity.this,"Please upload a photo",Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadFile();
                    addItemToSheet(employeeIdString,employeeNameString,numberOfStepsString);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 4000ms
                            logoutUser();

                        }
                    }, 4000);



                }
            }
        });


    }
    public void openGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }
    private String getFileExtension(Uri uri)
    {
    ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    public void uploadFile()
    {
        if(imageUrl!=null)
        {
            StorageReference storageReference = mStorageRef.child(employeeNameString+System.currentTimeMillis()+
            "."+getFileExtension(imageUrl));
            storageReference.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 500);
                    Toast.makeText(SubmitActivity.this,"Upload Successful",Toast.LENGTH_SHORT).show();
                    Upload upload = new Upload(employeeNameString,imageUrl.toString());


                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SubmitActivity.this,"Could not upload image",Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());


                        }
                    });
            userInfoMap.put("Employee Name",employeeNameString);
            userInfoMap.put("Employee ID",employeeIdString);
            userInfoMap.put("Image URL",imageUrl.toString());
            userInfoMap.put("Steps",numberOfStepsString);
            firebaseFirestore.collection(teamName).document(employeeNameString).collection(""+System.currentTimeMillis()).document(employeeNameString+System.currentTimeMillis()).set(userInfoMap, SetOptions.merge()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SubmitActivity.this,"Some Error Occured",Toast.LENGTH_SHORT).show();

                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SubmitActivity.this,"Data Successfully Saved",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

        else
        {
            Toast.makeText(SubmitActivity.this,"No Image Selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == RESULT_OK && requestCode == PICK_IMAGE)
    {
        imageUrl = data.getData();
        image.setImageURI(imageUrl);
    }
    }

    private void   addItemToSheet(final String employeeIdString, final String employeeNameString, final String numberOfStepsString) {


        final ProgressDialog loading = ProgressDialog.show(SubmitActivity.this,"Saving Data","Please wait");




        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbxSN969xRg9u61efI83mjqxJ7rSZ-CkdMxycCtjIjcGk0gyy09N/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();

                        Toast.makeText(SubmitActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d("Response",response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action","addRow");
                parmas.put("employeeId",employeeIdString);
                parmas.put("employeeName",employeeNameString);
                parmas.put("teamName",teamName);
                parmas.put("steps",numberOfStepsString);
                if(empDateString.equals("")) {
                    parmas.put("date", "NULL");
                }
                else
                {
                    parmas.put("date",empDateString);
                }


                return parmas;
            }
        };
        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);


    }
    public void determineTeamLogo(String teamName)
    {
        switch(teamName)
        {
            case "CTSO Eagles":
                teamLogo.setImageResource(R.drawable.eagles);
                break;
            case "Gully Boys":
                teamLogo.setImageResource(R.drawable.gully_boys);
                break;
            case "A Team":
                teamLogo.setImageResource(R.drawable.a_team);
                break;
            case "Goal Digger":
                teamLogo.setImageResource(R.drawable.goal_digger);
                break;
            case "Rampage":
                teamLogo.setImageResource(R.drawable.rampage);
                break;
            case "Professional Pirates":
                teamLogo.setImageResource(R.drawable.professional_pirates);
                break;
            case "TrailBlazers":
                teamLogo.setImageResource(R.drawable.trailblazers);
                break;
                default:
                    teamLogo.setImageResource(R.drawable.ctso_ngage);
        }
    }
    public void logoutUser()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SubmitActivity.this);
        builder1.setMessage("Data Successfully Saved, you will be logged out.");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        dialog.cancel();
                        Intent loginIntent = new Intent(SubmitActivity.this,MainActivity.class);
                        startActivity(loginIntent);
                    }
                });



        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public void determineUserNameandEmployeeId(String teamName)
    {
        firebaseFirestore.collection(teamName).document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        employeeName.setText(documentSnapshot.getString("Employee Name"));
                        employeeId.setText(documentSnapshot.getString("Employee ID"));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        employeeName.setText("");
                        employeeId.setText("");

                    }
                });

    }

    @Override
    public void onBackPressed() {

    }
}
