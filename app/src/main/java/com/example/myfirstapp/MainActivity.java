package com.example.myfirstapp;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Smart Phone Sensing Example 2. Working with sensors.
 */
public class MainActivity extends Activity implements SensorEventListener {

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    /**
     * The wifi info.
     */
    private WifiInfo wifiInfo;
    /**
     * Accelerometer x value
     */
    private float aX = 0;
    /**
     * Accelerometer y value
     */
    private float aY = 0;
    /**
     * Accelerometer z value
     */
    private float aZ = 0;
    /**
     * The text view.
     */
    private TextView textView;
    /**
     * The edit text box.
     */
    private EditText editText;

    /**
     * Text fields to show the sensor values.
     */
    private TextView currentX, currentY, currentZ, textRssi; //took out titleAcc from example 2

    Button buttonRssi, buttonStart, buttonStop, buttonInitial;

    FileOutputStream outputStream;

    boolean started=false;

    String csvName="";

    //used to get data every certain time interval
    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        //titleAcc = (TextView) findViewById(R.id.titleAcc);
        textRssi = (TextView) findViewById(R.id.textRSSI);
        textView = (TextView) findViewById(R.id.textView2);
        editText = (EditText) findViewById(R.id.editText2);


        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);

        // Create the Start data collcetion button
        buttonStart = (Button) findViewById(R.id.button3);

        // Create the Stop data collcetion button
        buttonStop = (Button) findViewById(R.id.button4);

        // Create the Stop data collcetion button
        buttonInitial = (Button) findViewById(R.id.button1);

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No accelerometer!
        }

        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Create a click listener for our button.
        buttonRssi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the wifi info.
                wifiInfo = wifiManager.getConnectionInfo();
                // update the text.
                textRssi.setText("\n\tSSID = " + wifiInfo.getSSID()
                        + "\n\tRSSI = " + wifiInfo.getRssi()
                        + "\n\tLocal Time = " + System.currentTimeMillis());
            }
        });
        // Create a click listener for our start button.
        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started){
                    //File creation to store data (uncomment when algorithm done)
                    if (editText.getText().toString().contains("locate") ||
                            editText.getText().toString().contains("motionW") ||
                            editText.getText().toString().contains("motionS") ) {
                        csvName = editText.getText().toString();
                        textView.setText("A file " + csvName + ".csv will be created, when stop is pressed.");
                        started=true;
                        try{
                            outputStream = openFileOutput(csvName+".csv", Context.MODE_APPEND);
                            //outputStream.write("Hello World test!\n".getBytes()); //for debugging
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        textView.setText("Please enter a suitable string containing 'locateCell#' or 'motionW/S'");
                    }
                } else {
                    textView.setText(csvName + ".csv is running");
                }
                editText.setText("");
            }
        });
        // Create a click listener for our stop button.
        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started) {
                    textView.setText(csvName + ".csv has been created.");
                    try {
                        //outputStream.write("Hello World test!\n".getBytes()); for debugging
                        outputStream.close();
                        started = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    csvName = "";
                }else {
                    textView.setText("Nothing is running");
                }
            }
        });
        // Create a click listener for our initial button.
        //currently used for testing reading of csv
        //how to read from internal storage https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app
        buttonInitial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = getApplicationContext().openFileInput("motion.csv");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                textView.setText(sb);
            }
        });
    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        //handler to save data into csv file every 1 second (https://stackoverflow.com/questions/11434056/how-to-run-a-method-every-x-seconds)
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                if(started){
                    try {
                        //outputStream.write("10-Hello World test!\n".getBytes());//for debugging
                        if (csvName.contains("motionW")){
                            outputStream.write(("X="+Float.toString(aX)+",Y="+Float.toString(aY)+
                                    ",Z="+Float.toString(aZ)+",Walk,\n").getBytes());
                        } else if(csvName.contains("motionS")){
                            outputStream.write(("X="+Float.toString(aX)+",Y="+Float.toString(aY)+
                                    ",Z="+Float.toString(aZ)+",Still,\n").getBytes());
                        } else if (csvName.contains("locate")){
                            String[] cellNo = csvName.split("Cell"); //cellNo[1] is cell no
                            // Start a wifi scan.
                            wifiManager.startScan();
                            // Store results in a list.
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            // Write results to a label
                            for (ScanResult scanResult : scanResults) {
                                outputStream.write(("BSSID="+scanResult.BSSID+",RSSI="+
                                        scanResult.level+","+cellNo[1]+",\n").getBytes());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                h.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        //pause recording data to csv file when app closed
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");

        // get the the x,y,z values of the accelerometer
        aX = event.values[0];
        aY = event.values[1];
        aZ = event.values[2];

        // display the current x,y,z accelerometer values
        currentX.setText(Float.toString(aX));
        currentY.setText(Float.toString(aY));
        currentZ.setText(Float.toString(aZ));

/*        if ((Math.abs(aX) > Math.abs(aY)) && (Math.abs(aX) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.RED);
        }
        if ((Math.abs(aY) > Math.abs(aX)) && (Math.abs(aY) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.BLUE);
        }
        if ((Math.abs(aZ) > Math.abs(aY)) && (Math.abs(aZ) > Math.abs(aX))) {
            titleAcc.setTextColor(Color.GREEN);
        }*/
    }
}