package com.home.smart;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.UUID;


public class deviceControl extends AppCompatActivity {

   // Button btnOn, btnOff, btnDis;
    Button GetState, SetState, CtrlSwitch, Abt, SendCommand, GetResponse;
    String address = null;
    TextInputLayout inputText;
    TextView response_txt;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_device_control);

        //call the widgets
        GetState = (Button)findViewById(R.id.get_state);
        SetState = (Button)findViewById(R.id.set_state);
        CtrlSwitch = (Button)findViewById(R.id.ctrl_switch);
        Abt = (Button)findViewById(R.id.abt_btn);
        SendCommand = (Button) findViewById(R.id.send_btn);
        GetResponse = (Button) findViewById(R.id.recv_btn);
        inputText = findViewById(R.id.cmd_txt);
        response_txt = findViewById(R.id.recv_txt);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        GetState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(btSocket != null) {
                    try{
                        String text = inputText.getEditText().getText().toString();
                        btSocket.getOutputStream().write(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        SetState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(btSocket != null) {
                    try{
                        btSocket.getOutputStream().write(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        CtrlSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(btSocket != null) {
                    try{
                        btSocket.getOutputStream().write(2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        SendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btSocket != null) {
                    try{
                        String text = inputText.getEditText().getText().toString();
                        btSocket.getOutputStream().write(text.getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        GetResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resp = getResponse();
                response_txt.setText(resp);
            }
        });
    }

    private String getResponse() {
        String resp_str = "";
        byte[] response = new byte[200];
        int numBytes = 0;
        if(btSocket !=null && btSocket.isConnected() ) {
            try {
                if(btSocket.getInputStream().available() > 0) {
                    numBytes = btSocket.getInputStream().read(response);
                }
                if(numBytes > 0) {
                    resp_str = new String(response);
                    Log.d(getClass().toString(), "new String " + resp_str);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp_str;
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public  void about(View v)
    {
//        if(v.getId() == R.id.abt)
//        {
//            Intent i = new Intent(this, AboutActivity.class);
//            startActivity(i);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(deviceControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
