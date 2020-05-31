package it.qrntine.chatbluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
public class ConnectThread extends Thread {

    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private String mSocketType;
    private final BluetoothAdapter mAdapter;
    private BluetoothChatService mBluetoothChatService;


    public ConnectThread(BluetoothDevice device, boolean secure, BluetoothAdapter mAdapter, BluetoothChatService mBluetoothChatService ) {
        System.out.println("*********************ConnectThread ENTRO IN COSTRUTTORE");
        mmDevice = device;
        BluetoothSocket tmp = null;
        mSocketType = secure ? "Secure" : "Insecure";
        this.mAdapter =mAdapter;
        this.mBluetoothChatService=mBluetoothChatService;


        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            if (secure) {
                tmp = device.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE);
            } else {
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE);
            }
        } catch (IOException e) {
            Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
        }
        mmSocket = tmp;
        mBluetoothChatService.setmState(STATE_CONNECTING);
    }

    public void run() {
        System.out.println("*********************ConnectThread ENTRO IN RUN");
        Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
        setName("ConnectThread" + mSocketType);

        // Always cancel discovery because it will slow down a connection
        mAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mmSocket.close();
            } catch (IOException e3) {
                Log.e(TAG, "unable to close() " + mSocketType +
                        " socket during connection failure", e3);
            }
            mBluetoothChatService.connectionFailed();
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (mBluetoothChatService) {
            mBluetoothChatService.setmConnectThread(null);
        }

        // Start the connected thread
        mBluetoothChatService.connected(mmSocket, mmDevice, mSocketType);
            /*
            }
             */
    }

    public void cancel () {
        System.out.println("*********************ConnectThread ENTRO IN CANCEL");
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
        }
    }
}