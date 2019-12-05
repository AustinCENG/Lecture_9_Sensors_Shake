package com.ceng319.sensors_shake;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView mTextMessage1;
    private TextView mTextMessage2;
    // private Button mButton;
    private MediaPlayer mPlayer;

    private SensorManager mSensorManager;
    private long mlastUpdate;
    private int mcurrentTab = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllViews();

        // TODO: Generate a list of sensors available.
        String listofSensors = generateSensorList();
        mTextMessage1.setText(getString(R.string.sensor_list) + listofSensors);


        // TODO: Get the timestamp (epoch time)
        mlastUpdate = System.currentTimeMillis()/1000;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // TODO: prevent the screen from orientation change.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    void findAllViews(){
        // TODO: Find all the views on the layout
        mTextMessage1 = (TextView) findViewById(R.id.message1);
        mTextMessage2 = (TextView) findViewById(R.id.message2);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorEvent(mcurrentTab);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            unregisterSensors(); // remove the sensors and reregister a new one.

            switch (item.getItemId()) {
                case R.id.navigation_acc:
                    //TODO: Get the sensor names.
                    String listofSensors = generateSensorList();
                    mTextMessage1.setText(getString(R.string.sensor_list) +" \n\n" + listofSensors);
                    mcurrentTab = 1;
                    break;
                case R.id.navigation_light:
                    registerSensorEvent(2);
                    mTextMessage1.setText(" \n\nLight Sensor Readings");
                    mcurrentTab = 2;
                    break;
                case R.id.navigation_proximity:
                    mTextMessage1.setText(" \n\nProximity Sensor Readings");
                    mcurrentTab = 3;
                    break;
                case R.id.navigation_location:
                Intent intent = new Intent(getApplicationContext(), LocationServ.class);
                startActivity(intent);
                   // mcurrentTab = 4;
                break;
                default:
                    return false;
            }
            registerSensorEvent(mcurrentTab);
            return true;
        }
    };


    private String generateSensorList() {
        boolean DEVICE_HAS_ACCELEROMETER = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        String str = "";
        for (Sensor sensor: deviceSensors)
        {
            str = str + sensor.getName() + "\n";
            Log.d("SensorList", "" + sensor.getName());

        }
        // TODO: Detect if Accelerometer is available.
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            DEVICE_HAS_ACCELEROMETER = true;
        } else {
            DEVICE_HAS_ACCELEROMETER = false;
        }

        return str;
    }


    // TODO: at different tab, register different sensors.
    private void registerSensorEvent(int currentTab)
    {
        //unregisterSensors();
        switch (currentTab)
            {

                case 1:
                    // TODO: register mainactivity class as a listener for the accelerometer sensor
                mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                break;
                case 2:
                    // TODO: register mainactivity class as a listener for the light sensors
                    mSensorManager.registerListener(this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                            SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                case 3:
                    // TODO: register mainactivity class as a listener for the proximity sensors
                    mSensorManager.registerListener(this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                            SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                default:
                    break;
            }
    }

    private void unregisterSensors()
    {
        mSensorManager.unregisterListener(this);
    }

    // TODO: This is the list of the SensorEventListener

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            getProximity(sensorEvent);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            getLight(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void getLight(SensorEvent sensorEvent) {
        float light = sensorEvent.values[0];
        mTextMessage2.setText("Light: " + light);
        Log.d("Light","Light: " + light);
        if (light <= 80){
            playMusic(false);  // play the music in the dark
        }
        else
            playMusic(true);
    }


    void playMusic(boolean stop){
        if (stop){
            if((mPlayer != null) && (mPlayer.isPlaying()))
                stopASong();
        }
        else{
            if((mPlayer != null) && (mPlayer.isPlaying()))
                return;
            playASong();
        }
    }

    private void getProximity(SensorEvent sensorEvent) {
        float distance = sensorEvent.values[0];
        mTextMessage2.setText("Distance: " + distance);
        Log.d("Distance","Distance: " + distance);
        // Do something with this sensor data.
        if (distance == 0){  // if that is near the object.
            playMusic(false);
        }
        else {
            playMusic(true);
        }
    }

    private void stopASong() {
        mPlayer.pause();
    }

    private void playASong() {
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.track1);
        mPlayer.start();
    }



    // TODO: Helper method for onSensorChanged
    private void getAccelerometer(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        mTextMessage2.setText("Accelerometer X: " + x +" Y: "+ y + " Z: " + z);

        // TODO: Calculate the angular speed of the sample
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        long actualTime = System.currentTimeMillis() / 1000;
        if (accelationSquareRoot >= 6) //
        {
            // if last shack was within 3 seconds, ignore it.
            if (actualTime - mlastUpdate < 3) {
                return;
            }
            mlastUpdate = actualTime;
            Toast.makeText(this, "What can I do for you? ", Toast.LENGTH_SHORT)
                    .show();
            // set a random color on the background of message2
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            mTextMessage2.setBackgroundColor(color);
        }
    }
}
