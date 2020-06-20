package it.qrntine.chatbluetooth;

import java.util.UUID;

public final class Constants {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_OBJECT_WRITE = 6;
    public static final int MESSAGE_OBJECT_READ = 7;
    public static final int MESSAGE_OBJECT_ERROR_READ = 8;

    // Name for the SDP record when creating server socket
    public static final String NAME_SECURE = "BluetoothChatSecure";
    public static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final String TAG_BlUETOOTH_CHAT_SERVICE = "BluetoothChatService";
    public static final String TAG_CONNECTED_THREAD = "ConnectedThread";
    public static final String TAG_ACCEPT_THREAD = "AcceptThread";
    public static final String TAG_CONNECT_THREAD = "ConnectThread";
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //lista errori
    public static final int ERROR_NOT_FOUND = 0;
    public static final int ERROR_USER_DISCONNECTED = 1;

    //Activity constants
    public static final int ACTIVITY_MAIN = 0;
    public static final int ACTIVITY_CHATLIST = 1;
    public static final int ACTIVITY_CHAT = 2;
    public static final int RV_HEIGHT=10;

    //permission constants
    public static final int REQUEST_PERM_ACL=101;
    public static final int REQUEST_PERM_AFL=102;

}
