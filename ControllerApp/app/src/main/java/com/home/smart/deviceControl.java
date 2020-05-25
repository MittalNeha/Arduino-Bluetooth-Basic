package com.home.smart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public class deviceControl extends AppCompatActivity {

    private String TAG = getClass().toString();
    SwitchBoard sb;
   // Button btnOn, btnOff, btnDis;
    Button GetState, SetState, CtrlSwitch, Abt, SendCommand, GetResponse;
    Button bDone;
    String address = null;
    TextInputLayout inputText;
    TextView response_txt;
    TextView titleText;
    private ProgressDialog progress;
    private PopupWindow selectPopWindow;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean testingCommands = false;
    private int board_state = -1; // unconfigured, configuring, configured

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
        SendCommand = (Button) findViewById(R.id.send_btn);
        GetResponse = (Button) findViewById(R.id.recv_btn);
        inputText = findViewById(R.id.cmd_txt);
        response_txt = findViewById(R.id.recv_txt);

        bDone = findViewById(R.id.btn_done);

        titleText = findViewById(R.id.textView2);
        titleText.setFocusable(true);
        titleText.setEnabled(true);
        titleText.setClickable(true);

        new ConnectBT().execute(); //Call the class to connect

        //Wait for BT to connect
        while(null == btSocket) {
            try {
                Thread.sleep(1000);
                Log.i(TAG, "Waiting for BT connection");
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(false == testingCommands) {
            GetState.setVisibility(View.INVISIBLE);
            SetState.setVisibility(View.INVISIBLE);
            CtrlSwitch.setVisibility(View.INVISIBLE);
            SendCommand.setVisibility(View.INVISIBLE);
            GetResponse.setVisibility(View.INVISIBLE);
            response_txt.setVisibility(View.INVISIBLE);
            inputText.setVisibility(View.INVISIBLE);
        }
        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save device config
                String config = sb.formBoardConfig();
                Log.i(TAG, config);
                if(btSocket != null) {
                    try {
                        btSocket.getOutputStream().write(1);
                        Thread.sleep(200);
                        btSocket.getOutputStream().write(config.getBytes());
                        Thread.sleep(2000);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Disconnect();
            }
        });
    }

    void btConnectionFinished(boolean isBtConnected) {
        if(isBtConnected) {

            String config = getDeviceConfig();
            sb = new SwitchBoard(config);
            if(sb.getStatus() == 0) {
                //Board is unconfigured, show the view to configure
                titleText.setText("Room 1");
                sb.setBoardName(titleText.getText().toString());
                board_state = -1;
                setAppliancesConfigLayout();
            } else {
                //Board is configured.
                board_state = 1;
                titleText.setText(sb.getBoardName());

                setAppliancesControlLayout(sb.getSwitches());
            }

        }

    }

    private void setAppliancesConfigLayout() {

        RelativeLayout appLayout = (RelativeLayout) findViewById(R.id.dev_ctrl);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = 100;

        for(int i = 0; i < 4; i++) {
            LinearLayout ll = new LinearLayout(this);

            Switch onOff = new Switch(this);
            onOff.setId(i);
            onOff.setChecked(false);

            RelativeLayout.LayoutParams params_switch = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            params_switch.topMargin = params.topMargin;
            ll.addView(onOff, params_switch);
            onOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controlDeviceListener(view);
                }
            });

            appLayout.addView(ll, params);
            params.topMargin += 150;
        }
    }

    private void setAppliancesControlLayout(List<Appliances> appliancesList) {
        int switch_id = 0;
        RelativeLayout appLayout = (RelativeLayout) findViewById(R.id.dev_ctrl);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = 100;

        for(Appliances appliance : appliancesList) {
            LinearLayout ll = new LinearLayout(this);

            TextView name = new TextView(this);
            name.setText(appliance.type.getText());
            name.setTextAppearance( R.style.TextAppearance_AppCompat_Medium);
            RelativeLayout.LayoutParams params_text = new RelativeLayout.LayoutParams(250, 80);
            params_text.leftMargin = 30;
            params_text.topMargin = params.topMargin;
            name.setGravity(Gravity.BOTTOM);
            ll.addView(name, params_text);

            Switch onOff = new Switch(this);
            onOff.setId(switch_id);
            if(appliance.status == 0)
                onOff.setChecked(false);
            else
                onOff.setChecked(true);
            RelativeLayout.LayoutParams params_switch = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params_switch.addRule(RelativeLayout.RIGHT_OF, name.getId());
            params_switch.topMargin = params.topMargin;
            ll.addView(onOff, params_switch);
            onOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controlDeviceListener(view);
                }
            });

            appLayout.addView(ll, params);
            params.topMargin += 150;
            switch_id++;
        }
    }
    private void controlDeviceListener(View view) {
        int id = view.getId();

        Switch onOff = (Switch)view;
        int state = 0;
        if (onOff.isChecked()) {
            state = 1;
        } else {
            state = 0;
        }

        String cmd_data = (byte)id + ":" + state;

        //Send control command for this device over bluetooth
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write(2);
                btSocket.getOutputStream().write(cmd_data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(board_state == -1) {
            ShowPopupWindow(id);
        } else {
            sb.setSwitchState(id, state);
        }
    }

    private String getDeviceConfig() {
        String read_config = "";
        if(btSocket != null) {
            try {
                //Send command to get config
                btSocket.getOutputStream().write(0);
                Thread.sleep(500);
                read_config = getResponse();
                Log.i(TAG, "getDeviceConfig: response " + read_config);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return read_config;
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
                    resp_str = new String(response, 0, numBytes);
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

    private void ShowPopupWindow(final int selected_switch){
        try {
            Button btnfan, btnlight, btnbulb, btnplugpoint;

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.appliance_select_popup, null);
//                        View layout = inflater.inflate(R.id.submitPopup, null);
            selectPopWindow = new PopupWindow(layout, 610, 1050, true);

            selectPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            selectPopWindow.setOutsideTouchable(true);
            selectPopWindow.showAtLocation(layout, Gravity.CENTER, 40, 60);
            //  window.showAtLocation(layout, 17, 100, 100);

            btnfan = layout.findViewById(R.id.btnfan);
            btnlight = layout.findViewById(R.id.btnlight);
            btnbulb = layout.findViewById(R.id.btnbulb);
            btnplugpoint = layout.findViewById(R.id.btnplugpoint);

            btnfan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, " button call press ");
                    getSelectPopWindow(v, selected_switch);
                    selectPopWindow.dismiss();
                }

            });
            btnlight.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.e(TAG, " sound  touch");
                    getSelectPopWindow(v, selected_switch);
                    selectPopWindow.dismiss();

                }

            });
            btnbulb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.e(TAG, " camera select touch");
                    getSelectPopWindow(v, selected_switch);
                    selectPopWindow.dismiss();

                }

            });
            btnplugpoint.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.e(TAG, " video select  touch");
                    getSelectPopWindow(v, selected_switch);
                    selectPopWindow.dismiss();

                }

            });

        }catch (Exception e){
            Log.e(TAG, "Exception "+e.getMessage());

        }
    }
    private void getSelectPopWindow(View view, int selected_switch) {

        //get the state from the Switch's state
        int state = 0;
        Switch select_switch = findViewById(selected_switch);
        if(null != select_switch) {
            if(select_switch.isChecked())
                state = 1;
        }

        switch(view.getId()) {
            case R.id.btnfan:
                sb.setSwitches(selected_switch, Appliances.Types.eFAN, state);
                break;
            case R.id.btnbulb:
                sb.setSwitches(selected_switch, Appliances.Types.eBULB, state);
                break;
            case R.id.btnlight:
                sb.setSwitches(selected_switch, Appliances.Types.eTUBELIGHT, state);
                break;
            case R.id.btnplugpoint:
                sb.setSwitches(selected_switch, Appliances.Types.ePLUGPOINT, state);
                break;
            default:
                Log.e(TAG, "getSelectPopWindow(): Invalid view id");
        }
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
            btConnectionFinished(isBtConnected);
            progress.dismiss();
        }
    }
}
