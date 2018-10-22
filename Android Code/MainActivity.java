package com.slycord.carsimfinal;

//Suppresses warning about translatable text
import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.os.Bundle;
import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.util.Locale;



@SuppressWarnings("ResourceType")
public class MainActivity extends Activity{

    //User Default Preferences
    public String defaultIP = "192.168.0.11";
    public String defaultName = "Josh";
    public int defaultPort = 444;

    //Thread Globals
    private CarElement carThread;
    private Client commThread;

    //Volatile Variables
    public volatile static String slipAngle = null;
    public volatile static String acceleration = null;
    public volatile static double mCurrAngle = 0;
    public volatile static double mPrevAngle = 0;
    public volatile static String brakingDistance = null;
    public volatile static String rpm = null;
    public volatile static String sixty;
    public volatile static double speed = 0;
    public volatile static double xPos = 0;
    public volatile static double yPos = 0;
    public volatile static double check = 0;
    public volatile static String[] physicsBuffer = {null, null, null, null, null};
    public volatile static String[] carBuffer = {null, null, null};
    public volatile static double curX = 0;
    public volatile static double curY = 0;


    //UI Globals
    public ImageView wheel;
    public ImageView track;
    public ImageView car;
    public static ImageView car2;
    public static ImageView car3;
    public Button gas, brake, engineControl, autoPilot;
    public volatile  TextView posTxt, accTxt, spdTxt, slipTxt, wheelTxt, sixtyTxt, brakeTxt, rpmTxt, gearTxt;
    public volatile  TextView posLbl, accLbl, spdLbl, slipLbl, wheelLbl, sixtyLbl, brakeLbl, rpmLbl, gearLbl;


    //Ints
    public static volatile String currentGear = null;
    public int height = 0;
    public static int width = 0;
    public int buttonWidth = 0;
    public int buttonHeight = 0;
    public int buttonBaseHeight  = 0;
    public int offset = 0;
    public int gasID = 0;
    public int autoPilotID = 0;

    //Booleans
    public Boolean gasOn = false, brakeOn = false, engineOn = false,  impact = false;
    public Boolean serverSet = false;

    //Doubles
    public double time_start = 0;
    public double widthRatio = 0.9;

    //String
    public String setIP = null;
    public String setPort = null ;
    public String nickName = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initialize Activity

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Configure Layout Parameters
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = (width * 2);
        //       height = Resources.getSystem().getDisplayMetrics().heightPixels;
        buttonHeight = height / 14;
        buttonWidth = width / 4;
        //width = width -350;





        offset = buttonWidth / 10;
        buttonBaseHeight = height - 3 * (buttonHeight + offset);

        //Configure Primary Layout
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Enter IP");
        final EditText userIP = new EditText(this);

        userIP.setInputType(InputType.TYPE_CLASS_TEXT);
        builder1.setView(userIP);

        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setIP = userIP.getText().toString();
                if(setIP.isEmpty()) setIP = defaultIP;

