package it.qrntine.chatbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import it.qrntine.chatbluetooth.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }
}
