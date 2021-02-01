package com.example.womensafetyapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.womensafetyapp.Model.DBHandler;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private EditText edtReceiverPhone;
    private Button btnAddEmergencyNumber,btnAddEmergencyNumberToDB,btnViewNumbers;
    private ListView lvReceiverPhones;
    private ArrayList<String> mArrayList;
    private ArrayAdapter<String> mArrayAdapter;
    private DBHandler mDBHandler;
    private SensorManager sensorMgr;
    private LocationManager mLocationManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private String message;
    private Boolean msgSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgSent=false;
        edtReceiverPhone=findViewById(R.id.edtReceiverNumber);
        btnAddEmergencyNumber=findViewById(R.id.btnAddNumber);
        btnAddEmergencyNumberToDB=findViewById(R.id.btnAddNumberToDB);
        btnViewNumbers=findViewById(R.id.btnViewNumbers);
        lvReceiverPhones=findViewById(R.id.lvReceiverNumbers);
        mDBHandler=new DBHandler(MainActivity.this);

        sensorMgr= (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this,
                sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        mArrayList=new ArrayList<>();

        btnAddEmergencyNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayList=new ArrayList<>();
                mArrayAdapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,mArrayList);
                lvReceiverPhones.setAdapter(mArrayAdapter);
                mArrayAdapter.notifyDataSetChanged();

                btnAddEmergencyNumberToDB.setVisibility(View.VISIBLE);
                edtReceiverPhone.setVisibility(View.VISIBLE);
                btnAddEmergencyNumberToDB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String receiverMobileNumber=edtReceiverPhone.getText().toString();
                        if (!receiverMobileNumber.equals("") && receiverMobileNumber.length()==10){
                            try{
                                mDBHandler.addMobile(receiverMobileNumber);
                                Toast.makeText(MainActivity.this,"Number is added successfully!",Toast.LENGTH_SHORT).show();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            edtReceiverPhone.setText("");
                            edtReceiverPhone.setVisibility(View.INVISIBLE);
                            btnAddEmergencyNumberToDB.setVisibility(View.INVISIBLE);
                        }else if (receiverMobileNumber.equals("")){
                            Toast.makeText(MainActivity.this,"Enter the number!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,"Enter the correct number!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        btnViewNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddEmergencyNumberToDB.setVisibility(View.INVISIBLE);
                edtReceiverPhone.setVisibility(View.INVISIBLE);
                try{
                    mArrayList=mDBHandler.returnAllMobileNumbers();
                    mArrayAdapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,mArrayList);
                    lvReceiverPhones.setAdapter(mArrayAdapter);
                    mArrayAdapter.notifyDataSetChanged();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        lvReceiverPhones.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete the number!");
                builder.setMessage("Do you really want to delete the number?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDBHandler.deleteMobile(mArrayList.get(position));
                        mArrayList.remove(position);
                        mArrayAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            }
        });




    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 12) {
            if (!msgSent) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                } else {
                    getLocation();
                }
                sensorMgr.unregisterListener(this,sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            }
        }
        }

        @Override
        public void onAccuracyChanged (Sensor sensor,int accuracy){

        }
        private void getLocation() {
            final LocationRequest locationRequest=new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                    .requestLocationUpdates(locationRequest,new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                    .removeLocationUpdates(this);

                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                                message="Sender is in trouble maybe! \n"+
                                "Type "+locationResult.getLocations().get(latestLocationIndex).getLatitude()+" "+locationResult.getLocations().get(latestLocationIndex).getLongitude() +" in Google Map to get the sender's location.";
                                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 2000);
                                } else {
                                    sendSMS(message);
                                }
                            }
                        }
                    }, Looper.getMainLooper());

    }

    private void sendSMS(String message) {
        ArrayList<String> receiverNo=mDBHandler.returnAllMobileNumbers();
        for(String mobile:receiverNo){
            SmsManager smsManager=SmsManager.getDefault();
            smsManager.sendTextMessage(mobile,null,message,null,null);
            msgSent=true;
        }
        if (receiverNo.size()==0){
            Toast.makeText(MainActivity.this,"There is no emergency number!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Message sent!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1000 && grantResults.length>0){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLocation();
            }else{
                Toast.makeText(MainActivity.this,"Permission Denied!",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==2000 && grantResults.length>0){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                sendSMS(message);
            }else{
                Toast.makeText(MainActivity.this,"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.instructions){
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Instructions!");
            String message="1. Enter numbers that are reachable in emergencies.\n \n "+
                    "2. Please enter the correct number.\n \n" +
                    "3. Open the app and shake your phone whenever you feel unsafe. An SMS along with your location will be sent to the registered users.";
            builder.setMessage(message);
            builder.show();
        }
        return super.onOptionsItemSelected(item);

    }
}

