
package it.qrntine.chatbluetooth.bluetooth;

public interface MessageConstants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    int MESSAGE_OBJECT_WRITE = 6;
    int MESSAGE_OBJECT_READ = 7;
    int MESSAGE_OBJECT_ERROR_READ = 8;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

}