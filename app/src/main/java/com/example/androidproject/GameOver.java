package com.example.androidproject;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;
import android.provider.Settings;
import com.google.android.gms.location.LocationRequest;
import android.location.LocationManager;

public class GameOver extends AppCompatActivity {

    private LinearLayout.LayoutParams buttonLayoutParams;
    public ValueEventListener saveListener;
    private static final String TAG = "ViewDatabase";
    public FirebaseDatabase mDatabase;
    public DatabaseReference usersRef;
    public User someUser;
    public int points;
    public UserLocation userLocation;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    public static String userName;
    public static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        userName = getIntent().getStringExtra(Global_Variable.USER_NAME_FOR_MOVE_INTENT);
        password=getIntent().getStringExtra(Global_Variable.PASSWORD_FOR_MOVE_INTENT);
        points = getIntent().getIntExtra(Global_Variable.POINTS_FOR_MOVE_INTENT,0);

        setContentView(R.layout.activity_game_over);
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.setBackgroundResource(R.drawable.backgroundlogin);
        someUser = new User();
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference(Global_Variable.USERS_TABLE);
        initParamsForButtons();

        userLocation = new UserLocation();
        createMainTitle();
        createYourPoints();
        createButtonHighScore();
        createStartNewGameButton();
    }


    @Override
    public void onStop() {
        super.onStop();
        //DATA BASE:
        usersRef.removeEventListener(saveListener);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
        //DATA BASE:
        saveNewUSerToDB();

    }


    public void saveNewUSerToDB(){
        saveListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                saveNewScore(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        usersRef.addValueEventListener(saveListener);
    }

    public void saveNewScore(DataSnapshot dataSnapshot){
        if (dataSnapshot.getChildrenCount() < Global_Variable.MAX_USERS_ON_HIGH_SCORE ){
            // if the user exist -> cheack if his got better -> and update if he his
            String indexUserExist = checkIfUserExist(dataSnapshot);
            if (indexUserExist.equals(Global_Variable.USER_NOT_EXIST) ){
                writeNewUser(userName, password , userLocation, points);
            }
            else{ //the user is exist
                if ( (Long)dataSnapshot.child(indexUserExist).child("points").getValue() < points) {
                    updateUser(indexUserExist,userLocation,points,dataSnapshot);
                }
            }
        }

        else { // if the DB size == 10:
            String indexUserExist = checkIfUserExist(dataSnapshot);
            //the user not exist -> try to replace with other user
            if (indexUserExist.equals(Global_Variable.USER_NOT_EXIST) ){
                String indexOfUserWithMinPoints = getIndexOfTheUserWithMinPoints(dataSnapshot);
                //if my points bigger then the smallest points - delete the other user and add me insted:
                if (points > (Long)dataSnapshot.child(indexOfUserWithMinPoints).child("points").getValue()) {
                    dataSnapshot.getRef().child(indexOfUserWithMinPoints).removeValue();
                    writeNewUser(userName, password, userLocation, points);
                }
            }
            else{
                //the user exist -> update the points if the points have been bigger
                if (points > (Long)dataSnapshot.child(indexUserExist).child("points").getValue()){
                    updateUser(indexUserExist, userLocation,points,dataSnapshot);
                }
            }
        }

    }

    ///DATA BASE //
    private void writeNewUser(String userName, String UserPassword, UserLocation location, int points) {
        if ( userName==null || UserPassword==null || userName.isEmpty() || UserPassword.isEmpty() ){
            return;
        }
        else {
            String userId = usersRef.push().getKey();
            User user = new User(userName, UserPassword,location,points);
            Log.e(TAG,"new user "+points);
            usersRef.child(userId).setValue(user);
        }
    }

    //TODO: write this when user that already exist try to login and we need to change the userLocation
    private void updateUser(final String userId, UserLocation location, int points, DataSnapshot dataSnapshot){
        dataSnapshot.getRef().child(userId).child("userLocation").setValue(location);
        dataSnapshot.getRef().child(userId).child("points").setValue(points);
    }


    public String checkIfUserExist(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            someUser = postSnapshot.getValue(User.class);
            if (someUser.getUsername().equals(userName) &&
                    someUser.getPassword().equals(password)) {
                return postSnapshot.getKey();
            }
        }
        return Global_Variable.USER_NOT_EXIST;
    }

    public String getIndexOfTheUserWithMinPoints(DataSnapshot dataSnapshot){
        Integer min = null ;
        String userId = Global_Variable.USER_NOT_EXIST;
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            someUser = postSnapshot.getValue(User.class);
            if (min == null){
                min= someUser.getPoints();
            }
            if (someUser.getPoints() < min ) {
                userId = postSnapshot.getKey();
            }
        }
        return userId;
    }


    public void initButtonStyle(Button button){
        button.setLayoutParams(buttonLayoutParams);
        button.setBackgroundResource(R.color.appColor);
        button.setGravity(Gravity.CENTER);
        button.setText(Global_Variable.START_NEW_GAME);
        button.setTextColor(getApplication().getResources().getColor(R.color.white));
    }

    public void initParamsForButtons(){
        //find size of all the screen:
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int halfScreenWidth = (int)(screenWidth *0.5);
        int quarterScreenWidth = (int)(halfScreenWidth * 0.40);
        buttonLayoutParams =
                new LinearLayout.LayoutParams((int)(screenWidth *0.5),screenHeight/20);
        buttonLayoutParams.gravity = Gravity.CENTER;
        buttonLayoutParams.setMargins(0
                ,screenHeight/25
                ,0
                ,screenHeight/25);
    }

    public void createStartNewGameButton(){
        Button loginButton = new Button(this);
        initButtonStyle(loginButton);
        loginButton.setText(Global_Variable.START_NEW_GAME);
        loginButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(loginButton);
    }

    public void createButtonHighScore(){
        Button topHighScoreButton = new Button(this);
        initButtonStyle(topHighScoreButton);
        topHighScoreButton.setText(Global_Variable.TOP_NUMBER_TEXT_ON_BUTTON);
        topHighScoreButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        topHighScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScoreSheetActivity();
            }
        });
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(topHighScoreButton);
    }

    private void openScoreSheetActivity() {
        Intent myIntent = new Intent(GameOver.this, MapsActivity.class);
        finish();
        try {
            startActivity(myIntent);
        }catch (Exception e){
            e.getMessage();
        }
    }

    private void startNewGame(){
        Intent myIntent = new Intent(GameOver.this,
                GameActivity.class);
        GameActivity.myPoints=0 ;
        finish();
        startActivity(myIntent);
    }

    void createMainTitle(){
        TextView titleText = new TextView(this);
        titleText.setText(Global_Variable.GAME_OVER);
        titleText.setTextColor(getResources().getColor(R.color.white));
        titleText.setTextSize(35);
        titleText.setGravity(Gravity.CENTER);
        titleText.setBackgroundResource(R.color.purple);
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(titleText);
    }

    void createYourPoints(){
        TextView someText = new TextView(this);
        someText.setTextColor(getResources().getColor(R.color.white));
        someText.setTextSize(22);
        someText.setGravity(Gravity.CENTER);
        someText.setBackgroundResource(R.color.pink);
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(someText);

        TextView yourPointsText = new TextView(this);
        int pointToPersent = GameActivity.myPoints;
        GameActivity.myPoints=0;
        yourPointsText.setText("You earn: "+ pointToPersent +" Points");
        yourPointsText.setTextColor(getResources().getColor(R.color.white));
        yourPointsText.setTextSize(22);
        yourPointsText.setGravity(Gravity.CENTER);
        yourPointsText.setBackgroundResource(R.color.purple);
        mainActivityLayout.addView(yourPointsText);

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    userLocation.setY(location.getLatitude());
                                    userLocation.setX(location.getLongitude());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }
    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //Location mLastLocation = locationResult.getLastLocation();
            //latTextView.setText(mLastLocation.getLatitude()+"");
            //lonTextView.setText(mLastLocation.getLongitude()+"");
        }
    };
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

}
