package com.example.enzo.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends BluetoothBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_server = findViewById(R.id.btn_bluetooth_server);
        btn_server.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBluetoothEnabled() && isBluetoothDiscoverable()) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_LAUNCH_MODE, ChatActivity.SERVER_MODE);
                    startActivity(intent);
                } else {
                    showToastInCenter("Please enable bluetooth, make it discoverable and retry");
                    requestEnableBluetooth();
                    requestBluetoothDiscoverable();
                }
            }
        });

        Button btn_client = findViewById(R.id.btn_bluetooth_client);
        btn_client.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBluetoothEnabled()) {
                    Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivity(intent);
                } else {
                    showToastInCenter("Please enable bluetooth and retry");
                    requestEnableBluetooth();
                }
            }
        });

    }

}
