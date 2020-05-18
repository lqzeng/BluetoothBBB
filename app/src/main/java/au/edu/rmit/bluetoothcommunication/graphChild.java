package au.edu.rmit.bluetoothcommunication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.Toast;


public class graphChild extends Activity {

    //create variables for use in functions

    Button btngraphFile;
    Button btngraphDB;

    GraphView graph;

    public DatabaseHelper myDb = new DatabaseHelper(this);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        btngraphFile = (Button) findViewById(R.id.btngraphFile);
        btngraphDB = (Button) findViewById(R.id.btngraphDB);

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
        gridLabel.setHorizontalAxisTitle("Time Period");
        gridLabel.setVerticalAxisTitle("Ventilation Percentage");



    }

    /** function to graph from db
     *
     */

    public void graphDB(View view){

        List<String> yval_db = new ArrayList<String>(10);


        String TAG = "graphDb";

        // accessing the database here
        //Cursor res = myDb.getAllData();
        Cursor res = myDb.getBuildingData("80");
        if (res.getCount() == 0) {
            // show message
            Log.d(TAG, "Error: Nothing found");
            return;
        }

        //StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            //Log.d(TAG, "reading db:" + res.getString(3));
            yval_db.add(res.getString(3));
        }

        int x_count = 0;
        List<DataPoint> dataPoints = new ArrayList<>(yval_db.size());

        for (String y : yval_db) {
            dataPoints.add(new DataPoint(x_count, Double.parseDouble(y)) );
            x_count = x_count+5;
        }


        Log.d(TAG, "showing dataPoints array" + dataPoints);

        //making graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
        graph.addSeries(series);
        series.setColor(Color.MAGENTA);


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
}
