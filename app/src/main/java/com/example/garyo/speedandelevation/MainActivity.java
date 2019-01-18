package com.example.garyo.speedandelevation;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    SensorManager mySensorManager;
    Sensor baroSensor;
    TextView altitudeTV;
    TextView speedTV;

    SharedPreferences prefs;

    float zeroLevel;
    Float currentPressure;

    public static final String KEY_SEA_LEVEL = "SEA_LEVEL";
    SharedPreferences.Editor prefsEditor;

    Button setSeaButton;
    Button resetButton;

    LocationManager myLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getPreferences(MODE_PRIVATE);

        speedTV = findViewById(R.id.speedText);
        speedTV.setText("0 km/h");
        altitudeTV = findViewById(R.id.altitude_text_view);
        setSeaButton = findViewById(R.id.set_sea_level);
        resetButton = findViewById(R.id.reset);

        //SETTING BUTTONS
        setSeaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefsEditor = prefs.edit();
                if (currentPressure != null)
                    prefsEditor.putFloat(KEY_SEA_LEVEL, currentPressure);
                prefsEditor.apply();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefsEditor = prefs.edit();
                prefsEditor.putFloat(KEY_SEA_LEVEL, SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
                prefsEditor.apply();
            }
        });


        //SENSORS
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            baroSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }

        //LOCATION
        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1234);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        zeroLevel = prefs.getFloat(KEY_SEA_LEVEL, SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
        currentPressure = sensorEvent.values[0];
        Float altitudeF =  SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                currentPressure
        );

        //should give 0
        //but allows ppl to set zeroLevel
        Float altitudeF2 =  SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                zeroLevel
        );

        Float altitudeDiff = altitudeF -altitudeF2;

        String altitude = String.valueOf(roundF(altitudeDiff,1)
               )+"m";

        altitudeTV.setText(altitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(this,baroSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(this,baroSensor);

    }


    public static float roundF(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location==null){speedTV.setText("0");}
        else{
            int speed=(int) ((location.getSpeed()*3600)/1000);
            speedTV.setText(String.valueOf(speed+" km/h"));

        }
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
