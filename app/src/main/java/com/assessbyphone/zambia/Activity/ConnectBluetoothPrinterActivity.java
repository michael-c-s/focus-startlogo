package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.assessbyphone.zambia.R;
import com.zj.btsdk.BluetoothService;

import java.util.ArrayList;

public class ConnectBluetoothPrinterActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static String TAG = "PhonicsByPhone";
    public static BluetoothService mService = null;
    private final Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_LONG).show();
                            nextActivity();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(GetBanksActivity.TAG, "BluetoothService.STATE_CONNECTING");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d(GetBanksActivity.TAG, "BluetoothService.STATE_NONE");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_LONG).show();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    public ArrayList<ArrayList<ArrayList<String>>> textBanks = new ArrayList<ArrayList<ArrayList<String>>>();
    Button btnConnectBTPrinter;
    Button btnSkipConnectBTPrinter;
    TextView newsTV;
    BluetoothDevice con_dev = null;
    private ImageView bluetoothBack;
    private String assessmentName;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_bluetooth_printer);

        assessmentName =  getIntent().getStringExtra("assessmentName");

        newsTV = findViewById(R.id.txtNews);
        bluetoothBack = findViewById(R.id.bluetoothBack_ids);

        mService = new BluetoothService(this, mHandler);

        if (!mService.isAvailable()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (GetMasterTemplateActivity.sTemplate == null) {
            finish();
            return;
        }

        if (mService == null) {
            mService = new BluetoothService(this, mHandler);
        }

        if (mService.isAvailable() && !mService.isBTopen()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (GetMasterTemplateActivity.sNews != null)
            newsTV.setText(Html.fromHtml(GetMasterTemplateActivity.sNews));
        else
            newsTV.setText("No data found, Please try again!");

        btnConnectBTPrinter = this.findViewById(R.id.btnConnectBTPrinter);
        btnConnectBTPrinter.setOnClickListener(new ClickEvent());

        btnSkipConnectBTPrinter = this.findViewById(R.id.btnSkipConnectBTPrinter);
        btnSkipConnectBTPrinter.setOnClickListener(new ClickEvent());

        //nextActivity();

        bluetoothBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }

    void nextActivity() {
        Intent myIntent = new Intent(this, GetBanksActivity.class);
        myIntent.putExtra("assessmentName", assessmentName);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    try {
                        con_dev = mService.getDevByMac(address);
                        mService.connect(con_dev);
                    } catch (Exception e) {
                        Log.e(TAG, "Error with bluetooth: " + e.getMessage());
                        Toast.makeText(this, "Error with bluetooth: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    @Override
    public void onBackPressed() {
        if (mService != null)
            mService.stop();
        mService = null;
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == btnConnectBTPrinter) {
                Intent serverIntent = new Intent(ConnectBluetoothPrinterActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            } else if (v == btnSkipConnectBTPrinter) {
                nextActivity();
            }
        }
    }
}
