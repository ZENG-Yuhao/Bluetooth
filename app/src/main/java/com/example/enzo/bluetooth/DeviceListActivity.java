package com.example.enzo.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.enzo.bluetooth.Bluetooth.BluetoothService.BluetoothFoundBroadcastReceiver;

import java.util.ArrayList;

public class DeviceListActivity extends BluetoothBaseActivity {
    private Button btn_show_paired, btn_scan;
    private RecyclerView               device_list;
    private BluetoothDeviceListAdapter mDeviceListAdapter;
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();

    private BluetoothFoundBroadcastReceiver mBluetoothFoundBroadcastReceiver = new BluetoothFoundBroadcastReceiver() {
        @Override
        protected void onBluetoothDeviceFound(BluetoothDevice device) {
            showToastInCenter("Device found " + device.getName());
            onAddBluetoothDevice(device);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        // register bluetooth broadcast receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBluetoothFoundBroadcastReceiver, filter);

        btn_show_paired = findViewById(R.id.btn_show_paired);
        btn_scan = findViewById(R.id.btn_scan);
        device_list = findViewById(R.id.device_list);

        btn_show_paired.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                for (BluetoothDevice device : getBondedBluetoothDevices())
                    onAddBluetoothDevice(device);
                showToastInCenter("Paired devices added.");
            }
        });

        btn_scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                discoveryBluetoothDevices();
            }
        });

        mDeviceListAdapter = new BluetoothDeviceListAdapter();
        device_list.setAdapter(mDeviceListAdapter);
        device_list.setLayoutManager(new LinearLayoutManager(mContext));
    }

    protected void onAddBluetoothDevice(BluetoothDevice device) {
        if (!mBluetoothDevices.contains(device)) {
            mBluetoothDevices.add(device);
            mDeviceListAdapter.notifyItemInserted(mBluetoothDevices.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothFoundBroadcastReceiver);
    }

    private class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public BluetoothDeviceViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBluetoothEnabled()) {
                        BluetoothDevice selectedDevice = mBluetoothDevices.get(getAdapterPosition());
                        Intent intent = new Intent(DeviceListActivity.this, ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_LAUNCH_MODE, ChatActivity.CLIENT_MODE);
                        intent.putExtra(ChatActivity.EXTRA_BLUETOOTH_DEVICE, selectedDevice);
                        startActivity(intent);
                    } else {
                        requestEnableBluetooth();
                        showToastInCenter("Please enable bluetooth and retry");
                    }
                }
            });
        }
    }

    private class BluetoothDeviceListAdapter extends RecyclerView.Adapter<BluetoothDeviceViewHolder> {

        @Override
        public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_bluetooth_device, parent, false);
            return new BluetoothDeviceViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
            String deviceName = mBluetoothDevices.get(position).getName();
            String macAddress = mBluetoothDevices.get(position).getAddress();
            holder.textView.setText(deviceName + "\n" + macAddress);
        }

        @Override
        public int getItemCount() {
            return mBluetoothDevices.size();
        }
    }
}
