package com.example.windows7.sms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double latitude=0,longitude=0;
    public static final int RequestPermissionCode = 1;

    final int SEND_SMS_PERMISSION_REQUEST_CODE=1;

    EditText number;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        number=findViewById(R.id.inputNumber);
        send=findViewById(R.id.buttonSend);

        send.setEnabled(false);
        if(checkPermission(Manifest.permission.SEND_SMS)){
            send.setEnabled(true);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop(){
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
        Log.e("MainActivity","Connection Failed! "+ connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i){
        Log.e("MainActivity","Connection Suspended");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if(ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermission();
        }
        else
        {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location!=null){
                                latitude=location.getLatitude();
                                longitude=location.getLongitude();
                            }
                        }
                    });
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{ACCESS_FINE_LOCATION},RequestPermissionCode);
    }

    public void onSend(View v) {
        String phoneNumber = number.getText().toString();

        if (phoneNumber == null || phoneNumber.length() == 0)
        {
            return;
        }



        String smsMessage="My current location is: Latitude = "+latitude+" & Longitude = "+longitude;

        if(checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber,null,smsMessage,null,null);
            Toast.makeText(this,"Message Sent!",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Permission Denied!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkPermission(String permission)
    {
        int check= ContextCompat.checkSelfPermission(this,permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
}