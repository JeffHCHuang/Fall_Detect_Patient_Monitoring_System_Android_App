package com.dnk.fallingmyo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class LoginActivity extends ActionBarActivity {

    private final String ROOT_FIREBASE_URL = "https://fallingmyo.firebaseIO.com";
    private final String UNAUTHORIZED_FIREBASE_URL = ROOT_FIREBASE_URL + "/Unauthorized";
    private final String UNASSIGNED_ROOM_NUMBER = "";
    private final String REGISTRATION_SUCCESSFUL_MESSAGE = "You are now registered! Please proceed to log in.";
    private LoginActivity me = this;
    private Firebase rootReference;
    private Firebase unauthorizedReference;

    private RelativeLayout newPatientLayout;
    private EditText newPatientNameEditText;
    private EditText newPatientEmailEditText;
    private EditText newPatientPasswordEditText;
    private EditText newPatientPasswordConfirmEditText;
    private TextView registrationMessageTextView;

    private RelativeLayout loginLayout;
    private EditText patientEmailEditText;
    private EditText patientPasswordEditText;
    private TextView loginMessageTextView;

    private boolean isRegisteringNewPatient = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeAllViews();
        initializeFirebaseReferences();
        updateViewVisibility();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    private void initializeAllViews() {
        newPatientLayout = (RelativeLayout) findViewById((R.id.newPatientLayout));
        newPatientNameEditText = (EditText) findViewById(R.id.newPatientNameEditText);
        newPatientEmailEditText = (EditText) findViewById(R.id.newPatientEmailEditText);
        newPatientPasswordEditText = (EditText) findViewById(R.id.newPatientPasswordEditText);
        newPatientPasswordConfirmEditText = (EditText) findViewById(R.id.newPatientPasswordConfirmEditText);
        registrationMessageTextView = (TextView) findViewById(R.id.registrationMessageTextView);

        loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
        patientEmailEditText = (EditText) findViewById(R.id.patientEmailEditText);
        patientPasswordEditText = (EditText) findViewById(R.id.patientPasswordEditText);
        loginMessageTextView = (TextView) findViewById(R.id.loginMessageTextView);
    }

    private void initializeFirebaseReferences() {
        Firebase.setAndroidContext(this);
        rootReference = new Firebase(ROOT_FIREBASE_URL);
        unauthorizedReference = new Firebase(UNAUTHORIZED_FIREBASE_URL);
    }

    private void updateViewVisibility() {
        if (isRegisteringNewPatient) {
            goToRegistrationPage();
        } else {
            goToLoginPage();
        }
    }

    private void goToRegistrationPage() {
        newPatientLayout.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
    }

    private void goToLoginPage() {
        newPatientLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    public void registerNewPatient(View view) {
        try {
            confirmAllFieldsRegistrationFilled();
            confirmPasswordsMatch();

            final String email = getEditTextString(newPatientEmailEditText);
            final String password = getEditTextString(newPatientPasswordEditText);

            rootReference.createUser(email, password,
                    new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            unauthorizedReference = new Firebase(UNAUTHORIZED_FIREBASE_URL);

                            String name = getEditTextString(newPatientNameEditText);
                            String accountID = (String) result.get("uid");

                            Patient newPatient = new Patient(name, accountID, email, UNASSIGNED_ROOM_NUMBER,
                                    false, Assistance.NONE);

                            unauthorizedReference.push().setValue(newPatient);

                            goToLoginPage();
                            displayLoginMessage(REGISTRATION_SUCCESSFUL_MESSAGE);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            displayRegistrationMessage(firebaseError.getMessage());
                        }
                    });
        } catch (Exception exception) {
            displayRegistrationMessage(exception.getMessage());
        }
    }

    private void confirmAllFieldsRegistrationFilled() throws Exception {
        if (isFieldEmpty(newPatientNameEditText) || isFieldEmpty(newPatientEmailEditText) ||
                isFieldEmpty(newPatientPasswordEditText) || isFieldEmpty(newPatientPasswordConfirmEditText)) {
            throw new Exception("Please fill in all the required fields.");
        }
    }

    private boolean isFieldEmpty(EditText editText) {
        return getEditTextString(editText).length() == 0;
    }

    public void confirmPasswordsMatch() throws Exception {
        if (!doPasswordsMatch()) {
            throw new Exception("The passwords do not match.");
        }
    }

    private boolean doPasswordsMatch() {
        return getEditTextString(newPatientPasswordEditText).equals(getEditTextString(newPatientPasswordConfirmEditText));
    }

    private String getEditTextString(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void displayRegistrationMessage(String message) {
        registrationMessageTextView.setText(message);
    }

    public void goToLoginPage(View view) {
        isRegisteringNewPatient = false;
        updateViewVisibility();
    }

    public void goToRegistrationPage(View view) {
        isRegisteringNewPatient = true;
        updateViewVisibility();
    }

    public void loginPatient(View view) {
        try {
            confirmLoginFieldsFilled();
            loginPatient(getEditTextString(patientEmailEditText), getEditTextString(patientPasswordEditText));
        } catch (Exception exception) {
            displayLoginMessage(exception.getMessage());
        }
    }

    private void confirmLoginFieldsFilled() throws Exception {
        if (isFieldEmpty(patientEmailEditText) || isFieldEmpty(patientPasswordEditText)) {
            throw new Exception("Please fill in all fields.");
        }
    }

    private void loginPatient(String patientEmail, String patientPassword) {
        rootReference.authWithPassword(patientEmail, patientPassword,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        String accountID = authData.getUid();
                        Intent middleManIntent = new Intent(me, VerificationActivity.class);
                        middleManIntent.putExtra("accountID", accountID);
                        startActivity(middleManIntent);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        displayLoginMessage(firebaseError.getMessage());
                    }
                });
    }

    private void displayLoginMessage(String message) {
        loginMessageTextView.setText(message);
    }
}
