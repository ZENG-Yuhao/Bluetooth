package com.example.enzo.bluetooth.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * Created by ZENG Yuhao(Enzo)<br>
 * Contact: enzo.zyh@gmail.com
 * </p>
 */

public class BluetoothService {
    private static final String TAG                   = "BluetoothService";
    private static final int    DISCOVERABLE_DURATION = 180;

    private Context                         mContext;
    private BluetoothAdapter                mBluetoothAdapter;
    private BluetoothSocket                 mConnectedSocket;
    private OnBluetoothDataReceivedListener mBluetoothDataReceivedListener;
    private AcceptThread                    mAcceptThread;
    private ConnectThread                   mConnectThread;
    private CommunicationThread             mCommunicationThread;
    private Toast                           mToast;

    public static BluetoothService create(Context context) throws BluetoothNotAvailableException,
            BluetoothNotEnabledException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            throw new BluetoothNotAvailableException();

        if (!bluetoothAdapter.isEnabled())
            throw new BluetoothNotEnabledException();

        return new BluetoothService(context, bluetoothAdapter);
    }

    private BluetoothService(Context context, BluetoothAdapter adapter) {
        mContext = context;
        mBluetoothAdapter = adapter;
    }

    public void setOnDataReceivedListener(OnBluetoothDataReceivedListener listener) {
        mBluetoothDataReceivedListener = listener;
    }

    public void showToast(final String text) {
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

    public void stop() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
        }

        if (mCommunicationThread != null) {
            mCommunicationThread.cancel();
        }

        mConnectedSocket = null;
    }

    public void acceptClient() {
        // make current device discoverable
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
            mContext.startActivity(discoverableIntent);
        }

        stop();

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void stopAcceptingClient() {
        if (mAcceptThread != null)
            mAcceptThread.cancel();
    }

    public void connectToServer(BluetoothDevice device) {
        stop();

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

    }

    public void stopConnectingToServer() {
        if (mConnectThread != null)
            mConnectThread.cancel();
    }

    private void onSocketObtained(BluetoothSocket socket) {
        showToast("Bluetooth connected.");
        if (mCommunicationThread != null)
            mCommunicationThread.cancel();

        mConnectedSocket = socket;
        mCommunicationThread = new CommunicationThread(socket);
        mCommunicationThread.start();
    }

    public void write(byte[] bytes) {
        if (mCommunicationThread != null && isSocketConnected()) {
            mCommunicationThread.write(bytes);
        }
    }

    public boolean isSocketConnected() {
        return (mConnectedSocket != null && mConnectedSocket.isConnected());
    }

    public void closeConnectedSocket() {
        if (isSocketConnected()) {
            try {
                mConnectedSocket.close();
            } catch (IOException e) {
                showToast("Failed to close previous socket.");
            }
        }
    }

    public BluetoothDevice getRemoteDevice() {
        if (isSocketConnected())
            return mConnectedSocket.getRemoteDevice();
        else
            return null;
    }

    /**
     * AcceptThread
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth", MyUUID.getUUID());
            } catch (IOException e) {
                showToast("Socket's listen() method failed.");
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
//                    showToast("Socket's accept() method failed");
                    break;
                }

                if (socket != null) {
                    onSocketObtained(socket);
                    // close current server socket.
                    cancel();
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                showToast("Could not close the server socket.");
            }
        }
    }


    /**
     * ConnectThread
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MyUUID.getUUID());
            } catch (IOException e) {
                showToast("Socket's create() method failed");
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                cancel();
                return;
            }

            onSocketObtained(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                showToast("Could not close the client socket.");
            }
        }
    }


    /**
     * CommunicationThread
     */
    private class CommunicationThread extends Thread {
        private static final int BUFF_LEN = 1024;
        private final BluetoothSocket mmConnectedSocket;
        private final InputStream     mmInStream;
        private final OutputStream    mmOutStream;

        public CommunicationThread(BluetoothSocket socket) {
            mmConnectedSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                showToast("Error occurred when creating input stream.");
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error occurred when creating output stream.");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[BUFF_LEN];
            int numBytes; // bytes returned from read()
            while (true) {
                try {
                    numBytes = mmInStream.read(buffer);
                    if (mBluetoothDataReceivedListener != null)
                        mBluetoothDataReceivedListener.onBluetoothDataReceived(buffer, numBytes);
                } catch (IOException e) {
                    showToast("Input stream was disconnected.");
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                showToast("Error occurred when sending data.");
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mConnectedSocket.close();
            } catch (IOException e) {
                showToast("Could not close the connected socket.");
            }
        }
    }


    /**
     * BluetoothFoundBroadcastReceiver
     */
    public static abstract class BluetoothFoundBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onBluetoothDeviceFound(device);
            }
        }

        protected abstract void onBluetoothDeviceFound(BluetoothDevice device);
    }


    /**
     * OnBluetoothDataReceivedListener
     */
    public interface OnBluetoothDataReceivedListener {
        void onBluetoothDataReceived(byte[] data, int numBytes);
    }


    /**
     * BluetoothException
     */
    public static class BluetoothException extends RuntimeException {

        public BluetoothException(String message) {
            super(message);
        }
    }

    /**
     * BluetoothNotAvailableException
     */
    public static class BluetoothNotAvailableException extends BluetoothException {
        public BluetoothNotAvailableException() {
            super("Bluetooth is not supported on this device.");
        }
    }


    /**
     * BluetoothNotEnabledException
     */
    public static class BluetoothNotEnabledException extends BluetoothException {
        public BluetoothNotEnabledException() {
            super("Bluetooth is not enabled.");
        }
    }
}