                try {
                    commThread = new Client(setIP, Integer.parseInt(setPort), nickName);
                    commThread.setRunning(true);
                    commThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder1.show();

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Enter Port");
        final EditText userPort = new EditText(this);

        userPort.setInputType(InputType.TYPE_CLASS_TEXT);
        builder2.setView(userPort);

        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setPort = userPort.getText().toString();
                if(setPort.isEmpty()) setPort = Integer.toString(defaultPort);
            }
        });
        builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder2.show();

        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setTitle("Enter Name");
        final EditText name = new EditText(this);

        name.setInputType(InputType.TYPE_CLASS_TEXT);
        builder3.setView(name);

        builder3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nickName = name.getText().toString();
                if(nickName.isEmpty()) nickName = defaultName;

            }
        });
        builder3.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder3.show();


        serverSet = true;

        //Configure Track ImageView
        track = new ImageView(this);
        track.setImageResource(R.drawable.racetrack2);
        //Using width, width to create a square image even though they are the width and height parameters
        //noinspection SuspiciousNameCombination
        track.setLayoutParams(new ViewGroup.LayoutParams(width, width));
        layout.addView(track);

        //Configure Steering Wheel ImageView
        wheel = new ImageView(this);
        wheel.setImageResource(R.drawable.ferrari_wheel);
        wheel.setLayoutParams(new ViewGroup.LayoutParams(width - buttonWidth - 3 * offset, width - buttonWidth - offset));
        layout.addView(wheel);

        //Configure Car ImageView
        car = new ImageView(this);
        car.setImageResource(R.drawable.ferrari_car_red);
        car.setLayoutParams(new ViewGroup.LayoutParams(50, 100));
        car.setX(2 * offset);
        car.setY(-(height) + width / 2 + 2*offset);
        layout.addView(car);

        //Configure Buttons Programmatically
        //Initialize Buttons
        gas = new Button(this);
        brake = new Button(this);
        engineControl = new Button(this);
        autoPilot = new Button(this);

        //Configure AutoPilot Button
        autoPilot.setId(View.generateViewId());
        autoPilot.setText("AutoPilot");
        autoPilot.setTextColor(Color.WHITE);
        autoPilot.setBackgroundColor(Color.BLACK);
        autoPilot.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonHeight));
        autoPilot.setX(width - buttonWidth - offset);
        autoPilot.setY(wheel.getY() - (width - buttonWidth ));
        autoPilotID = autoPilot.getId();
        layout.addView(autoPilot);

        //Configure Gas Button
        gas.setId(View.generateViewId());
        gas.setText("Gas");
        gas.setTextColor(Color.WHITE);
        gas.setBackgroundColor(Color.BLACK);
        gas.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonHeight));
        gas.setX(width - buttonWidth - offset);
        gas.setY(wheel.getY() - (width - buttonWidth - offset));
        gasID = gas.getId();
        layout.addView(gas);

        //Configure Brake Button
        brake.setId(View.generateViewId());
        brake.setText("Brake");
        brake.setTextColor(Color.WHITE);
        brake.setBackgroundColor(Color.BLACK);
        brake.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonHeight));
        brake.setX(width - buttonWidth - offset);
        brake.setY(gas.getY() + offset);
        layout.addView(brake);

        //Configure Start/Stop Button
        engineControl.setId(View.generateViewId());
        engineControl.setText("Start");
        engineControl.setTextColor(Color.WHITE);
        engineControl.setBackgroundColor(Color.BLACK);
        engineControl.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonHeight));
        engineControl.setX(width - buttonWidth - offset);
        engineControl.setY(brake.getY() + offset);
        layout.addView(engineControl);

        //Configure textLayout
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setLayoutParams(new ViewGroup.LayoutParams(width, width / 2));
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setX(width / 4);
        textLayout.setY(-(height) - width / 7);
        textLayout.setBackgroundColor(Color.TRANSPARENT);

        posLbl = new TextView(this);
        posTxt = new TextView(this);
        spdLbl = new TextView(this);
        spdTxt = new TextView(this);
        accLbl = new TextView(this);
        accTxt = new TextView(this);
        rpmLbl = new TextView(this);
        rpmTxt = new TextView(this);
        sixtyLbl = new TextView(this);
        sixtyTxt = new TextView(this);
        wheelLbl = new TextView(this);
        wheelTxt = new TextView(this);
        slipLbl = new TextView(this);
        slipTxt = new TextView(this);
        brakeLbl = new TextView(this);
        brakeTxt = new TextView(this);
        gearTxt = new TextView(this);
        gearLbl = new TextView(this);

        //Configure rowLayouts and add TextViews to textLayout
        textLayout.addView(setupTextView(posLbl, posTxt, "Position: "));
        textLayout.addView(setupTextView(spdLbl, spdTxt, "Speed: "));
        textLayout.addView(setupTextView(accLbl, accTxt, "Acceleration: "));
        textLayout.addView(setupTextView(rpmLbl, rpmTxt, "RPM: "));
        textLayout.addView(setupTextView(gearLbl, gearTxt, "Current Gear: "));
        textLayout.addView(setupTextView(sixtyLbl, sixtyTxt, "0-60 Time: "));
        textLayout.addView(setupTextView(wheelLbl, wheelTxt, "Steering Angle: "));
        textLayout.addView(setupTextView(slipLbl, slipTxt, "Slip Angle: "));
        textLayout.addView(setupTextView(brakeLbl, brakeTxt, "Braking Distance: "));

        //Add textLayout to Main Layout
        layout.addView(textLayout);

        /*
        To-Do
        Figure out how to make it so all 3 players cars do not start on top of each other, either by each player selecting
        a perticular colored car to use and leaving them all 3 on screen, or by each persons code just have a different
        starting position for their car, or other ideas
         */

        //Configure Car ImageView
        car2 = new ImageView(this);
        car2.setImageResource(R.drawable.ferrari_car_green);
        car2.setLayoutParams(new ViewGroup.LayoutParams(50, 100));
        car2.setX(2 * offset);
        car2.setY(-(height) - width / 2 );
        layout.addView(car2);

        //Configure Car ImageView
        car3 = new ImageView(this);
        car3.setImageResource(R.drawable.ferrari_car_blue);
        car3.setLayoutParams(new ViewGroup.LayoutParams(50, 100));
        car3.setX(2 * offset);
        car3.setY(-(height) - width / 2 + offset);
        layout.addView(car3);

        //Steering wheel listener
        wheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final float xc = (wheel.getWidth()) / 2;          //Wheel Vars
                final float yc = (wheel.getHeight()) / 2;

                final float x = event.getX();
                final float y = event.getY();
                wheelTxt.setText(String.format(Locale.US, "%.2f", mCurrAngle));

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        mPrevAngle = mCurrAngle;
                        mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                        animate(mPrevAngle, mCurrAngle, 100, 1);
                        car.setRotation((float) mCurrAngle);
                        System.out.println("Move Wheel");
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        break;
                    }
                }
                return true;
            }
        });

        //Gas button listener
        gas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        System.out.println("Gas Down");
                        gas.setBackgroundColor(Color.GREEN);
                        gasOn = true;
                        time_start = System.nanoTime();

                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        System.out.println("Gas Up");
                        gas.setBackgroundColor(Color.BLACK);
                        gasOn = false;
                        break;
                    }
                }

                return false;
            }
        });

        //Brake button listener
        brake.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        System.out.println("Brake Down");
                        brake.setBackgroundColor(Color.RED);
                        brakeOn = true;
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        System.out.println("Brake Up");
                        brake.setBackgroundColor(Color.BLACK);
                        brakeOn = false;
                        break;
                    }
                }

                return false;
            }
        });

        //Start/Stop button listener
        engineControl.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        System.out.println("Start Down");
                        if (!engineOn) {
                            engineControl.setText("Stop");
                            engineOn = true;
                        } else {
                            engineControl.setText("Start");
                            engineOn = false;
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        System.out.println("Start Up");
                        break;
                    }
                }
                return false;
            }
        });


        /*
        To-Do
        Setup on touch listener for Autopilot button and do something
        that makes it obvious if it is engaged or not
         */

        layout.setBackgroundColor(Color.DKGRAY);
        setContentView(layout);



            carThread = new CarElement(MainActivity.this);
            carThread.setRunning(true);
            carThread.start();


        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                // upadte textView here
                posTxt.setText(String.format(Locale.US, "%.2f", car.getX()) + ", " + String.format(Locale.US, "%.2f", car.getY()));
                accTxt.setText(acceleration);
                spdTxt.setText(String.format(Locale.US, "%.2f",speed));
                slipTxt.setText(slipAngle);
                sixtyTxt.setText(sixty);
                rpmTxt.setText(rpm);
                gearTxt.setText(currentGear);
                brakeTxt.setText(brakingDistance);
                handler.postDelayed(this,5); // set time here to refresh textView
            }
        });


}


    @SuppressWarnings("StatementWithEmptyBody")
    private class CarElement extends Thread {
        private boolean running;
        private Context mContext;
        private double rad;


        CarElement(MainActivity mainActivity) {
            mContext = mainActivity.getApplicationContext();
        }

        private void setRunning(boolean running){

            this.running = running;

        }

        @Override
        public void run(){

            while(running){

                try {
                    if(engineOn) {
                        Thread.sleep(5);
                        if (mCurrAngle < 0) {
                            check = Math.abs(mCurrAngle) + 90;
                        } else {
                            check = Math.abs((mCurrAngle + 270) % 360 - 360);
                        }
                        rad = check * (3.14 / 180);
                        xPos = speed * Math.cos(rad) / 30;
                        yPos = speed * Math.sin(rad) / 30;

                        curX = (car.getX() + xPos);
                        curY = (car.getY() - yPos);

                        //Making widthRatio a smaller value should keep the outer wall boundaries the same
                        //And only bring the inner boundaries closer to center equally around the track

                        /*
                        To-Do
                        Autopilot / Impact logic

                        Car can still go out of bounds in corner squares, not sure why

                        Needs to be adjusted to larger track
                         */

                        if((curX >= offset) && (curX <= width*(1-widthRatio))){
                            //Left Side
                            if((curY >= 0) && (curY <= (width*(1-widthRatio)))){
                                //Top Left Square
                                if(curX == offset){
                                    //On left side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some
                                }

                                else if(curY == 0){
                                    //On top outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not on wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                            else if((curY >= width*widthRatio) && (curY <= (width))){
                                //Bottom Left Square
                                if(curX == offset) {
                                    //On left side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else if(curY == (width)){
                                    //On bottom outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not on wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                            else{
                                //Left Side Middle
                                if(curX == offset){
                                    //On left side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                if(curX == width*(1-widthRatio)){
                                    //On left side inner wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not on a wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                        }

                        else if((curX <= width-2*offset) && (curX >= width*widthRatio-2*offset)) {
                            //Right Side
                            if ((curY > 0) && (curY < (width * (1 - widthRatio)))) {
                                //Top Right Square
                                if(curX == width-2*offset){
                                    //On right side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else if(curY == 0){
                                    //On top outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not on wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                            else if ((curY >= width * widthRatio) && (curY <= (width))) {
                                //Bottom Right Square
                                if(curX == width-2*offset) {
                                    //On right side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else if(curY == (width)){
                                    //On bottom outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not on wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                            else {
                                //Right Side Middle
                                if (curX == width - 2 * offset) {
                                    //On right side outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }
                                else if (curX == width*widthRatio-2*offset) {
                                    //On right side inner wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }
                                else {
                                    //Not on wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }

                            }
                        }

                        else{
                            //Middle Sections
                            if((curY >= 0) && (curY <= (width*(1-widthRatio)))){
                                //Top Middle
                                if(curY == 0){
                                    //On top outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                if(curY == width*(1-widthRatio)){
                                    //On top inner wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not hitting a wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car
                                }
                            }

                            else if((curY >= width*widthRatio-2*offset) && (curY <= (width - 2*offset))){
                                //Bottom Middle
                                if(curY == width*widthRatio-2*offset){
                                    //On bottom outer wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                if(curY == width - 2*offset){
                                    //On bottom inner wall

                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid wall

                                    //if !Autopilot
                                    //Slow down car
                                    //Bounce off wall some

                                }

                                else {
                                    //Not hitting a wall

                                    //Check if no car to car collision
                                    //If true
                                    car.setX((float) curX);
                                    car.setY((float) curY);

                                    //else
                                    //Collision Detected!
                                    //if Autopilot
                                    //Avoid Car

                                    //if !Autopilot
                                    //Slow down your car
                                    //Bounce off other car

                                }
                            }

                            else{

                                //Im not entirely sure how you could get to this point but its here


                                }

                        }


                        // Transmitted to physics: ID1, Gas, Brake, Engine, Impact, current Angle
                        // Transmitted to other phones: ID2,  Current Angle, xpos, ypos
                        //xpos , ypos should be scaled before sending

                        physicsBuffer[0] = Boolean.toString(gasOn);
                        physicsBuffer[1] = Boolean.toString(brakeOn);
                        physicsBuffer[2] = Boolean.toString(engineOn);
                        physicsBuffer[3] = Boolean.toString(impact);
                        physicsBuffer[4] = String.format(Locale.US, "%.2f", mCurrAngle);

                        carBuffer[0] = String.format(Locale.US, "%.2f", mCurrAngle);
                        //Car position is normalized as a percentage off the available pixels on screen
                        //To account for different screen sizes
                        carBuffer[1] = String.format(Locale.US, "%.2f", (car.getX() / width));
                        carBuffer[2] = String.format(Locale.US, "%.2f", (car.getY() / width));

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    View setupTextView(TextView label, TextView value, String text){
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new ViewGroup.LayoutParams(width/2, width/18));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundColor(Color.TRANSPARENT);

        label.setTextColor(Color.WHITE);
        label.setTextSize(10);
        label.setPadding(5, 3, 0, 3);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setGravity(Gravity.START | Gravity.CENTER);

        value.setTextColor(Color.WHITE);
        value.setTextSize(10);
        value.setPadding(5, 3, 0, 3);
        value.setTypeface(Typeface.DEFAULT_BOLD);
        value.setGravity(Gravity.START | Gravity.CENTER);

        label.setText(text);
        value.setText("-----");

        row.addView(label);
        row.addView(value);
//        textLayout.addView(row);

        return row;
    }

    private synchronized void animate(double fromDegrees, double toDegrees, long durationMillis, int what) {
        if(what == 1) {
            RotateAnimation rotateWheel = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateWheel.setDuration(durationMillis);
            rotateWheel.setFillEnabled(true);
            rotateWheel.setFillAfter(true);
            wheel.startAnimation(rotateWheel);
        }

    }

}




