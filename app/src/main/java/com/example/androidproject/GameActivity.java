package com.example.androidproject;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener , SensorEventListener{

    private boolean isToCreateRainPiano;
    List <Integer> hearts = new ArrayList<>();
    private int currentLife = Global_Variable.LIFE_CHANCES;
    public static int myPoints = 0;
    private int screenHeight;
    private int screenWidth;
    private RelativeLayout attacksLinearWrapper;
    public static int pianoSize;
    public static int cookieSize;
    private ImageView player;
    private int playerSize;
    public TextView pointsText;
    public static LinearLayout linear_heart;
    private SensorManager manager;
    public String userName;
    public String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isToCreateRainPiano = true;

        userName = getIntent().getStringExtra(Global_Variable.USER_NAME_FOR_MOVE_INTENT);
        password=getIntent().getStringExtra(Global_Variable.PASSWORD_FOR_MOVE_INTENT);


        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.pointsText = new TextView(this);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        this.screenHeight= size.y;
        this.screenWidth = size.x;
        this.currentLife= Global_Variable.LIFE_CHANCES;
        this.linear_heart = new LinearLayout(this);

        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor accel=manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        createLinearHeart();
        createAttacksLinearWrapper();
        createPlayer();
        createPianoFall();
    }

    public void createAttacksLinearWrapper(){
        attacksLinearWrapper = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        attacksLinearWrapper.setLayoutParams(params);
        attacksLinearWrapper.setBackgroundResource(R.color.pink);
        attacksLinearWrapper.setOnTouchListener(this);
        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(attacksLinearWrapper);
    }

    public void createPlayer(){
        this.player = new ImageView(this);
        player.setBackgroundResource(R.drawable.monster);
        player.setAdjustViewBounds(true);
        player.setScaleType(ImageView.ScaleType.FIT_XY);
        this.playerSize = (int)(screenWidth *0.18);
        RelativeLayout.LayoutParams playerParams = new RelativeLayout.LayoutParams(playerSize, playerSize);
        playerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        player.setLayoutParams(playerParams);
        attacksLinearWrapper.addView(player);
    }

    public final ImageView createPiano(){
        ImageView piano = new ImageView(this);
        piano.setImageResource(R.drawable.piano);
        piano.setAdjustViewBounds(true);
        piano.setScaleType(ImageView.ScaleType.FIT_XY);
        this.pianoSize = (int)(screenWidth *0.15);
        piano.setLayoutParams( new RelativeLayout.LayoutParams(pianoSize,pianoSize));
        return piano;
    }

    public final ImageView createCookie(){
        ImageView cookie = new ImageView(this);
        cookie.setImageResource(R.drawable.cookie);
        cookie.setAdjustViewBounds(true);
        cookie.setScaleType(ImageView.ScaleType.FIT_XY);
        this.cookieSize = (int)(screenWidth *0.15);
        cookie.setLayoutParams( new RelativeLayout.LayoutParams(cookieSize, cookieSize));
        return cookie;
    }

    public void createPianoFall(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    if (isToCreateRainPiano) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (randomNumberGenerator() < Global_Variable.PERCENTAGE_OF_PIANOS_TO_FALL){
                                    ImageView pianoAttack = getElementProducer();
                                    attacksLinearWrapper.addView(pianoAttack);
                                    collisionDetection(pianoAttack, player, Global_Variable.TYPE_PIANO);
                                }
                                else{
                                    ImageView CookieAttack = getCookieAttack();
                                    attacksLinearWrapper.addView(CookieAttack);
                                    collisionDetection(CookieAttack, player, Global_Variable.TYPE_COOKIE);
                                }

                            }
                        });
                    }
                createPianoFall();
            }
        }, 1000);
    }


    private synchronized void collisionDetection(final ImageView v1,final ImageView v2 ,final int elementType){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (isToCreateRainPiano) {
                        Thread.sleep(30);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                if (isCllision(v1, v2)) {
                                    if (elementType == Global_Variable.TYPE_PIANO){
                                        linear_heart.removeView(findViewById(currentLife-1));
                                        currentLife--;
                                        if(currentLife <=0 ){
                                            openGameOverActivity();
                                        }
                                    }
                                    else{
                                        myPoints+=Global_Variable.POINTS_PER_COOKIE_ATTACK;
                                    }
                                    v1.setX(0);
                                    v1.setY(0);
                                    v1.animate().cancel();
                                    attacksLinearWrapper.removeView(v1);
                                }

                                else {
                                    // myPoints+=Global_Variable.POINTS_PER_PIANO_ATTACK;
                                    pointsText.setText(Global_Variable.POINTS_TEXT + myPoints);
                                }
                            }
                        });
                    }
                }catch (InterruptedException e) {}
            };

        };
        t.start();
    }

    private static boolean isCllision(ImageView v1, ImageView v2) {
        return ((v1.getX() <= v2.getX() + pianoSize && v2.getX() <= v1.getX() + v1.getWidth())
                && (v1.getY() <= v2.getY() + pianoSize && v2.getY() <= v1.getY() + v1.getHeight()));
    }

    //TODO: check the sensor + piano on android's Yarden + location
    private ImageView getElementProducer(){
        final ImageView pianoAttack = createPiano();
        //Random r = new Random();
        //float rangeMax = attacksLinearWrapper.getX();
        //float rangeMin = pianoAttack.getX();
        //pianoAttack.setX(new Random().nextInt(d - c));
        //pianoAttack.setX(rangeMin + (rangeMax - rangeMin) * r.nextDouble());
        pianoAttack.setX(new Random().nextInt(screenWidth-pianoSize));
        pianoAttack.animate()
                .translationY(this.screenHeight)
                .setDuration(Global_Variable.DURATION_OF_PIANO_ANIMATE)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        attacksLinearWrapper.removeView(pianoAttack);
                        myPoints+=Global_Variable.POINTS_PER_PIANO_ATTACK;
                    }
                }).start();
        return pianoAttack;
    }

    private ImageView getCookieAttack(){
        final ImageView cookieAttack = createCookie();
        cookieAttack.setX(new Random(4).nextInt(screenWidth - cookieSize));

        cookieAttack.animate()
                .translationY(this.screenHeight)
                .setDuration(Global_Variable.DURATION_OF_PIANO_ANIMATE)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        attacksLinearWrapper.removeView(cookieAttack);
                    }
                }).start();
        return cookieAttack;
    }

    public void createLinearHeart(){
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;
        int halfScreenWidth = (int)(screenWidth *0.5);
        int quarterScreenWidth = (int)(halfScreenWidth * 0.25);

        this.linear_heart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,quarterScreenWidth));
        this.linear_heart.setBackgroundResource(R.color.purple);
        this.linear_heart.setOrientation(LinearLayout.HORIZONTAL);

        pointsText.setText(Global_Variable.POINTS_TEXT + myPoints);
        pointsText.setTextColor(getResources().getColor(R.color.white));
        pointsText.setTextSize(20);
        linear_heart.addView(pointsText);

        createHearts(linear_heart);
        createButtons(linear_heart);

        LinearLayout mainActivityLayout = findViewById(R.id.gameLayout);
        mainActivityLayout.addView(this.linear_heart);
    }


    //TODO: Actions to the button
    public void createButtons(LinearLayout linear_heart){
        LinearLayout.LayoutParams buttonLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.gravity = Gravity.LEFT;
        buttonLayoutParams.setMargins(20,0,20,0);

        ImageButton exitButton = new ImageButton(this);
        final ImageButton pauseButton = new ImageButton(this);
        ImageButton playButton = new ImageButton(this);

        exitButton.setImageResource(R.drawable.exit);
        exitButton.setBackgroundResource(R.color.purple);
        exitButton.setAdjustViewBounds(true);
        exitButton.setScaleType(ImageView.ScaleType.FIT_XY);
        exitButton.setLayoutParams(buttonLayoutParams);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGameOverActivity();
            }
        });

        pauseButton.setImageResource(R.drawable.pouse);
        pauseButton.setBackgroundResource(R.color.purple);
        pauseButton.setAdjustViewBounds(true);
        pauseButton.setScaleType(ImageView.ScaleType.FIT_XY);
        pauseButton.setLayoutParams(buttonLayoutParams);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });



        playButton.setImageResource(R.drawable.play);
        playButton.setBackgroundResource(R.color.purple);
        playButton.setAdjustViewBounds(true);
        playButton.setScaleType(ImageView.ScaleType.FIT_XY);
        playButton.setLayoutParams(buttonLayoutParams);

        linear_heart.addView(exitButton);
        linear_heart.addView(pauseButton);
        linear_heart.addView(playButton);
    }


    public void createHearts (LinearLayout linear_heart){
        for (int i = 0 ; i< Global_Variable.LIFE_CHANCES ; i++) {
            hearts.add(i);
            ImageView heart = new ImageView(this);
            heart.setId(i);
            heart.setImageResource(R.drawable.heart);
            linear_heart.addView(heart, i);
            heart.setAdjustViewBounds(true);
            heart.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        if (event.getAction() == MotionEvent.ACTION_MOVE)
            player.setX(x - (player.getWidth() / 2f));
        return true;
    }

    @Override
    public void onBackPressed(){
        openGameOverActivity();
    }


    private void openGameOverActivity() {
        isToCreateRainPiano = false;
        //for db:
        MainActivity.points = myPoints;
        //move to next screen:
        Intent myIntent = new Intent(GameActivity.this, GameOver.class);
        myIntent.putExtra(Global_Variable.POINTS_FOR_MOVE_INTENT, myPoints);
        myIntent.putExtra(Global_Variable.USER_NAME_FOR_MOVE_INTENT,this.userName);
        myIntent.putExtra(Global_Variable.PASSWORD_FOR_MOVE_INTENT, this.password);
        finish();
        try {
            startActivity(myIntent);
        }catch (Exception e){
            e.getMessage();
        }
    }

    public static double randomNumberGenerator()
    {
        double rangeMin = 0.0f;
        double rangeMax = 1.0f;
        Random r = new Random();
        double createdRanNum = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return(createdRanNum);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight1= size.y;
        int screenWidth1 = size.x;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            player.animate()
                    .translationX((int)(player.getWidth()* (-event.values[0])*Global_Variable.SPEED_OF_PLAYER))
                    .setDuration(Global_Variable.DURATION_OF_PIANO_ANIMATE)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float nextX = player.getX();
                            nextX = nextX > 0? nextX:0;
                            nextX = nextX < screenWidth1 - player.getWidth()? nextX:screenWidth1-player.getWidth();
                            player.setX(nextX);
                        }
                    })
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            float nextX = player.getX();
                            nextX = nextX > 0? nextX:0;
                            nextX = nextX < screenWidth1 - player.getWidth()? nextX:screenWidth1-player.getWidth();
                            player.setX(nextX);
                        }
                    })
                    .start();
            float nextX = player.getX();
            nextX = nextX > 0? nextX:0;
            nextX = nextX < screenWidth1 - player.getWidth()? nextX:screenWidth1-player.getWidth();
            player.setX(nextX);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
