package com.example.saunak.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    final static String TAG = MainActivity.class.getSimpleName();
    static Boolean isRider = true;
    Switch userTypeSwitch;
    Button signInButton;
    Intent riderViewIntent;
    Intent driverViewIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userTypeSwitch = findViewById(R.id.isDriverswitch);
        signInButton = findViewById(R.id.signInButton);

        if(ParseUser.getCurrentUser() == null){
            Log.i(TAG,"Null current User");
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null){
                        Log.i(TAG,"Anonymous login Success!!!");
                    }else{
                        Log.i(TAG,"Anonymous login Failed!!!");
                    }
                }
            });
        }else{
            redirectToMainApp(isRider);
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void signIn(View view) {

        if(userTypeSwitch.isChecked()){
            Log.i(TAG,"Logged In as Driver");
            redirectToMainApp(false);
        }else{
            Log.i(TAG,"Logged In as Rider");
            redirectToMainApp(true);
        }
    }

    public void redirectToMainApp(Boolean isRider){
        if(isRider){
            riderViewIntent = new Intent(this,RiderMapsActivity.class);
            startActivity(riderViewIntent);
        }else{
            Log.i(TAG,"Log in as Driver");
            driverViewIntent = new Intent(this,ViewRequestActivity.class);
            startActivity(driverViewIntent);
        }
    }
}
