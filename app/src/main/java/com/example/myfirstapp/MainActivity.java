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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

    private float aX_Range=0, aY_Range=0, aZ_Range=0, aX_Max=0, aY_Max=0, aZ_Max=0, aX_Min=0, aY_Min=0, aZ_Min=0;
    /**
     * The text view.
     */
    private TextView textView;
    /**
     * The edit text box.
     */
    private EditText editText;

    Button buttonStart, buttonStop, buttonInitial, buttonLocateMe, buttonTest, buttonTrain;

    FileOutputStream outputStream;

    boolean started=false, refreshed=true;

    String csvName="";

    //used to get data every certain time interval
    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //titleAcc = (TextView) findViewById(R.id.titleAcc);
        textView = (TextView) findViewById(R.id.textView2);
        editText = (EditText) findViewById(R.id.editText2);


        // Create the Start data collcetion button
        buttonStart = (Button) findViewById(R.id.button3);
        // Create the Stop data collcetion button
        buttonStop = (Button) findViewById(R.id.button4);
        // Create the Initial data collcetion button
        buttonInitial = (Button) findViewById(R.id.button1);
        // Create the Locate Me data collcetion button
        buttonLocateMe = (Button) findViewById(R.id.button2);
        // Create the Initial data collcetion button
        buttonTest = (Button) findViewById(R.id.button5);
        // Create the Locate Me data collcetion button
        buttonTrain = (Button) findViewById(R.id.button6);

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
                            outputStream = openFileOutput(csvName+".csv", Context.MODE_PRIVATE);
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
                textView.setText("Used for Bug testing now\n" + sb);
            }
        });
        // Create a click listener for our LocateMe button, to see which cell we are at, can iterate
        buttonLocateMe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //locate user
                textView.setText("WIP\n");
            }
        });
        // Create a click listener for our Test button, to get confusion matrix.
        buttonTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMotionKNN("motionS-1.csv","motionS-2.csv");
                checkMotionKNN("motionW-1.csv","motionW-2.csv");
                textView.setText("WIP\n");
                //test and create confusionmatrix csv file in internal storage
/*                csvName = editText.getText().toString();
                textView.setText("A file " + csvName + ".csv will be created, when stop is pressed.");
                started=true;
                try{
                    outputStream = openFileOutput(csvName+".csv", Context.MODE_APPEND);
                    outputStream.write(("X="+Float.toString(aX)+",Y="+Float.toString(aY)+
                            ",Z="+Float.toString(aZ)+",Walk,\n").getBytes());
                } catch(Exception e){
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });
        // Create a click listener for our train button.
        buttonTrain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().contains("KNN")){

                } else if (editText.getText().toString().contains("Bayes")){
                    textView.setText("WIP");
                }
                else {
                    textView.setText("Please enter string with 'KNN' or 'Bayes', which you want to train\n");
                }
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
                            outputStream.write(("X="+Float.toString(aX_Range)+",Y="+Float.toString(aY_Range)+
                                    ",Z="+Float.toString(aZ_Range)+",Walk,\n").getBytes());
                            refreshed = true;
                        } else if(csvName.contains("motionS")){
                            outputStream.write(("X="+Float.toString(aX_Range)+",Y="+Float.toString(aY_Range)+
                                    ",Z="+Float.toString(aZ_Range)+",Still,\n").getBytes());
                            refreshed = true;
                        } else if (csvName.contains("locate")){
                            String[] cellNo = csvName.split("Cell"); //cellNo[1] is cell no
                            // Start a wifi scan.
                            wifiManager.startScan();
                            // Store results in a list.
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            // Write results to a label
                            for (ScanResult scanResult : scanResults) {
                                outputStream.write(("BSSID="+scanResult.BSSID+",RSSI="+
                                        scanResult.level+",Cell="+cellNo[1]+",\n").getBytes());
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
        // get the the x,y,z values of the accelerometer
        if (refreshed) {
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];
            aX_Max = aX;
            aY_Max = aY;
            aZ_Max = aZ;
            aX_Min = aX;
            aY_Min = aY;
            aZ_Min = aZ;
        } else {
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];
            aX_Max = Math.max(aX_Max, aX);
            aY_Max = Math.max(aY_Max, aY);
            aZ_Max = Math.max(aZ_Max, aZ);
            aX_Min = Math.min(aX_Min, aX);
            aY_Min = Math.min(aY_Min, aY);
            aZ_Min = Math.min(aZ_Min, aZ);
            aX_Range = aX_Max-aX_Min;
            aY_Range = aY_Max-aY_Min;
            aZ_Range = aZ_Max-aZ_Min;
        }
        refreshed = false;
    }

    private Map checkMotionKNN(String fileTest, String fileTrain){
        Map<String, String> checked = new HashMap<String, String>();
        String valueS,valueW,valueOS,valueOW;
        int valS=0,valW=0,valOS=0,valOW=0;
        //code to read file and compare for knn here
        //do euclidean dist? compare difference x,y,z then add tgt
        valueS = Integer.toString(valS);
        valueW = Integer.toString(valW);
        valueOS = Integer.toString(valOS);
        valueOW = Integer.toString(valOW);
        checked.put("Still",valueS);
        checked.put("Walk",valueW);
        checked.put("ObsStill",valueOS);
        checked.put("ObsWalk",valueOW);

        return checked;
    }

    private int[][] checkCellKNN(String fileTest, String fileTrain){
        int checked[][]={{}}; //4x4 confusion matrix
        //read file and do the hamming dist
        return checked;
    }


    private void prepareLocateDataKNN(String fileTest, String fileTrain){
        File test = new File(getApplicationContext().getFilesDir(), fileTest);
        File train = new File(getApplicationContext().getFilesDir(), fileTrain);
        if (test.exists()&&train.exists()){

        } else{

        }
    }
}