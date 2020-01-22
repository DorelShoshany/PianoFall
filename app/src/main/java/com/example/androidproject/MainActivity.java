package com.example.androidproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


public class MainActivity  extends AppCompatActivity    {
    private LinearLayout.LayoutParams buttonLayoutParams;
    private LinearLayout.LayoutParams params;
    private LinearLayout.LayoutParams imageLoginParams;
    private EditText userNameEditText;
    private EditText passwordEditText;
    //for DB - users
    public static String userName;
    public static String password;
    public static int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createLoginScreen();
    }




    public void createImageLogin(ImageView imageLogin){
        imageLogin.setBackgroundResource(R.drawable.login);
        imageLogin.setLayoutParams(imageLoginParams);
        imageLogin.setAdjustViewBounds(true);
        imageLogin.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void initImageLoginParams(){
        //find size of all the screen:
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        //create imageLogin Params:
        int halfScreenWidth = (int)(screenWidth *0.5);
        int quarterScreenWidth = (int)(halfScreenWidth * 0.40);
        imageLoginParams = new LinearLayout.LayoutParams(quarterScreenWidth,quarterScreenWidth);
        imageLoginParams.setMargins(0,50,0,50);
        imageLoginParams.gravity = Gravity.CENTER;
    }

    public void initButtonStyle(Button button){
        button.setLayoutParams(buttonLayoutParams);
        button.setBackgroundResource(R.color.appColor);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(getApplication().getResources().getColor(R.color.white));
    }

    public void initTextLabelStyle(TextView textView){
        textView.setGravity(Gravity.LEFT);
        textView.setLayoutParams(params);
        textView.setPadding(30,0,0,0);
        textView.setText(Global_Variable.USER_NAME_TEXT);
        textView.setTextSize(30);
        textView.setBackgroundResource(R.color.blue);
        textView.setTextColor(getApplication().getResources().getColor(R.color.white));
    }

    public void initEditTextStyle(EditText editText){
        editText.setGravity(Gravity.LEFT);
        editText.setLayoutParams(params);
        editText.setBackgroundColor(getApplication().getResources().getColor(R.color.backgroundForEditText));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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

    public void initParams(){
        //params for the rest:
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(25,10,25,10);
    }

    public void createLoginScreen(){
        LinearLayout mainActivityLayout = findViewById(R.id.mainLayout);
        mainActivityLayout.setBackgroundResource(R.drawable.backgroundcookieblue);

        initImageLoginParams();
        ImageView imageLogin = new ImageView(this);
        createImageLogin(imageLogin);
        mainActivityLayout.addView(imageLogin);

        initParams();

        //create textFiled for user:
        TextView userNameTextView = new TextView(this);
        initTextLabelStyle(userNameTextView);
        mainActivityLayout.addView(userNameTextView);

        userNameEditText = new EditText(this);
        initEditTextStyle(userNameEditText);
        mainActivityLayout.addView(userNameEditText);

        //create textFiled for password:
        TextView passwordNameTextView = new TextView(this);
        initTextLabelStyle(passwordNameTextView);
        passwordNameTextView.setText(Global_Variable.PASSWORD_TEXT);
        mainActivityLayout.addView(passwordNameTextView);

        passwordEditText = new EditText(this);
        initEditTextStyle(passwordEditText);
        mainActivityLayout.addView(passwordEditText);

        initParamsForButtons();

        //create LoginButton:
        Button loginButton = new Button(this);
        initButtonStyle(loginButton);
        loginButton.setText(Global_Variable.LOGIN_TEXT_ON_BUTTON);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });
        mainActivityLayout.addView(loginButton);

        //create topScoreButton:
        Button topScoreButton = new Button(this);
        initButtonStyle(topScoreButton);
        topScoreButton.setText(Global_Variable.TOP_NUMBER_TEXT_ON_BUTTON);
        topScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this,
                        MapsActivity.class);
                //Intent myIntent = new Intent(MainActivity.this,
             //           ScoreSheetActivity.class);
                startActivity(myIntent);
            }
        });
        mainActivityLayout.addView(topScoreButton);

    }

    public void  startNewGame(){
        //save this for DB when the game finish
        this.userName = userNameEditText.getText().toString();
        this.password =passwordEditText.getText().toString();
        if (this.userName.isEmpty() || this.password.isEmpty()){
            Toast.makeText(MainActivity.this , "You must enter a username and password both!", Toast.LENGTH_SHORT).show();
        }
        else {

            Intent myIntent = new Intent(MainActivity.this,
                    GameActivity.class);
            myIntent.putExtra(Global_Variable.USER_NAME_FOR_MOVE_INTENT,this.userName);
            myIntent.putExtra(Global_Variable.PASSWORD_FOR_MOVE_INTENT, this.password);
            startActivity(myIntent);


        }
    }



}
