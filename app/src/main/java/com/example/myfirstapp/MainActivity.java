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
import android.util.Pair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.lang.Math;

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

    private float aX_Range = 0, aY_Range = 0, aZ_Range = 0, aX_Max = 0, aY_Max = 0, aZ_Max = 0, aX_Min = 0, aY_Min = 0, aZ_Min = 0;
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

    boolean started = false, refreshed = true;

    String csvName = "";

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
                if (!started) {
                    //File creation to store data (uncomment when algorithm done)
                    if (editText.getText().toString().contains("locate") ||
                            editText.getText().toString().contains("motionW") ||
                            editText.getText().toString().contains("motionS")) {
                        csvName = editText.getText().toString();
                        textView.setText("A file " + csvName + ".csv will be created, when stop is pressed.");
                        started = true;
                        try {
                            outputStream = openFileOutput(csvName + ".csv", Context.MODE_PRIVATE);
                            //outputStream.write("Hello World test!\n".getBytes()); //for debugging
                        } catch (Exception e) {
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
                } else {
                    textView.setText("Nothing is running");
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    fileInputStream = getApplicationContext().openFileInput("motionS.csv");
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    fileInputStream.close();
                } catch (IOException e) {
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
                if (!started) {
                    if (editText.getText().toString().contains("KNN")) {
                        int[] answer = {0, 0, 0, 0, 0, 0}; //ans[0]=predicted still,when still,
                        //ans[1]=predicted walk, when still,
                        //ans[2]=total actual still,
                        int[] temp = {0, 0, 0};
                        int[] temp2 = {0, 0, 0};
                        temp = checkMotionKNN("motionS-2.csv", "combinedSW.csv");
                        temp2 = checkMotionKNN("motionW-2.csv", "combinedSW.csv");
                        answer[0] = temp[0];
                        answer[1] = temp[1];
                        answer[2] = temp[2];
                        answer[3] = temp2[0]; //predicted still, when walk
                        answer[4] = temp2[1]; //predicted walk, when walk
                        answer[5] = temp2[2]; //total actual walk
                        textView.setText("Conf Matrix stored in internal storage\n");
                        editText.setText("");
                        try {
                            outputStream = openFileOutput("confMatMotion" + ".csv", Context.MODE_PRIVATE);
                            outputStream.write(Arrays.toString(answer).getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //checkCellKNN("locateCell2-2.csv", "combinedCells.csv");
                        //checkCellKNN("locateCell6-2.csv", "combinedCells.csv");
                        //checkCellKNN("locateCell9-2.csv", "combinedCells.csv");
                        //checkCellKNN("locateCell15-2.csv", "combinedCells.csv");
                    } else if (editText.getText().toString().contains("Bayes")) {
                        textView.setText("WIP");
                    } else {
                        textView.setText("Please enter string with 'KNN' or 'Bayes', which you want to test\n");
                    }
                }
            }
        });
        // Create a click listener for our train button.
        buttonTrain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().contains("KNN")) {
                    prepareMotionDataKNN("motionS.csv", "motionW.csv");
                    prepareLocateDataKNN("locateCell2.csv", "locateCell6.csv",
                            "locateCell9.csv", "locateCell15.csv");
                    textView.setText("Trained KNN-SW&Cell2,6,9,15");
                    editText.setText("");
                } else if (editText.getText().toString().contains("Bayes")) {
                    textView.setText("WIP");
                } else {
                    textView.setText("Please enter string with 'KNN' or 'Bayes', which you want to train\n");
                }
            }
        });
    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        //handler to save data into csv file every 1 second (https://stackoverflow.com/questions/11434056/how-to-run-a-method-every-x-seconds)
        h.postDelayed(runnable = new Runnable() {
            public void run() {
                //do something
                if (started) {
                    try {
                        //outputStream.write("10-Hello World test!\n".getBytes());//for debugging
                        if (csvName.contains("motionW")) {
                            outputStream.write((Float.toString(aX_Range) + "," + Float.toString(aY_Range) +
                                    "," + Float.toString(aZ_Range) + ",Walk,\n").getBytes());
                            refreshed = true;
                        } else if (csvName.contains("motionS")) {
                            outputStream.write((Float.toString(aX_Range) + "," + Float.toString(aY_Range) +
                                    "," + Float.toString(aZ_Range) + ",Still,\n").getBytes());
                            refreshed = true;
                        } else if (csvName.contains("locate")) {
                            String[] cellNo = csvName.split("Cell"); //cellNo[1] is cell no
                            // Start a wifi scan.
                            wifiManager.startScan();
                            // Store results in a list.
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            // Write results to a label
                            for (ScanResult scanResult : scanResults) {
                                outputStream.write((scanResult.BSSID + "," + cellNo[1]+",").getBytes());
                            }
                            outputStream.write(";".getBytes()); //to show end of each sample
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
            aX_Range = aX_Max - aX_Min;
            aY_Range = aY_Max - aY_Min;
            aZ_Range = aZ_Max - aZ_Min;
        }
        refreshed = false;
    }

    private int[] checkMotionKNN(String fileTest, String fileTrain) {
        int[] checked = {0, 0, 0};
        //do euclidean dist: compare difference x,y,z then add tgt, knn sqrt sample size
        StringBuilder sbTest = new StringBuilder();
        sbTest = combiner(fileTest, sbTest);
        StringBuilder sbTrain = new StringBuilder();
        sbTrain = combiner(fileTrain, sbTrain);
        String[] leftoverTest;
        String[] leftoverTrain;
        double x1, x2, x, y1, y2, y, z1, z2, z;
        ArrayList<Pair<String, Double>> toSort =
                new ArrayList<Pair<String, Double>>();
        leftoverTest = sbTest.toString().split(",");
        leftoverTrain = sbTrain.toString().split(",");
        int i = 0;
        int k = 0;
        int totalCountStill = 0;
        int totalCountWalk = 0;
        //K is the Kth nearest neighbours
        int numSamples = leftoverTest.length/4;
        int K = (int) Math.sqrt(numSamples);
        if (K % 2 == 0) {
            K++; //ensure K is always odd
        }
        do {
            x1 = Float.parseFloat(leftoverTest[i]);
            y1 = Float.parseFloat(leftoverTest[i + 1]);
            z1 = Float.parseFloat(leftoverTest[i + 2]);
            do {
                x2 = Float.parseFloat(leftoverTrain[k]);
                x = Math.pow(x1 - x2, 2);
                y2 = Float.parseFloat(leftoverTrain[k + 1]);
                y = Math.pow(y1 - y2, 2);
                z2 = Float.parseFloat(leftoverTrain[k + 2]);
                z = Math.pow(z1 - z2, 2);
                toSort.add(new Pair<String, Double>(leftoverTrain[k + 3], Math.sqrt(x + y + z)));
                k = k + 4;
            } while (k < leftoverTrain.length);
            int countStill = 0;
            int countWalk = 0;
            //sort the list to lowest distance on top
            Collections.sort(toSort, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(final Pair<String, Double> o1, final Pair<String, Double> o2) {
                    return o1.second.compareTo(o2.second);
                }
            });
            for (int j = 0; j < K; j++) {
                if (toSort.get(j).first.equals("Still")) {
                    countStill++;
                } else {
                    countWalk++;
                }
            }
            if (countWalk > countStill) {
                totalCountWalk++;
            } else {
                totalCountStill++;
            }
            k = 0;
            i = i + 4; // due to 4 variables per sample
        } while (i < leftoverTest.length);
        //textView.setText(toSort.toString()); //for debugging
        //textView.setText(Integer.toString(leftoverTrain.length)); //for debugging

        checked[0] = totalCountStill;//predicted still
        checked[1] = totalCountWalk;//predicted walk
        checked[2] = numSamples; //actual still/walk


        return checked;
    }

    private int[] checkCellKNN(String fileTest, String fileTrain) {
        int[] checked = {0,0,0,0}; //4x4 confusion matrix
        //do hamming distance with KNN
        StringBuilder sbTest = new StringBuilder();
        sbTest = combiner(fileTest, sbTest);
        StringBuilder sbTrain = new StringBuilder();
        sbTrain = combiner(fileTrain, sbTrain);
        String[] leftoverTest;
        String[] leftoverTrain;
        int totalCount2 = 0;
        int totalCount6 = 0;
        int totalCount9 = 0;
        int totalCount15 = 0;
        ArrayList<Pair<String, Integer>> toSort =
                new ArrayList<Pair<String, Integer>>();
        leftoverTest = sbTest.toString().split(",");
        leftoverTrain = sbTrain.toString().split(",");
        int i=0;
        int k =0;
        int numSamples = leftoverTest.length/2;
        int K = (int) Math.sqrt(numSamples);
        if (K % 2 == 0) {
            K++; //ensure K is always odd
        }
        int hamDist = 0;
        do{
            do{
                if(!(leftoverTest[i].equals(leftoverTrain[k]))){
                    hamDist++;
                }
                if(leftoverTrain[k+1].contains(";")){
                    toSort.add(new Pair<String, Integer>(leftoverTrain[k+1].split(";")[0],hamDist));
                    hamDist =0; //going to check new sample, ; was used to mark end of sample
                }
                k+=2;
            } while (k < leftoverTrain.length);
            if(leftoverTest[k+1].contains(";")) {
                //sort the list to lowest distance on top
                Collections.sort(toSort, new Comparator<Pair<String, Integer>>() {
                    @Override
                    public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2) {
                        return o1.second.compareTo(o2.second);
                    }
                });
                int count2=0,count6=0,count9=0,count15=0;
                String temp;
                for (int j = 0; j < K; j++) {
                    temp = toSort.get(j).first;
                    if (temp.equals("2")) {
                        count2++;
                    } else if (temp.equals("6")){
                        count6++;
                    } else if (temp.equals("9")){
                        count9++;
                    } else if (temp.equals("15")) {
                        count15++;
                    }
                }
                ArrayList<Pair<String,Integer>> getCount = new ArrayList<Pair<String,Integer>>();
                getCount.add(new Pair<String,Integer>("2",count2));
                getCount.add(new Pair<String,Integer>("6",count6));
                getCount.add(new Pair<String,Integer>("9",count9));
                getCount.add(new Pair<String,Integer>("15",count15));
                Collections.sort(getCount, new Comparator<Pair<String, Integer>>() {
                    @Override
                    public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2) {
                        return o1.second.compareTo(o2.second);
                    }
                });
                temp = getCount.get(0).first;
                if(temp.equals("2")){
                    totalCount2++;
                } else if(temp.equals("6")){
                    totalCount6++;
                } else if(temp.equals("9")){
                    totalCount9++;
                } else if(temp.equals("15")){
                    totalCount15++;
                }
            }
            k=0;
            i+=2; //due to 2 variables per sample
        } while(i<leftoverTest.length);
        checked[0] = totalCount2;//predicted cell2
        checked[1] = totalCount6;//predicted cell6
        checked[2] = totalCount9; //predicted cell 9
        checked[3] = totalCount15; //predicted cell 15
        return checked;
    }

    //combines still and walking training data
    private void prepareMotionDataKNN(String file1, String file2) {
        if (!started) {
            StringBuilder sb = new StringBuilder();
            sb = combiner(file1, sb);
            sb = combiner(file2, sb);
            try {
                outputStream = openFileOutput("combinedSW" + ".csv", Context.MODE_PRIVATE);
                outputStream.write(sb.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //combines all the cell training info
    private void prepareLocateDataKNN(String file1, String file2, String file3, String file4) {
        if (!started) {
            StringBuilder sb = new StringBuilder();
            sb = combiner(file1, sb);
            sb = combiner(file2, sb);
            sb = combiner(file3, sb);
            sb = combiner(file4, sb);
            try {
                outputStream = openFileOutput("combinedCells" + ".csv", Context.MODE_PRIVATE);
                outputStream.write(sb.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private StringBuilder combiner(String file, StringBuilder sb) {
        File first = new File(getApplicationContext().getFilesDir(), file);
        if (first.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = getApplicationContext().openFileInput(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb;
    }

}