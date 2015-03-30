package com.dnk.fallingmyo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private static final String MYO = "MyoStatus";
    private static final String ACCEL = "AccelValue";
    private static final String GYRO = "GyroVal";
    private Firebase authorizedPatientReference;
    private String accountID;
    private String patientKey;
    private Patient patient;
    private boolean startCheckingMyo = false;
    private boolean startCheckingGyro = false;

    private double accelVal;

    private int sampleCount = 0;
    private int ignoreCount = 0;

    private boolean isFirstValue = true;
    private Vector3 firstGyroSample;

    private DeviceListener mListener = new AbstractDeviceListener() {

        @Override
        public void onConnect(Myo myo, long timestamp) {
            Log.d(MYO, "Connected to Myo!");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            Log.d(MYO, "Disconnected from Myo!");
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Log.d(MYO, "Just initiated " + pose.toString());
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            if (startCheckingMyo) {
                accelVal = accel.length();
                Log.d(ACCEL, "AccelValue is " + accelVal);
                if (accelVal > 2) {
                    Log.d("onAccelerometerData", "Passed threshold of 2. Val = " + accel.length());
                    if (!startCheckingGyro) {
                        startCheckingGyro = true;
                        sampleCount = 0;
                        ignoreCount = 0;
                    }
                }
            }
        }

        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            if (startCheckingGyro) {
                if (ignoreCount < 150) {
                    ignoreCount++;
                    return;
                }

                if (isFirstValue) {
                    firstGyroSample = gyro;
                    isFirstValue = false;
                } else if (sampleCount < 200) {
                    if (ifVectorsWithinRange(firstGyroSample, gyro, 10)) {
                        sampleCount++;
                        Log.d(GYRO, "Gyro: " + gyro);
                        return;
                    } else {
                        startCheckingGyro = false;
                    }
                } else {
                    setEmergencyTrue();
                    startCheckingGyro = false;
                }
            }
        }
    };

    private void setEmergencyTrue() {
        Log.d("setEmergencyTrue", "Setting emergency to true");
        patient.setEmergency(true);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void createPatient(Context context) {
        Firebase.setAndroidContext(context);
        authorizedPatientReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");

        authorizedPatientReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                HashMap<String, Object> patientsData = FirebaseCommunicator.getData(snapshot);
                Set<String> patientKeys = patientsData.keySet();

                for (String key : patientKeys) {
                    HashMap<String, Object> patientData = FirebaseCommunicator.getDataSubset(patientsData, key);
                    if (Patient.getAccountID(patientData).equals(accountID)) {
                        patientKey = key;
                        BackgroundService.this.patient = new Patient(patientData);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private boolean ifVectorsWithinRange(Vector3 v1, Vector3 v2, double range) {
        Vector3 differenceVector = new Vector3();
        differenceVector.set(v1);
        differenceVector.subtract(v2);
        double x = Math.abs(differenceVector.x());
        double y = Math.abs(differenceVector.x());
        double z = Math.abs(differenceVector.x());
        Log.d("ifVectorsWithinRange", "values: " + x + ", " + y + ", " + z);
        if (x < range && y < range && z < range) {
            return true;
        }
        return false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            accountID = extras.getString("accountID");
        }

        Log.d("onStartCommand", "Entered service. Id is " + accountID);

        createPatient(this);
        Log.d("onStartCommand", "Created patient within service");
        startCheckingMyo = true;
        startCheckingGyro = true;

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        accelVal = 0;
        super.onCreate();
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            Log.d(MYO, "Couldn't initialize Hub");
            stopSelf();
            return;
        }
        hub.setLockingPolicy(Hub.LockingPolicy.NONE);
        hub.addListener(mListener);
        hub.attachToAdjacentMyo();
        Log.d("onCreateService", "Added listeners to the myo");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Hub.getInstance().removeListener(mListener);
        Hub.getInstance().shutdown();
    }
}