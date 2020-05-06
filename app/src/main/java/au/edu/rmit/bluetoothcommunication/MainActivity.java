package au.edu.rmit.bluetoothcommunication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


//MAC ADDRESS OF PHONE  A0:93:47:65:D0:26

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

    // Create local variables
    BluetoothAdapter mBluetoothAdapter;
    Button btnDiscoverability;
    Button btnONOFF;
    Button btnStartConnection;
    Button btnSend;
    Button btnReadFile;
    Button btnDeleteFileContents;

    BluetoothConnectionService mBluetoothConnection;

    EditText etSend;


    ParcelUuid[] mDeviceUUIDs;
    private static UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    // create text view for logcat messages
    TextView currentStatus;

    // create enum for BB
    public enum BB {UNKNOWN_LED, GREEN_LED, RED_LED, POT_ON};

    // socket for control
    private BluetoothSocket _socket = null;

    private static final int REQUEST_CODE = 10; // Code to send to child, and expect back.
    private static final int REQUEST_CODE2 = 20;


    /** start methods **/

    // Create a BroadcastReceiver for ACTION_FOUND

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device

            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {

                //integer defines state as extra passed back from intent
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);


                // broadcast receiver catches every action
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "onReceive: STATE OFF");
                    break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                    break;
                    case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                    break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                    break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for changes made to BT states such as Discoverability mode on/off

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };
    
    // Broadcast Reciever for bonding
    
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            final String action = intent.getAction();
            
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 CASES:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    // onDestroy destroys the broadcast receiver when the event is finished
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        
    }

    // onCreate
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create Buttons

        btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnDiscoverability = (Button) findViewById(R.id.btnDiscoverability);
        btnStartConnection = (Button)findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);
        btnReadFile = (Button) findViewById(R.id.btnReadFile);
        btnDeleteFileContents = (Button) findViewById(R.id.btnDeleteFileContents);

        // create TextView
        //currentStatus = (TextView) findViewById(R.id.currentStatus);
        
        // create BTAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // make list for new devices
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //broadcasts when bond state changes (ie. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);
        
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        btnStartConnection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view) {
                try{startConnection();
                }catch (NullPointerException e){
                    Log.e(TAG, "startConnection: Connection failed " + e.getMessage());
                    }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }

        });
    }

    //create method for starting connection
    //***remember the conncction will fail and app will crash if you haven't paired first.
    // will pass BT device and UUD and initiate ConnectedThread in BluetoothConnectionService
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting connection here
     *
     */

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }


    // BT enable/disable config
    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");

            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    // onClick method for the ON/OFF button
    public void  btnONOFF(View view) {

        btnONOFF.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                                            enableDisableBT();
                                        }
                                    }
        );
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

    // defines button click for btnDiscoverability

    public void btnDiscoverability(View view) {
        Log.d(TAG, "btnDiscoverability: Making device discoverable for 300 seconds.");
        //currentStatus.setText(currentStatus.getText() + "... device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    /** this is for clicking devices on the bluetooth list **/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);



        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBTDevice = mBTDevices.get(i);

            //Log.d(TAG, "onItemClick: Device UUID =  " + mBTDevice.getUuids());

            //getUUIDs here
            mDeviceUUIDs = mBTDevice.getUuids();


            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        }
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

    public void toGraphChild(View view){

        Intent myIntent = new Intent(MainActivity.this, graphChild.class);
        startActivityForResult(myIntent, REQUEST_CODE2);
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
