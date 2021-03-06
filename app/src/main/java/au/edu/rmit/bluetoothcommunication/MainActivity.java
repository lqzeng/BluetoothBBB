package au.edu.rmit.bluetoothcommunication;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;


//MAC ADDRESS OF PHONE  A0:93:47:65:D0:26

public class MainActivity extends AppCompatActivity implements AdapterView.OnClickListener{
    private static final String TAG = "MainActivity";

    // Create local variables
    Button btnReadFile;
    Button btnDeleteFileContents;
    Button btnBT;
    Button btnToggleGreenLed;
    Button btnToggleRedLed;
    Button btnTogglePotOn;
    Button btnUpdateMain;
    Button btnUpdateSpecific;

    TextView textName;
    TextView textVentilation;
    TextView textSensor;
    TextView textTimestamp;
    EditText editTextReceive;
    EditText editTextUpdate;


    public DatabaseHelper myDb = new DatabaseHelper(this);


    @Override
    public void onClick(View v) {
        //do nothing here for child extension
    }

    // create child object for unregistering receivers
    BTChild myBTChild = new BTChild();


    // create enum for BB
    public enum BB {UNKNOWN_LED, GREEN_LED, RED_LED, POT_ON};

    // socket for control
    private BluetoothSocket _socket = null;

    private static final int REQUEST_CODE = 10; // Code to send to child, and expect back.
    private static final int REQUEST_CODE2 = 20;
    private static final int REQUEST_CODE3 = 30;

    // bool for BT conncted or not
    private boolean bt_connected = false;

    // bool for sensors on off
    private boolean vent_on = false;
    private boolean sense_on = false;
    private boolean recv_on = false;


    /** start methods **/

    // onCreate
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create Button pointers

        //btnReadFile = (Button) findViewById(R.id.btnReadFile);
        //btnDeleteFileContents = (Button) findViewById(R.id.btnDeleteFileContents);
        btnBT = (Button) findViewById(R.id.btnBT);

        // change these names later for consistency
        btnToggleGreenLed = (Button) findViewById(R.id.buttonToggleGreenLed);
        btnToggleRedLed = (Button) findViewById(R.id.buttonToggleRedLed);
        btnTogglePotOn = (Button) findViewById(R.id.buttonTogglePotOn);

        btnUpdateMain = (Button) findViewById(R.id.btnUpdateMain);
        btnUpdateSpecific = (Button) findViewById(R.id.btnUpdateSpecific);

        // create textview pointers
        textName = (TextView)findViewById(R.id.textName);
        textVentilation = (TextView)findViewById(R.id.textVentilation);
        textSensor = (TextView)findViewById(R.id.textSensor);
        textTimestamp = (TextView)findViewById(R.id.textTimestamp);

        //create editText pointer
        editTextReceive = (EditText)findViewById(R.id.editTextReceiveLabel);
        editTextReceive.setInputType(InputType.TYPE_CLASS_NUMBER);

        editTextUpdate = (EditText)findViewById(R.id.editTextUpdate);
        editTextUpdate.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    // onClick method for updating textVent and sensor


    // onClick method for reading file

