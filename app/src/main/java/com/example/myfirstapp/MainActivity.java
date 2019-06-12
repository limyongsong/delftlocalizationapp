package com.example.myfirstapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
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
import android.widget.Toast;

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
import java.util.Random;
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
    private Sensor stepCounter; //LG stylus 3 doesnt seem to have this
    private Sensor rotationVector;
    private Sensor gyroscope;
    float[] orientationVals = new float[3]; //if orientationvals[0] (yaw) changes more than 90 we made a turn
                                            //when turn left will minus when turn right will add, it flips at 180/-180
                                            // pitch, orientationvals[1], should be held at around 90 degrees
    int[] allParticles = new int [25000]; //every 5 is the left top right bottom direction
    float[] gyroVals = new float[3];
    boolean resetGyro = false;
    int PFcount = 0;
    int turnedDegree = 0;
    int width, height;
    ArrayList<Integer> PFCellCount = new ArrayList<Integer>();
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

    private float steps = 0;

    private float aX_Range = 0, aY_Range = 0, aZ_Range = 0, aX_Max = 0, aY_Max = 0, aZ_Max = 0, aX_Min = 0, aY_Min = 0, aZ_Min = 0;
    //probabilities of each cell
    private double Pstart = 0.0625;
    private double[] cellProb = new double[16];
    /**
     * The text view.
     */
    private TextView textView, textView1, textView2, textView3, textView4, textView5, textView6,
            textView7, textView8, textView9, textView10, textView11, textView12, textView13,
            textView14, textView15, textView16, textViewMotion, textViewBestCell, textViewRotation;
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
    private ShapeDrawable[] drawable = new ShapeDrawable[5000];

    /** The table for bayes**/
    private TableLayout tableLayout;

    FileOutputStream outputStream;

    boolean started = false, refreshed = true;
    boolean bayesCheck = false, PFCheck = false;

    Map<String, String[]> bayesLookup = new HashMap<String, String[]>();

    /**
     * The walls.
     */
    private List<ShapeDrawable> walls;
    /**
     * The cells. should be white
     */
    private List<ShapeDrawable> cells;

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
        textViewRotation = (TextView) findViewById(R.id.currentRotation);
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
        width = size.x+2600;
        height = size.y;

        //Create the canvas
        canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        //2880x572 pixels (scaled 40 times from 72x14.3m) walls 5-10pixel thick
        walls = new ArrayList<>();
        cells = new ArrayList<>();
//Left of Cell 16 and Cell 14
        ShapeDrawable d1 = new ShapeDrawable(new RectShape());
        d1.setBounds(width/2-1445, height/2-291, width/2-965, height/2-42);
        ShapeDrawable d2 = new ShapeDrawable(new RectShape());
        d2.setBounds(width/2-1445, height/2+42, width/2-965, height/2+291);

//North Rooms - Left + Up + Right

//Cell 16
        ShapeDrawable d3 = new ShapeDrawable(new RectShape());
        d3.setBounds(width/2-965, height/2-291, width/2-955, height/2-42);
        ShapeDrawable d4 = new ShapeDrawable(new RectShape());
        d4.setBounds(width/2-955, height/2-291, width/2-805, height/2-281);
        ShapeDrawable d5 = new ShapeDrawable(new RectShape());
        d5.setBounds(width/2-805, height/2-291, width/2-800, height/2-42);

        ShapeDrawable c16 = new ShapeDrawable(new RectShape());
        c16.getPaint().setColor(Color.GREEN);
        c16.setBounds(width/2-955, height/2-281,width/2-800,height/2-42);

//Cell 13
        ShapeDrawable d6 = new ShapeDrawable(new RectShape());
        d6.setBounds(width/2-800, height/2-291, width/2-795, height/2-42);
        ShapeDrawable d7 = new ShapeDrawable(new RectShape());
        d7.setBounds(width/2-795, height/2-291, width/2-645, height/2-281);
        ShapeDrawable d8 = new ShapeDrawable(new RectShape());
        d8.setBounds(width/2-645, height/2-291, width/2-640, height/2-42);

        ShapeDrawable c13 = new ShapeDrawable(new RectShape());
        c13.getPaint().setColor(Color.GREEN);
        c13.setBounds(width/2-795, height/2-281,width/2-640,height/2-42);

