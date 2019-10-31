
package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback,GoogleMap.OnMapClickListener {

    GoogleMap gmap;
    Marker marker;
    Double lat, lng;
    int count = 0;
    private Polyline currentPolyline;
    MarkerOptions place1, place2;
    FusedLocationProviderClient fusedLocationProviderClient;
    BitmapDescriptor bitmapDescriptor1, bitmapDescriptor2;
    DatabaseReference check;
    Boolean startService=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseReference mDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Drivers");
        check=mDatabaseReference.push();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        bitmapDescriptor1=bitmapDescriptorFromVector(MainActivity.this, R.drawable.ic_directions_bus_black_24dp);
        bitmapDescriptor2=bitmapDescriptorFromVector(MainActivity.this, R.drawable.ic_directions_bus_magenta_24dp);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    LocationCallback locationCallback=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(place1!=null&&place2!=null){
                gmap.addPolyline(new PolylineOptions().add(place1.getPosition()).add(place2.getPosition())
                        .width(18).color(Color.MAGENTA));
                if(marker!=null)
                    marker.remove();
                marker=gmap.addMarker(new MarkerOptions()
                        .position(place1.getPosition())
                        .icon(bitmapDescriptor1)
                        .title("Current Position"));
                marker.showInfoWindow();
            }
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                lat=location.getLatitude();
                lng=location.getLongitude();
                check.child("lat").setValue(lat);
                check.child("long").setValue(lng);
                if(startService){
                    startService=false;
                    Intent intent = new Intent(getApplicationContext(), myService.class);
                    intent.putExtra("key", check.getKey());
                    startService(intent);
                }
                if(place1==null) {
                    place1 = new MarkerOptions().position(new LatLng(lat, lng));
                    gmap.addMarker(new MarkerOptions()
                            .position(place1.getPosition())
                            .icon(bitmapDescriptor2)
                            .title("Started From")).showInfoWindow();
                }
                else if(place2==null) {
                    place2 = new MarkerOptions().position(new LatLng(lat, lng));
                }
                else{
                    place1=place2;
                    place2=new MarkerOptions().position(new LatLng(lat, lng));
                }
                if(gmap.getCameraPosition().zoom>15);
                else
                    gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng),15));
            }
    }
    };
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;
        gmap.setOnMapClickListener(this);
        LocationRequest mLocationRequest = new LocationRequest();
        //xs
        mLocationRequest.setInterval(1200); // two minute interval
        mLocationRequest.setFastestInterval(1200);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }
    @Override
    public void onMapClick(LatLng latLng) {
        /*gmap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.ic_person_pin_black_24dp))
                .title("You are Here")).showInfoWindow();
        if(count==2){
            Toast.makeText(getApplicationContext(), "No more", Toast.LENGTH_SHORT).show();
             new FetchURL(MainActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
            if (currentPolyline != null)
                currentPolyline.remove();

            currentPolyline=gmap.addPolyline(new PolylineOptions().add(place1.getPosition()).add(place2.getPosition())
                    .width(5).color(Color.BLUE));
            return;
        }
        count++;
        if(place1==null){
            place1=new MarkerOptions().position(latLng);
            gmap.addMarker(place1);
        }
        else{
            place2=new MarkerOptions().position(latLng);
            gmap.addMarker(place2);
        }*/
    }
//hua kya?
       private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.Direction_map_api_key);
        return url;
    }
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = gmap.addPolyline((PolylineOptions) values[0]);
        currentPolyline.setColor(R.color.colorPrimaryDark);
    }
}

