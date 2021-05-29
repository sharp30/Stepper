package com.App;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class HomeActivity extends Activity implements SensorEventListener {

    ImageButton historyBtn;

    SensorManager sensorManager;
    Sensor stepCounter;

    ProgressBar pbStep;
    TextView tvStepCount;
    int stepCount;
    BottomNavigationView bottomNavigationView;
    SharedPreferences sp;
    DateFormat df;
    Dal dal;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sp = getSharedPreferences("values",0);
        dal = new Dal(getApplicationContext());
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED)
        {

            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        bottomNavigationView = findViewById(R.id.btn_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent next = null;
                switch(item.getItemId())
                {
                    case R.id.contests_nav:
                        next = new Intent(getApplicationContext(),ContestsActivity.class);
                        startActivity(next);
                        break;
                    case R.id.profile_nav:
                        next = new Intent(getApplicationContext(),MyDetailsActivity.class);
                        startActivity(next);
                        break;
                    case R.id.weight_nav:
                        next = new Intent(getApplicationContext(),EditWeightActivity.class);
                        startActivity(next);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        //initials
        historyBtn = findViewById(R.id.ibHistory);

        //on clicks
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);

                saveCount();
                startActivity(i);
            }
        });


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepCounter == null)
            //cant find step counter sensor
            return;

        tvStepCount = findViewById(R.id.tvStepsAmount);
        pbStep = findViewById(R.id.progress_bar);

        pbStep.setMax(sp.getInt("steps_target",5000));        //other option - save just today - and others on sqldb
        df = new SimpleDateFormat("dd/MM/yyyy");



        stepCount = sp.getInt("steps",0);
        updateProgressBar();



        createAlarm();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

            stepCount++;
           updateProgressBar();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    //when activity is finally active
    protected void onResume() {
        super.onResume();
        //if (stepCounter != null)
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if(stepCount == 0)
        {
            stepCount = sp.getInt("steps",0);
        }
        updateProgressBar();


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (stepCounter != null) {
            sensorManager.unregisterListener(this, stepCounter);
            //stepCounter = null;
        }
        saveCount();
    }
    protected void saveCount()
    {
        SharedPreferences.Editor editor = sp.edit() ;
        editor.putInt("steps",stepCount);
        editor.apply();

    }
    protected void updateProgressBar()
    {
        pbStep.setProgress(stepCount);
        tvStepCount.setText(String.valueOf(stepCount));
    }

    public void createAlarm() {
        //System request code
        int DATA_FETCHER_RC = 123;
        //Create an alarm manager
        AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        //Create the time of day you would like it to go off. Use a calendar
        //just before midnight
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE,14);

        //Create an intent that points to the receiver. The system will notify the app about the current time, and send a broadcast to the app
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, DATA_FETCHER_RC,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //set time, it is usually a few seconds off your requested time.
        //initialize the alarm by using inexactrepeating. This allows the system to scheduler your alarm at the most efficient time around your
        // you can also use setExact however this is not recommended. Use this only if it must be done then.

        //Also set the interval using the AlarmManager constants
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);

    }
    public void onBackPressed()
    {
    }

}