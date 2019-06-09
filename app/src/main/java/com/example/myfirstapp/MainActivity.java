package com.example.myfirstapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
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
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
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
    private double[] cellProb = new double[16];
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

    /**
     * The canvas for pf
     */
    private Canvas canvas;
    ImageView canvasView;
    /**
     * The shape.
     */
    private ShapeDrawable drawable;

    /** The table for bayes**/
    private TableLayout tableLayout;

    FileOutputStream outputStream;

    boolean started = false, refreshed = true;

    Map<String, String[]> bayesLookup = new HashMap<String, String[]>();

    /**
     * The walls.
     */
    private List<ShapeDrawable> walls;

    String csvName = "";

    DecimalFormat numberFormat = new DecimalFormat("#.000");

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
        // get the screen dimensions
        //might be place for bug
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x+2200;
        int height = size.y;

        //Create the canvas
        canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        // create a drawable object of the point
        //will need a for loop for 5000-10000 points
        drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.BLUE);
        drawable.setBounds(width/2-5, height/2-5, width/2+5, height/2+5);

        //Limits for top and bottom is height/2-500 or +500, 1000~1001 pixels for 14.3m actual height
        //Limits for left and right is height/2-1440, 2880 pixels for 72m actual width
        walls = new ArrayList<>();
//Left of Cell 16 and Cell 14
        ShapeDrawable d1 = new ShapeDrawable(new RectShape());
        d1.setBounds(width/2-1445, height/2-505, width/2-965, height/2-73);
        ShapeDrawable d2 = new ShapeDrawable(new RectShape());
        d2.setBounds(width/2-1445, height/2+73, width/2-965, height/2+505);

//North Rooms - Left + Up + Right

//Cell 16
        ShapeDrawable d3 = new ShapeDrawable(new RectShape());
        d3.setBounds(width/2-965, height/2-505, width/2-955, height/2-73);
        ShapeDrawable d4 = new ShapeDrawable(new RectShape());
        d4.setBounds(width/2-955, height/2-505, width/2-805, height/2-495);
        ShapeDrawable d5 = new ShapeDrawable(new RectShape());
        d5.setBounds(width/2-805, height/2-505, width/2-800, height/2-73);

//Cell 13
        ShapeDrawable d6 = new ShapeDrawable(new RectShape());
        d6.setBounds(width/2-800, height/2-505, width/2-795, height/2-73);
        ShapeDrawable d7 = new ShapeDrawable(new RectShape());
        d7.setBounds(width/2-795, height/2-505, width/2-645, height/2-495);
        ShapeDrawable d8 = new ShapeDrawable(new RectShape());
        d8.setBounds(width/2-645, height/2-505, width/2-640, height/2-73);

//Cell 11
        ShapeDrawable d9 = new ShapeDrawable(new RectShape());
        d9.setBounds(width/2-640, height/2-505, width/2-635, height/2-73);
        ShapeDrawable d10 = new ShapeDrawable(new RectShape());
        d10.setBounds(width/2-635, height/2-505, width/2-485, height/2-495);
        ShapeDrawable d11 = new ShapeDrawable(new RectShape());
        d11.setBounds(width/2-485, height/2-505, width/2-480, height/2-73);

//Cell 3
        ShapeDrawable d12 = new ShapeDrawable(new RectShape());
        d12.setBounds(width/2+480, height/2-505, width/2+485, height/2-73);
        ShapeDrawable d13 = new ShapeDrawable(new RectShape());
        d13.setBounds(width/2+485, height/2-505, width/2+635, height/2-495);
        ShapeDrawable d14 = new ShapeDrawable(new RectShape());
        d14.setBounds(width/2+635, height/2-505, width/2+640, height/2-73);

//Between Cell 11 and 3
        ShapeDrawable d15 = new ShapeDrawable(new RectShape());
        d15.setBounds(width/2-480, height/2-505, width/2+480, height/2-73);

//Right of Cell 3
        ShapeDrawable d16 = new ShapeDrawable(new RectShape());
        d16.setBounds(width/2+635, height/2-505, width/2+1445, height/2-73);

