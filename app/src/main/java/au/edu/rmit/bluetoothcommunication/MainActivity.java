package au.edu.rmit.bluetoothcommunication;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


//MAC ADDRESS OF PHONE  A0:93:47:65:D0:26

public class MainActivity extends AppCompatActivity implements AdapterView.OnClickListener{
    private static final String TAG = "MainActivity";

    // Create local variables
    Button btnReadFile;
    Button btnDeleteFileContents;


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


    /** start methods **/

    // onCreate
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create Buttons

        btnReadFile = (Button) findViewById(R.id.btnReadFile);
        btnDeleteFileContents = (Button) findViewById(R.id.btnDeleteFileContents);

    }


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


    /** this will be the function to send messages to the BBB
     *
     */

    class WriteReadBBB implements Runnable {
        public final String LOG = getClass().getName();

        private final BB _BB;
        private final BluetoothSocket _socket;

        private Reader _reader;
        private Writer _writer;

        private final StringBuilder _stringBuilder = new StringBuilder();

        WriteReadBBB(BluetoothSocket socket, BB BB) {
            _socket = socket;
            _BB = BB;
        }

        public void run() {
            try {
                _writer = new OutputStreamWriter(_socket.getOutputStream(), "UTF-8");

                switch(_BB) {
                    case GREEN_LED:
                        Log.i(LOG, "write green");
                        _writer.write("green\n");
                        _writer.flush();
                        break;
                    case RED_LED:
                        Log.i(LOG, "write red");
                        _writer.write("red\n");
                        _writer.flush();
                        break;
                    case POT_ON:
                        Log.i(LOG, "Turning on data receive");
                        _writer.write("receive_data\n");
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

        //get socket from BCS
        _socket = BluetoothConnectionService.socketForMain;

        switch (view.getId()) {
            case R.id.buttonToggleGreenLed:
                writeRead = new WriteReadBBB(_socket, BB.GREEN_LED);
                new Thread(writeRead).start();
                break;
            case R.id.buttonToggleRedLed:
                writeRead = new WriteReadBBB(_socket, BB.RED_LED);
                new Thread(writeRead).start();
                break;
            case R.id.buttonTogglePotOn:
                writeRead = new WriteReadBBB(_socket, BB.POT_ON);
                new Thread(writeRead).start();
                break;
            default:
                Log.i(TAG, "unknown click event");
        }

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

    //--- Handle returned data from child.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent replyIntent) {

        // just debugging here or do something with data passed back

        Log.d(TAG, "onActivityResult: returning from secondary activity");

        super.onActivityResult(requestCode, resultCode, replyIntent);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) { // right child and OK result.

            if (replyIntent.hasExtra("returnMessage")) {

                //remove the unnecessary messages, say return from child
            }
        }

    }

    public void toBTChild(View view){

        Intent myIntent = new Intent(MainActivity.this, BTChild.class);
        startActivityForResult(myIntent, REQUEST_CODE3);
    }

    public void toGraphChild(View view){

        Intent myIntent = new Intent(MainActivity.this, graphChild.class);
        startActivityForResult(myIntent, REQUEST_CODE2);
    }

    // onDestroy destroys the broadcast receivers when the event is finished, through the child class

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
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
