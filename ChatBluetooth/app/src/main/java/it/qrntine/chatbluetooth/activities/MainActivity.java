package it.qrntine.chatbluetooth.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private Holder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        holder = new Holder();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //inizializzazione del BTAdapter
        
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    class Holder implements View.OnClickListener {

        private Button btnChat;

        public Holder(){
            btnChat = findViewById(R.id.btnChat);
            btnChat.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnChat){
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        }
    }
}