//Left of corridor to Cell 14
        ShapeDrawable d17 = new ShapeDrawable(new RectShape());
        d17.setBounds(width/2-960, height/2-73, width/2-860, height/2-290);

//South Rooms - Left + Down + Right

//Cell 14
        ShapeDrawable d18 = new ShapeDrawable(new RectShape());
        d18.setBounds(width/2-965, height/2+285, width/2-955, height/2+505);
        ShapeDrawable d19 = new ShapeDrawable(new RectShape());
        d19.setBounds(width/2-965, height/2+285, width/2-875, height/2+295);
        //block for passageway
        ShapeDrawable d20 = new ShapeDrawable(new RectShape());
        d20.setBounds(width/2-965, height/2+73, width/2-875, height/2+295);
        //bottom of cell14
        ShapeDrawable d21 = new ShapeDrawable(new RectShape());
        d21.setBounds(width/2-965, height/2+495, width/2-800, height/2+505);


//Right of Cell 14
        ShapeDrawable d22 = new ShapeDrawable(new RectShape());
        d22.setBounds(width/2-800, height/2+73, width/2+475, height/2+505);

//Cell 1
        ShapeDrawable d23 = new ShapeDrawable(new RectShape());
        d23.setBounds(width/2+475, height/2+73, width/2+485, height/2+505);
        ShapeDrawable d24 = new ShapeDrawable(new RectShape());
        d24.setBounds(width/2+475, height/2+495, width/2+635, height/2+505);

//Right of Cell 1
        ShapeDrawable d25 = new ShapeDrawable(new RectShape());
        d25.setBounds(width/2+635, height/2+73, width/2+1445, height/2+505);

//Left corridor end
        ShapeDrawable d26 = new ShapeDrawable(new RectShape());
        d26.setBounds(width/2-1445, height/2-73, width/2-1435, height/2+73);

