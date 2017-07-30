package com.example.enzo.bluetooth.Bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * <p>
 * Created by ZENG Yuhao(Enzo)<br>
 * Contact: enzo.zyh@gmail.com
 * </p>
 */

public interface OnBluetoothSocketObtainedListener {
    void onSocketObtained(BluetoothSocket socket);
}
