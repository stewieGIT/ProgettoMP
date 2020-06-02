package it.qrntine.chatbluetooth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.qrntine.chatbluetooth.bluetooth.BluetoothChatService;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.bluetooth.MessageConstants;
import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.R;
//import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.database.AppDatabase;

public class MainActivity extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private AppDatabase db;
    private Holder holder;
    private Handler mHandler;
    private BluetoothSession session =BluetoothSession.getInstance();
    private List<String> devices = new ArrayList<>();   //serve solo per la stampa dei nomi nella recycler (DEBUG)
    private List<BluetoothDevice> objDevices = new ArrayList<>();   //lista di oggetti Bluetooth device

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

        //HANDLER SETUP
          mHandler = new Handler(new Handler.Callback() {
              @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {

                    case MessageConstants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                Intent intent=new Intent(MainActivity.this, ChatActivity.class);
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

        session.setmBluetoothChatService(new BluetoothChatService(MainActivity.this, mHandler));
        session.getmBluetoothChatService().start();
        System.out.println("*********************RIFERIMENTO MAIN ACTIVITY*******************"+session.getmBluetoothChatService());
        holder = new Holder();
        
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    class Holder implements View.OnClickListener{
        Button btnSearch, btnChat;
        CardView cvDevice;
        RecyclerView rvDevices;
        RecyclerView.Adapter rvAdapter;

        public Holder(){
            btnSearch=findViewById(R.id.btnSearch);
            btnChat=findViewById(R.id.btnChat);
            rvDevices=findViewById(R.id.rvDevices);
            cvDevice = findViewById(R.id.cvDevice);
            btnSearch.setOnClickListener(this);
            btnChat.setOnClickListener(this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            rvDevices.setLayoutManager(layoutManager);
            rvAdapter = new RecycleAdapter(devices);
            rvDevices.setAdapter(rvAdapter);
        }
        public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> implements View.OnClickListener {
            private List<String> dev;

            RecycleAdapter(List<String> listaDev) {
                dev = listaDev;
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View nameView = inflater.inflate(R.layout.layout_main, parent, false);
                nameView.setOnClickListener(this);
                return new ViewHolder(nameView);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                TextView tv = holder.tvDev;
                tv.setText(dev.get(position));
            }

            @Override
            public int getItemCount() {
                return dev.size();
            }

            @Override
            public void onClick(View v) {
                int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
                // parte la connessione...

                //fermo il discovering
                if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                }

                //prendo l'indirizzo del dispositivo selezionato
                String devAddress = objDevices.get(position).getAddress();
                session.getmBluetoothChatService().connect(objDevices.get(position),true);
                session.setDevice(objDevices.get(position));
            }

            public class ViewHolder extends RecyclerView.ViewHolder {
                private TextView tvDev;

                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvDev = itemView.findViewById(R.id.tvDevice);
                }
            }   //end Holder

        }

        //CONTINUE HOLDER
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.btnSearch):
                    if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        showToast("Scansione interrotta");
                    } else {
                        IntentFilter filter = new IntentFilter();

                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        registerReceiver(mReceiver, filter);
                        //quando clicco su ricerca azzero la lista dei nomi. Vanno gestiti diversamente i dispositivi gia' associati
                        if(devices.size() > 0) {
                            devices.clear();
                        }
                        BluetoothAdapter.getDefaultAdapter().startDiscovery();
                        showToast("Scansione iniziata");
                    }
                    break;
            }

        }


    }
    //END HOLDER

    // receiver bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                showToast("Ricerca...");
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                showToast("Ricerca completata.");

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!devices.contains(device.getName())) {
                    // se il dispositivo non ha un nome prendo l'indirizzo
                    if(device.getName() == null) {
                        showToast("Dispositivo trovato: " + device.getAddress());
                        devices.add(device.getAddress());
                    } else {
                        showToast("Dispositivo trovato: " + device.getName());
                        devices.add(device.getName());
                    }
                    objDevices.add(device);
                    holder.rvAdapter.notifyDataSetChanged();
                }
            }
        }
    };  //END BROADCAST RECEIVER



    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
