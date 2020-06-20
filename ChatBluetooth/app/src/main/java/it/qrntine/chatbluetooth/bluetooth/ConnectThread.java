
package it.qrntine.chatbluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import it.qrntine.chatbluetooth.Constants;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
public class ConnectThread extends Thread {

    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private String mSocketType;
    private final BluetoothAdapter mAdapter;
    private BluetoothChatService mBluetoothChatService;


    public ConnectThread(BluetoothDevice device, boolean secure, BluetoothAdapter mAdapter, BluetoothChatService mBluetoothChatService ) {
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
                        Constants.MY_UUID_SECURE);
            } else {
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                        Constants.MY_UUID_INSECURE);
            }
        } catch (IOException e) {
            Log.e(Constants.TAG_CONNECT_THREAD, "Socket Type: " + mSocketType + "create() failed", e);
        }
        mmSocket = tmp;
        mBluetoothChatService.setmState(Constants.STATE_CONNECTING);
    }

    public void run() {
        Log.i(Constants.TAG_CONNECT_THREAD, "BEGIN mConnectThread SocketType:" + mSocketType);
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
                Log.e(Constants.TAG_CONNECT_THREAD, "unable to close() " + mSocketType +
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
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG_CONNECT_THREAD, "close() of connect " + mSocketType + " socket failed", e);
        }
    }
}