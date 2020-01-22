package com.example.androidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreSheetActivity extends AppCompatActivity {
    //FOR DATA BASE:
    private static final String TAG = "ViewDatabase";
    public FirebaseDatabase mDatabase;
    public DatabaseReference usersRef;

    // Write to the database
    private ArrayList<User> arrayToShowOnTheScreen;
    private ListView listView;
    private List<String> keys;
    public ValueEventListener postListener;

    @Override
    public void onResume() {
        super.onResume();
        //DATA BASE:
        arrayToShowOnTheScreen.clear();
        keys.clear();
        readUsers();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_sheet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LinearLayout mainActivityLayout = findViewById(R.id.scoreSheetLayout);
        mainActivityLayout.setBackgroundResource(R.drawable.backgroundlogin);
        arrayToShowOnTheScreen = new ArrayList<>();
        keys= new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference(Global_Variable.USERS_TABLE);
    }

    @Override
    public void onBackPressed(){
        GameActivity.myPoints=0;
        openLoginScreen();
    }

    public void openLoginScreen(){
        //finish();
        Intent myIntent = new Intent(ScoreSheetActivity.this, MainActivity.class);
        startActivity(myIntent);
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
                ArrayAdapter<User> adapter = new ArrayAdapter<>(ScoreSheetActivity.this, R.layout.activity_list_view,R.id.textView, arrayToShowOnTheScreen);
                listView = (ListView) findViewById(R.id.listview);
                listView.setAdapter(adapter);
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
                Toast.makeText(ScoreSheetActivity.this , "The read failed: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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



    //FOR DYNAMIC
    void createMainTitle(){
        TextView titleText = new TextView(this);
        titleText.setText(Global_Variable.TOP_10);
        titleText.setTextColor(getResources().getColor(R.color.white));
        titleText.setTextSize(35);
        titleText.setGravity(Gravity.CENTER);
        titleText.setBackgroundResource(R.color.purple);
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(titleText);
    }




}
