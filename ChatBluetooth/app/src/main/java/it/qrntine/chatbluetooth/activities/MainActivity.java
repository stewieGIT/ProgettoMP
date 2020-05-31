package it.qrntine.chatbluetooth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import it.qrntine.chatbluetooth.bluetooth.BluetoothChatService;
import it.qrntine.chatbluetooth.bluetooth.MessageConstants;
import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.R;
//import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private Holder holder;
    private BluetoothChatService mBluetoothChatService;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int REQUEST_CODE = 101;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // controllo se il permesso NON sia stato gia' dato
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // richiedo il permesso
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // richiedo il permesso
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
            // ********* i prossimi due non dovrebbero essere necessari
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                // richiedo il permesso
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_CODE);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                // richiedo il permesso
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_CODE);
            }
        }  //  END VERIFICA PERMESSI
        // SET DISCOVERABLE
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
          mHandler = new Handler(new Handler.Callback() {
              @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {

                    case MessageConstants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                Intent intent=new Intent(MainActivity.this, ChatActivity.class);
                                intent.putExtra("BluetoothChatService", mBluetoothChatService);
                                startActivity(intent);
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                showToast("Connessione...");
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                                showToast("Listen...");     //P8 si mentre il RedMi non lo fa all'avvio
                            case BluetoothChatService.STATE_NONE:
                                showToast("Non connesso...");
                                break;
                        }
                        break;
                }
                return true;
            }
        } ) ;
        mBluetoothChatService=new BluetoothChatService(MainActivity.this, mHandler);

        holder = new Holder();
        
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    class Holder{

        public Holder(){

        }

    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
