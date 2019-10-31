package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class myService extends Service {
    Intent intent;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent=intent;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String DriverKey=intent.getExtras().getString("key");
        Toast.makeText(getApplicationContext(), DriverKey, Toast.LENGTH_SHORT).show();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Drivers");
        databaseReference.child(DriverKey).setValue(null);
    }
}
