package com.dnk.fallingmyo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DashboardActivity extends ActionBarActivity {

    private String accountID;

    private Firebase authorizedPatientReference;

    private String patientKey;

    private Patient patient;

    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent backgroundServiceIntent = new Intent(this, BackgroundService.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            accountID = extras.getString("accountID");
            backgroundServiceIntent.putExtra("accountID", accountID);
        }
        DashboardActivity.this.startService(backgroundServiceIntent);

        initializeViews();
        initializeFirebaseReferences(this);

        authorizedPatientReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                HashMap<String, Object> authorizedPatientsData = FirebaseCommunicator.getData(snapshot);

                Set<String> patientKeys = authorizedPatientsData.keySet();

                for (String key : patientKeys) {
                    HashMap<String, Object> patientData = FirebaseCommunicator.getDataSubset(authorizedPatientsData, key);
                    if (Patient.getAccountID(patientData).equals(accountID)) {
                        patientKey = key;
                        patient = new Patient(patientData);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                errorMessageTextView.setText(firebaseError.getMessage());
            }
        });
    }

    private void initializeViews() {
        errorMessageTextView = (TextView) findViewById(R.id.errorMessageTextView);
    }

    private void initializeFirebaseReferences(Context context) {
        Firebase.setAndroidContext(context);
        authorizedPatientReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void emergencyHelp(View view) {
        patient.setEmergency(true);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void clickedSpeedDial(View view) {
        //TODO: call number in phone
    }

    public void requestMedications(View view) {
        patient.setAssistanceRequired(Assistance.Medication);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void requestFood(View view) {
        patient.setAssistanceRequired(Assistance.Food);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void requestWashroom(View view) {
        patient.setAssistanceRequired(Assistance.Washroom);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void requestLinens(View view) {
        patient.setAssistanceRequired(Assistance.Linens);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
}