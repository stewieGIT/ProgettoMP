package it.qrntine.chatbluetooth.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

public class BluetoothSession {
    private static BluetoothSession instance;
    private static BluetoothChatService mBluetoothChatService;
    private static BluetoothDevice device;
    private static int currentActivity;

    private BluetoothSession(){
    }

    public static synchronized BluetoothSession getInstance(){
        if(instance==null){
            instance=new BluetoothSession();
        }
        return instance;
    }

    public static BluetoothChatService getmBluetoothChatService() {
        return mBluetoothChatService;
    }

    public static void setmBluetoothChatService(BluetoothChatService mBluetoothChatService) {
        BluetoothSession.mBluetoothChatService = mBluetoothChatService;
    }

    public static BluetoothDevice getDevice(){
        return device;
    }

    public static void setDevice(BluetoothDevice device) {
        BluetoothSession.device = device;
    }

    public static void setCurrentActivity(int currentActivity) {
        BluetoothSession.currentActivity = currentActivity;
    }

    public static int getCurrentActivity() {
        return currentActivity;
    }
}
