package com.example.enzo.bluetooth;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.example.enzo.bluetooth.Bluetooth.BluetoothService;
import com.example.enzo.bluetooth.Bluetooth.BluetoothService.OnBluetoothDataReceivedListener;

import java.io.UnsupportedEncodingException;

public class ChatActivity extends BluetoothBaseActivity {
    public static final String EXTRA_LAUNCH_MODE      = "launch.mode";
    public static final String EXTRA_BLUETOOTH_DEVICE = "bluetooth.device";
    public static final int    SERVER_MODE            = 1;
    public static final int    CLIENT_MODE            = 2;

    private ChatFragment         mChatFragment;
    private EditText             editxt_message;
    private FloatingActionButton btn_send;
    private Integer              mLaunchMode;
    private BluetoothService     mBluetoothService;
    private String               remoteName;
    private String localName = "Me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        setTitle("Chat");

        mChatFragment = new ChatFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mChatFragment);
        transaction.commit();

        configureBluetooth();

        editxt_message = findViewById(R.id.editxt_message);
        btn_send = findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothService == null || !mBluetoothService.isSocketConnected()) {
                    Toast.makeText(ChatActivity.this, "Bluetooth is not connected.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (editxt_message.length() > 0) {
                    String message = editxt_message.getText().toString();
                    try {
                        mBluetoothService.write(message.getBytes("UTF-8"));
                        if (localName == null)
                            localName = mDefaultBluetoothAdapter.getName();
                        mChatFragment.onMessageSent(localName, message);
                        editxt_message.getText().clear();
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void configureBluetooth() {
        mLaunchMode = getIntent().getIntExtra(EXTRA_LAUNCH_MODE, -1);
        if (mLaunchMode != SERVER_MODE && mLaunchMode != CLIENT_MODE)
            throw new RuntimeException("Wrong launch mode code : " + mLaunchMode);
        if (!isBluetoothEnabled())
            throw new RuntimeException("Bluetooth is not enabled.");

        mBluetoothService = BluetoothService.create(this);
        mBluetoothService.setOnDataReceivedListener(new OnDataReceivedListener());
        if (mLaunchMode == SERVER_MODE) {
            mBluetoothService.acceptClient();
        } else { // client mode
            BluetoothDevice device = getIntent().getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);
            mBluetoothService.connectToServer(device);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null)
            mBluetoothService.stop();
    }

    private class OnDataReceivedListener implements OnBluetoothDataReceivedListener {
        @Override
        public void onBluetoothDataReceived(final byte[] data, int numBytes) {
            final byte[] newData = new byte[numBytes];
            System.arraycopy(data, 0, newData, 0, numBytes);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (remoteName == null)
                        remoteName = mBluetoothService.getRemoteDevice().getName();
                    String message = new String(newData);
                    mChatFragment.onMessageReceived(remoteName, message);
                }
            });
        }
    }
}
