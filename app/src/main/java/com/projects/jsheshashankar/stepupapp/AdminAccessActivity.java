package com.projects.jsheshashankar.stepupapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAccessActivity extends AppCompatActivity {
    private ArrayAdapter<String> teamAdapter;
    private Spinner teamText;
    private Button showTeamListButton;
    private String teamName;
    private FirebaseFirestore firebaseFirestore;
    private List<String> employeeNames;
    private List<String> employeeHobbies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_access);
        teamText = findViewById(R.id.adminAccessTeamSelectId);
        showTeamListButton = findViewById(R.id.adminAccessSubmitBtnId);
        employeeNames = new ArrayList<String>();
        employeeHobbies = new ArrayList<String>();
        teamAdapter  = new ArrayAdapter<String>(AdminAccessActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.teamNames));

        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamText.setAdapter(teamAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
        showTeamListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teamName = teamText.getSelectedItem().toString();
                if(teamName!=null) {
                    findNamesAndHobbiesForTeam(teamName);
                }
            }
        });
    }
    private void findNamesAndHobbiesForTeam(String teamName)
    {
        firebaseFirestore.collection(teamName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> employeeDocuments = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot : employeeDocuments)
                {
                    if(documentSnapshot.getId().contains("@manh.com"))
                    {
                        if(documentSnapshot.getString("Employee Name")!=null)
                        {
                            employeeNames.add(documentSnapshot.getString("Employee Name"));

                        }
                        else
                        {
                            employeeNames.add("null");
                        }
                        if(documentSnapshot.getString("Hobbies")!=null)
                        {
                            employeeHobbies.add(documentSnapshot.getString("Hobbies"));
                        }
                        else
                        {
                            employeeHobbies.add("No Hobbies specified");
                        }

                    }
                }


            }
        });
        initRecyclerView();
    }
    public void initRecyclerView()
    {
        RecyclerView recyclerView = findViewById(R.id.teamRecyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(employeeNames, employeeHobbies, AdminAccessActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdminAccessActivity.this));
    }
}
