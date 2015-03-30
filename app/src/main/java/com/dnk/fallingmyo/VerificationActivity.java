package com.dnk.fallingmyo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class VerificationActivity extends ActionBarActivity {

    private final String UNVALIDATED_ACCOUNT_MESSAGE = "Your account has not yet been validated by " +
            "hospital personnel. Please try again later.";
    private VerificationActivity me = this;
    private Firebase authorizedPatientReference;
    private String accountID;
    private TextView verificationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        initializeViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            accountID = extras.getString("accountID");
            checkIdAndLaunchActivity(this);
        }
    }

    private void initializeViews() {
        verificationTextView = (TextView) findViewById(R.id.verificationTextView);
    }

    public void checkIdAndLaunchActivity(Context context) {
        initializeFirebaseReferences(context);

        authorizedPatientReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                ArrayList<Object> authorizedPatientList = FirebaseCommunicator.getObjectList(snapshot);

                for (int i = 0; i < authorizedPatientList.size(); i++) {

                    HashMap<String, Object> patientFirebaseData = FirebaseCommunicator.getObjectFromList(i, authorizedPatientList);
                    Patient patient = new Patient(patientFirebaseData);

                    if (patient.verify(accountID)) {
                        Intent intent = new Intent(me, DashboardActivity.class);
                        intent.putExtra("accountID", accountID);
                        startActivity(intent);
                        return;
                    }
                }

                verificationTextView.setText(UNVALIDATED_ACCOUNT_MESSAGE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                verificationTextView.setText(firebaseError.getMessage());
            }
        });
    }

    private void initializeFirebaseReferences(Context context) {
        Firebase.setAndroidContext(context);
        authorizedPatientReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_middle_man, menu);
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
}