//Right corridor end
        ShapeDrawable d27 = new ShapeDrawable(new RectShape());
        d27.setBounds(width/2+1435, height/2-73, width/2+1445, height/2+73);

        walls.add(d1);
        walls.add(d2);
        walls.add(d3);
        walls.add(d4);
        walls.add(d5);
        walls.add(d6);
        walls.add(d7);
        walls.add(d8);
        walls.add(d9);
        walls.add(d10);
        walls.add(d11);
        walls.add(d12);
        walls.add(d13);
        walls.add(d14);
        walls.add(d15);
        walls.add(d16);
        walls.add(d17);
        walls.add(d18);
        walls.add(d19);
        walls.add(d20);
        walls.add(d21);
        walls.add(d22);
        walls.add(d23);
        walls.add(d24);
        walls.add(d25);
        walls.add(d26);
        walls.add(d27);

        // draw the objects
        drawable.draw(canvas);
        for(ShapeDrawable wall : walls)
            wall.draw(canvas);

        tableLayout = (TableLayout) findViewById(R.id.probTable);

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
                            editText.getText().toString().contains("motionS") ||
                            editText.getText().toString().contains("bayes")) {
                        csvName = editText.getText().toString();
                        textView.setText("A file " + csvName + ".csv will be created, when stop is pressed.");
                        started = true;
                        try {
                            //change append back to private after more data collection
                            outputStream = openFileOutput(csvName + ".csv", Context.MODE_APPEND);
                            //outputStream.write("Hello World test!\n".getBytes()); //for debugging
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        textView.setText("Please enter a suitable string containing 'locateCell#' " +
                                "or 'motionW/S' or 'bayesCell#'");
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
                //locate user
                if (editText.getText().toString().contains("Bayes")) {
                    canvasView.setVisibility(v.INVISIBLE);
                    tableLayout.setVisibility(v.VISIBLE);
                    cellProb = new double[16];
                    for (int i = 0; i < 16; i++) {
                        cellProb[i] = 0.0625;
                    }
                    textView1.setText(numberFormat.format(cellProb[0]));
                    textView2.setText(numberFormat.format(cellProb[1]));
                    textView3.setText(numberFormat.format(cellProb[2]));
                    textView4.setText(numberFormat.format(cellProb[3]));
                    textView5.setText(numberFormat.format(cellProb[4]));
                    textView6.setText(numberFormat.format(cellProb[5]));
                    textView7.setText(numberFormat.format(cellProb[6]));
                    textView8.setText(numberFormat.format(cellProb[7]));
                    textView9.setText(numberFormat.format(cellProb[8]));
                    textView10.setText(numberFormat.format(cellProb[9]));
                    textView11.setText(numberFormat.format(cellProb[10]));
                    textView12.setText(numberFormat.format(cellProb[11]));
                    textView13.setText(numberFormat.format(cellProb[12]));
                    textView14.setText(numberFormat.format(cellProb[13]));
                    textView15.setText(numberFormat.format(cellProb[14]));
                    textView16.setText(numberFormat.format(cellProb[15]));
                    textViewBestCell.setText("");
                    StringBuilder trainingData = new StringBuilder();
                    trainingData = combiner("ufCombinedBayes.csv", trainingData);
                    String[] trainingBssidString;
                    trainingBssidString = trainingData.toString().split("@");
                    bayesLookup.clear();
                    for (int i = 1; i < trainingBssidString.length; i = i + 2) {
                        bayesLookup.put(trainingBssidString[i], trainingBssidString[i + 1].split(","));
                    } //the values are in string, so change to double when need to calculate probability
                    textView.setText("Bayes mode selected");
                } else if (editText.getText().toString().contains("PF")) {
                    tableLayout.setVisibility(v.INVISIBLE);
                    canvasView.setVisibility(v.VISIBLE);
                    textView.setText("WIP\n");
                } else {
                    textView.setText("Please enter string with 'Bayes' or 'PF', which you want to train\n");
                }
            }
        });
        // Create a click listener for our LocateMe button, to see which cell we are at, can iterate
        buttonLocateMe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //locate user
                if (editText.getText().toString().contains("Bayes")) {
                    canvasView.setVisibility(v.INVISIBLE);
                    tableLayout.setVisibility(v.VISIBLE);
                    // Start a wifi scan.
                    wifiManager.startScan();
                    wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    // Store results in a list.
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    ArrayList<Pair<String, Integer>> currentScan =
                            new ArrayList<Pair<String, Integer>>();
                    // Write results to a label
                    for (ScanResult scanResult : scanResults) {
                        currentScan.add(new Pair<String, Integer>(scanResult.BSSID.substring(0,14), scanResult.level));
                    }
                    //sort the list to highest rssi on top
                    Collections.sort(currentScan, new Comparator<Pair<String, Integer>>() {
                        @Override
                        public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2) {
                            return o2.second.compareTo(o1.second);
                        }
                    });
                    for (Pair<String, Integer> currentBSSID : currentScan) {
                        double probWifi = 0.0; //denominator of bayes formula
                        double currentRssiProb = 0.0;
                        double[] numerator = new double[16];
                        if (bayesLookup.containsKey(currentBSSID.first) && (currentBSSID.second * -1) < 100) {//only compared ssids in training data
                            for (int i = 0; i < cellProb.length; i++) {
                                currentRssiProb = Double.parseDouble(
                                        bayesLookup.get(currentBSSID.first)[(currentBSSID.second * -1) + i * 100]);
                                probWifi += currentRssiProb*cellProb[i];
                                numerator[i] = currentRssiProb * cellProb[i];
                            }
                            for (int i = 0; i < cellProb.length; i++) {
                                cellProb[i] = numerator[i] / probWifi;
                            }
                        }
                    }
                    textView1.setText(numberFormat.format(cellProb[0]));
                    textView2.setText(numberFormat.format(cellProb[1]));
                    textView3.setText(numberFormat.format(cellProb[2]));
                    textView4.setText(numberFormat.format(cellProb[3]));
                    textView5.setText(numberFormat.format(cellProb[4]));
                    textView6.setText(numberFormat.format(cellProb[5]));
                    textView7.setText(numberFormat.format(cellProb[6]));
                    textView8.setText(numberFormat.format(cellProb[7]));
                    textView9.setText(numberFormat.format(cellProb[8]));
                    textView10.setText(numberFormat.format(cellProb[9]));
                    textView11.setText(numberFormat.format(cellProb[10]));
                    textView12.setText(numberFormat.format(cellProb[11]));
                    textView13.setText(numberFormat.format(cellProb[12]));
                    textView14.setText(numberFormat.format(cellProb[13]));
                    textView15.setText(numberFormat.format(cellProb[14]));
                    textView16.setText(numberFormat.format(cellProb[15]));
                    double highestP = 0.0;
                    for (int i = 0; i < 16; i++) {
                        if (cellProb[i] > highestP) {
                            highestP = cellProb[i];
                            textViewBestCell.setText("Cell" + (i + 1));
                        }
                    }

                    textView.setText("Iteration finished\n");
                } else if (editText.getText().toString().contains("PF")) {
                    tableLayout.setVisibility(v.INVISIBLE);
                    canvasView.setVisibility(v.VISIBLE);
                    textView.setText("WIP\n");
                } else {
                    textView.setText("Please enter string with 'Bayes' or 'PF', which you want to train\n");
                }
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
                        checkCellBayes("bayesCell1-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell2-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell3-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell4-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell5-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell6-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell7-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell8-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell9-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell10-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell11-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell12-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell13-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell14-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell15-2.csv", "ufCombinedBayes.csv");
                        checkCellBayes("bayesCell16-2.csv", "ufCombinedBayes.csv");
                        editText.setText("");
                        textView.setText("Files to create conf matrix created");
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
                    prepareLocateDataBayes("bayesCell1.csv", "bayesCell2.csv", "bayesCell3.csv",
                            "bayesCell4.csv", "bayesCell5.csv", "bayesCell6.csv", "bayesCell7.csv",
                            "bayesCell8.csv", "bayesCell9.csv", "bayesCell10.csv", "bayesCell11.csv",
                            "bayesCell12.csv", "bayesCell13.csv", "bayesCell14.csv", "bayesCell15.csv",
                            "bayesCell16.csv");
                    textView.setText("Trained bayes on all 16 cells");
                    editText.setText("");
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
                            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            // Store results in a list.
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            // Write results to a label
                            for (ScanResult scanResult : scanResults) {
                                outputStream.write(("," + scanResult.BSSID + "," + cellNo[1] + "\n").getBytes());
                            }
                            outputStream.write(";\n".getBytes()); //to show end of each sample
                        } else if (csvName.contains("bayes")) {
                            String[] cellNo = csvName.split("Cell"); //cellNo[1] is cell no
                            // Start a wifi scan.
                            wifiManager.startScan();
                            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            // Store results in a list.
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            // Write results to a label
                            for (ScanResult scanResult : scanResults) {
                                outputStream.write(("," + scanResult.BSSID + ","
                                        + scanResult.level + "," + cellNo[1] + "\n").getBytes());
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
            //sort the list to highest distance on top
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
            }
            if (count6 > highestCount) {
                highestCount = count6;
                checker = 6;
            }
            if (count9 > highestCount) {
                highestCount = count9;
                checker = 9;
            }
            if (count15 > highestCount) {
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
    //only checks 1 iteration
    private int[] checkCellBayes(String fileTest, String fileTrain) {
        int[] checked = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; //each cell corresponds to total count of cell1, cell2, etc
        //do hamming distance with KNN
        StringBuilder sbTest = new StringBuilder();
        sbTest = combiner(fileTest, sbTest);
        String[] leftoverTest;
        leftoverTest = sbTest.toString().split(";"); //split to each sample/iteration
        Map<String, String[]> bayesLookupTest = new HashMap<String, String[]>();

        StringBuilder trainingData = new StringBuilder();
        trainingData = combiner(fileTrain, trainingData);
        String[] trainingBssidString;
        trainingBssidString = trainingData.toString().split("@");
        for (int i = 1; i < trainingBssidString.length; i=i+2) {
            bayesLookupTest.put(trainingBssidString[i], trainingBssidString[i+1].split(","));
        } //the values are in string, so change to double when need to calculate probability
        double[] cellProbTest = new double[16];
        String[] leftoverTestInner;
        for(int j=0; j<leftoverTest.length; j++) { //go thorough each sample, 1 iteration
            cellProbTest = new double[16];
            for (int i = 0; i < 16; i++) {
                cellProbTest[i] = 1.0/16.0;
            }
            leftoverTestInner = leftoverTest[j].split(",");
            ArrayList<Pair<String, Integer>> currentScan =
                    new ArrayList<Pair<String, Integer>>();
            for (int i=1; i<leftoverTestInner.length; i=i+3){
                currentScan.add(new Pair<String, Integer>(leftoverTestInner[i].substring(0,14),
                        Integer.parseInt(leftoverTestInner[i+1])));
            }
            for (Pair<String, Integer> currentBSSID : currentScan) {
                double probWifi = 0.0; //denominator of bayes formula
                double currentRssiProb = 0.0;
                double[] numerator = new double[16];
                if (bayesLookupTest.containsKey(currentBSSID.first) && (currentBSSID.second * -1) < 100) {//only compared ssids in training data
                    for (int i = 0; i < cellProbTest.length; i++) {
                        currentRssiProb = Double.parseDouble(
                                bayesLookupTest.get(currentBSSID.first)[(currentBSSID.second * -1) + i * 100]);
                        probWifi += currentRssiProb*cellProbTest[i];
                        numerator[i] = currentRssiProb * cellProbTest[i];
                    }
                    for (int i = 0; i < cellProbTest.length; i++) {
                        cellProbTest[i] = numerator[i] / probWifi;
                    }
                }
            }
            double highestP = 0.0;
            int flag=0;
            int i=0;
            for (i=0; i < 16; i++) {
                if (cellProbTest[i] > highestP) {
                    highestP = cellProbTest[i];
                    flag = i;
                }
            }
            checked[flag]++;
        }
        try {
            outputStream = openFileOutput("checked_" + fileTest, Context.MODE_PRIVATE);
            outputStream.write((Arrays.toString(checked)+"\n").getBytes());
            //for debugging: cellprobtest of last sample
            //outputStream.write((Arrays.toString(cellProbTest)+"\n").getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    //combines all the cell training info, and get a unique cell list (further processing done in checkCellKNN)
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

    //combines all the bayesCell data to form a pair<bssid,pair<cell,array of rssi>>
    private void prepareLocateDataBayes(String file1, String file2, String file3, String file4,
                                        String file5, String file6, String file7, String file8,
                                        String file9, String file10, String file11, String file12,
                                        String file13, String file14, String file15, String file16) {
        if (!started) {
            StringBuilder sb = new StringBuilder();
            sb = combiner(file1, sb);
            sb = combiner(file2, sb);
            sb = combiner(file3, sb);
            sb = combiner(file4, sb);
            sb = combiner(file5, sb);
            sb = combiner(file6, sb);
            sb = combiner(file7, sb);
            sb = combiner(file8, sb);
            sb = combiner(file9, sb);
            sb = combiner(file10, sb);
            sb = combiner(file11, sb);
            sb = combiner(file12, sb);
            sb = combiner(file13, sb);
            sb = combiner(file14, sb);
            sb = combiner(file15, sb);
            sb = combiner(file16, sb);
            int[] rssiValue = new int[1600];
            String currentBssid = "";
            int currentRssi = 100;
            String[] combinedBayes = sb.toString().split(",");
            ArrayList<accessPoint> aps = new ArrayList<accessPoint>();
            for (int i = 1; i < combinedBayes.length; i++) {
                if (combinedBayes[i].length() > 16) {
                    currentBssid = combinedBayes[i].substring(0, 14);
                    if (!aps.contains(new accessPoint(currentBssid))) {
                        aps.add(new accessPoint(currentBssid)); //everytime new bssid, add it to the back but only first 14char so wont double count same ssid
                    }
                } else {
                    if (combinedBayes[i].contains("-")) {
                        currentRssi = Integer.parseInt(combinedBayes[i].replace("-", "")) - 1;
                    } else if (currentRssi < 100 && currentRssi > -1) { //at the end of each data
                        if (combinedBayes[i].contains(";")) {
                            rssiValue = aps.get(aps.indexOf(new accessPoint(currentBssid))).getRssiValue().clone();
                            rssiValue[(Integer.parseInt(combinedBayes[i].replace(";", "")) - 1) * 100 + currentRssi]++;
                            aps.get(aps.indexOf(new accessPoint(currentBssid))).setRssiValue(rssiValue);
                        } else {
                            rssiValue = aps.get(aps.indexOf(new accessPoint(currentBssid))).getRssiValue().clone();
                            rssiValue[(Integer.parseInt(combinedBayes[i]) - 1) * 100 + currentRssi]++; //cell number-1 *100 is where the rssi count is stored
                            aps.get(aps.indexOf(new accessPoint(currentBssid))).setRssiValue(rssiValue);
                        }
                    }
                }
            }
            double[] rssiProb = new double[1600];
            int totalSamples = 0;
            int flag = 0; //will be from 0 to 32, used to reset j
            for (int i = 0; i < aps.size(); i++) {
                flag = 0;
                for (int j = 0; j < 1600; j++) {
                    if ((j % 100 == 0) && (flag % 2 == 0)) {
                        totalSamples = 0;
                    }
                    if (flag % 2 == 0) {
                        totalSamples += aps.get(i).getRssiValue()[j];
                    } else if (flag % 2 == 1) {
                        rssiProb = aps.get(i).getRssiProb().clone();
                        rssiProb[j] = ((double) aps.get(i).getRssiValue()[j]) / totalSamples;
                        aps.get(i).setRssiProb(rssiProb);
                    }
                    if ((j % 100 == 99) & (flag % 2 == 0)) {//last element of the each cell
                        j = (int) (flag / 2) * 100; //reset j so we can go through again to calculate rssiProb
                        flag++;
                    } else if ((j % 100 == 99) & (flag % 2 == 1)) {
                        flag++;
                    }
                }
            }
            try {
                String string_aps = aps.toString();
                string_aps = string_aps.substring(1, string_aps.length() - 1); //remove []
                string_aps = string_aps.replaceAll(" ", "");
                outputStream = openFileOutput("ufCombinedBayes" + ".csv", Context.MODE_PRIVATE);
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

    public class accessPoint {
        private String bssid;
        private int[] rssiValue; //every cell takes 100 slots
        private double[] rssiProb;

        public accessPoint(String bssid, int[] rssiValue) {
            this.bssid = bssid;
            this.rssiValue = rssiValue;
            this.rssiProb = new double[1600];
        }

        public accessPoint(String bssid) {
            this.bssid = bssid;
            int[] temp = new int[1600];
            Arrays.fill(temp, 1);//smoothing, to prevent zero probability
            this.rssiValue = temp.clone();
            //this.rssiValue = new int[1600];
            this.rssiProb = new double[1600];
        }

        public String getBssid() {
            return bssid;
        }

        public void setBssid(String bssid) {
            this.bssid = bssid;
        }

        public int[] getRssiValue() {
            return rssiValue;
        }

        public void setRssiValue(int[] rssiValue) {
            this.rssiValue = rssiValue.clone();
        }

        public double[] getRssiProb() {
            return rssiProb;
        }

        public void setRssiProb(double[] rssiProb) {
            this.rssiProb = rssiProb.clone();
        }

        @Override
        public String toString() {
            return "@" + bssid + "@" + Arrays.toString(rssiProb) //change rssiProb to rssiValue to get count instead
                    .replace("[", "").replace("]", "");
            //result for the array string is every 100 to each cell starting from 1, will have 1600 rssi values counts
        }

        @Override //overide .contains and .indexof
        public boolean equals(Object o) {
            if (o instanceof accessPoint) {
                String toCompare = ((accessPoint) o).bssid;
                return bssid.equals(toCompare);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return bssid.hashCode();
        }
    }
}