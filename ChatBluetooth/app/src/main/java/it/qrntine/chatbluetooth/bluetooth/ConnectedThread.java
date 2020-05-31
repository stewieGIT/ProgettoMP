package it.qrntine.chatbluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
    public class ConnectedThread extends Thread {
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

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] buffer;
    private final BluetoothAdapter mAdapter;
    private BluetoothChatService mBluetoothChatService;
    private final Handler mHandler;


    public ConnectedThread(BluetoothSocket socket, String socketType, BluetoothAdapter mAdapter, BluetoothChatService mBluetoothChatService, Handler mHandler ) {
        System.out.println("*********************Connected_Thread ENTRO IN COSTRUTTORE");
        Log.d(TAG, "create ConnectedThread: " + socketType);
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.mAdapter=mAdapter;
        this.mBluetoothChatService=mBluetoothChatService;
        this.mHandler=mHandler;
        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        mBluetoothChatService.setmState(STATE_CONNECTED);
    }

    public void run() {
        System.out.println("*********************Connected_Thread ENTRO IN RUN");
        Log.i(TAG, "BEGIN mConnectedThread");
        buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (mBluetoothChatService.getmState() == STATE_CONNECTED) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                mHandler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();

                System.out.println("*****************Messaggio ricevuto: (bytes) " + bytes);

            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                mBluetoothChatService.connectionLost();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        System.out.println("*********************Connected_Thread ENTRO IN WRITE");
        try {
            mmOutStream.write(buffer);

            // Share the sent message back to the UI Activity
            mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget();

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);

            Message writeErrorMsg =
                    mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            mHandler.sendMessage(writeErrorMsg);

        }
    }

    public void cancel() {
        System.out.println("*********************Connected_Thread ENTRO IN CANCEL");
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}