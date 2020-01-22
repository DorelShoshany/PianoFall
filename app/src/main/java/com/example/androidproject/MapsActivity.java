package com.example.androidproject;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //FOR DATA BASE:
    private static final String TAG = "ViewDatabase";
    public FirebaseDatabase mDatabase;
    public DatabaseReference usersRef;

    // Write to the database
    private ArrayList<User> arrayToShowOnTheScreen;
    private ListView listView;
    private List<String> keys;
    public ValueEventListener postListener;


    private GoogleMap mMap;
    // New variables for Current Place Picker
    private static final String TAGFORMAP = "MapsActivity";
    ListView lstPlaces;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        LinearLayout mainActivityLayout = findViewById(R.id.scoreSheetLayout);
        mainActivityLayout.setBackgroundResource(R.drawable.backgroundlogin);

        arrayToShowOnTheScreen = new ArrayList<>();
        keys= new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference(Global_Variable.USERS_TABLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        lstPlaces = (ListView) findViewById(R.id.listPlaces);

        // Initialize the Places client
        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(getApplicationContext(), apiKey);
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void setLocationWhenClick(){
        listView = (ListView) findViewById(R.id.listPlaces);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                int itemPosition     = position;
                // ListView Clicked item value
                String itemValue= (String)listView.getItemAtPosition(position).toString();
                User itemValue2= (User)listView.getItemAtPosition(position);
                UserLocation userLocation = itemValue2.getLocation();
                if (userLocation != null ){
                    LatLng l = new LatLng(userLocation.getY(),userLocation.getX());
                }
                else{
                    LatLng l = new LatLng(0,0);
                }

                // Show Alert
                // Toast.makeText(getApplicationContext(),
                 //        "location  :"+userLocation , Toast.LENGTH_LONG)
                //        .show();
                if (userLocation != null ){
                    setLocation(userLocation.getY(),userLocation.getX());
                }
               // Toast.makeText(getApplicationContext(),
                //        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
               //         .show();
                 Toast.makeText(getApplicationContext(),
                        "find the player's location :) ... "+userLocation , Toast.LENGTH_LONG)
                        .show();
            }

        });
    }

    private void setLocation(double chosenUserLocationLatitude, double chosenUserLocationLongitude){
        if(mMap!= null) {
            LatLng location = new LatLng(chosenUserLocationLongitude,chosenUserLocationLatitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(location).title(""));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //DATA BASE:
        arrayToShowOnTheScreen.clear();
        keys.clear();
        readUsers();
        setLocationWhenClick();
    }

    @Override
    public void onStop() {
        super.onStop();
        //DATA BASE:
        arrayToShowOnTheScreen.clear();
        keys.clear();
        usersRef.removeEventListener(postListener);
    }

    @Override
    public void onBackPressed(){
        GameActivity.myPoints=0;
        openLoginScreen();
    }

    public void openLoginScreen(){
        //finish();
        Intent myIntent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(myIntent);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_geolocate:

                // COMMENTED OUT UNTIL WE DEFINE THE METHOD
                // Present the current place picker
                // pickCurrentPlace();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public void readUsers(){//final DataStatus dataStatus){
        arrayToShowOnTheScreen.clear();
        keys.clear();
        postListener = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.e(TAG+ " Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    keys.add(postSnapshot.getKey());
                    User someUser = new User();
                    someUser = postSnapshot.getValue(User.class);
                    arrayToShowOnTheScreen.add(someUser);
                }
                Collections.reverse(arrayToShowOnTheScreen);
                ArrayAdapter<User> adapter = new ArrayAdapter<>(MapsActivity.this, R.layout.activity_list_view,R.id.textView, arrayToShowOnTheScreen);
                listView = (ListView) findViewById(R.id.listPlaces);
                listView.setAdapter(adapter);
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
                Toast.makeText(MapsActivity.this , "The read failed: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        Query queryRef = usersRef.orderByChild("points");
        queryRef.addValueEventListener(postListener);

    }



    public void printToLogUsersScoresHmap(){
        Log.e(TAG,"d "+ arrayToShowOnTheScreen.size());
        for (int i = 0; i < arrayToShowOnTheScreen.size() ; i ++ ){
            Log.e(TAG ,""+ arrayToShowOnTheScreen.get(i));
        }
    }


}