    public void btnReadFile(View view){

        btnReadFile.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d(TAG, "onbtnReadFileClick: reading file.");
                                            readFromFile(getApplicationContext());

                                        }
                                    }
        );
    }

    // onClick for deleting file contents

    public void btnDeleteFileContents(View view){

        btnDeleteFileContents.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               Log.d(TAG, "btnDeleteFileContents: clearing file contents.");
                                               deleteFileContents();
                                           }
                                       }
        );
    }


    /** this will be the function to send messages to the BBB, called in onBBBClick
     *
     */

    class WriteReadBBB implements Runnable {
        public final String LOG = getClass().getName();

        private final BB _BB;
        private final BluetoothSocket _socket;

        private Reader _reader;
        private Writer _writer;

        //private final StringBuilder _stringBuilder = new StringBuilder();
        String building_number = editTextReceive.getText().toString();

        WriteReadBBB(BluetoothSocket socket, BB BB) {
            _socket = socket;
            _BB = BB;
        }

        public void run() {
            try {
                _writer = new OutputStreamWriter(_socket.getOutputStream(), "UTF-8");

                switch(_BB) {
                    case GREEN_LED:
                        Log.i(LOG, "toggle ventilation");
                        _writer.write("ventilation\n");
                        _writer.flush();
                        break;
                    case RED_LED:
                        Log.i(LOG, "toggle traffic sensor");
                        _writer.write("sensor\n");
                        _writer.flush();
                        break;
                    case POT_ON:
                        // add ventilation from edit text receive label
                        // checking for empty
                        if (building_number.matches("")){
                            Log.d(TAG, "no buildling number");
                            Toast.makeText(MainActivity.this, "Enter Building #", Toast.LENGTH_SHORT).show();

                            //end function early
                            break;
                        }

                        Log.i(LOG, "toggle ventilation data receive");

                        _writer.write("receive_data "+ building_number + "\n");
                        _writer.flush();
                        break;
                }


            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /** this is the onCLick function for the BBB interface **/

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onBBBClick(View view) {
        WriteReadBBB writeRead;
        String building_number = editTextReceive.getText().toString();
        //get socket from BCS
        _socket = BluetoothConnectionService.socketForMain;

        switch (view.getId()) {
            case R.id.buttonToggleGreenLed:
                writeRead = new WriteReadBBB(_socket, BB.GREEN_LED);
                new Thread(writeRead).start();

                if (vent_on == false){
                    btnToggleGreenLed.setBackgroundResource(R.color.green);
                    vent_on = true;
                    Toast.makeText(MainActivity.this, "Ventilation ON", Toast.LENGTH_SHORT).show();
                }
                else {
                    btnToggleGreenLed.setBackgroundResource(android.R.drawable.btn_default);
                    vent_on = false;
                    Toast.makeText(MainActivity.this, "Ventilation OFF", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.buttonToggleRedLed:
                writeRead = new WriteReadBBB(_socket, BB.RED_LED);
                new Thread(writeRead).start();

                if (sense_on == false){
                    btnToggleRedLed.setBackgroundResource(R.color.red);
                    sense_on = true;
                    Toast.makeText(MainActivity.this, "SENSOR ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    btnToggleRedLed.setBackgroundResource(android.R.drawable.btn_default);
                    sense_on = false;
                    Toast.makeText(MainActivity.this, "SENSOR OFF", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.buttonTogglePotOn:
                writeRead = new WriteReadBBB(_socket, BB.POT_ON);
                new Thread(writeRead).start();
                if (recv_on == false){
                    if (building_number.matches("")){
                        Log.d(TAG, "no buildling number");
                        Toast.makeText(MainActivity.this, "Enter Building #", Toast.LENGTH_SHORT).show();

                        //end function early
                        break;
                    }
                    btnTogglePotOn.setBackgroundResource(R.color.orange);
                    recv_on = true;
                    Toast.makeText(MainActivity.this, "RECEIVE DATA ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    btnTogglePotOn.setBackgroundResource(android.R.drawable.btn_default);
                    recv_on = false;
                    Toast.makeText(MainActivity.this, "RECEIVE DATA OFF", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.i(TAG, "unknown click event");
        }

    }

    /**
     * update the textviews in main
     */

    // method to get the most recent data entry to database

    public void updateData(View view){

        Cursor res = myDb.getLastData();
        // now get the last database entry for these textviews
        textName.setText(res.getString(1));
        textSensor.setText(res.getString(2));
        //converting to a percentage.
        textVentilation.setText(Double.parseDouble(res.getString(3)) * 100 + " %");

        //get timestamp, convert tiem to just seconds
        long time = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(time);
        textTimestamp.setText(timestamp.toString());
        //Log.d(TAG,"updateData: " + timestamp);
    }

    //method to get specific last building number entry
    public void updateDataSpecific(View view){

        //get Building number from editText
        String building_number = editTextUpdate.getText().toString();
        Log.d(TAG, "updateDataSpefic:" + building_number);

        // checking for empty
        if (building_number.matches("")){
            Toast.makeText(this, "Enter Building #", Toast.LENGTH_SHORT).show();

            //end function early
            return;
        }

        Cursor res = myDb.getLastBuildingData(building_number);

        if (res.getCount()==0) {
            Toast.makeText(this, "Building # does not exist", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: Nothing found");
            return;
        }

        // now get the last database entry for these textviews
        textName.setText(res.getString(1));
        textSensor.setText(res.getString(2));
        //converting to a percentage.
        textVentilation.setText(Double.parseDouble(res.getString(3)) * 100 + " %");

        //get timestamp, convert tiem to just seconds
        long time = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(time);
        textTimestamp.setText(timestamp.toString());
        //Log.d(TAG,"updateData: " + timestamp);

    }


    /**
    read from data file
     */

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                Log.d(TAG, "FileRead: " + ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    // function to delete file contents

    private void deleteFileContents()
    {
        try{
            String FILENAME = "config.txt";
            OutputStreamWriter clearStreamWriter = new OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_PRIVATE));
            clearStreamWriter.write("");
            Log.d(TAG, "deleteFileContents: successfully cleared " + FILENAME);
            clearStreamWriter.close();
        }
        catch (IOException e){
            Log.e("Exception", "File clear failed: " + e.toString());
        }
    }

    /** code to move to SQL child page **/

    public void toSQLChild(View view) {// function named in OnClick

        //savedStateText = currentStateText;
        Intent myIntent = new Intent(MainActivity.this, SQLChild.class);

        //myIntent.putExtra("integervalue", integer); //sending over some info integer to child
        //startActivity(myIntent); //if do not need to get data back.

        Log.d(TAG, "toSQLChild: moving to SQL child");
        startActivityForResult(myIntent, REQUEST_CODE);
        // come back via onResume() event.
    }


    public void toBTChild(View view){

        Intent myIntent = new Intent(MainActivity.this, BTChild.class);
        myIntent.putExtra("bt_connected", bt_connected);
        Log.d(TAG, "sending bt_connected = " + bt_connected);
        startActivityForResult(myIntent, REQUEST_CODE3);
    }

    public void toGraphChild(View view){

        Intent myIntent = new Intent(MainActivity.this, graphChild.class);
        startActivityForResult(myIntent, REQUEST_CODE2);
    }

    //--- Handle returned data from child.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent replyIntent) {

        // just debugging here or do something with data passed back

        Log.d(TAG, "onActivityResult: returning from secondary activity");

        super.onActivityResult(requestCode, resultCode, replyIntent);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE3) { // right child and OK result.

            //getting bt_connected flag back from child
            if (replyIntent.hasExtra("bt_connected")) {

                bt_connected = replyIntent.getBooleanExtra("bt_connected", false);
                Log.d(TAG, "onActivityResult: bt_connected = " + bt_connected);

                if (bt_connected ==true){
                    btnBT.setBackgroundResource(R.color.blue);
                } else {
                    btnBT.setBackgroundResource(android.R.drawable.btn_default);

                    // if no bluetooth connection just covers the buttons lighting up

                    btnToggleGreenLed.setBackgroundResource(android.R.drawable.btn_default);
                    vent_on = false;

                    btnToggleRedLed.setBackgroundResource(android.R.drawable.btn_default);
                    sense_on = false;

                    btnTogglePotOn.setBackgroundResource(android.R.drawable.btn_default);
                    recv_on = false;

                }

            }
        }

    }

    // onDestroy destroys the broadcast receivers when the event is finished, through the child class

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();

        //unregister any receivers that may not have been unregistered
        myBTChild.unregisterReceivers();

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



    /** end of main **/

}
