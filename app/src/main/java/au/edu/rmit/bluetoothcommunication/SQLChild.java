package au.edu.rmit.bluetoothcommunication;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.app.AlertDialog;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.view.View;          // View widget include.

public class SQLChild extends Activity {

    DatabaseHelper myDb;
    EditText editName, editTrafficCount, editVentilation, editTextId;
    Button btnAddData;
    Button btnviewAll;
    Button btnDelete;
    Button btnDeleteAll;

    Button btnviewUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        myDb = new DatabaseHelper(this);

        editName = (EditText) findViewById(R.id.editText_name);
        editTrafficCount = (EditText) findViewById(R.id.editText_TrafficCount);
        editVentilation = (EditText) findViewById(R.id.editText_Ventilation);
        editTextId = (EditText) findViewById(R.id.editText_id);
        btnAddData = (Button) findViewById(R.id.button_add);
        btnviewAll = (Button) findViewById(R.id.button_viewAll);
        btnviewUpdate = (Button) findViewById(R.id.button_update);
        btnDelete = (Button) findViewById(R.id.button_delete);
        btnDeleteAll = (Button) findViewById(R.id.button_deleteAll);

        btnDeleteAll.setBackgroundResource(R.color.red);


        AddData();
        viewAll();
        UpdateData();
        DeleteData();
    }

    public void DeleteData() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer deletedRows = myDb.deleteData(editTextId.getText().toString());
                        if (deletedRows > 0)
                            Toast.makeText(SQLChild.this, "Data Deleted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(SQLChild.this, "Data not Deleted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void deleteAllData(View view){
        myDb.deleteAllData();
    }

    public void UpdateData() {
        btnviewUpdate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isUpdate = myDb.updateData(editTextId.getText().toString(),
                                editName.getText().toString(),
                                editTrafficCount.getText().toString(), editVentilation.getText().toString());
                        if (isUpdate == true)
                            Toast.makeText(SQLChild.this, "Data Update", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(SQLChild.this, "Data not Updated", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void AddData() {
        btnAddData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInserted = myDb.insertData(editName.getText().toString(),
                                editTrafficCount.getText().toString(),
                                editVentilation.getText().toString());
                        if (isInserted == true)
                            Toast.makeText(SQLChild.this, "Data Inserted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(SQLChild.this, "Data not Inserted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void viewAll() {
        btnviewAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb.getAllData();
                        if (res.getCount() == 0) {
                            // show message
                            showMessage("Error", "Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("Id :" + res.getString(0) + "\n");
                            buffer.append("Name :" + res.getString(1) + "\n");
                            buffer.append("Traffic Count :" + res.getString(2) + "\n");
                            buffer.append("Ventilation :" + res.getString(3) + "\n\n");
                        }

                        // Show all data
                        showMessage("Data", buffer.toString());
                    }
                }
        );
    }

    /*
    public void deleteTable(){
        myDb.deleteTable();
        Toast.makeText(SQLChild.this, "Table Deleted", Toast.LENGTH_LONG).show();
    }*/


    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    //--- Click on button, close activity and return to calling activity.
    public void onReturnClick(View view) {// function named in OnClick

        //--- set static public field in main activity.

        //--- return information via intent.
        Intent replyIntent = new Intent();
        // Return some hard-coded values
        replyIntent.putExtra("returnMessage", "Returning from SQL Child page");
        setResult(RESULT_OK, replyIntent);
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