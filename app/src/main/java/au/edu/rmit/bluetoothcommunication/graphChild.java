package au.edu.rmit.bluetoothcommunication;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import com.jjoe64.graphview.GraphView;
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


public class graphChild extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        String FILENAME = "config.txt";

        BufferedReader br = null;
        List<String> yval = new ArrayList<String>(10);
        String TAG = "graphChild";

        /** read the file and put the lines into an array
         */

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
        if( null != br )
        {
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
        for (String y: yval) {
                dataPoints.add(new DataPoint(x_count, Double.parseDouble(y)));
                x_count++;
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
        graph.addSeries(series);

    }


    public void onReturnClick(View view) {// function named in OnClick

        finish(); // close this activity.
    }
}
