package it.qrntine.chatbluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import it.qrntine.chatbluetooth.Constants;
import it.qrntine.chatbluetooth.codifica.MetaMessaggio;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] buffer;
    private final BluetoothAdapter mAdapter;
    private BluetoothChatService mBluetoothChatService;
    private Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, String socketType, BluetoothAdapter mAdapter, BluetoothChatService mBluetoothChatService, Handler mHandler) {

        Log.d(Constants.TAG_CONNECTED_THREAD, "create ConnectedThread: " + socketType);
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
            Log.e(Constants.TAG_CONNECTED_THREAD, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        mBluetoothChatService.setmState(Constants.STATE_CONNECTED);
    }

    public void run() {
        Log.i(Constants.TAG_CONNECTED_THREAD, "BEGIN mConnectedThread");

        // Keep listening to the InputStream while connected
        while (mBluetoothChatService.getmState() == Constants.STATE_CONNECTED) {
            try{
                    ObjectInputStream ois = new ObjectInputStream(mmInStream); //apri un ObjectInputStream
                    Object obj = ois.readObject(); //leggi il MetaMessaggio
                    MetaMessaggio meta = (MetaMessaggio) obj;
                    if(obj != null){ //se l'oggetto è null c'è stato un errore
                        if(meta.getIvp() == null){ //se il campo IVP è vuoto è un messaggio non codificato
                            mHandler.obtainMessage(Constants.MESSAGE_READ, obj).sendToTarget();
                        }
                        else mHandler.obtainMessage(Constants.MESSAGE_OBJECT_READ, obj).sendToTarget(); //altrimenti è cod.
                    }
                }catch(IOException e1){
                    mHandler.sendEmptyMessage(Constants.MESSAGE_OBJECT_ERROR_READ);
                    e1.printStackTrace();
                }catch(ClassNotFoundException e2){
                    e2.printStackTrace();
                }
        }
    }

    /**
     * scrivi il metaMessaggio
     * @param msg
     */
    public void writeObject(Object msg){
        try{
            ObjectOutputStream oos = new ObjectOutputStream(mmOutStream); //apri un ObjectOutputStream
            oos.writeObject(msg); //scrivi il MetaMessaggio
            MetaMessaggio meta = (MetaMessaggio) msg;
            if(meta.getIvp() == null){ //se il campo IVP è vuoto si tratta di un messaggio non codificato
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, msg).sendToTarget(); //notifica che il meta non è codificato
            }
            else mHandler.obtainMessage(Constants.MESSAGE_OBJECT_WRITE, msg).sendToTarget(); //altrimenti è codificato
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG_CONNECTED_THREAD, "close() of connect socket failed", e);
        }
    }

    /**
     * cambia l'handler e imposta quello della ChatActivity
     * @param mHandler
     */
    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }
}