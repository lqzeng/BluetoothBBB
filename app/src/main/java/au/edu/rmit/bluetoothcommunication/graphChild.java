package au.edu.rmit.bluetoothcommunication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.Toast;


public class graphChild extends Activity {

    //create variables for use in functions

    //Button btngraphFile;
    Button btngraphVent;
    Button btngraphVent10;
    EditText editTextBuildingNumber;

    GraphView graph;

    int graph_count = 0;
    public int[] graphColour = {Color.RED, Color.BLUE, Color.YELLOW , Color.GREEN};

    public DatabaseHelper myDb = new DatabaseHelper(this);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //btngraphFile = (Button) findViewById(R.id.btngraphFile);
        btngraphVent = (Button) findViewById(R.id.btngraphVent);
        btngraphVent10 = (Button) findViewById(R.id.btngraphVent10);

        editTextBuildingNumber = (EditText)findViewById(R.id.editBuildingNumber);
        editTextBuildingNumber.setInputType(InputType.TYPE_CLASS_NUMBER);

        //graph settings
        graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setYAxisBoundsManual(true);
        //graph labels

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Data Points");
        gridLabel.setVerticalAxisTitle("Ventilation Percentage");


    }

    /** function to graph from db
     *
     */

    public void graphVent(View view){

        List<String> yval_db = new ArrayList<String>(10);

        String TAG = "graphDb";

        //get Building number from editText
        String building_number = editTextBuildingNumber.getText().toString();
        Log.d(TAG, "graphDB:" + building_number);

        if (building_number.matches("")){
            Toast.makeText(this, "Enter Building #", Toast.LENGTH_SHORT).show();
            return;
        }

        // accessing the database here
        Cursor res = myDb.getBuildingData(building_number);

        if (res.getCount() == 0) {
            // show message
            Toast.makeText(this, "Building # does not exist", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: Nothing found");
            return;
        }

        while (res.moveToNext()) {
            // this will plot the ventilation
            yval_db.add(res.getString(3));
        }

        int x_count = 0;
        List<DataPoint> dataPoints = new ArrayList<>(yval_db.size());

        for (String y : yval_db) {
            dataPoints.add(new DataPoint(x_count, Double.parseDouble(y)*100) );
            x_count = x_count+1;
        }


        Log.d(TAG, "showing dataPoints array" + dataPoints);


        //making graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
        graphRender(series);

        /*
        graph.addSeries(series);
        series.setTitle(building_number);
        graph.getLegendRenderer().setVisible(true);
        //series.setColor(Color.MAGENTA);
        */

    }

    //function to graph just most recent last 10 entries
    public void graphVent10(View view){

        List<String> yval_db = new ArrayList<String>(10);

        String TAG = "graphVent10";

        //get Building number from editText
        String building_number = editTextBuildingNumber.getText().toString();
        Log.d(TAG, "graphVent10:" + building_number);

        if (building_number.matches("")){
            Toast.makeText(this, "Enter Building #", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: Nothing found");
            return;
        }

        // accessing the database here
        Cursor res = myDb.getLast10BuildingData(building_number);

        if (res.getCount() == 0) {
            // show message
            Toast.makeText(this, "Building # does not exist", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: Nothing found");
            return;
        }

        while (res.moveToNext()) {
            // this will plot the ventilation
            yval_db.add(res.getString(3));
        }

        int x_count = 0;
        List<DataPoint> dataPoints = new ArrayList<>(yval_db.size());

        for (String y : yval_db) {
            dataPoints.add(new DataPoint(x_count, Double.parseDouble(y)*100) );
            x_count = x_count+1;
        }


        Log.d(TAG, "showing dataPoints array" + dataPoints);


        //making graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
        graphRender(series);

    }

    //function to render graph
    public void graphRender(LineGraphSeries series){

        String building_number = editTextBuildingNumber.getText().toString();
        graph.addSeries(series);
        series.setTitle(building_number);
        graph.getLegendRenderer().setVisible(true);

        series.setColor(graphColour[graph_count]);

        graph_count = graph_count + 1;
    }


    /**
     * function to graph from file
     */

    public void graphFile(View view){

        /** read the file and put the lines into an array
         */
        String FILENAME = "config.txt";
        BufferedReader br = null;
        List<String> yval = new ArrayList<String>(10);
        String TAG = "graphChild";
        FileInputStream in = null;

        try {
            in = openFileInput("config.txt");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "cannot find file");
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            Log.d(TAG, "reading file, line by line");
            while (bufferedReader.ready()) {
                line = bufferedReader.readLine();
                yval.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != br) {
            try {
                Log.d(TAG, "closing buffered reader");
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /** now transfer to a data point array for graphing */

        int x_count = 0;
        List<DataPoint> dataPoints = new ArrayList<>(yval.size());
        for (String y : yval) {
            dataPoints.add(new DataPoint(x_count, Double.parseDouble(y) * 100));
            x_count = x_count + 5;
        }

        //making graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
        graph.addSeries(series);
    }



    public void onReturnClick(View view) {// function named in OnClick

        finish(); // close this activity.
    }

    // hide keyboard method

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