//Cell 11
        ShapeDrawable d9 = new ShapeDrawable(new RectShape());
        d9.setBounds(width/2-640, height/2-291, width/2-635, height/2-42);
        ShapeDrawable d10 = new ShapeDrawable(new RectShape());
        d10.setBounds(width/2-635, height/2-291, width/2-485, height/2-281);
        ShapeDrawable d11 = new ShapeDrawable(new RectShape());
        d11.setBounds(width/2-485, height/2-291, width/2-480, height/2-42);

        ShapeDrawable c11 = new ShapeDrawable(new RectShape());
        c11.getPaint().setColor(Color.GREEN);
        c11.setBounds(width/2-635, height/2-281,width/2-480,height/2-42);

//Cell 3
        ShapeDrawable d12 = new ShapeDrawable(new RectShape());
        d12.setBounds(width/2+480, height/2-291, width/2+485, height/2-42);
        ShapeDrawable d13 = new ShapeDrawable(new RectShape());
        d13.setBounds(width/2+485, height/2-291, width/2+635, height/2-281);
        ShapeDrawable d14 = new ShapeDrawable(new RectShape());
        d14.setBounds(width/2+635, height/2-291, width/2+640, height/2-42);

        ShapeDrawable c3 = new ShapeDrawable(new RectShape());
        c3.getPaint().setColor(Color.GREEN);
        c3.setBounds(width/2+485, height/2-281,width/2+640,height/2-42);

//Between Cell 11 and 3
        ShapeDrawable d15 = new ShapeDrawable(new RectShape());
        d15.setBounds(width/2-480, height/2-291, width/2+480, height/2-42);

//Right of Cell 3
        ShapeDrawable d16 = new ShapeDrawable(new RectShape());
        d16.setBounds(width/2+635, height/2-291, width/2+1445, height/2-42);

//South Rooms - Left + Down + Right

//Cell 14
        ShapeDrawable d18 = new ShapeDrawable(new RectShape());
        d18.setBounds(width/2-965, height/2+161, width/2-955, height/2+291);
        ShapeDrawable d19 = new ShapeDrawable(new RectShape());
        d19.setBounds(width/2-965, height/2+161, width/2-875, height/2+171);
        //block for passageway
        ShapeDrawable d20 = new ShapeDrawable(new RectShape());
        d20.setBounds(width/2-965, height/2+42, width/2-875, height/2+171);
        //bottom of cell14
        ShapeDrawable d21 = new ShapeDrawable(new RectShape());
        d21.setBounds(width/2-965, height/2+281, width/2-800, height/2+291);

        ShapeDrawable c14 = new ShapeDrawable(new RectShape());
        c14.getPaint().setColor(Color.GREEN);
        c14.setBounds(width/2-955, height/2+42,width/2-800,height/2+281);

//Right of Cell 14
        ShapeDrawable d22 = new ShapeDrawable(new RectShape());
        d22.setBounds(width/2-800, height/2+42, width/2+475, height/2+291);

//Cell 1
        ShapeDrawable d23 = new ShapeDrawable(new RectShape());
        d23.setBounds(width/2+475, height/2+42, width/2+485, height/2+291);
        ShapeDrawable d24 = new ShapeDrawable(new RectShape());
        d24.setBounds(width/2+475, height/2+281, width/2+635, height/2+291);

        ShapeDrawable c1 = new ShapeDrawable(new RectShape());
        c1.getPaint().setColor(Color.GREEN);
        c1.setBounds(width/2+485, height/2+42,width/2+635,height/2+281);

//Right of Cell 1
        ShapeDrawable d25 = new ShapeDrawable(new RectShape());
        d25.setBounds(width/2+635, height/2+42, width/2+1445, height/2+291);

