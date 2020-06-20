package it.qrntine.chatbluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import it.qrntine.chatbluetooth.Constants;


/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
public class AcceptThread extends Thread {

    // The local server socket
    private final BluetoothServerSocket mmServerSocket;
    private String mSocketType;
    private final BluetoothAdapter mAdapter;
    private BluetoothChatService mBluetoothChatService;
    private final Handler mHandler;

    public AcceptThread(boolean secure, BluetoothAdapter mAdapter, Handler mHandler, BluetoothChatService mBluetoothChatService) {
        BluetoothServerSocket tmp = null;
        mSocketType = secure ? "Secure" : "Insecure";
        this.mHandler=mHandler;
        this.mBluetoothChatService=mBluetoothChatService;
        this.mAdapter=mAdapter;
        // Create a new listening server socket
        try {
            if (secure) {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SECURE,
                        Constants.MY_UUID_SECURE);
            } else {
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        Constants.NAME_INSECURE, Constants.MY_UUID_INSECURE);
            }
        } catch (IOException e) {
            Log.e(Constants.TAG_ACCEPT_THREAD, "Socket Type: " + mSocketType + "listen() failed", e);
        }
        mmServerSocket = tmp;
        mBluetoothChatService.setmState(Constants.STATE_LISTEN);
    }

    public void run() {
        Log.d(Constants.TAG_ACCEPT_THREAD, "Socket Type: " + mSocketType +
                "BEGIN mAcceptThread" + this);
        setName("AcceptThread" + mSocketType);
        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected


        while (mBluetoothChatService.getmState() != Constants.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(Constants.TAG_ACCEPT_THREAD, "Socket Type: " + mSocketType + "accept() failed", e);
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (mBluetoothChatService) {
                    switch (mBluetoothChatService.getmState()) {
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_CONNECTING:
                            BluetoothSession.getInstance().setDevice(socket.getRemoteDevice());
                            // Situation normal. Start the connected thread.
                            mBluetoothChatService.connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case Constants.STATE_NONE:
                        case Constants.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(Constants.TAG_ACCEPT_THREAD, "Could not close unwanted socket", e);
                            }
                            break;

                    }
                }
            }
        }

        Log.i(Constants.TAG_ACCEPT_THREAD, "END mAcceptThread, socket Type: " + mSocketType);

    }

    public void cancel() {
        Log.d(Constants.TAG_ACCEPT_THREAD, "Socket Type" + mSocketType + "cancel " + this);
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG_ACCEPT_THREAD, "Socket Type" + mSocketType + "close() of server failed", e);
        }
    }
}