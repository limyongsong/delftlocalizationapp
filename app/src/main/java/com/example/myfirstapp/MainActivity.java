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

import static java.util.Arrays.asList;

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
    //probabilities of each cell
    private double Pstart = 0.0625;
    private double P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16;
    /**
     * The text view.
     */
    private TextView textView, textView1, textView2, textView3, textView4, textView5, textView6,
            textView7, textView8, textView9, textView10, textView11, textView12, textView13,
            textView14, textView15, textView16, textViewMotion, textViewBestCell;
    /**
     * The edit text box.
     */
    private EditText editText;

    Button buttonStart, buttonStop, buttonInitial, buttonLocateMe, buttonTest, buttonTrain;

    FileOutputStream outputStream;

    boolean started = false, refreshed = true, Bayes = false;

    String csvName = "";

    //used to get data every certain time interval
    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond, 500=500milisecond
    Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //titleAcc = (TextView) findViewById(R.id.titleAcc);
        textView = (TextView) findViewById(R.id.textView2);
        textView1 = (TextView) findViewById(R.id.cell1);
        textView2 = (TextView) findViewById(R.id.cell2);
        textView3 = (TextView) findViewById(R.id.cell3);
        textView4 = (TextView) findViewById(R.id.cell4);
        textView5 = (TextView) findViewById(R.id.cell5);
        textView6 = (TextView) findViewById(R.id.cell6);
        textView7 = (TextView) findViewById(R.id.cell7);
        textView8 = (TextView) findViewById(R.id.cell8);
        textView9 = (TextView) findViewById(R.id.cell9);
        textView10 = (TextView) findViewById(R.id.cell10);
        textView11 = (TextView) findViewById(R.id.cell11);
        textView12 = (TextView) findViewById(R.id.cell12);
        textView13 = (TextView) findViewById(R.id.cell13);
        textView14 = (TextView) findViewById(R.id.cell14);
        textView15 = (TextView) findViewById(R.id.cell15);
        textView16 = (TextView) findViewById(R.id.cell16);
        textViewMotion = (TextView) findViewById(R.id.currentMotion);
        textViewBestCell = (TextView) findViewById(R.id.bestCell);
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
                textView1.setText(Double.toString(Pstart));
                textView2.setText(Double.toString(Pstart));
                textView3.setText(Double.toString(Pstart));
                textView4.setText(Double.toString(Pstart));
                textView5.setText(Double.toString(Pstart));
                textView6.setText(Double.toString(Pstart));
                textView7.setText(Double.toString(Pstart));
                textView8.setText(Double.toString(Pstart));
                textView9.setText(Double.toString(Pstart));
                textView10.setText(Double.toString(Pstart));
                textView11.setText(Double.toString(Pstart));
                textView12.setText(Double.toString(Pstart));
                textView13.setText(Double.toString(Pstart));
                textView14.setText(Double.toString(Pstart));
                textView15.setText(Double.toString(Pstart));
                textView16.setText(Double.toString(Pstart));
                P1 = Pstart;
                P2 = Pstart;
                P3 = Pstart;
                P4 = Pstart;
                P5 = Pstart;
                P6 = Pstart;
                P7 = Pstart;
                P8 = Pstart;
                P9 = Pstart;
                P10 = Pstart;
                P11 = Pstart;
                P12 = Pstart;
                P13 = Pstart;
                P14 = Pstart;
                P15 = Pstart;
                P16 = Pstart;
                Bayes = true;
                textView.setText("Bayes mode selected");
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
                        ArrayList<Integer> answer = new ArrayList<Integer>(); //ans[0]=predicted still,when still,
                        //ans[1]=predicted walk, when still,
                        //ans[2]=total actual still, ans[3]=predicted still, when walk,
                        //ans[4]=predicted walk, when walk,ans[5]=total actual walk
                        int[] temp;
                        temp = checkMotionKNN("motionS-2.csv", "combinedSW.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        temp = checkMotionKNN("motionW-2.csv", "combinedSW.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        try {
                            outputStream = openFileOutput("confMatMotionKNN" + ".csv", Context.MODE_PRIVATE);
                            outputStream.write(Arrays.toString(answer.toArray()).getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        answer.clear();
                        temp = checkCellKNN("locateCell2-2.csv", "combinedCells.csv", "UcombinedCells.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        temp = checkCellKNN("locateCell6-2.csv", "combinedCells.csv", "UcombinedCells.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        temp = checkCellKNN("locateCell9-2.csv", "combinedCells.csv", "UcombinedCells.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        temp = checkCellKNN("locateCell15-2.csv", "combinedCells.csv", "UcombinedCells.csv");
                        for (int i : temp) {
                            answer.add(i);
                        }
                        try {
                            outputStream = openFileOutput("confMatLocateKNN" + ".csv", Context.MODE_PRIVATE);
                            outputStream.write(Arrays.toString(answer.toArray()).getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        textView.setText("Conf Matrix stored in internal storage\n");
                        editText.setText("");
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
                                outputStream.write(("," + scanResult.BSSID + "," + cellNo[1] + "\n").getBytes());
                            }
                            outputStream.write(";\n".getBytes()); //to show end of each sample
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (checkMotionKNN(aX_Range, aY_Range, aZ_Range, "combinedSW.csv") == 0) {
                    refreshed = true;
                    textViewMotion.setText("Still");
                } else if (checkMotionKNN(aX_Range, aY_Range, aZ_Range, "combinedSW.csv") == 1) {
                    refreshed = true;
                    textViewMotion.setText("Walk");
                } else {
                    refreshed = true;
                    textViewMotion.setText("Still");
                }
                double highestP = 0.0;
                if (Bayes) {
                    if (P1 > highestP) {
                        highestP = P1;
                        textViewBestCell.setText("Cell1");
                    }
                    if (P2 > highestP) {
                        highestP = P2;
                        textViewBestCell.setText("Cell2");
                    }
                    if (P3 > highestP) {
                        highestP = P3;
                        textViewBestCell.setText("Cell3");
                    }
                    if (P4 > highestP) {
                        highestP = P4;
                        textViewBestCell.setText("Cell4");
                    }
                    if (P5 > highestP) {
                        highestP = P5;
                        textViewBestCell.setText("Cell5");
                    }
                    if (P6 > highestP) {
                        highestP = P6;
                        textViewBestCell.setText("Cell6");
                    }
                    if (P7 > highestP) {
                        highestP = P7;
                        textViewBestCell.setText("Cell7");
                    }
                    if (P8 > highestP) {
                        highestP = P8;
                        textViewBestCell.setText("Cell8");
                    }
                    if (P9 > highestP) {
                        highestP = P9;
                        textViewBestCell.setText("Cell9");
                    }
                    if (P10 > highestP) {
                        highestP = P10;
                        textViewBestCell.setText("Cell10");
                    }
                    if (P11 > highestP) {
                        highestP = P11;
                        textViewBestCell.setText("Cell11");
                    }
                    if (P12 > highestP) {
                        highestP = P12;
                        textViewBestCell.setText("Cell12");
                    }
                    if (P13 > highestP) {
                        highestP = P13;
                        textViewBestCell.setText("Cell13");
                    }
                    if (P14 > highestP) {
                        highestP = P14;
                        textViewBestCell.setText("Cell14");
                    }
                    if (P15 > highestP) {
                        highestP = P15;
                        textViewBestCell.setText("Cell15");
                    }
                    if (P16 > highestP) {
                        highestP = P16;
                        textViewBestCell.setText("Cell16");
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
        int numSamples = leftoverTest.length / 4;
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

    private int checkMotionKNN(float xT, float yT, float zT, String fileTrain) {
        int checked = 0;
        //do euclidean dist: compare difference x,y,z then add tgt, knn sqrt sample size
        StringBuilder sbTrain = new StringBuilder();
        sbTrain = combiner(fileTrain, sbTrain);
        String[] leftoverTest;
        String[] leftoverTrain;
        double x2, x, y2, y, z2, z;
        ArrayList<Pair<String, Double>> toSort =
                new ArrayList<Pair<String, Double>>();
        leftoverTrain = sbTrain.toString().split(",");
        int i = 0;
        int k = 0;
        //K is the Kth nearest neighbours
        int numSamples = leftoverTrain.length / 4;
        int K = (int) Math.sqrt(numSamples);
        if (K % 2 == 0) {
            K++; //ensure K is always odd
        }

        do {
            x2 = Float.parseFloat(leftoverTrain[k]);
            x = Math.pow(xT - x2, 2);
            y2 = Float.parseFloat(leftoverTrain[k + 1]);
            y = Math.pow(yT - y2, 2);
            z2 = Float.parseFloat(leftoverTrain[k + 2]);
            z = Math.pow(zT - z2, 2);
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
            checked = 1;
        } else {
            checked = 0;
        }


        return checked;
    }

    private int[] checkCellKNN(String fileTest, String fileTrain, String fileUnique) {
        int[] checked = {0, 0, 0, 0}; //4x4 confusion matrix
        //do hamming distance with KNN
        StringBuilder sbTest = new StringBuilder();
        sbTest = combiner(fileTest, sbTest);
        StringBuilder sbTrain = new StringBuilder();
        sbTrain = combiner(fileTrain, sbTrain);
        StringBuilder sbUnique = new StringBuilder();
        sbUnique = combiner(fileUnique, sbUnique);
        String[] leftoverTest;
        String[] leftoverTrain;
        String[] leftoverUnique;
        int totalCount2 = 0;
        int totalCount6 = 0;
        int totalCount9 = 0;
        int totalCount15 = 0;
        ArrayList<Pair<Integer, Integer>> toSort =
                new ArrayList<Pair<Integer, Integer>>();
        leftoverTest = sbTest.toString().split(",");
        leftoverTrain = sbTrain.toString().split(",");
        leftoverUnique = sbUnique.toString().split(",");
        //get a unique ssid characteristics
        int uniqueL = leftoverUnique.length;
        int size = uniqueL + 1;
        int[] allBSSID = new int[size];
        ArrayList<String> tempSList = new ArrayList<String>(Arrays.asList(leftoverUnique));
        ArrayList<int[]> hammedTrain = new ArrayList<int[]>();
        for (String training : leftoverTrain) {
            if (tempSList.contains(training) && training.length() > 16) {
                allBSSID[tempSList.indexOf(training)] = 1;
            }
            if (training.contains(";")) { //new sample
                allBSSID[uniqueL] = Integer.parseInt(training.split(";")[0]); //take only cell value
                hammedTrain.add(allBSSID);
                allBSSID = new int[size];
            }
        }
        //textView.setText(Integer.toString(hammedTrain.get(0)[6]));
        ArrayList<int[]> hammedTest = new ArrayList<int[]>();
        Arrays.fill(allBSSID, 0);
        for (String testing : leftoverTest) {
            if (tempSList.contains(testing) && testing.length() > 16) {
                allBSSID[tempSList.indexOf(testing)] = 1;
            }
            if (testing.contains("-2")) {
                allBSSID[uniqueL] = Integer.parseInt(testing.split("-2")[0]); //take only cell value
            }
            if (testing.contains(";")) { //new sample
                hammedTest.add(allBSSID);
                allBSSID = new int[size];
            }
        }
        //textView.setText(Integer.toString(hammedTrain.get(0)[uniqueL]));
/*        try {
            outputStream = openFileOutput("hammedTrain" + ".csv", Context.MODE_PRIVATE);
            outputStream.write((Arrays.toString(hammedTrain.get(0))+"\n").getBytes());
            outputStream.write((Arrays.toString(hammedTrain.get(1))+"\n").getBytes());
            outputStream.write((Arrays.toString(hammedTrain.get(2))+"\n").getBytes());
            outputStream.write((Arrays.toString(hammedTrain.get(30))+"\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        int i = 0;
        int k = 0;
        int numSamples = hammedTrain.size();
        int K = (int) Math.sqrt(numSamples);
        if (K % 2 == 0) {
            K++; //ensure K is always odd
        }
        //textView.setText(Integer.toString(hammedTrain.get(0)[uniqueL]));
        int hamDist = 0;
        do {
            do {
                for (int a = 0; a < uniqueL; a++) {
                    if (hammedTrain.get(k)[a] == hammedTest.get(i)[a]) {
                        hamDist++;
                    }
                }
                toSort.add(new Pair<Integer, Integer>(hammedTrain.get(k)[uniqueL], hamDist));
                hamDist = 0; //going to check new sample, ; was used to mark end of sample
                k += 1;
            } while (k < hammedTrain.size());
            //sort the list to lowest distance on top
            Collections.sort(toSort, new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(final Pair<Integer, Integer> o1, final Pair<Integer, Integer> o2) {
                    return o2.second.compareTo(o1.second);
                }
            });
            int count2 = 0, count6 = 0, count9 = 0, count15 = 0;
            int temp;
            for (int j = 0; j < K; j++) {
                temp = toSort.get(j).first;
                if (temp == 2) {
                    count2++;
                } else if (temp == 6) {
                    count6++;
                } else if (temp == 9) {
                    count9++;
                } else if (temp == 15) {
                    count15++;
                }
            }
            int highestCount = 0;
            int checker = 0;
            if (count2 > highestCount) {
                highestCount = count2;
                checker = 2;
            } else if (count6 > highestCount) {
                highestCount = count6;
                checker = 6;
            } else if (count9 > highestCount) {
                highestCount = count9;
                checker = 9;
            } else if (count15 > highestCount) {
                highestCount = count15;
                checker = 15;
            }

            if (checker == 2) {
                totalCount2++;
            } else if (checker == 6) {
                totalCount6++;
            } else if (checker == 9) {
                totalCount9++;
            } else if (checker == 15) {
                totalCount15++;
            }

            k = 0;
            i += 1; //due to 2 variables per sample
        } while (i < hammedTest.size());
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
            String[] getUnique = sb.toString().split(",");
            ArrayList<String> aps = new ArrayList<String>();
            for (String check : getUnique) {
                if (!aps.contains(check) && check.length() > 16) {
                    if (aps.isEmpty()) {
                        aps.add("," + check);
                    } else {
                        aps.add(check);
                    }
                }
            }
            try {
                String string_aps = aps.toString();
                string_aps = string_aps.substring(1, string_aps.length() - 1); //remove []
                string_aps = string_aps.replaceAll(" ", "");
                outputStream = openFileOutput("UcombinedCells" + ".csv", Context.MODE_PRIVATE);
                outputStream.write(string_aps.getBytes());
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