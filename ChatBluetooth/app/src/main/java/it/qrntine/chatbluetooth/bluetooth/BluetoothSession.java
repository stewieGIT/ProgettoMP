package it.qrntine.chatbluetooth.bluetooth;

public class BluetoothSession {
    private static BluetoothSession instance;
    private static BluetoothChatService mBluetoothChatService;

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

}