//Left corridor end
        ShapeDrawable d26 = new ShapeDrawable(new RectShape());
        d26.setBounds(width/2-1445, height/2-42, width/2-1435, height/2+42);

//Right corridor end
        ShapeDrawable d27 = new ShapeDrawable(new RectShape());
        d27.setBounds(width/2+1435, height/2-42, width/2+1445, height/2+42);

        //4 surrounding barriers right, left up down
        ShapeDrawable d28 = new ShapeDrawable(new RectShape());
        d28.setBounds(width/2+1445, height/2-500, width/2+1645, height/2+500);
        ShapeDrawable d29 = new ShapeDrawable(new RectShape());
        d29.setBounds(width/2-1645, height/2-500, width/2-1445, height/2+500);
        ShapeDrawable d30 = new ShapeDrawable(new RectShape());
        d30.setBounds(width/2-1445, height/2-500, width/2+1445, height/2-291);
        ShapeDrawable d31 = new ShapeDrawable(new RectShape());
        d31.setBounds(width/2-1445, height/2+291, width/2+1445, height/2+500);

        //cells in corridoor
        ShapeDrawable c15 = new ShapeDrawable(new RectShape());
        c15.getPaint().setColor(Color.GREEN);
        c15.setBounds(width/2-960, height/2-42,width/2-800,height/2+42);
        ShapeDrawable c12 = new ShapeDrawable(new RectShape());
        c12.getPaint().setColor(Color.GREEN);
        c12.setBounds(width/2-800, height/2-42,width/2-640,height/2+42);
        ShapeDrawable c10 = new ShapeDrawable(new RectShape());
        c10.getPaint().setColor(Color.GREEN);
        c10.setBounds(width/2-640, height/2-42,width/2-480,height/2+42);
        ShapeDrawable c9 = new ShapeDrawable(new RectShape());
        c9.getPaint().setColor(Color.GREEN);
        c9.setBounds(width/2-480, height/2-42,width/2-320,height/2+42);
        ShapeDrawable c8 = new ShapeDrawable(new RectShape());
        c8.getPaint().setColor(Color.GREEN);
        c8.setBounds(width/2-320, height/2-42,width/2-160,height/2+42);
        ShapeDrawable c7 = new ShapeDrawable(new RectShape());
        c7.getPaint().setColor(Color.GREEN);
        c7.setBounds(width/2-160, height/2-42,width/2,height/2+42);
        ShapeDrawable c6 = new ShapeDrawable(new RectShape());
        c6.getPaint().setColor(Color.GREEN);
        c6.setBounds(width/2, height/2-42,width/2+160,height/2+42);
        ShapeDrawable c5 = new ShapeDrawable(new RectShape());
        c5.getPaint().setColor(Color.GREEN);
        c5.setBounds(width/2+160, height/2-42,width/2+320,height/2+42);
        ShapeDrawable c4 = new ShapeDrawable(new RectShape());
        c4.getPaint().setColor(Color.GREEN);
        c4.setBounds(width/2+320, height/2-42,width/2+480,height/2+42);
        ShapeDrawable c2 = new ShapeDrawable(new RectShape());
        c2.getPaint().setColor(Color.GREEN);
        c2.setBounds(width/2+480, height/2-42,width/2+640,height/2+42);

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
        walls.add(d16); //d17 was unnecessary and removed
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
        walls.add(d28);
        walls.add(d29);
        walls.add(d30);
        walls.add(d31);

        cells.add(c1);
        cells.add(c2);
        cells.add(c3);
        cells.add(c4);
        cells.add(c5);
        cells.add(c6);
        cells.add(c7);
        cells.add(c8);
        cells.add(c9);
        cells.add(c10);
        cells.add(c11);
        cells.add(c12);
        cells.add(c13);
        cells.add(c14);
        cells.add(c15);
        cells.add(c16);

        for(ShapeDrawable cell : cells)
            cell.draw(canvas);
        //particles only drawn when PFCheck true
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
            Toast.makeText(this, "Type accelerometer sensor not found", Toast.LENGTH_SHORT).show();
        }

        // if the default gyro
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // set accelerometer
            gyroscope = sensorManager
                    .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No gyroscope
            Toast.makeText(this, "Type gyroscope sensor not found", Toast.LENGTH_SHORT).show();
        }
        // if the default rotation vector
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            // set accelerometer
            rotationVector = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, rotationVector,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No rotation vector
            Toast.makeText(this, "Type rotation sensor not found", Toast.LENGTH_SHORT).show();
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
                    bayesCheck = true;
                    PFCheck = false;
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
                    textViewBestCell.setText("");
                    textView.setText("Please select PF using 'Locate Me' button\n");
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
                    bayesCheck = true;
                    PFCheck = false;
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
                    bayesCheck = false;
                    PFCheck = true;
                    // create a drawable object of the point
                    //will need a for loop for 5000-10000 points
                    for(int i =0; i<5000;i++) {
                        drawable[i] = new ShapeDrawable(new OvalShape());
                        drawable[i].getPaint().setColor(Color.RED);
                        int randomWidth = new Random().nextInt(2880) - 1440;
                        int randomHeight = new Random().nextInt(572) - 286;
                        //int randomDegree = new Random().nextInt(90)-45; // direction should be random +/-45 degree
                        allParticles[i*5]=width / 2 + randomWidth -3;
                        allParticles[i*5+1]=height / 2 + randomHeight - 3;
                        allParticles[i*5+2]=width / 2 + randomWidth + 3;
                        allParticles[i*5+3]=height / 2 + randomHeight + 3;
                        //allParticles[i*5+4]= randomDegree+ Math.round(orientationVals[0]);// will store the yaw
                        turnedDegree = Math.round(orientationVals[0]-180);//first turneddegree
                        if (turnedDegree+180>300) { //rotation needs work
                            allParticles[i*5+4] = 3; //1 right, 2 up, 3 left, 4 down based on onsite check
                        } else if (turnedDegree+180>200){
                            allParticles[i*5+4] = 4;
                        } else if (turnedDegree+180>100){
                            allParticles[i*5+4] = 1;
                        } else if (turnedDegree+180>0){
                            allParticles[i*5+4] = 2;
                        }
                        drawable[i].setBounds(allParticles[i*5] , allParticles[i*5+1], allParticles[i*5+2], allParticles[i*5+3]);
                        if (isCollision(drawable[i])){
                            i--;
                        }
                    }
                    // draw the objects
                    for (int i=0; i<5000;i++) {
                        drawable[i].draw(canvas);
                    }
                    textViewBestCell.setText("");
                    textView.setText("PF selected\n");
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
                    if(PFCheck){
                        PFcount++;
                        //changedDegree = Math.round(orientationVals[0]-180) - turnedDegree; //if negative means turned left
                        if (!resetGyro && PFcount > 1) {
                            if (gyroVals[2] > 1) { //offset? //rotation needs work vector >90, gyro <-1 for turn right, not sure why is oppsite when i try
                                for (int i = 0; i < 5000; i++) {
                                    allParticles[i * 5 + 4] += 1; //turn 90degree right
                                    if (allParticles[i * 5 + 4] > 4) {
                                        allParticles[i * 5 + 4] -= 4;
                                    }
                                }
                                resetGyro = true;
                                gyroVals[2] = 0;
                                //turnedDegree = Math.round(orientationVals[0]-180);
                            } else if (gyroVals[2] < -1) {
                                for (int i = 0; i < 5000; i++) {
                                    allParticles[i * 5 + 4] -= 1; //turn 90degree left
                                    if (allParticles[i * 5 + 4] < 1) {
                                        allParticles[i * 5 + 4] += 4;
                                    }
                                }
                                resetGyro = true;
                                gyroVals[2] = 0;
                                //turnedDegree = Math.round(orientationVals[0]-180);
                            }
                        }
                    }
                } else if (checkMotionKNN(aX_Range, aY_Range, aZ_Range, "combinedSW.csv") == 1) {
                    refreshed = true;
                    textViewMotion.setText("Walk");//come in here every sec when walking
                    if (PFCheck) {
                        PFcount++;
                        if (!resetGyro && PFcount>1) {
                            if (gyroVals[2] > 1) { //offset? //rotation needs work vector >90, gyro <-1 for turn right, not sure why is oppsite when i try
                                for (int i = 0; i < 5000; i++) {
                                    allParticles[i * 5 + 4] += 1; //turn 90degree right
                                    if (allParticles[i * 5 + 4] > 4) {
                                        allParticles[i * 5 + 4] -= 4;
                                    }
                                }
                                resetGyro = true;
                                gyroVals[2] = 0;
                                //turnedDegree = Math.round(orientationVals[0]-180);
                            } else if (gyroVals[2] < -1) {
                                for (int i = 0; i < 5000; i++) {
                                    allParticles[i * 5 + 4] -= 1; //turn 90degree left
                                    if (allParticles[i * 5 + 4] < 1) {
                                        allParticles[i * 5 + 4] += 4;
                                    }
                                }
                                resetGyro = true;
                                gyroVals[2] = 0;
                                //turnedDegree = Math.round(orientationVals[0]-180);
                            }
                        }
                        //textView.setText(String.valueOf(steps));
                        for(int i=0; i<5000;i++) { //stride 0.4X1.7m = 0.68m > ~28pixels, 0.5sec per step, 2 steps = 56pix
                           if(allParticles[i*5+4]==1){
                                drawable[i].setBounds(allParticles[i*5] +=56, allParticles[i*5+1], allParticles[i*5+2] +=56, allParticles[i*5+3]);
                            } else if(allParticles[i*5+4]==2){
                                drawable[i].setBounds(allParticles[i*5] , allParticles[i*5+1]-=56, allParticles[i*5+2], allParticles[i*5+3]-=56);
                            } else if(allParticles[i*5+4]==3){
                                drawable[i].setBounds(allParticles[i*5] -=56, allParticles[i*5+1], allParticles[i*5+2]-=56, allParticles[i*5+3]);
                            } else if(allParticles[i*5+4]==4){
                                drawable[i].setBounds(allParticles[i*5] , allParticles[i*5+1]+=56, allParticles[i*5+2], allParticles[i*5+3]+=56);
                            }
                        } //need work to add collision and other stuffs
                        for (int i=0; i <5000;i++){
                            if (isCollision(drawable[i])){
                                //do work to respawn particle
                                allParticles[i*5+4]=-1; //-1 at direction means particle killed
                            }
                        }
                        int randNum;
                        int randPixX;
                        int randPixY;
                        int cellPFCurrent;
                        PFCellCount = new ArrayList<Integer>();
                        for(int i =0; i<16;i++){
                            PFCellCount.add(0);
                        }
                        for (int i=0; i <5000;i++){
                            if (allParticles[i*5+4]==-1){
                                randNum = new Random().nextInt(4999);//0-4999 respwan near someone alive
                                if (allParticles[randNum*5+4]==-1){
                                    i--; //retry
                                } else {
                                    randPixX =  new Random().nextInt(120)-60; //within +/- x pixel distance away (need to be big enough to not have too dense)
                                    randPixY =  new Random().nextInt(120)-60; //within +/- y pixel distance away
                                    allParticles[i*5]=allParticles[randNum*5]+randPixX;
                                    allParticles[i*5+1]=allParticles[randNum*5+1]+randPixY;
                                    allParticles[i*5+2]=allParticles[randNum*5+2]+randPixX;
                                    allParticles[i*5+3]=allParticles[randNum*5+3]+randPixY;
                                    allParticles[i*5+4]=allParticles[randNum*5+4];
                                    drawable[i].setBounds(allParticles[i*5], allParticles[i*5+1], allParticles[i*5+2], allParticles[i*5+3]);
                                    if(isCollision(drawable[i])){
                                        allParticles[i*5+4]=-1;
                                        i--;//retry
                                    }
                                }
                            } else {
                                cellPFCurrent = isCell(drawable[i]);
                                PFCellCount.set(cellPFCurrent, PFCellCount.get(cellPFCurrent) + 1);
                            }
                        }
                        int highestCount = 0;
                        int PFbestCell = 0;
                        for (Integer number: PFCellCount ){
                            PFbestCell++;
                            if ((number > highestCount)){
                                highestCount = number;
                                textViewBestCell.setText("Cell" + (PFbestCell));
                            }
                        }
                        canvas.drawColor(Color.WHITE); //reset canvas for redrawing
                        for(ShapeDrawable cell : cells)
                            cell.draw(canvas);
                        for(ShapeDrawable wall : walls)
                            wall.draw(canvas);
                        for(int i=0; i<5000;i++){
                            drawable[i].draw(canvas);
                        }
                        //turnedDegree = Math.round(orientationVals[0]); //when finish walking, might have new value
                    }
                } else {
                    refreshed = true;
                    textViewMotion.setText("Unknown");
                }
                h.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVector,
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
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            steps = event.values[0];
            textView.setText(String.valueOf(steps));
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE){
            if (PFCheck && PFcount>1 && (event.values[2]>1 || event.values[2]<-1)) {
                gyroVals[0] = event.values[0];
                gyroVals[1] = event.values[1];
                gyroVals[2] = event.values[2];
                resetGyro=false;
                PFcount =0; //added this count cause it keep turn too much (works sort of well, but makes it 2sec detect time)
            }
            if (resetGyro){
                gyroVals[0] = event.values[0];
                gyroVals[1] = event.values[1];
                gyroVals[2] = event.values[2];
            }
            //textView.setText("Rotate X=" + gyroVals[0] + "\nRotate Y=" + gyroVals[1]+"\nRotate Z=" + gyroVals[2]);
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            //https://stackoverflow.com/questions/14740808/android-problems-calculating-the-orientation-of-the-device
            float[] mRotationMatrix = new float[16];
            float[] mRotationMatrixNew = new float[16];
            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,
                    event.values);
            SensorManager
                    .remapCoordinateSystem(mRotationMatrix,
                            SensorManager.AXIS_X, SensorManager.AXIS_Z,
                            mRotationMatrixNew);
            SensorManager.getOrientation(mRotationMatrixNew, orientationVals);

            // Optionally convert the result from radians to degrees
            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]) +180; //make it 0-360 degree
            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);
            //compass always wrong, turn right is +, turn left is minus though, flips at -180/180degree, +180 to make it 0-360
            //(0 is north, 180 is south, 270 is west, 90 is east)

            textViewRotation.setText(" Yaw: " + orientationVals[0] + "\n Pitch (maintain @88): "
                    + orientationVals[1] + "\n Roll (maintain @150): "
                    + orientationVals[2]);
        } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
            //textView.setText("aX =" + aX + "\naY=" +aY + "\naZ="+ aZ);
        }
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
    /**
     * Determines if the drawable dot intersects with any of the walls.
     * @return True if that's true, false otherwise.
     */
    private boolean isCollision() {
        for(ShapeDrawable wall : walls) {
            for (ShapeDrawable particles : drawable) {
                if (isCollision(wall, particles))
                    return true; //need work to change to only affect that particle
            }
        }
        return false;
    }
    private boolean isCollision(ShapeDrawable drawableS) {
        for(ShapeDrawable wall : walls) {
            if(isCollision(wall,drawableS))
                return true;
        }
        return false;
    }
    /**
     * Determines if two shapes intersect.
     * @param first The first shape.
     * @param second The second shape.
     * @return True if they intersect, false otherwise.
     */
    private boolean isCollision(ShapeDrawable first, ShapeDrawable second) {
        Rect firstRect = new Rect(first.getBounds());
        return firstRect.intersect(second.getBounds());
    }

    private int isCell(ShapeDrawable drawableS){
        int cellN=0; //cell number
        for(ShapeDrawable cell : cells) {
            if(isCollision(cell,drawableS))
                return cellN;
            cellN++;
        }
        return 0; //return 0 if not in any cell
    }
}