package com.example.enzo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public abstract class BluetoothBaseActivity extends AppCompatActivity {
    protected static final String  TAG                   = "Bluetooth";
    protected static final int     REQUEST_ENABLE_BT     = 1011;
    protected static final int     DISCOVERABLE_DURATION = 240;
    protected final        Context mContext              = this;
    private Toast mToast;

    protected BluetoothAdapter mDefaultBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    protected ArrayList<BluetoothDevice> getBondedBluetoothDevices() {
        if (mDefaultBluetoothAdapter == null || !mDefaultBluetoothAdapter.isEnabled())
            return null;

        ArrayList<BluetoothDevice> bondedBluetoothDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mDefaultBluetoothAdapter.getBondedDevices();
        bondedBluetoothDevices.addAll(pairedDevices);
        return bondedBluetoothDevices;
    }

    protected void discoveryBluetoothDevices() {
        mDefaultBluetoothAdapter.startDiscovery();
    }

    protected void requestBluetoothDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivity(discoverableIntent);
    }

    protected boolean isBluetoothDiscoverable() {
        return mDefaultBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }

    protected void requestEnableBluetooth() {
        if (mDefaultBluetoothAdapter == null) return;

        if (!mDefaultBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else
            Toast.makeText(this, "Bluetooth is enabled already.", Toast.LENGTH_SHORT).show();
    }

    protected boolean isBluetoothEnabled() {
        return mDefaultBluetoothAdapter.isEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth enabled successfully.", Toast.LENGTH_SHORT).show();
        }
    }


    public void showToastInCenter(final String text) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null)
                    mToast.cancel();
                mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.CENTER, 0, 0);
                mToast.show();

            }
        });
    }
}
