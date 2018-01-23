package com.example.saunak.uberclone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity implements LocationListener {

    ListView requestListView;


    ArrayList<String> requests;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<Double> requestLatitudes;
    ArrayList<Double> requestLongitudes;
    ArrayList<String> userNames;

    LocationManager locationManager;
    Location lastKnownLocation;

    final static String TAG = ViewRequestActivity.class.getCanonicalName();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        requestListView = findViewById(R.id.requestListView);
        requests = new ArrayList<String>();
        requestLatitudes = new ArrayList<Double>();
        requestLongitudes = new ArrayList<Double>();
        userNames = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, requests);
        requests.clear();
        requests.add("Getting Nearby Requests ... ");
        requestListView.setAdapter(arrayAdapter);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation != null) {
                    updateListView(lastKnownLocation);
                }
            }
        }
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(requestLatitudes.size() > i && requestLongitudes.size() > i && userNames.size() > i){
                    if (lastKnownLocation != null) {
                        Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);
                        intent.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent.putExtra("requestLongitude", requestLongitudes.get(i));
                        intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                        intent.putExtra("username",userNames.get(i));
                        startActivity(intent);
                    }else {
                        Log.i(TAG,"Last known loc is null");
                    }
                }else {
                    Log.i(TAG,"lat or long is small");
                }
            }
        });

    }


    public void updateListView(Location location){
        if(location != null) {

            ParseQuery<ParseObject> nearbyRequestQuery = ParseQuery.getQuery("Request");
            final ParseGeoPoint geoPointDriverLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            nearbyRequestQuery.whereNear("location",geoPointDriverLocation);
            nearbyRequestQuery.whereEqualTo("driverusername",null);
            nearbyRequestQuery.setLimit(10);
            nearbyRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null){
                        requests.clear();
                        requestLatitudes.clear();
                        requestLongitudes.clear();
                        if(objects != null && objects.size() > 0){
                            for(ParseObject obj: objects){
                                ParseGeoPoint requestLocation = obj.getParseGeoPoint("location");
                                if(requestLocation != null) {
                                    userNames.add(obj.getString("username"));
                                    requestLatitudes.add(requestLocation.getLatitude());
                                    requestLongitudes.add(requestLocation.getLongitude());
                                    Double distanceInKm = geoPointDriverLocation.distanceInKilometersTo(obj.getParseGeoPoint("location"));
                                    Double distanceOneDp = (double) Math.round(distanceInKm * 10 / 10);
                                    requests.add(distanceOneDp.toString() + "Kms");
                                }
                            }

                        }else {
                            requests.add("No Active Requests Near by");
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation != null) {
                    updateListView(lastKnownLocation);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateListView(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
